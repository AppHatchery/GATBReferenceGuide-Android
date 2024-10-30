package org.apphatchery.gatbreferenceguide.retrofit


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


//@HiltViewModel
class WebViewViewModel @Inject constructor(
   private val repository: Repository
) : ViewModel() {

    private val _contents = MutableStateFlow<List<GitHubContent>>(emptyList())
    val contents: StateFlow<List<GitHubContent>> = _contents.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedContent = MutableStateFlow<String?>(null)
    val selectedContent: StateFlow<String?> = _selectedContent.asStateFlow()

    fun fetchContents() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            repository.getContents()
                .onSuccess { content ->
                    _contents.value = content
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }

            _loading.value = false
        }
    }

    fun fetchFileContent(path: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            repository.getFileContent(path)
                .onSuccess { content ->
                    content.content?.let { rawContent ->
                        val decodedContent = if (content.encoding == "base64") {
                            android.util.Base64.decode(rawContent, android.util.Base64.DEFAULT)
                                .toString(Charsets.UTF_8)
                        } else {
                            rawContent
                        }
                        _selectedContent.value = decodedContent
                    } ?: run {
                        _error.value = "No content available"
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }

            _loading.value = false
        }
    }

//    private val _pageContent = MutableLiveData<String?>()
//    val pageContent : LiveData<String?> get() = _pageContent
//
//    fun loadPageContent(url: String){
//        viewModelScope.launch {
//            repository.fetchPageContent(url) { content ->
//                _pageContent.postValue(content)
//            }
//        }
//    }
}