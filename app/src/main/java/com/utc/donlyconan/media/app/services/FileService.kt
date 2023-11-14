package com.utc.donlyconan.media.app.services

import android.app.Activity
import android.app.Service
import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Binder
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.provider.MediaStore.Video.Media
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.core.net.toUri
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.EGMApplication
import com.utc.donlyconan.media.app.localinteraction.Client
import com.utc.donlyconan.media.app.localinteraction.EGPMediaClient
import com.utc.donlyconan.media.app.localinteraction.EGPSystem
import com.utc.donlyconan.media.app.localinteraction.Packet
import com.utc.donlyconan.media.app.localinteraction.serialize
import com.utc.donlyconan.media.app.utils.Logs
import com.utc.donlyconan.media.app.utils.androidFile
import com.utc.donlyconan.media.app.utils.browse
import com.utc.donlyconan.media.app.utils.now
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.repo.TrashRepository
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.extension.components.getMediaUri
import com.utc.donlyconan.media.extension.widgets.showMessage
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.net.InetAddress
import java.net.Socket
import java.net.SocketException
import javax.inject.Inject
import kotlin.reflect.KClass


/**
 * Perform the file managing consist of save, delete files in the local or external storage
 */
class FileService : Service() {

    companion object {
        const val TAG = "FileService"
        const val DELAY_BEFORE_DELETING = 5000L
        const val DELAY_1000S = 1000L
    }

    private val binder by lazy { LocalBinder() }
    private val context by lazy { application }
    @Inject lateinit var videoRepository: VideoRepository
    @Inject lateinit var trashRepository: TrashRepository
    private val supervisorJob by lazy { SupervisorJob() }
    private val coroutineScope by lazy { CoroutineScope(Dispatchers.IO + supervisorJob) }
    private var syncJob: Job? = null
    private lateinit var listUris: MutableSet<Uri>
    private val listeners by lazy { mutableSetOf<OnFileServiceListener>() }
    private var deletingJob: Job? = null
    private var updateRecycleBinJob: Job? = null
    private var socketJob: Job? = null

