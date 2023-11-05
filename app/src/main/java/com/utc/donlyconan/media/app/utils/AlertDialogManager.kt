package com.utc.donlyconan.media.app.utils

import android.R
import android.content.Context
import android.content.DialogInterface
import androidx.annotation.MainThread
import androidx.appcompat.app.AlertDialog


object AlertDialogManager {

    /**
     * Create a AlertDialog for delete action
     */
    @MainThread
    fun createDeleteAlertDialog(context: Context, title: String, msg: String, onAccept: () -> Unit, onDeny: () -> Unit = {}, cancelable: Boolean = true): AlertDialog {
        return AlertDialog.Builder(context, com.utc.donlyconan.media.R.style.MyAlertDialog)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(R.string.yes) { _, _ -> onAccept() }
            .setNegativeButton(R.string.no) { _, _ -> onDeny() }
            .setIcon(R.drawable.ic_dialog_alert)
            .setCancelable(cancelable)
            .create()
    }

}