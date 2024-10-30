package org.apphatchery.gatbreferenceguide.retrofit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WebViewViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WebViewViewModel(repository) as T
    }
}
