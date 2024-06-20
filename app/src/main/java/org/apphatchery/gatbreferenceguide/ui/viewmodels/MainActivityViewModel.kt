package org.apphatchery.gatbreferenceguide.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    val title = MutableLiveData<String>()
}