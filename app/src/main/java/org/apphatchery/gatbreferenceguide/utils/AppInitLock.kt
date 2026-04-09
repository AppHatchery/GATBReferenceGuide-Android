// Process-wide mutex that serialises the app's two destructive startup operations:
//   1. replaceBundledGuideWebContent() — wipes and re-copies HTML/CSS/JS/images into cacheDir.
//   2. purgeAndSeedFromAssets()         — drops and re-inserts every Room DB table from JSON.
//
// Both operations run inside MainFragment.firstLaunch() on a background coroutine.
// Without this lock, a second call (e.g. rapid back-nav + re-enter) could start a second
// seed while the first is mid-flight, leaving the DB in a partially-cleared state and
// potentially serving users a blank or corrupted guide.
//
// Callers must use: AppInitLock.mutex.withLock { ... }
// Related: GuideContentUpdater.kt (replaceBundledGuideWebContent),
//          MainFragment.firstLaunch() (the only current caller), LegacyNotesMigrator.kt.
package org.apphatchery.gatbreferenceguide.utils

import kotlinx.coroutines.sync.Mutex

/**
 * Process-wide lock to prevent concurrent initialization work.
 *
 * We use this to serialize operations that can temporarily invalidate app state
 * (e.g., replacing cached HTML content and purging+seeding the Room DB).
 */
object AppInitLock {
    val mutex: Mutex = Mutex()
}
