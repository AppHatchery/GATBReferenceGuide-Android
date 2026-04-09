// Minimal projection returned by RecentDao when querying the distinct search terms the user has
// typed into the global search bar. Unlike RecentEntity (which tracks visited guide pages),
// RecentSearchItem captures the text strings from the search history so the search fragment can
// display autocomplete suggestions or a "recent searches" list below the search field.
//
// Only the searchText string is needed for display and re-submission; no timestamp or ID is
// required because duplicates are eliminated at the query level (DISTINCT) and the list is
// ordered by recency in the DAO query.
//
// Related: RecentEntity, RecentDao, GlobalSearchFragment, FAGlobalSearchViewModel.
package org.apphatchery.gatbreferenceguide.db.data

data class RecentSearchItem(
    val searchText : String
)
