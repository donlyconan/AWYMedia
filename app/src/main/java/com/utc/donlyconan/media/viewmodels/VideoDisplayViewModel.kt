package com.utc.donlyconan.media.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.utc.donlyconan.media.app.utils.Logs
import com.utc.donlyconan.media.data.dao.PlaylistWithVideosDao
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.extension.widgets.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject

class VideoDisplayViewModel : ViewModel() {
    @Inject lateinit var videoRepo: VideoRepository
    @Inject lateinit var playlistWithVideosDao: PlaylistWithVideosDao
    var isFinished = false
    var isInitialized = true
    var videoId: Int = 0
    var isResetPosition = false
    var continued: Boolean = false

    var playlistId: Int = -1
    private var initialized: Boolean = false
    private val _videoMld: MutableLiveData<Video> = MutableLiveData<Video>()
    val videoMld: LiveData<Video> get() = _videoMld

    var playlist = listOf<Video>()
    private val _playlistMld: MutableLiveData<List<Video>> = MutableLiveData()
    val playlistMld: LiveData<List<Video>> get() = _playlistMld

    var playingIndexMld =  MutableLiveData<Int>(0)
    var playingTimeMld = MutableLiveData<Long>(0L)
    var playWhenReadyMld = MutableLiveData<Boolean>(false)
    var speedMld = MutableLiveData(1.0f)
    var repeatModeMld = MutableLiveData(ExoPlayer.REPEAT_MODE_OFF)
    var currentPlayWhenReadyState = false
    var shouldRotate: Boolean = true

    private val _events = MutableLiveData<Result>()
    val events get() = _events
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + job)


    fun initialize(videoId: Int, playlistId: Int, continued: Boolean) {
        Logs.d(TAG, "initialize() called with: videoId = $videoId, playlistId = $playlistId, continued = $continued")
        if(!initialized) {
            this.videoId = videoId
            this.continued = continued
            this.playlistId = playlistId
            val video = videoRepo.get(videoId)

            if(video != null) {
                _videoMld.value = video
                if(continued) {
                    playingTimeMld.value = video.playedTime
                }
                playWhenReadyMld.value = true
            } else {
                _events.value = Result.VideoNotFound()
            }

            playlist = playlistWithVideosDao.get(playlistId)?.videos
            if(playlist != null) {
                _playlistMld.value = playlist
                playingIndexMld.value = playlist.indexOfFirst { it.videoId == videoId }
            } else {
                playingIndexMld.value = 0
            }
        }
        initialized = true
    }

    fun replaceVideo(videoId: Int) {
        Log.d(TAG, "replaceVideo() called with: videoId = $videoId")
        playlist.forEachIndexed { index, video ->
            if(video.videoId == videoId) {
                _videoMld.value = video
                playingIndexMld.value = index
            }
        }
    }

    fun hasNext() = videoId < playlist.size - 1

    fun hasPrev() = videoId > 0

    /**
     * Set status finish for playing operation and save video state
     */
    suspend fun finish() {
        Log.d(TAG, "endVideo() called")
        isFinished = true
        videoMld.value?.let { video ->
            video.playedTime = 0
            videoRepo.update(video)
        }
    }

    suspend fun save(position: Long = 0L) {
        Log.d(TAG, "save() called with: position = $position")
        val video = videoMld.value
        video?.apply {
            playedTime = if(isFinished) 0L else position
            videoRepo.update(this)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Logs.d(TAG, "onCleared() called")
        job.cancel()
    }

    fun moveNext() {
        var index = playingIndexMld.value!!
        index++
        if(index < playlist.size) {
            _videoMld.value = playlist[index]
        }
        playingIndexMld.value = index
    }

    fun movePrevious() {
        var index = playingIndexMld.value!!
        index--
        if(index >= 0) {
            _videoMld.value = playlist[index]
        }
        playingIndexMld.value = index
    }

    sealed class Result {
        class VideoNotFound(): Result()
    }

}