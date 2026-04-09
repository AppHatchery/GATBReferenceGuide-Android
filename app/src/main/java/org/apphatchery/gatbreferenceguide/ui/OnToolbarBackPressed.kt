// Interface for fragments that need to intercept the toolbar/system back-press event.
// Implemented by BodyFragment to let the WebView consume browser back-navigation (stepping
// through HTML page history) before the NavController pops the fragment back stack.
//
// Contract: implement in a Fragment and return true if the event was consumed (NavController
// will NOT pop), or false to let MainActivity.onSupportNavigateUp() handle it normally.
// Related: BaseFragment, MainActivity.onSupportNavigateUp(), BodyFragment.
package org.apphatchery.gatbreferenceguide.ui

interface OnToolbarBackPressed {
    /**
     * Called when the Activity toolbar/back button is pressed. Return true if
     * the fragment consumed the event (no nav pop), false to let the Activity
     * handle navigation.
     */
    fun onToolbarBackPressed(): Boolean
}
