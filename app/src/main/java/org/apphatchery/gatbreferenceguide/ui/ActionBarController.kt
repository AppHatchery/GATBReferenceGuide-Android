// Interface implemented by MainActivity that gives fragments a decoupled way to control
// the custom action bar (custom_action_bar_title layout) without direct Activity references.
//
//   setActionBarTitle   - updates only the title text (used by BodyFragment mid-navigation)
//   setActionBarConfig  - sets title AND shows/hides the back button + spacer in one call
//   setActionBarSearchVisible - shows/hides the in-fragment search view container
//   setupActionBarSearch - wires the action bar search EditText to a fragment-supplied callback
//
// Fragments call these via BaseFragment helper delegates. Related: BaseFragment, MainActivity.
package org.apphatchery.gatbreferenceguide.ui

interface ActionBarController {
    fun setActionBarTitle(title: String)
    fun setActionBarConfig(title: String, showBackButton: Boolean)
    fun setActionBarSearchVisible(visible: Boolean)
    fun setupActionBarSearch(onSearchAction: (String) -> Unit, onSearchIconClick: (String) -> Unit)
}