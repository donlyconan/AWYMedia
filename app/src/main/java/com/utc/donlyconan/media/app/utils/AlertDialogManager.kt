package com.utc.donlyconan.media.app.utils

import android.R
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog


object AlertDialogManager {

    /**
     * Create a AlertDialog for delete action
     */
    fun createDeleteAlertDialog(context: Context, title: String, msg: String, onAccept: () -> Unit): AlertDialog {
        return AlertDialog.Builder(context, com.utc.donlyconan.media.R.style.MyAlertDialog)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(R.string.yes, DialogInterface.OnClickListener { _, _ -> onAccept() })
            .setNegativeButton(R.string.no, null)
            .setIcon(R.drawable.ic_dialog_alert)
            .create()
    }

}