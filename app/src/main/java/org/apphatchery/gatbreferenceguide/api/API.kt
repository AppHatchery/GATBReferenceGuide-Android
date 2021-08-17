package org.apphatchery.gatbreferenceguide.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface API {

    @GET
    suspend fun downloadFile(@Url url: String): Response<ResponseBody>


}