    var egmSystem: EGPSystem? = null
        private set
    private val clientHandlers by lazy { HashMap<Long, ClientHandler>() }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")
        contentResolver.registerContentObserver(Media.EXTERNAL_CONTENT_URI, true, mediaObserver)
        (application as EGMApplication)
            .applicationComponent()
            .inject(this)
        listUris = mutableSetOf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand() called with: intent = $intent, flags = $flags, startId = $startId")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
        contentResolver.unregisterContentObserver(mediaObserver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): FileService = this@FileService
    }


    private val mediaObserver =  object : ContentObserver(Handler(Looper.myLooper()!!)){

        override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) {
            super.onChange(selfChange, uri, flags)
            syncJob?.cancel()
            syncJob = runIO {
                delay(DELAY_1000S)
                yield()
                deletingJob?.join()
                videoRepository.sync()
            }

            if (flags and ContentResolver.NOTIFY_DELETE == 0) {
                updateRecycleBinJob?.cancel()
                updateRecycleBinJob = runIO {
                    delay(DELAY_1000S)
                    yield()
                    deletingJob?.join()
                    trashRepository.sync()
                }
            }
        }

        override fun onChange(selfChange: Boolean, uris: MutableCollection<Uri>, flags: Int) {
            super.onChange(selfChange, uris, flags)
            Log.d(TAG, "onChange() called with: selfChange = $selfChange, uris = $uris, flags = $flags")
            // sync data from external into local data
        }
    }
    
    private fun generateExceptionHandler() = CoroutineExceptionHandler { v, e ->
        e.printStackTrace()
        val count = listeners.count { listener -> listener.onError(e) }
        Log.d(TAG, "generateExceptionHandler() called with: v = $v, e = $e")
       if(count != 0) {
           coroutineScope.launch(Dispatchers.Main) {
               context.showMessage(R.string.toast_when_failed_user_action, duration = Toast.LENGTH_SHORT)
           }
       }
    }

    @Throws(IOException::class)
    @Synchronized
    suspend fun saveIntoInternal(uri: Uri, filename: String): Pair<String, Uri> {
        Logs.d( "save() called with: uri = $uri, filename = $filename")
        val file = File(filename)
        val newFilename = handleFilename(file)
        val outputStream = context.openFileOutput(newFilename, Context.MODE_PRIVATE)
        context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.copyTo(outputStream)
            outputStream.flush()
            outputStream.close()
        }
        val newUri = context.getFileStreamPath(newFilename).toUri()
        return Pair(newFilename, newUri)
    }

    @Throws(IOException::class)
    @Synchronized
    suspend fun saveIntoExternal(filename: String, finish: (result: Boolean, file: File?, uri: Uri) -> Unit){
        Logs.d("removeFromInternal() called with: filename = $filename")
        val rootFolder = androidFile(Environment.DIRECTORY_DOWNLOADS)
        val file = File(rootFolder, filename)
        val newFilename = handleFilename(file)
        val newFile = File(rootFolder, newFilename)
        if (!newFile.exists()) {
            newFile.createNewFile()
        }
        val outputStream = newFile.outputStream()
        context.getFileStreamPath(filename).inputStream().use { stream ->
            stream.copyTo(outputStream)
        }
        val result = context.deleteFile(filename)
        newFile.getMediaUri(context) {
            finish(result, newFile, it)
        }
    }

    private fun handleFilename(file: File): String {
        Logs.d("handleFilename() called with: file = $file")
        var index = 1
        var filename = file.name.substringBeforeLast('.')
        var ext = file.name.substringAfterLast('.')
        var newFile = file
        while (newFile.exists()) {
            var newName = "$filename($index)"
            index++
            newFile = File("$newName.$ext")
        }
        return newFile.name
    }

    @Throws(SecurityException::class)
    @Synchronized
    private suspend fun deleteFileFromExternalStorage(vararg uris: Uri): Int {
        Log.d(TAG, "deleteFileFromExternalStorage() called with: uris = $uris")
        val operations = uris.map { uri -> ContentProviderOperation.newDelete(uri).build() }
            .toMutableList()
       return contentResolver.applyBatch(MediaStore.AUTHORITY, ArrayList(operations)).size
    }

    @Synchronized
    suspend fun deleteFileFromLocalData(vararg names: String): Int {
        Log.d(TAG, "deleteFileFromLocalData() called with: filenames = $names")
        return names.count { name -> context.deleteFile(name) }
    }

    /**
     * Allow to sync video between the app and android systems
     */
    fun syncAllVideos() = runIO {
        deletingJob?.join()
        videoRepository.sync()
    }

    fun syncRecycleBin() = runIO {
        Log.d(TAG, "syncRecycleBin() called")
        deletingJob?.join()
        trashRepository.sync()
    }


    private fun createRequestDelete() = runIO {
        delay(DELAY_BEFORE_DELETING)
        Log.d(TAG, "createRequestDelete: deleting files = $listUris")
        try {
            yield()
            deleteFileFromExternalStorage(*listUris.toTypedArray())
            Log.d(TAG, "createRequestDelete() cleared ${listUris.size} files")
        } catch (e: SecurityException) {
            Log.e(TAG, "createRequestDelete:", e)
            listeners.forEach { it.onDeletedFileError(listUris.toList(), e) }
        } catch (e: Exception) {
            Log.e(TAG, "createRequestDelete: ", e)
            syncJob?.cancel()
            syncJob = syncAllVideos()
        }
        listUris.clear()
    }

    fun requestDeletingFile(uri: Uri) {
        Log.d(TAG, "requestDelete() called with: uri = $uri")
        if (deletingJob?.isActive == true) {
            deletingJob?.cancel()
        }
        listUris.add(uri)
        deletingJob = createRequestDelete()
    }

    fun registerOnFileServiceListener(listener: OnFileServiceListener) {
        listeners.add(listener)
        listeners.browse {
            onEgpConnectionChanged(isReadyService(), egmSystem?.isGroupOwner() == true)
            onClientConnectionChanged(egmSystem?.listClients ?: listOf())
        }
    }

    fun unregisterOnFileServiceListener(listener: OnFileServiceListener) {
        listeners.remove(listener)
    }

    /**
     * Push task into IO thread and run
     */
    fun runIO(runnable: suspend FileService.() -> Unit) = coroutineScope.launch(Dispatchers.IO + generateExceptionHandler()) {
        runnable.invoke(this@FileService)
    }

    fun notifyDeletedResult(result: ActivityResult) {
        Log.d(TAG, "notifyDeletedFile() called with: result = $result")
        if(result.resultCode != Activity.RESULT_OK) {
            syncJob?.cancel()
            syncJob = syncAllVideos()
            context.showMessage(R.string.the_files_have_not_been_deleted)
        }  else {
            context.showMessage(R.string.the_files_have_been_moved_to_recycle_bin)
        }
    }


    private val egmHandler = CoroutineExceptionHandler { _, e ->
        listeners.browse { onError(e) }
        e.printStackTrace()
        showMessage("An error has occurred. ${e.message}")
        if(e is SocketException) {
            closeEgpSystem()
        }
    }
    fun <T: EGPSystem>openEgmService(kClass: KClass<T>, inetAddress: InetAddress?, onFinish: (connected: Boolean) -> Unit = {}): Job? {
        Log.d(TAG, "openEgmService() called with: kClass = $kClass, inetAddress = $inetAddress")
        socketJob = coroutineScope.launch(Dispatchers.IO + egmHandler) {
            if(isReadyService()) {
                Log.d(TAG, "openEgmService: System is running.")
                onFinish(false)
            } else {
                egmSystem?.shutdown()
                egmSystem = EGPSystem.create(kClass, inetAddress).apply {
                    val name = Settings.Global.getString(contentResolver, Settings.Global.DEVICE_NAME)
                    setName(name)
                    registerClientServiceListener(clientServiceListener)
                    runIO { start() }.invokeOnCompletion {
                        listeners.browse {
                            onEgpConnectionChanged(false, egmSystem?.isGroupOwner() == true)
                        }
                    }
                }
                listeners.browse {
                    onEgpConnectionChanged(true, egmSystem?.isGroupOwner() == true)
                }
                onFinish(egmSystem != null)
            }
        }
        return socketJob
    }

    /**
     * Return true if service is working
     */
    fun isReadyService(): Boolean {
        return egmSystem?.isAlive == true
    }

    fun closeEgpSystem() {
        Log.d(TAG, "close() called")
        listeners.browse {
            onEgpConnectionChanged(false, egmSystem?.isGroupOwner() == true)
        }
        egmSystem?.shutdown()
        socketJob?.cancel()
        clientHandlers.clear()
        egmSystem = null
        socketJob = null
    }

    private val clientServiceListener = object : Client.ClientServiceListener {
        override fun onStart(clientId: Long, socket: Socket) {
            Log.d(TAG, "onStart() called with: clientId = $clientId, socket = $socket")
            clientHandlers[clientId] = ClientHandler(clientId)
            runIO {
                listeners.forEach { e -> e.onClientConnectionChanged(egmSystem?.listClients ?: listOf()) }
            }
        }

        override fun onClose(clientId: Long, socket: Socket) {
            Log.d(TAG, "onClose() called with: clientId = $clientId, socket = $socket")
            clientHandlers.remove(clientId)
            listeners.forEach { e -> e.onClientConnectionChanged(egmSystem?.listClients ?: listOf()) }
            if(clientId == EGPMediaClient.CLIENT_ID) {
                egmSystem = null
            }
        }

        override fun onReceive(clientId: Long, packet: Packet) {
            try {
                clientHandlers[clientId]?.handle(packet)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "onReceive: ",e)
            }
        }
    }

    private fun createNewFile(filename: String): File {
        Log.d(TAG, "createVideoFile() called with: filename = $filename")
        val rootFolder = androidFile(Environment.DIRECTORY_DOWNLOADS)
        val file = File(rootFolder, filename)
        val newFilename = handleFilename(file)
        val newFile = File(rootFolder, newFilename)
        if (!newFile.exists()) {
            newFile.createNewFile()
        }
        return newFile
    }


    fun send(video: Video) {
        Log.d(TAG, "send() called with: videos = $video")
        coroutineScope.launch(Dispatchers.IO + egmHandler) {
            handleSendRequest(video)
        }
    }

    @Synchronized
    private suspend fun handleSendRequest(video: Video) {
        Log.d(TAG, "handleSendRequest() called")
        video.copy(createdAt = now(), updatedAt = now(), isSecured = false, isFavorite = false)
            .apply {
                serialize()?.let { bytes ->
                    val purpose = Packet.from(Packet.CODE_VIDEO_ENCODE, bytes)
                    contentResolver.openInputStream(videoUri.toUri())?.use { inp ->
                        egmSystem?.sendFile(purpose, inp)
                        Log.d(TAG, "Video is sent.")
                    }
                    if (subtitleUri != null) {
                        val subtitlePacket =
                            Packet.from(Packet.CODE_SUBTITLE_ENCODE, "$title.srt".toByteArray())
                        contentResolver.openInputStream(subtitleUri?.toUri()!!)?.use { inp ->
                            egmSystem?.sendFile(subtitlePacket, inp)
                            Log.d(TAG, "Subtitle is sent.")
                        }
                    }
                }
            }
    }

    inner class ClientHandler(val clientId: Long) {
        private var outputStream: OutputStream? = null
        private var video: Video? = null
        private var file: File? = null
        private var flag: Byte = -1
        private var expiredTime: Long = 0
        private var downloadingProgress: Long = 0

        /**
         * Handle message which is sent by clients or server
         */
        @Synchronized
        fun handle(packet: Packet) {
            when(packet.code()) {
                Packet.CODE_DEVICE_NAME -> {
                    Log.d(TAG, "handle() deviceName = ${packet.get<String>()}")
                    listeners.browse {
                        onDeviceNameUpdated(packet.get())
                    }
                }
                Packet.CODE_VIDEO_ENCODE -> createVideoFile(packet)
                Packet.CODE_SUBTITLE_ENCODE -> createSubtitleFile(packet)
                Packet.CODE_FILE_SENDING, Packet.CODE_FILE_END -> writeFile(packet)
                else -> {
                    video = null
                    flag = packet.code()
                    Log.d(TAG, "handle: packet=$packet is not processed.")
                }
            }
        }

        private fun createSubtitleFile(packet: Packet) {
            Log.d(TAG, "createSubtitleFile() called with: packet = $packet")
            packet.get<String>()?.let { fileName ->
                file = createNewFile(fileName)
                outputStream = file?.outputStream()
                flag = packet.code()
            }
            downloadingProgress = 0
        }

        private fun createVideoFile(packet: Packet) {
            video = packet.get()
            Log.d(TAG, "createVideoFile() called with: packet = $packet, video=$video")
            video?.let { video ->
                video.videoId = 0
                file = createNewFile(video.title!!)
                Log.d(TAG, "createVideoFile file = $file")
                outputStream = file?.outputStream()
                file?.getMediaUri(this@FileService) { uri ->
                    video.videoUri = uri.toString()
                    videoRepository.insertOrUpdate(video)
                }
            }
            downloadingProgress = 0
            flag = packet.code()
        }

        private fun writeFile(packet: Packet) {
            outputStream?.write(packet.data())
            downloadingProgress += packet.length()
            if(expiredTime < now() - 1000 && packet.code() == Packet.CODE_FILE_SENDING) {
                listeners.browse {
                    video?.videoUri?.let { uri ->
                        onDownloadingProgress(uri, downloadingProgress, video?.size ?: 0)
                    }
                }
                expiredTime = now()
            }
            if (packet.code() == Packet.CODE_FILE_END) {
                outputStream?.flush()
                outputStream?.close()
                listeners.browse {
                    val size = video?.size ?: 0
                    video?.videoUri?.let { uri ->
                        onDownloadingProgress(uri, size, size)
                    }
                }
                if(flag == Packet.CODE_VIDEO_ENCODE) {
                    handleWhenFinish()
                }
                outputStream = null
                file = null
                downloadingProgress = 0
                flag = -1
                Log.d(TAG, "handle: sending file is finished.")
            }
        }

        private fun handleWhenFinish() {
            Log.d(TAG, "handleWhenFinish() called with: file = $file, video = $video, flag=$flag")
            if (video != null) {
                file?.getMediaUri(this@FileService) { uri ->
                    Log.d(TAG, "handleWhenFinish: uri = $uri")
                    video!!.videoUri = uri.toString()
                    if (flag == Packet.CODE_VIDEO_ENCODE) {
                        videoRepository.update(video!!)
                    }
                    if (flag == Packet.CODE_SUBTITLE_ENCODE) {
                        video?.subtitleUri = uri.toString()
                        videoRepository.update(video!!)
                        video = null
                    }
                }
            }
        }
    }

    interface OnFileServiceListener {
        fun onDeviceNameUpdated(deviceName: String?) {}
        fun onClientConnectionChanged(clients: List<Client>) {}
        fun onDiskSizeNotEnough() {}
        fun onDeletedFileError(uris: List<Uri>,e: Exception) {}
        fun onError(e: Throwable?): Boolean { return false }
        fun onEgpConnectionChanged(isConnected: Boolean, isGroupOwner: Boolean) {}
        fun onDownloadingProgress(uri: String, progress: Long, total: Long) {}
    }
}