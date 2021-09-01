package org.apphatchery.gatbreferenceguide.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.createDataStore
import androidx.lifecycle.asLiveData
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
        val FIRST_LAUNCH = booleanPreferencesKey("FIRST_LAUNCH")
    }


    suspend fun setFirstLaunch(isFirstLaunch: Boolean) {
        dataStore.edit {
            it[FIRST_LAUNCH] = isFirstLaunch
        }
    }

    val getFirstLaunch: Flow<Boolean> = dataStore.data.map {
        it[FIRST_LAUNCH] ?: true
    }

}