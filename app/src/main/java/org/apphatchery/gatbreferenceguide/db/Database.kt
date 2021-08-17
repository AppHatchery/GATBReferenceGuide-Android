package org.apphatchery.gatbreferenceguide.db

import androidx.room.Database
import androidx.room.RoomDatabase
import org.apphatchery.gatbreferenceguide.db.dao.*
import org.apphatchery.gatbreferenceguide.db.entities.*

@Database(
    entities = [
        ChapterEntity::class,
        RecentEntity::class,
        SubChapterEntity::class,
        NoteEntity::class,
        ChartEntity::class,
        BookmarkEntity::class
    ], version = 1, exportSchema = false
)
abstract class Database : RoomDatabase() {
    abstract fun chapterDao(): ChapterDao
    abstract fun subChapterDao(): SubChapterDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun noteDao(): NoteDao
    abstract fun chartDao(): ChartDao
}