package com.utc.donlyconan.media.views.fragments

import com.utc.donlyconan.media.data.models.Video

class VideoTask(val videos: List<Video>, val succeed: Runnable? = null, val error: Runnable? = null)