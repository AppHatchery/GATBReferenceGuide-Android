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
