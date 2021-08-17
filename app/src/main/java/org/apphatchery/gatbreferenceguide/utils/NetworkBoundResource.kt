package org.apphatchery.gatbreferenceguide.utils

import kotlinx.coroutines.flow.*
import org.apphatchery.gatbreferenceguide.resource.Resource


fun <ResultType, RequestType> networkBoundResource(
    query: () -> Flow<RequestType>,
    fetch: suspend () -> List<ResultType>,
    saveToDb: suspend (List<ResultType>) -> Unit,
    shouldFetch: (RequestType) -> Boolean = { true }
) = flow {
    val data = query().first()
    val flow = try {
        if (shouldFetch(data)) {
            emit(Resource.Loading(data))
            saveToDb(fetch())
            query().map { Resource.Success(it) }
        } else {
            query().map { Resource.Success(it) }
        }
    } catch (e: Exception) {
        query().map { Resource.Error(e, it) }
    }
    emitAll(flow)
}