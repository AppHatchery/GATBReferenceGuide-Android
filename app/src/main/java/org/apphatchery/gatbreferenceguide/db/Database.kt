// Room database contract for the GA-TB Reference Guide — defines all 9 tables and DAO accessors.
// Room generates the SQLite implementation at compile time; the physical DB file on device is
// named "ga_tb_reference_guide.db" (set in AppModule.kt). Current schema is version 2.
//
// Two categories of tables:
//   Seeded content (wiped + re-seeded on every app update via GuideContentUpdater.kt):
//     ChapterEntity, SubChapterEntity, ChartEntity, GlobalSearchEntity, HtmlInfoEntity
//   User data (never auto-wiped — belongs to the user):
//     BookmarkEntity, NoteEntity, RecentEntity, Contact
//
// purgeData(): clears ONLY seeded-content tables in a single atomic transaction. It intentionally
// skips user data. If you add a new seeded table in the future, add its deleteAll() call here.
// The Log.w with a stack trace helps catch unexpected callers during debugging.
//
// All DAO accessors return Room-generated implementations. Call them from Repository or ViewModels
// inside coroutines — never from the main thread or directly from Fragment/Activity code.
// Related: di/AppModule.kt (singleton builder + migrations), db/repositories/Repository.kt.
package org.apphatchery.gatbreferenceguide.db

import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import org.apphatchery.gatbreferenceguide.db.dao.*
import org.apphatchery.gatbreferenceguide.db.entities.*

@Database(
    entities = [
        ChapterEntity::class,
        RecentEntity::class,
        SubChapterEntity::class,
        NoteEntity::class,
        ChartEntity::class,
        BookmarkEntity::class,
        GlobalSearchEntity::class,
        HtmlInfoEntity::class,
        Contact::class,
    ], version = 2
)
abstract class Database : RoomDatabase() {
    abstract fun chapterDao(): ChapterDao
    abstract fun subChapterDao(): SubChapterDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun noteDao(): NoteDao
    abstract fun chartDao(): ChartDao
    abstract fun htmlInfoDao(): HtmlInfoDao
    abstract fun globalSearchDao(): GlobalSearchDao
    abstract fun recentDao(): RecentDao
    abstract fun contactDao(): ContactDao

    suspend fun purgeData() {
        Log.w("DB_PURGE", "purgeData() called", Throwable("purgeData stack"))
        withTransaction {
            chapterDao().deleteAll()
            chartDao().deleteAll()
            subChapterDao().deleteAll()
            htmlInfoDao().deleteAll()
            globalSearchDao().deleteAll()
        }
    }
}
