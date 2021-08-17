package org.apphatchery.gatbreferenceguide.di

import android.content.Context
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.api.API
import org.apphatchery.gatbreferenceguide.db.Database
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
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
    fun providesRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl("BASE_URL")
            .addConverterFactory(GsonConverterFactory.create())
            .build()


    @RetrofitDownloadClient
    @Singleton
    @Provides
    fun providesRetrofitDownloadClient(): Retrofit =
        Retrofit.Builder()
            .baseUrl("")
            .build()

    @Singleton
    @Provides
    fun providesAPI(retrofit: Retrofit): API = retrofit.create(API::class.java)


    @RetrofitDownloadClientAPI
    @Singleton
    @Provides
    fun providesRetrofitDownloadClientAPI(
        @RetrofitDownloadClient
        retrofit: Retrofit
    ): API = retrofit.create(
        API::class.java
    )

    @Singleton
    @Provides
    fun providesRoomDB(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(context, Database::class.java, "ga_tb_reference_guide.db")
        .fallbackToDestructiveMigration()
        .build()
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class RetrofitDownloadClientAPI


@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class RetrofitDownloadClient