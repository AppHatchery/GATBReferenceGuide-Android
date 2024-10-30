package org.apphatchery.gatbreferenceguide.di

import android.content.Context
import androidx.room.Room
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
import org.apphatchery.gatbreferenceguide.retrofit.GitHubService
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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
        .fallbackToDestructiveMigration()
        .build()

    @Singleton
    @Provides
    fun providesFirebaseAnalytics(
        @ApplicationContext context: Context
    ): FirebaseAnalytics = FirebaseAnalytics.getInstance(context)



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
