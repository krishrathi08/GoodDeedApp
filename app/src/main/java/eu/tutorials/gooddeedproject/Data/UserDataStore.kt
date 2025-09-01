package eu.tutorials.gooddeedproject.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create a DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserDataStore(private val context: Context) {

    companion object {
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    // Function to save the email
    suspend fun saveEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL_KEY] = email
        }
    }

    // Function to clear the email
    suspend fun clearEmail() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_EMAIL_KEY)
        }
    }

    // A Flow to read the saved email in real-time
    val getEmail: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[USER_EMAIL_KEY] ?: ""
        }
}