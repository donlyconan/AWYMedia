// IGoogleDrive.aidl
package com.utc.donlyconan.media;

import com.utc.donlyconan.media.IGoogleDriveListener;

// Declare any non-default types here with import statements

interface IGoogleDrive {

    void removeFromQueue(in String path);

    void downloadFile(in String path);

    void downloadAllFile(in List<String> path);

    void pushOnQueue(in String path);

    void pushAllOnQueue(in List<String> paths);

    void cancel();

    void registerGoogleDriveListener(in IGoogleDriveListener listener);

    void unregisterGoogleDriveListener(in IGoogleDriveListener listener);

}