package com.example.earlyreader.storage

import android.content.Context
import android.content.SharedPreferences

/** The few operations progress needs from a key-value store, so the bookkeeping can be tested without Android. */
interface KeyValueStore {
    fun getInt(key: String, default: Int): Int
    fun putInt(key: String, value: Int)
    fun getString(key: String, default: String): String
    fun putString(key: String, value: String)
    fun getStringSet(key: String): Set<String>
    fun putStringSet(key: String, value: Set<String>)
    fun clear()
}

class SharedPreferencesStore(private val prefs: SharedPreferences) : KeyValueStore {
    override fun getInt(key: String, default: Int): Int = prefs.getInt(key, default)
    override fun putInt(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
    override fun getString(key: String, default: String): String = prefs.getString(key, default) ?: default
    override fun putString(key: String, value: String) = prefs.edit().putString(key, value).apply()
    override fun getStringSet(key: String): Set<String> = prefs.getStringSet(key, emptySet())?.toSet() ?: emptySet()
    override fun putStringSet(key: String, value: Set<String>) = prefs.edit().putStringSet(key, value).apply()
    override fun clear() = prefs.edit().clear().apply()
}

class ProgressStorage(private val store: KeyValueStore) {
    constructor(context: Context) : this(
        SharedPreferencesStore(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)),
    )

    var completedWordsCount: Int
        get() = store.getInt(KEY_COMPLETED_WORDS, 0)
        set(value) = store.putInt(KEY_COMPLETED_WORDS, value)

    var completedStoryTitles: Set<String>
        get() = store.getStringSet(KEY_COMPLETED_STORIES)
        set(value) = store.putStringSet(KEY_COMPLETED_STORIES, value)

    var lastActiveStoryIndex: Int
        get() = store.getInt(KEY_LAST_STORY_INDEX, 0)
        set(value) = store.putInt(KEY_LAST_STORY_INDEX, value)

    var lastSentenceLabLevel: Int
        get() = store.getInt(KEY_LAST_SENTENCE_LEVEL, 0)
        set(value) = store.putInt(KEY_LAST_SENTENCE_LEVEL, value)

    var completedSentenceLevels: Set<String>
        get() = store.getStringSet(KEY_COMPLETED_SENTENCE_LEVELS)
        set(value) = store.putStringSet(KEY_COMPLETED_SENTENCE_LEVELS, value)

    var lastAppMode: String
        get() = store.getString(KEY_LAST_APP_MODE, MODE_WORDS)
        set(value) = store.putString(KEY_LAST_APP_MODE, value)

    fun recordWordCompleted() {
        completedWordsCount += 1
    }

    fun recordStoryCompleted(title: String) {
        val updated = completedStoryTitles.toMutableSet()
        updated.add(title)
        completedStoryTitles = updated
    }

    fun recordSentenceLevelCompleted(levelId: String) {
        val updated = completedSentenceLevels.toMutableSet()
        updated.add(levelId)
        completedSentenceLevels = updated
    }

    fun resetAll() {
        store.clear()
    }

    companion object {
        const val PREFS_NAME = "early_reader_prefs"
        const val KEY_COMPLETED_WORDS = "completed_words_count"
        const val KEY_COMPLETED_STORIES = "completed_story_titles"
        const val KEY_LAST_STORY_INDEX = "last_active_story_index"
        const val KEY_LAST_SENTENCE_LEVEL = "last_sentence_lab_level"
        const val KEY_COMPLETED_SENTENCE_LEVELS = "completed_sentence_levels"
        const val KEY_LAST_APP_MODE = "last_app_mode"

        const val MODE_WORDS = "WORDS"
        const val MODE_STORY = "STORY"
        const val MODE_TRACKING = "TRACKING"
        const val MODE_SENTENCES = "SENTENCES"
    }
}
