package pe.edu.idat.dsi.dami.idatgram.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "idatgram_session")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_USER_ID = stringPreferencesKey("current_user_id")
    }

    val currentUserIdFlow: Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[KEY_USER_ID] }

    suspend fun setCurrentUserId(userId: String?) {
        context.dataStore.edit { prefs ->
            if (userId == null) prefs.remove(KEY_USER_ID)
            else prefs[KEY_USER_ID] = userId
        }
    }
}
