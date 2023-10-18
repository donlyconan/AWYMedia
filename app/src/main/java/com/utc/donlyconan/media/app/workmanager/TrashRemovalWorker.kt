package com.utc.donlyconan.media.app.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.utc.donlyconan.media.app.EGMApplication
import com.utc.donlyconan.media.app.utils.Logs

class TrashRemovalWorker(app: Context, params: WorkerParameters): Worker(app, params) {

    override fun doWork(): Result {
        Logs.d("doWork: start working...")
        var application = applicationContext as EGMApplication
        var trashDao = application.applicationComponent().getTrashDao()

        // TODO Remove files from the recycle bin


        return Result.success()
    }

}