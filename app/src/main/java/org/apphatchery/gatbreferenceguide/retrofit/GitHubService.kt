package org.apphatchery.gatbreferenceguide.retrofit


import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url


interface GitHubService {
    @GET("repos/apphatchery/GA-TB-Reference-Guide-Web/contents/pages")
    suspend fun getRepositoryContents() : List<GitHubContent>

    @GET("repos/apphatchery/GA-TB-Reference-Guide-Web/contents/pages/{path}")
    suspend fun getFileContents(@Path("path") path: String) : GitHubContent
}

//    @GET
//    fun getPageContent(@Url url: String): Call<String>


//val call = service.getPageContent("https://api.github.com/repos/apphatchery/GA-TB-Reference-Guide-Web/contents/pages/10_tb_infection_control_hospital_isolation_procedures.html")
