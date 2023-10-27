package com.utc.donlyconan.media.data.repo

import android.app.Application
import com.utc.donlyconan.media.data.dao.ListVideoDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListVideoRepository @Inject constructor(
    val app: Application,
    private val listDao: ListVideoDao) : ListVideoDao by listDao {

}