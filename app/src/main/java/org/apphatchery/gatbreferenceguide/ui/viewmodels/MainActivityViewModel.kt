package org.apphatchery.gatbreferenceguide.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    val title = MutableLiveData<String>()
    val showBackButton = MutableLiveData<Boolean>()
    
    fun setActionBarConfig(title: String, showBackButton: Boolean = true) {
        this.title.value = title
        this.showBackButton.value = showBackButton
    }
}