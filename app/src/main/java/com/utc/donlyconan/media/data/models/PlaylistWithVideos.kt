package com.utc.donlyconan.media.data.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PlaylistWithVideos(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "playlist_id",
        entityColumn = "video_id",
        associateBy = Junction(VideoPlaylistCrossRef::class)
    )
    val videos: List<Video>
)