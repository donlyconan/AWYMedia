package com.utc.donlyconan.media.views

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import com.utc.donlyconan.media.R


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: ")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        requestPermissionsIfNeed()
    }
    
    private fun requestPermissionsIfNeed() {
        Log.d(TAG, "requestPermissionsIfNeed() called")
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "requestPermissionsIfNeed: request permission from app!")
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_WRITE);
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult() called with: requestCode = $requestCode, " +
                "permissions = $permissions, grantResults = $grantResults")
        if (requestCode == REQUEST_READ_WRITE && grantResults.isEmpty()) {
            Log.d(TAG, "onRequestPermissionsResult: App hasn't permission to access the storage")
            finish()
        }
    }

    companion object {
        val TAG: String = MainActivity.javaClass.simpleName
        const val REQUEST_READ_WRITE = 1
    }

}