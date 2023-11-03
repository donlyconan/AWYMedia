package com.utc.donlyconan.media.views

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.services.FileService
import com.utc.donlyconan.media.views.fragments.maindisplay.ListVideosFragment


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() called with: savedInstanceState = $savedInstanceState")
        setContentView(R.layout.activity_main)
        application.getFileService()?.registerOnFileServiceListener(onFileServiceListener)
    }

    override fun onDestroy() {
        application.getFileService()?.unregisterOnFileServiceListener(onFileServiceListener)
        super.onDestroy()
    }

    private val onFileServiceListener = object : FileService.OnFileServiceListener {

        override fun onDeletedFileError(uris: List<Uri>, e: Exception) {
            Log.d(TAG, "onDeletedFileError() called with: uris = $uris, e = $e")
            val intentSender: IntentSender? = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    MediaStore.createDeleteRequest(contentResolver, uris)
                        .intentSender
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    val exception = e as? RecoverableSecurityException
                    exception?.userAction?.actionIntent?.intentSender
                }
                else -> {
                    Log.d(ListVideosFragment.TAG, "onDeletedFileError() cannot delete uri = $uris")
                    null
                }
            }
            intentSender?.let { intent ->
                intentSenderForResult.launch(IntentSenderRequest.Builder(intent).build())
            }
        }
    }

    private val intentSenderForResult = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        Log.d(TAG, "onDeletedResult() called with: result = ${result.resultCode == Activity.RESULT_OK}")
        if(result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, R.string.can_t_delete_the_file, Toast.LENGTH_SHORT)
        }
    }

    companion object {
        val TAG: String = MainActivity.javaClass.simpleName
    }

}