package org.apphatchery.gatbreferenceguide.retrofit

data class GitHubContent(
    val name: String,
    val path: String,
    val sha: String,
    val size: Int,
    val url: String,
    val html_url: String,
    val git_url: String,
    val download_url: String,
    val content: String?,
    val encoding: String?

)
