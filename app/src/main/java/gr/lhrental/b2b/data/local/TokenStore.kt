package gr.lhrental.b2b.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "lh_b2b_session")

/**
 * Holds the bearer token issued by POST auth/login.php, and the one small
 * user preference (biometric unlock) that has to survive a process restart
 * the same way the token does.
 *
 * Nothing here talks to Android Keystore-backed EncryptedSharedPreferences yet —
 * for a v1 this is plain DataStore, same trust boundary as any other app-private
 * file. Worth upgrading before a production release.
 */
class TokenStore(context: Context) {
    private val appContext = context.applicationContext
    private val tokenKey = stringPreferencesKey("api_token")
    private val biometricEnabledKey = booleanPreferencesKey("biometric_enabled")

    val tokenFlow: Flow<String?> = appContext.dataStore.data.map { it[tokenKey] }

    /**
     * Off by default — a returning session only gets gated behind
     * BiometricPrompt once the customer explicitly turns this on from the
     * account screen (and it's proven to work on their device right then).
     */
    val biometricEnabledFlow: Flow<Boolean> = appContext.dataStore.data.map { it[biometricEnabledKey] ?: false }

    suspend fun currentToken(): String? = tokenFlow.first()

    suspend fun save(token: String) {
        appContext.dataStore.edit { it[tokenKey] = token }
    }

    suspend fun clear() {
        appContext.dataStore.edit { it.remove(tokenKey) }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        appContext.dataStore.edit { it[biometricEnabledKey] = enabled }
    }
}
