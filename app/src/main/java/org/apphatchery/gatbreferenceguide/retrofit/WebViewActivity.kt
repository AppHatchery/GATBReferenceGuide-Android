package org.apphatchery.gatbreferenceguide.retrofit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.ActivityWebViewBinding
import org.apphatchery.gatbreferenceguide.databinding.FragmentWebViewBinding
import org.apphatchery.gatbreferenceguide.db.Database
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject


class WebViewFragment : BaseFragment(R.layout.activity_web_view){

    private lateinit var retrofit: Retrofit
    private lateinit var apiService: GitHubApiService
    private lateinit var fragmentWebViewBinding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupRetrofit()
    }

    private fun setupRetrofit() {
        retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(GitHubApiService::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentWebViewBinding = ActivityWebViewBinding.bind(view)
        fragmentWebViewBinding.apply {
            webView.settings.javaScriptEnabled = true
            webView.webViewClient = object : WebViewClient(){
                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    Log.d("WebView", "Page finished loading")
                }
            }
        }
        //super.onViewCreated(view, savedInstanceState)
        fetchData()
    }

    private fun fetchData() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getPages().execute()
                if (response.isSuccessful) {
                    val pages = response.body()
                    val targetPage = pages?.find { it.name == "10_tb_infection_control_hospital_isolation_procedures__a__administrative_controls.html" }
                    if (targetPage != null){
                        Log.d("TargetPage", "fetchData: ${targetPage.downloadUrl}")
                        fetchAndDisplayContent(targetPage.downloadUrl)
                    } else{
                        Log.d("TargetPage", "fetchData: not found")
                    }
                    Log.d("targetpage", "fetchData: ${targetPage?.downloadUrl}")
                    Log.d("GitHubPages", "Fetched ${pages?.size} pages")
                    displayPages(pages)
                } else {
                    Log.e("GitHubPages", "Failed to fetch pages")
                }
            } catch (e: Exception) {
                Log.e("GitHubPages", "Error fetching pages", e)
            }
        }
    }

        private suspend fun fetchAndDisplayContent(url: String) {
        try {
            val response = apiService.getContent(url).execute()
            if (response.isSuccessful) {
                val content = response.body()?.string()
                withContext(Dispatchers.Main) {
                    if (content != null) {
                        fragmentWebViewBinding.webView.loadData(content, "text/html; charset=utf-8", "UTF-8")
                    }
                }
            } else {
                Log.e("GitHubContent", "Failed to fetch content")
            }
        } catch (e: Exception) {
            Log.e("GitHubContent", "Error fetching content", e)
        }
    }

    private suspend fun displayPages(pages: List<GitHubPage>?) {
        withContext(Dispatchers.Main) {
            // You can replace this with your actual UI logic
            pages?.forEach { page ->
                Log.d("GitHubPage", "Name: ${page.name}, Path: ${page.path}")
            }
        }
    }

