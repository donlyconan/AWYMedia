package com.utc.donlyconan.media.app.settings

import android.content.Context
import androidx.preference.PreferenceManager
import java.util.*
import javax.inject.Inject

class Settings @Inject constructor(val appContext: Context) {

    companion object {
        const val SETTINGS_NAME = "sra-datn"
        // options of sort by
        const val SORT_BY_NAME = 1
        const val SORT_BY_CREATION = 2
        const val SORT_BY_RECENT = 3
        const val SORT_BY_DURATION = 4

        private var instance: Settings? = null

        fun getInstance(appContext: Context) = synchronized(Settings::class.java) {
            if(instance == null){
                instance = Settings(appContext)
            }
            instance!!
        }
    }

    var preferences = PreferenceManager.getDefaultSharedPreferences(appContext)
        private set
    var language by StringPreferenceDelegate(preferences, "language",
        if(Locale.getDefault().language == "vn" || Locale.getDefault().language == "en")
            Locale.getDefault().language else "en")
    var isWellcome by BooleanPreferenceDelegate(preferences, "wellcome_to_app", false)
    var sortBy by IntPreferenceDelegate(preferences, "sort_by", SORT_BY_NAME)
    var playlistSortBy by IntPreferenceDelegate(preferences, "playlist_sort_by", SORT_BY_NAME)
    var deleteFromStorage by BooleanPreferenceDelegate(preferences, "delete_from_storage", false)
    var autoPlay by BooleanPreferenceDelegate(preferences, "auto_play", true)
    var autoDownload by BooleanPreferenceDelegate(preferences, "auto_download", true)
    var erasureCycle by StringPreferenceDelegate(preferences, "erasure_cycle", "30")
    var previousDeletionDate by LongPreferenceDelegate(preferences, "deletion_date", 0L)

    /**
     * It represent for Auto Play Mode on Video Screen
     */
    var autoPlayMode by BooleanPreferenceDelegate(preferences, "auto_play_mode", true)
}
