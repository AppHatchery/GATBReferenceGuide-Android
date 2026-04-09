// ViewModel scoped to MainActivity — provides shared toolbar state observed by all fragments.
// Because it is Activity-scoped (created via viewModels() in MainActivity), it survives
// fragment transactions and allows any fragment to update the toolbar without direct Activity
// references, avoiding tight coupling.
//
// title (MutableLiveData<String>): the current toolbar title string; observed by MainActivity
//   to call supportActionBar.title. Each fragment sets this when it becomes visible.
// showBackButton (MutableLiveData<Boolean>): controls whether the toolbar back arrow is shown;
//   top-level fragments pass false, detail fragments pass true.
//
// setActionBarConfig(title, showBackButton): convenience method for fragments to update both
//   toolbar properties in a single call. showBackButton defaults to true for detail fragments.
//
// Related files: MainActivity, all Fragment classes that call setActionBarConfig().
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