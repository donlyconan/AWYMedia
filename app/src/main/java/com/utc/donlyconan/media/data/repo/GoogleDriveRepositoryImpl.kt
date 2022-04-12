package com.utc.donlyconan.media.data.repo

import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.data.models.Video
import com.utc.donlyconan.media.views.adapter.Constant
import kotlinx.coroutines.flow.Flow
//
//
//class GoogleDriveVideoRepositoryImpl(val service: Drive): VideoRepository {
//
//    companion object {
//        val TAG: String = GoogleDriveVideoRepositoryImpl::class.java.simpleName
//        const val ROOT_FOLDER = "AWYMedia"
//    }
//
//    private fun createFolderIfNotExists() {
//        Log.d(TAG, "createFolderIfNotExists() called")
//    }
//
//    private fun getFolderId(): String {
//        var pageToken: String? = null
//        var fileId: String? = null
//        do {
//            val result: FileList = service.files().list()
//                .setQ("mimeType='application/vnd.google-apps.folder'")
//                .setSpaces("drive")
//                .setFields("nextPageToken, files(id, name)")
//                .setPageToken(pageToken)
//                .execute()
//            fileId = result.files.first()?.id
//            pageToken = result.nextPageToken
//        } while (pageToken != null)
//        return fileId ?: ""
//    }
//
//    override fun getAllVideos(sortId: Int): Flow<PagingData<Video>> {
//        TODO("Not yet implemented")
//    }
////
////    override fun getAllVideos(sortId: Int): Flow<PagingData<Video>> = Pager(
////        config = PagingConfig(Constant.PAGE_SIZE, Constant.PREFETCH_DISTANCE, false),
////        pagingSourceFactory = {
////            Log.d(VideoRepositoryImpl.TAG, "getAllVideos() called")
//////             val result = service.files().list().setOrderBy("name asc")
//////                .execute()
////        }
////    ).flow
//
//    override suspend fun insert(vararg videos: Video) {
//        Log.d(TAG, "insertVideo() called with: videos = ${videos.size}")
//        for (video in videos) {
//            val fileMetaData = File()
//            fileMetaData.name = video.title
//            val file = video.data.toUri().toFile()
//            val fileContent = FileContent("video/*", file)
//            val fileRev = service.files()
//                .create(fileMetaData, fileContent)
//                .execute()
//            Log.d(TAG, "insertVideo: fileId=${fileRev.id}")
//        }
//    }
//
//    override fun update(video: Video): Int {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun countVideoWithUri(dataUri: String): Int {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun delete(videoId: Long): Int {
//        TODO("Not yet implemented")
//    }
//
//    override fun getAllPlayingVideos(): Flow<PagingData<Video>> {
//        TODO("Not yet implemented")
//    }
//
//    override fun getAllFavoriteVideo(): Flow<PagingData<Video>> {
//        TODO("Not yet implemented")
//    }
//
//    override fun getAllNextVideos(startId: Long): Flow<PagingData<Video>> {
//        TODO("Not yet implemented")
//    }
//
//    override fun hasUrl(url: String): Boolean {
//        TODO("Not yet implemented")
//    }
//
//    override fun findAllVideos(keyword: String): Flow<PagingData<Video>> {
//        TODO("Not yet implemented")
//    }
//
//}