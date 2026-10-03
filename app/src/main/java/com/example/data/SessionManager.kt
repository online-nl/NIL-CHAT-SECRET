package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nil_dark_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val SESSION_KEY = "nil_dark_session_v3"
        private const val KEY_CACHED_NAME = "nil_dark_cached_name"
        private const val KEY_CACHED_CHAT_ID = "nil_dark_cached_chat_id"
    }

    var currentUid: String?
        get() = prefs.getString(SESSION_KEY, null)
        set(value) {
            prefs.edit().apply {
                if (value == null) {
                    remove(SESSION_KEY)
                } else {
                    putString(SESSION_KEY, value)
                }
                apply()
            }
        }

    fun saveCachedInfo(name: String, chatId: String) {
        prefs.edit()
            .putString(KEY_CACHED_NAME, name)
            .putString(KEY_CACHED_CHAT_ID, chatId)
            .apply()
    }

    val cachedName: String? get() = prefs.getString(KEY_CACHED_NAME, null)
    val cachedChatId: String? get() = prefs.getString(KEY_CACHED_CHAT_ID, null)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
