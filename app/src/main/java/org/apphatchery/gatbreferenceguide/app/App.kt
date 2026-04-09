// GA-TB Reference Guide application class — the first code that runs when the app process starts.
// @HiltAndroidApp makes this the ROOT of the Hilt DI graph; removing it breaks every @Inject and
// @HiltViewModel throughout the entire app, so this annotation must never be removed or changed.
//
// attachBaseContext(): locks fontScale to 1.0f before any view inflates. Without this, users who
// enable "Large Text" in Android Accessibility settings would break the guide's HTML WebView
// layouts, which are designed for a fixed scale. Must override attachBaseContext (not onCreate)
// because resources are resolved from the base context before onCreate() runs.
//
// onCreate(): initializes process-wide SDKs in dependency order: FirebaseApp first (required
// before any Analytics/Crashlytics usage), then night-mode preference (applied before the first
// Activity frame so there is no visible light/dark flash on cold start), then Pendo analytics.
//
// Add new process-wide SDK inits here only if they must run before any Activity is created.
// Theme preference is written by SettingsFragment (R.string.theme_key / R.array.theme_values).
// Related: di/AppModule.kt (Hilt providers), ui/MainActivity.kt (first Activity after this runs).
package org.apphatchery.gatbreferenceguide.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import org.apphatchery.gatbreferenceguide.R
import sdk.pendo.io.Pendo

@HiltAndroidApp
class App : Application() {

    override fun attachBaseContext(base: Context) {
        val configuration = Configuration(base.resources.configuration)
        configuration.fontScale = 1.0f
        super.attachBaseContext(base.createConfigurationContext(configuration))
    }

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        val themeValue: Array<String> = resources.getStringArray(R.array.theme_values)
        when ( PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.theme_key), themeValue[0])
            .toString()) {
            themeValue[0] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            themeValue[1] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            themeValue[2] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        val pendoAppKey = "eyJhbGciOiJSUzI1NiIsImtpZCI6IiIsInR5cCI6IkpXVCJ9.eyJkYXRhY2VudGVyIjoidXMiLCJrZXkiOiJjNGRhZTZhYTRmN2Q1ZTcwMTk4OWRkZWE5MTczNTY5MmFlYmQ5N2QyMTEyZWFmZWUyNmU4ZDVmOWVlZjczY2RlMTYzOWRjOWY1ODUyM2M2MjcxM2IwNzFkMDY2ZjFhYmRlNDIwNDRhMmFiZTJmZmRkNjI3ODFjOTdhODUxMDAyZDMzZWZhMDU3ZTEwNGZiOGUyMGM3MWZkMWE1YTA3NzQ3LjQyZmQ1NDVlYzA1YTEwZDQ5NTQ1NzI2ODVhZDZjMzhkLmQwOGI3MzlmMjlmOTdmMzNiZjMxOWZlODkwNGVlNTMwYjBiOTkwYTU2MGNlNTM0ZmMzYmRkYzE1ZTUyZTU4ZTcifQ.VOjiWN-Wz479ZXkybvZiCcvfPEoxbchkOgOGo8DACG8qIsQZc694-3axB5b9Xqpa0BlLk5HAVyTVzq74AdpAwZ1yet22u-P8dnl8AbgxCBQTw_B67go-NtqiowmznVozFB_FnILe3XDDwiJEyViTA33Kn9kCpfBdRbwQfFf9_wQ"


        Pendo.setup(
            this,
            pendoAppKey,
            null,
        null)
    }
}
