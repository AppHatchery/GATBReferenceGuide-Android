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
    ], version = 3
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