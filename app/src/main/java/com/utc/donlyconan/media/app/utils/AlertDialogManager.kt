package com.utc.donlyconan.media.app.utils

import android.R
import android.content.Context
import androidx.annotation.MainThread
import androidx.appcompat.app.AlertDialog


object AlertDialogManager {

    /**
     * Create a AlertDialog for delete action
     */
    @MainThread
    fun createDeleteAlertDialog(
        context: Context,
        title: String,
        msg: String,
        positiveText: Int = R.string.yes,
        negativeText: Int = R.string.no,
        onAccept: () -> Unit,
        onDeny: () -> Unit = {},
        cancelable: Boolean = true
    ): AlertDialog {
        return AlertDialog.Builder(context, com.utc.donlyconan.media.R.style.MyAlertDialog)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(positiveText) { _, _ -> onAccept() }
            .setNegativeButton(negativeText) { _, _ -> onDeny() }
            .setIcon(R.drawable.ic_dialog_alert)
            .setCancelable(cancelable)
            .create()
    }

}