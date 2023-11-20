package com.utc.donlyconan.media.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.data.models.Playlist
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.data.repo.PlaylistRepository
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.extension.widgets.TAG
import kotlinx.coroutines.launch

class VideoDisplayViewModel(
    var videoId: Int,
    var playlistId: Int = Playlist.NO_PLAYLIST,
    var continued: Boolean = false,
    var videoRepo: VideoRepository,
    var playlistRepository: PlaylistRepository,
    var settings: Settings

) : ViewModel() {
    companion object {
        const val ACTION_PREVIOUS = 1
        const val NO_ACTION = 0
        const val ACTION_NEXT = 2
    }

    var isFinished = false
    var isInitialized = true

    private val _videoMld: MutableLiveData<Video> = MutableLiveData<Video>()
    val videoMld: LiveData<Video> get() = _videoMld

    var playlist: List<Video>? = null
    private val _playlistMld: MutableLiveData<List<Video>> = MutableLiveData()
    val playlistMld: LiveData<List<Video>> get() = _playlistMld

    var playingIndexMld =  MutableLiveData<Int>(0)
    var playingTimeMld = MutableLiveData<Long>(0L)
    var playWhenReadyMld = MutableLiveData<Boolean>(false)
    var speedMld = MutableLiveData(1.0f)
    var repeatModeMld = MutableLiveData(ExoPlayer.REPEAT_MODE_OFF)
    var currentPlayWhenReadyState = false
    var shouldRotate: Boolean = true
    var lockModeMdl: MutableLiveData<Boolean> = MutableLiveData(false)
    var resizeModeMdl: MutableLiveData<Int> = MutableLiveData(AspectRatioFrameLayout.RESIZE_MODE_FIT)

    private val _events = MutableLiveData<Result>()
    val events get() = _events
    var currentAction: Int = NO_ACTION
    val video: Video? get() = _videoMld.value

    init {
        viewModelScope.launch {
            val video = videoRepo.get(videoId)
            if(video != null) {
                _videoMld.postValue(video!!)
                if(continued || settings.restoreVideoState) {
                    playingTimeMld.postValue(video.playedTime)
                }
                playWhenReadyMld.postValue(settings.autoPlay)
            } else {
                _events.value = Result.VideoNotFound()
            }
            playlist = playlistRepository.getPlaylistWithVideos(playlistId)?.videos
            if(playlist != null) {
                playlist!!.forEach { e -> e.playedTime = 0 }
                _playlistMld.postValue(playlist)
                playingIndexMld.postValue(playlist!!.indexOfFirst { it.videoId == videoId })
            } else {
                playingIndexMld.postValue(0)
            }
        }
    }

    fun replaceVideo(videoId: Int) {
        Log.d(TAG, "replaceVideo() called with: videoId = $videoId")
        if(videoMld.value?.videoId != videoId) {
            playlist?.forEachIndexed { index, video ->
                if(video.videoId == videoId) {
                    _videoMld.value = video
                    playingIndexMld.value = index
                }
            }
        }
    }

    fun hasNext() = playingIndexMld.value!! < (playlist?.size ?: 0) - 1

    fun hasPrev() =  playingIndexMld.value!! > 0


    fun save(subtitle: String) {
        Log.d(TAG, "save() called with: subtitle = $subtitle")
        _videoMld.value = _videoMld.value?.copy(subtitleUri = subtitle)
        // Disable this feature
//        viewModelScope.launch {
//            videoRepo.update(videoMld.value!!)
//        }
    }

    suspend fun save(position: Long = 0L) {
        Log.d(TAG, "save() called with: position = $position")
        val video = videoMld.value
        video?.apply {
            playedTime = if(isFinished) 0L else position
            videoRepo.update(this)
        }
    }

    fun saveTempState(position: Long) {
        val index = playingIndexMld.value!!
        Log.d(TAG, "saveTempState() called with: index = $index")
        if(isListMode() && index >= 0 && index < playlist!!.size) {
            playlist!!.get(index).let { video ->
                video.playedTime = position
            }
        }
    }

    fun moveNext() {
        var index = playingIndexMld.value!!
        if(hasNext()) {
            index++
            _videoMld.postValue(playlist!!.get(index))
            playingIndexMld.postValue(index)
            if(continued) {
                playingTimeMld.postValue(videoMld.value!!.playedTime)
            }
            currentAction = ACTION_NEXT
        } else {
            _events.postValue(Result.CanNotMoveNext)
        }
    }

    fun movePrevious() {
        var index = playingIndexMld.value!!
        if(hasPrev()) {
            index--
            _videoMld.postValue(playlist!!.get(index))
            playingIndexMld.postValue(index)
            if(continued) {
                playingTimeMld.postValue(videoMld.value!!.playedTime)
            }
            currentAction = ACTION_PREVIOUS
        } else {
            _events.postValue(Result.CanNotMovePrevious)
        }
    }

    fun isListMode(): Boolean {
        return playlist?.isNotEmpty() == true
    }

    sealed class Result {
        class VideoNotFound(): Result()
        object CanNotMoveNext: Result()
        object CanNotMovePrevious: Result()
    }

}