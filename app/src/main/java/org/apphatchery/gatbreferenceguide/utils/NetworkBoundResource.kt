// Single-source-of-truth pattern: reads from the local Room DB first, optionally fetches from
// a remote source, saves the result, then re-reads from the DB so observers always get DB data.
//
// Flow of emissions for a successful fetch:
//   1. Resource.Loading  — emitted immediately with the current DB snapshot while fetching.
//   2. fetch() runs      — returns the remote list and saves it via saveToDb().
//   3. Resource.Success  — ongoing DB flow continues emitting updated data.
// If shouldFetch() returns false: Resource.Skipped is emitted and no network call is made.
// On exception: Resource.Error wraps the exception alongside the last known DB data.
//
// In this app the "network" is the bundled JSON assets — fetch() reads from assets and
// saveToDb() inserts into Room, so this pattern drives the DB seed flow in FAMainViewModel.
//
// Related: Resource.kt (the sealed result wrapper), FAMainViewModel (primary caller),
//          MainFragment (observes the resulting Resource flow to drive progress UI).
package org.apphatchery.gatbreferenceguide.utils

import kotlinx.coroutines.flow.*
import org.apphatchery.gatbreferenceguide.resource.Resource


/**
 * Builds a [Resource]-wrapped Flow using the DB-first pattern.
 * @param query     returns the Room Flow that backs the UI; always re-queried after saving.
 * @param fetch     suspending call that returns the fresh data list (remote or bundled assets).
 * @param saveToDb  persists the fetched list; Room then pushes an update through [query].
 * @param shouldFetch predicate on the current DB snapshot; return false to skip the fetch.
 */
fun <ResultType, RequestType> networkBoundResource(
    query: () -> Flow<RequestType>,
    fetch: suspend () -> List<ResultType>,
    saveToDb: suspend (List<ResultType>) -> Unit,
    shouldFetch: (RequestType) -> Boolean = { true }
) = flow {
    val data = query().first()

    val flow: Flow<Resource<RequestType>> = try {
        if (shouldFetch(data)) {
            emit(Resource.Loading(data))
            saveToDb(fetch())
            query().map { Resource.Success(it) as Resource<RequestType> }
        } else {
            query().map { Resource.Skipped(it) as Resource<RequestType> }
        }
    } catch (e: Exception) {
        query().map { Resource.Error(e, it) as Resource<RequestType> }
    }

    emitAll(flow)
}
