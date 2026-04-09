// Sealed result wrapper used by networkBoundResource() and the DB-seed ViewModels.
//
// States and when each is emitted by networkBoundResource():
//   Loading  — emitted immediately with the current DB snapshot while the fetch is in progress.
//              UI should show a progress indicator; [data] may already contain stale content.
//   Success  — emitted after saveToDb() completes and the Room Flow re-emits fresh data.
//   Error    — emitted when fetch() or saveToDb() throws; [error] carries the exception,
//              [data] carries the last DB snapshot so the UI can still display something.
//   Skipped  — emitted when shouldFetch() returns false; no network call was made and
//              [data] contains the existing DB content. The UI should render normally.
//
// Related: NetworkBoundResource.kt (the producer), FAMainViewModel (primary consumer),
//          MainFragment (observes Resource states to drive progressBar visibility).
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