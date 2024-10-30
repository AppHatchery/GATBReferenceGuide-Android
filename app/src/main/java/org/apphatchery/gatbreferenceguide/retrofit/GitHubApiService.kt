package org.apphatchery.gatbreferenceguide.retrofit

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface GitHubApiService {
    @GET("repos/apphatchery/GA-TB-Reference-Guide-Web/contents/pages")
    fun getPages(): Call<List<GitHubPage>>

    @GET
    fun getContent(@Url url: String): Call<ResponseBody>
}
