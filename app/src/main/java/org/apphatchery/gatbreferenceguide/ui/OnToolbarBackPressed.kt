package org.apphatchery.gatbreferenceguide.ui

interface OnToolbarBackPressed {
    /**
     * Called when the Activity toolbar/back button is pressed. Return true if
     * the fragment consumed the event (no nav pop), false to let the Activity
     * handle navigation.
     */
    fun onToolbarBackPressed(): Boolean
}
