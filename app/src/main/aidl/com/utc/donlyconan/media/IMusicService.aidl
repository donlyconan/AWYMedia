// IMusicService.aidl
package com.utc.donlyconan.media;

// Declare any non-default types here with import statements
import com.utc.donlyconan.media.data.models.Video;

interface IMusicService {

   void setPlaylist(in int position, in List<Video> playlist);

   void setKeepPlaying(in boolean isKeepPlaying);

   void play();

   void next();

   void previous();

   void pause();

   void release();

}