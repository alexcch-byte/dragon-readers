package com.example.earlyreader.storage

import android.content.Context
import android.content.SharedPreferences

class ProgressStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var completedWordsCount: Int
        get() = prefs.getInt(KEY_COMPLETED_WORDS, 0)
        set(value) = prefs.edit().putInt(KEY_COMPLETED_WORDS, value).apply()

    var completedStoryTitles: Set<String>
        get() = prefs.getStringSet(KEY_COMPLETED_STORIES, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_COMPLETED_STORIES, value).apply()

    var lastActiveStoryIndex: Int
        get() = prefs.getInt(KEY_LAST_STORY_INDEX, 0)
        set(value) = prefs.edit().putInt(KEY_LAST_STORY_INDEX, value).apply()

    var lastAppMode: String
        get() = prefs.getString(KEY_LAST_APP_MODE, MODE_WORDS) ?: MODE_WORDS
        set(value) = prefs.edit().putString(KEY_LAST_APP_MODE, value).apply()

    fun recordWordCompleted() {
        completedWordsCount += 1
    }

    fun recordStoryCompleted(title: String) {
        val updated = completedStoryTitles.toMutableSet()
        updated.add(title)
        completedStoryTitles = updated
    }

    fun resetAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        const val PREFS_NAME = "early_reader_prefs"
        const val KEY_COMPLETED_WORDS = "completed_words_count"
        const val KEY_COMPLETED_STORIES = "completed_story_titles"
        const val KEY_LAST_STORY_INDEX = "last_active_story_index"
        const val KEY_LAST_APP_MODE = "last_app_mode"

        const val MODE_WORDS = "WORDS"
        const val MODE_STORY = "STORY"
    }
}
