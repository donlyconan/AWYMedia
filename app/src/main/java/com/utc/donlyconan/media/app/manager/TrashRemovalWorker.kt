package com.utc.donlyconan.media.app.manager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.utc.donlyconan.media.app.EGMApplication
import com.utc.donlyconan.media.app.utils.Logs
import com.utc.donlyconan.media.app.utils.now
import com.utc.donlyconan.media.data.repo.TrashRepository

class TrashRemovalWorker(app: Context, params: WorkerParameters, val trashRepository: TrashRepository): Worker(app, params) {

    companion object {
        const val DURATION_30_DAYS = 84_000_000L * 30
    }

    override fun doWork(): Result {
        Logs.d("doWork: start working...")
        val application = applicationContext as EGMApplication
        application.getFileService()?.runIO {
            val timeNow = now()
            val listTrash = trashRepository.getAllTrashes().filter { trash ->
                trash.deletedAt < timeNow - DURATION_30_DAYS && trash.title != null
            }.map { it.title!! }
            Logs.d("doWork() the list will be removed $listTrash")
//            deleteFileFromLocalData(*listTrash.toTypedArray())
        }
        return Result.success()
    }

}