package org.apphatchery.gatbreferenceguide.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.db.Database
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // v2 introduces the Contact table. Creating it preserves existing tables/data.
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `Contact` (
                  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  `fullName` TEXT NOT NULL,
                  `additionalInfo` TEXT NOT NULL,
                  `contactCell` TEXT NOT NULL,
                  `contactEmail` TEXT NOT NULL,
                  `contactAddress` TEXT NOT NULL,
                  `officePhone` TEXT NOT NULL,
                  `officeFax` TEXT NOT NULL,
                  `personalNote` TEXT
                )
                """.trimIndent()
            )
        }
    }

    @Singleton
    @Provides
    fun providesGlide(
        @ApplicationContext context: Context
    ): RequestManager = Glide.with(context)
        .setDefaultRequestOptions(RequestOptions.placeholderOf(R.drawable.ic_launcher_background))

    @Singleton
    @Provides
    fun providesRoomDB(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(context, Database::class.java, "ga_tb_reference_guide.db")
        .addMigrations(MIGRATION_1_2)
        .build()

    @Singleton
    @Provides
    fun providesFirebaseAnalytics(
        @ApplicationContext context: Context
    ): FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

//    @Singleton
//    @Provides
//    fun providesContent(
//        @ApplicationContext context: Context
//    ): Context = context


//    @Singleton
//    @Provides
//    fun providesRetrofit(): Retrofit {
//        return Retrofit.Builder()
//            .baseUrl("https://api.github.com/repos/apphatchery/GA-TB-Reference-Guide-Web/contents/pages")
//            .addConverterFactory(ScalarsConverterFactory.create())
//            .build()
//    }
//
//    @Singleton
//    @Provides
//    fun providesGitHubService(retrofit: Retrofit): GitHubService {
//        return retrofit.create(GitHubService::class.java)
//    }
}
