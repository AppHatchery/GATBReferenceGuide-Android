package org.apphatchery.gatbreferenceguide.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.createDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPrefs @Inject constructor(
    @ApplicationContext context: Context
) {

    private val dataStore =
        context.createDataStore(name = "org.apphatchery.gatbreferenceguide.prefs")

    companion object {
        val BUILD_VERSION = intPreferencesKey("BUILD_VERSION")
        val PENDO_VISITOR_ID = stringPreferencesKey("PENDO_VISITOR_ID")
    }


    suspend fun setBuildVersion(version: Int) =
        dataStore.edit { it[BUILD_VERSION] = version }

    suspend fun setPendoVisitorId(id: String) =
        dataStore.edit { it[PENDO_VISITOR_ID] = id }

    val getBuildVersion: Flow<Int> = dataStore.data.map { it[BUILD_VERSION] ?: 1 }

    val getPendoVisitorId: Flow<String> = dataStore.data.map { it[PENDO_VISITOR_ID] ?: "" }

}