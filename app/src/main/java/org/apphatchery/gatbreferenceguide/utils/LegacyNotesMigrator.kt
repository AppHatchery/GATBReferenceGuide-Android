package org.apphatchery.gatbreferenceguide.utils

import android.util.Log
import org.apphatchery.gatbreferenceguide.db.Database

object LegacyNotesMigrator {

    private const val TAG = "LegacyNotesMigrator"

    /**
     * One-time data migration that rewrites NoteEntity.noteId to follow renamed/merged
     * tables/sections using [LegacyRedirects].
     *
     * Example: notes targeting old tables 10/11/12 are redirected to the new merged table 9.
     */
    suspend fun migrateNoteTargets(db: Database): Int {
        val targets = db.noteDao().getAllNoteTargets()
        var changed = 0

        for (row in targets) {
            val oldId = row.noteId
            val redirected = LegacyRedirects.redirectBookmarkId(oldId)

            // For non-table note targets, older versions may have stored a title or url.
            // Try to resolve it to the current subChapterId.
            val resolvedSubChapter = if (!oldId.startsWith("table_")) {
                db.subChapterDao().findSubChapterByKeyOnceOrNull(redirected)
                    ?: db.subChapterDao().findSubChapterByKeyOnceOrNull(oldId)
            } else {
                null
            }

            val finalId = resolvedSubChapter?.subChapterId?.toString() ?: redirected

            if (finalId != oldId) {
                if (resolvedSubChapter != null) {
                    db.noteDao().updateNoteTargetAndSubChapterByPk(
                        pk = row.noteIdPrimaryKey,
                        newNoteId = finalId,
                        newSubChapterId = resolvedSubChapter.subChapterId,
                    )
                } else {
                    // Table/chart note: update the target id only.
                    db.noteDao().updateNoteTargetByPk(row.noteIdPrimaryKey, finalId)
                }
                changed++
            }
        }

        Log.i(TAG, "Migrated $changed note(s) to updated targets")
        return changed
    }
}
