package org.apphatchery.gatbreferenceguide.app

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import sdk.pendo.io.Pendo

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

        val pendoAppKey = "eyJhbGciOiJSUzI1NiIsImtpZCI6IiIsInR5cCI6IkpXVCJ9.eyJkYXRhY2VudGVyIjoidXMiLCJrZXkiOiJjNGRhZTZhYTRmN2Q1ZTcwMTk4OWRkZWE5MTczNTY5MmFlYmQ5N2QyMTEyZWFmZWUyNmU4ZDVmOWVlZjczY2RlMTYzOWRjOWY1ODUyM2M2MjcxM2IwNzFkMDY2ZjFhYmRlNDIwNDRhMmFiZTJmZmRkNjI3ODFjOTdhODUxMDAyZDMzZWZhMDU3ZTEwNGZiOGUyMGM3MWZkMWE1YTA3NzQ3LjQyZmQ1NDVlYzA1YTEwZDQ5NTQ1NzI2ODVhZDZjMzhkLmQwOGI3MzlmMjlmOTdmMzNiZjMxOWZlODkwNGVlNTMwYjBiOTkwYTU2MGNlNTM0ZmMzYmRkYzE1ZTUyZTU4ZTcifQ.VOjiWN-Wz479ZXkybvZiCcvfPEoxbchkOgOGo8DACG8qIsQZc694-3axB5b9Xqpa0BlLk5HAVyTVzq74AdpAwZ1yet22u-P8dnl8AbgxCBQTw_B67go-NtqiowmznVozFB_FnILe3XDDwiJEyViTA33Kn9kCpfBdRbwQfFf9_wQ"


        Pendo.setup(
            this,
            pendoAppKey,
            null,
        null)
    }
}