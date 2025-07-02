package com.example.smarthome.session

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class SessionManager(private val context: Context) {
    companion object {
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_LAST_ACTIVE_TIME = longPreferencesKey("last_active_time")
        private const val SESSION_TIMEOUT = 10 * 60 * 1000L // 10 menit
    }

    suspend fun saveSession(isLoggedIn: Boolean, email: String?) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_LOGGED_IN] = isLoggedIn
            email?.let { preferences[KEY_USER_EMAIL] = it }
            preferences[KEY_LAST_ACTIVE_TIME] = System.currentTimeMillis()
        }
    }

    suspend fun updateLastActiveTime() {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_ACTIVE_TIME] = System.currentTimeMillis()
        }
    }

    // Digunakan untuk mengecek apakah sesi timeout (tanpa clear data)
    suspend fun isSessionExpired(): Boolean {
        val lastActiveTime = context.dataStore.data
            .map { it[KEY_LAST_ACTIVE_TIME] ?: System.currentTimeMillis() }
            .first()
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastActiveTime) >= SESSION_TIMEOUT
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_LOGGED_IN] ?: false
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_EMAIL]
    }

    // Ini hanya hapus data session, tidak sign out akun
    suspend fun clearSessionData() {
        context.dataStore.edit {
            it.remove(KEY_LAST_ACTIVE_TIME)
            it[KEY_IS_LOGGED_IN] = false // Untuk memicu redirect UI ke login
        }
    }

    suspend fun logout() {
        context.dataStore.edit { it.clear() }
        FirebaseAuth.getInstance().signOut()
    }
}
