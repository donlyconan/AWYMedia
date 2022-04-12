// IGoogleDrive.aidl
package com.utc.donlyconan.media;
import com.utc.donlyconan.media.IGoogleDriveListener;

// Declare any non-default types here with import statements
interface IGoogleDriveListener {

    void onUploading();

    void onDownloading();

    void onDownloadingCompleted();

    void onUploadingCompleted();

    void onCancel();

}