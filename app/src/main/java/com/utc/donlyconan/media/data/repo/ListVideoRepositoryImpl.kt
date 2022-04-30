package com.utc.donlyconan.media.data.repo

import android.app.Application
import android.provider.MediaStore
import com.utc.donlyconan.media.data.dao.ListVideoDao
import com.utc.donlyconan.media.extension.components.getAllVideos
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListVideoRepositoryImpl @Inject constructor(val app: Application, private val dao: ListVideoDao) : ListVideoRepository {

    override suspend fun loadAllVideos() = app.contentResolver
        .getAllVideos(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)

    override fun getAllVideos() = dao.getAllVideos()

    override fun getAllPlayingVideos() =  dao.getAllPlayingVideos()

    override fun getAllFavoriteVideo() = dao.getAllFavoriteVideos()

    override fun getAllNextVideos(startId: Long) = dao.getAllNextVideos(startId)

    override fun findAllVideos(keyword: String) = dao.findAllVideos(keyword)

    override fun getListInTrash() = dao.getListInTrash()

}