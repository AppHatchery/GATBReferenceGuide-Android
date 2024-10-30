package org.apphatchery.gatbreferenceguide.retrofit

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Inject
import javax.inject.Singleton


class Repository(
   private val apiService: GitHubService
){
    suspend fun getContents(): Result<List<GitHubContent>> = try{
        Result.success(apiService.getRepositoryContents())
    }catch (e: Exception){
        Result.failure(e)
    }
    suspend fun getFileContent(path: String): Result<GitHubContent> = try {
        Result.success(apiService.getFileContents(path))
    }catch (e: Exception){
        Result.failure(e)
    }

//val retrofit = Retrofit.Builder()
//    .baseUrl("https://api.github.com/repos/apphatchery/GA-TB-Reference-Guide-Web/contents/pages")
//    .addConverterFactory(ScalarsConverterFactory.create())
//    .build()
//
//val service = retrofit.create(GitHubService::class.java)
//
//    fun fetchPageContent(url: String, callback: (String?) -> Unit){
//        val call = service.getPageContent(url)
//        call.enqueue(object : retrofit2.Callback<String>{
//            override fun onResponse(call: Call<String>, response: retrofit2.Response<String>){
//                if(response.isSuccessful){
//                    callback(response.body())
//                } else{
//                    callback(null)
//                }
//            }
//
//            override fun onFailure(call: Call<String>, t: Throwable){
//                t.printStackTrace()
//                callback(null)
//            }
//        })
//    }
}