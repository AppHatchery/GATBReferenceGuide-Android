package org.apphatchery.gatbreferenceguide.ui

interface ActionBarController {
    fun setActionBarTitle(title: String)
    fun setActionBarConfig(title: String, showBackButton: Boolean)
    fun setActionBarSearchVisible(visible: Boolean)
    fun setupActionBarSearch(onSearchAction: (String) -> Unit, onSearchIconClick: (String) -> Unit)
}