package com.utc.donlyconan.media.app

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.core.net.toUri
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.utils.Logs
import com.utc.donlyconan.media.app.utils.androidFile
import com.utc.donlyconan.media.extension.widgets.showMessage
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * This class represents for a manager that handles all operations to involve files in the system
 */
class FileManager @Inject constructor(val context: Context) {

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    private fun generateExceptionHandler() = CoroutineExceptionHandler { _, e ->
        coroutineScope.launch(Dispatchers.Main) {
            context.showMessage(R.string.toast_when_failed_user_action, duration = Toast.LENGTH_LONG)
        }
    }

    @WorkerThread
    fun saveIntoInternal(
        uri: Uri, filename: String,
                         onFinished:(uri: Uri, newName: String) -> Unit = { uri: Uri, name: String -> }) {
        Logs.d( "save() called with: uri = $uri, filename = $filename")
        coroutineScope.launch (generateExceptionHandler()) {
            val file = File(filename)
            val newFilename = handleFilename(file)
            val outputStream = context.openFileOutput(newFilename, Context.MODE_PRIVATE)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.copyTo(outputStream)
                outputStream.flush()
                outputStream.close()
            }
            val newUri = context.getFileStreamPath(newFilename).toUri()
            onFinished.invoke(newUri, newFilename)
        }
    }

    fun removeFromInternal(filename: String, onFinished:(uri: Uri) -> Unit = {}) {
        Logs.d("removeFromInternal() called with: filename = $filename, onFinished = $onFinished")
        coroutineScope.launch(generateExceptionHandler()) {
            val newFile = File(androidFile(Environment.DIRECTORY_MOVIES), filename)
            val outputStream = newFile.outputStream()
            context.getFileStreamPath(filename).inputStream().use { stream ->
                stream.copyTo(outputStream)
            }
            context.deleteFile(filename)
            onFinished.invoke(newFile.toUri())
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
            println(newFile.name)
        }
        return newFile.name
    }
}


//fun main() {
//    println(InternalFileManger.rename(File("abv.df.mp4")))
//}