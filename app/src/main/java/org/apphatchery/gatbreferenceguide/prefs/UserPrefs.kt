package org.apphatchery.gatbreferenceguide.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.createDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPrefs @Inject constructor(
    @ApplicationContext context: Context
) {

    private val dataStore =
        context.createDataStore(name = "org.apphatchery.gatbreferenceguide.prefs")

    companion object {
        val BUILD_VERSION = intPreferencesKey("BUILD_VERSION")
    }


    suspend fun setBuildVersion(version: Int) =
        dataStore.edit { it[BUILD_VERSION] = version }

    val getBuildVersion = dataStore.data.map { it[BUILD_VERSION] ?: true }

}