package com.utc.donlyconan.media.data.repo

import android.app.Application
import android.provider.MediaStore
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.data.dao.ListVideoDao
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.extension.components.getAllVideos
import com.utc.donlyconan.media.views.adapter.Constant
import kotlinx.coroutines.flow.Flow

class ListVideoRepositoryImpl(val app: Application, private val dao: ListVideoDao) : ListVideoRepository {

    override suspend fun loadAllVideos() = app.contentResolver
        .getAllVideos(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)

    override fun getAllVideos(sortId: Int) = Pager(
        config = PagingConfig(Constant.PAGE_SIZE, Constant.PREFETCH_DISTANCE, false),
        pagingSourceFactory = {
            Log.d(VideoRepositoryImpl.TAG, "getAllVideos() called")
            when(sortId) {
                Settings.SORT_BY_NAME -> {
                    dao.getAllVideosOrderByVideoName()
                }
                Settings.SORT_BY_CREATION -> {
                    dao.getAllVideosOrderByCreation()
                }
                Settings.SORT_BY_RECENT -> {
                    dao.getAllVideosOrderByRecent()
                }
                else -> {
                    dao.getAllVideosOrderBySize()
                }
            }
        }
    ).flow

    override fun getAllPlayingVideos() = Pager(
        config = PagingConfig(Constant.PAGE_SIZE, Constant.PREFETCH_DISTANCE, false),
        pagingSourceFactory = { dao.getAllPlayingVideos() }
    ).flow

    override fun getAllFavoriteVideo() = Pager(
        config = PagingConfig(Constant.PAGE_SIZE, Constant.PREFETCH_DISTANCE, false),
        pagingSourceFactory = { dao.getAllFavoriteVideos() }
    ).flow

    override fun getAllNextVideos(startId: Long) = Pager(
        config = PagingConfig(Constant.PAGE_SIZE, Constant.PREFETCH_DISTANCE, false),
        pagingSourceFactory = { dao.getAllNextVideos(startId) }
    ).flow

    override fun findAllVideos(keyword: String) = Pager(
        config = PagingConfig(Constant.PAGE_SIZE, Constant.PREFETCH_DISTANCE, false),
        pagingSourceFactory = { dao.findAllVideos(keyword) }
    ).flow

    override fun getListInTrash() = Pager(
        config = PagingConfig(Constant.PAGE_SIZE, Constant.PREFETCH_DISTANCE, false),
        pagingSourceFactory = { dao.getListInTrash() }
    ).flow

}