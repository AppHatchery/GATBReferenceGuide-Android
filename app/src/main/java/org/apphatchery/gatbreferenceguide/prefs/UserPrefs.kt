// Persistent user preferences for the GA-TB Reference Guide app.
//
// TWO storage backends are intentionally used:
//   DataStore (Preferences)  — async, coroutine-safe; used for values that are observed as Flows
//                              (BUILD_VERSION, PENDO_VISITOR_ID). Prefer this for new keys.
//   SharedPreferences        — synchronous; used for values that must be read/written on the
//                              main thread without a coroutine (isFirstLaunch, KEY_UPDATE_VALUE).
//
// BUILD_VERSION: compared against MainFragment.BUILD_VERSION (= 14) on every cold start.
//   If they differ, firstLaunch() runs the full DB reseed + content-copy pipeline.
//   Bumping BUILD_VERSION in MainFragment is the mechanism for forcing a reseed on all devices.
//
// isFirstLaunch: set to false after the first Firebase Remote Config fetch completes.
//   The Remote Config listener uses this to suppress the "update available" popup on the
//   very first install (before the user has seen any content).
//
// Related: MainFragment (reads getBuildVersion, writes setBuildVersion, reads isFirstLaunch),
//          AppModule.kt (provides @Singleton UserPrefs via Hilt), App.kt (Hilt root).
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