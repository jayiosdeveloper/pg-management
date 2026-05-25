package com.pg.management.core.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pg.management.domain.auth.AuthSession
import com.pg.management.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore by preferencesDataStore(name = "auth_prefs")

@Singleton
class AuthStorage @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
) {
    private object Keys {
        val ACCESS = stringPreferencesKey("access_token")
        val REFRESH = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_CODE = stringPreferencesKey("user_code")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val ROLE = stringPreferencesKey("role")
        val REMEMBER = booleanPreferencesKey("remember_me")
    }

    val sessionFlow: Flow<AuthSession?> = context.authDataStore.data.map { prefs ->
        val access = prefs[Keys.ACCESS] ?: return@map null
        val refresh = prefs[Keys.REFRESH] ?: return@map null
        val role = prefs[Keys.ROLE]?.let(UserRole::fromString) ?: return@map null
        AuthSession(
            accessToken = access,
            refreshToken = refresh,
            userId = prefs[Keys.USER_ID].orEmpty(),
            userCode = prefs[Keys.USER_CODE].orEmpty(),
            fullName = prefs[Keys.USER_NAME].orEmpty(),
            email = prefs[Keys.USER_EMAIL],
            role = role,
        )
    }

    suspend fun currentSession(): AuthSession? = sessionFlow.first()

    suspend fun save(session: AuthSession, remember: Boolean) {
        context.authDataStore.edit { p ->
            p[Keys.ACCESS] = session.accessToken
            p[Keys.REFRESH] = session.refreshToken
            p[Keys.USER_ID] = session.userId
            p[Keys.USER_CODE] = session.userCode
            p[Keys.USER_NAME] = session.fullName
            session.email?.let { p[Keys.USER_EMAIL] = it }
            p[Keys.ROLE] = session.role.value
            p[Keys.REMEMBER] = remember
        }
    }

    suspend fun updateTokens(access: String, refresh: String) {
        context.authDataStore.edit { p ->
            p[Keys.ACCESS] = access
            p[Keys.REFRESH] = refresh
        }
    }

    suspend fun clear() {
        context.authDataStore.edit { it.clear() }
    }

    suspend fun rememberMe(): Boolean = context.authDataStore.data.first()[Keys.REMEMBER] ?: false
}
