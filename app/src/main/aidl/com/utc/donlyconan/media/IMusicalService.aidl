// IMusicalService.aidl
package com.utc.donlyconan.media;

// Declare any non-default types here with import statements

interface IMusicalService {

   void setVideoId(long videoId);

   void play();

   void release();

   void next();

   void previous();

   void stop();

   void restart();

}