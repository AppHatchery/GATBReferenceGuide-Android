package org.apphatchery.gatbreferenceguide.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.apphatchery.gatbreferenceguide.retrofit.GitHubService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

//
//@Module
//@InstallIn(SingletonComponent::class)
//object NetworkModule {
//    private const val BASE_URL = "https://api.github.com/"
//
//    @Singleton
//    @Provides
//    fun providesRetrofit() : Retrofit{
//        return Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(provideOkHttpClient())
//            .build()
//
//    }
//
//    @Singleton
//    @Provides fun provideOkHttpClient(): OkHttpClient{
//        val loggingInterceptor = HttpLoggingInterceptor().apply {
//            setLevel(HttpLoggingInterceptor.Level.BODY)
//        }
//        return OkHttpClient.Builder()
//            .addInterceptor(loggingInterceptor)
//            .build()
//    }
//
//
//    @Singleton
//    @Provides
//    fun provideGitHubApiService(retrofit: Retrofit): GitHubService {
//        return retrofit.create(GitHubService::class.java)
//    }
//}