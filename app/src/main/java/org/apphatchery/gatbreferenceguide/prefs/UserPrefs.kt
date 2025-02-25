package org.apphatchery.gatbreferenceguide.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "org.apphatchery.gatbreferenceguide.prefs")

class UserPrefs @Inject constructor(
    @ApplicationContext context: Context
) {

    private val dataStore =
        context.dataStore

    companion object {
        val BUILD_VERSION = intPreferencesKey("BUILD_VERSION")
        val PENDO_VISITOR_ID = stringPreferencesKey("PENDO_VISITOR_ID")
    }

    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)


    suspend fun setBuildVersion(version: Int) =
        dataStore.edit { it[BUILD_VERSION] = version }

    suspend fun setPendoVisitorId(id: String) =
        dataStore.edit { it[PENDO_VISITOR_ID] = id }

    val getBuildVersion: Flow<Int> = dataStore.data.map { it[BUILD_VERSION] ?: 1 }

    val getPendoVisitorId: Flow<String> = dataStore.data.map { it[PENDO_VISITOR_ID] ?: "" }

    fun getSavedUpdateValue(): Int {
        return sharedPreferences.getInt("KEY_UPDATE_VALUE", 0) // Default to -1 if not set
    }

    fun saveUpdateValue(value: Int) {
        sharedPreferences.edit()
            .putInt("KEY_UPDATE_VALUE", value)
            .apply()
    }


    var isFirstLaunch: Boolean
        get() = sharedPreferences.getBoolean("is_first_launch", true)
        set(value) {
            sharedPreferences.edit().putBoolean("is_first_launch", value).apply()
        }


}