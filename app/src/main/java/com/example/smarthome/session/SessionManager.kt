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
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class SessionManager(private val context: Context) {
    companion object {
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_LAST_ACTIVE_TIME = longPreferencesKey("last_active_time")
        private const val SESSION_TIMEOUT = 10 * 60 * 1000L // Timeout setelah 10 menit idle
    }

    // Simpan sesi saat user login
    suspend fun saveSession(isLoggedIn: Boolean, email: String?) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_LOGGED_IN] = isLoggedIn
            email?.let { preferences[KEY_USER_EMAIL] = it }
            preferences[KEY_LAST_ACTIVE_TIME] = System.currentTimeMillis() // Set waktu aktif saat login
        }
    }

    // Simpan waktu terakhir aplikasi digunakan
    suspend fun updateLastActiveTime() {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_ACTIVE_TIME] = System.currentTimeMillis()
        }
    }

    // Cek apakah sesi masih valid berdasarkan waktu idle
    private fun isSessionValid(): Boolean {
        val lastActiveTime = runBlocking {
            context.dataStore.data.map { it[KEY_LAST_ACTIVE_TIME] ?: System.currentTimeMillis() }.first()
        }
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastActiveTime) < SESSION_TIMEOUT
    }

    // Periksa status login berdasarkan waktu terakhir aktif
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        val isLoggedIn = preferences[KEY_IS_LOGGED_IN] ?: false
        val isValid = isSessionValid()
        if (!isValid) {
            runBlocking { logout() } // Hapus sesi jika sudah melebihi timeout
        }
        isLoggedIn && isValid
    }

    // Ambil email user
    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_EMAIL]
    }

    // Hapus sesi saat logout
    suspend fun logout() {
        context.dataStore.edit { it.clear() }
        FirebaseAuth.getInstance().signOut()
    }
}
