package org.apphatchery.gatbreferenceguide.resource

sealed class Resource<T>(
    val data: T? = null,
    val error: Exception? = null
) {
    class Error<T>(error: Exception, data: T? = null) : Resource<T>(data, error)
    class Success<T>(data: T) : Resource<T>(data)
    class Loading<T>(data: T? = null) : Resource<T>(data)

    class Skipped<T>(data: T? = null) : Resource<T>(data)
}