//    private lateinit var webView: WebView
//    private lateinit var retrofit: Retrofit
//    private lateinit var apiService: GitHubApiService
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setupRetrofit()
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return inflater.inflate(R.layout.fragment_main, container, false)
//    }
//
//    private fun setupRetrofit() {
//        retrofit = Retrofit.Builder()
//            .baseUrl("https://api.github.com/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        apiService = retrofit.create(GitHubApiService::class.java)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        //super.onViewCreated(view, savedInstanceState)
//        webView = view.findViewById(R.id.web_view)
//        //setupWebView()
////        setupRetrofit()
//         fetchData()
//
//    }
//
//
//
//
//
//    private fun setupWebView() {
//        webView.settings.javaScriptEnabled = true
//        webView.webViewClient = object : WebViewClient() {
//            override fun onPageFinished(view: WebView, url: String) {
//                super.onPageFinished(view, url)
//                Log.d("WebView", "Page finished loading")
//            }
//        }
//    }
//
//
//    private fun fetchData() {
//        GlobalScope.launch(Dispatchers.IO) {
//            try {
//                val response = apiService.getPages().execute()
//                if (response.isSuccessful) {
//                    val pages = response.body()
//                    //val targetPage = pages?.find { it.name == "10_tb_infection_control_hospital_isolation_procedures.html" }
//
//                    //Log.d(TAG, "fetchData: ")
//                    Log.d("GitHubPages", "Fetched ${pages?.size} pages")
//                    displayPages(pages)
//                } else {
//                    Log.e("GitHubPages", "Failed to fetch pages")
//                }
//            } catch (e: Exception) {
//                Log.e("GitHubPages", "Error fetching pages", e)
//            }
//        }
//    }
//
//    //                    if (targetPage != null) {
////                        Log.d("target Page in tb", "fetchData: $targetPage")
////                        //fetchAndDisplayContent(targetPage.toString())
////                    } else {
////                        Log.w("GitHubPages", "Target page not found")
////                    }
//
////    private suspend fun fetchAndDisplayContent(url: String) {
////        try {
////            val response = apiService.getContent(url).execute()
////            if (response.isSuccessful) {
////                val content = response.body()?.string()
////                withContext(Dispatchers.Main) {
////                    if (content != null) {
////                        webView.loadData(content, "text/html; charset=utf-8", "UTF-8")
////                    }
////                }
////            } else {
////                Log.e("GitHubContent", "Failed to fetch content")
////            }
////        } catch (e: Exception) {
////            Log.e("GitHubContent", "Error fetching content", e)
////        }
////    }
//
//    private suspend fun displayPages(pages: List<GitHubPage>?) {
//        withContext(Dispatchers.Main) {
//            // You can replace this with your actual UI logic
//            pages?.forEach { page ->
//                Log.d("GitHubPage", "Name: ${page.name}, Path: ${page.path}")
//            }
//        }
//    }
//    private lateinit var binding: ActivityWebViewBinding
//    private lateinit var viewModel: WebViewViewModel
//    private lateinit var repository: Repository
//
//    @Inject
//    lateinit var apiService: GitHubService
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Initialize the repository
//        repository = Repository(apiService)
//
//        // Create the ViewModel manually
//        viewModel = ViewModelProvider(this, WebViewViewModelFactory(repository)).get(WebViewViewModel::class.java)
//    }
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        binding = ActivityWebViewBinding.bind(view)
//        setupWebView()
//        setupObservers()
//        // Fetch specific page content
//        viewModel.fetchFileContent("pages/10_tb_infection_control_hospital_isolation_procedures.html")
//    }
//
//    private fun setupWebView() {
//        binding.webView.settings.apply {
//            javaScriptEnabled = true
//            domStorageEnabled = true
//            loadsImagesAutomatically = true
//            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
//        }
//    }
//
//    private fun setupObservers() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewModel.loading.collect { isLoading ->
//                binding.progressBar.isVisible = isLoading
//            }
//        }
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewModel.error.collect { error ->
//                error?.let {
//                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewModel.selectedContent.collect { content ->
//                content?.let {
//                    // Load the HTML content into WebView
//                    binding.webView.loadDataWithBaseURL(
//                        "https://raw.githubusercontent.com/apphatchery/GA-TB-Reference-Guide-Web/main/",
//                        it,
//                        "text/html",
//                        "UTF-8",
//                        null
//                    )
//                }
//            }
//        }
//    }
//
//    //    override fun onCreateView(
////        inflater: LayoutInflater,
////        container: ViewGroup?,
////        savedInstanceState: Bundle?
////    ): View {
////        _binding = FragmentWebViewBinding.inflate(inflater, container, false)
////        return binding.root
////    }
//
//
////    override fun onDestroyView() {
////        super.onDestroyView()
////        _binding = null
////    }
//
//    companion object {
//        fun newInstance() = WebViewFragment()
//    }
}