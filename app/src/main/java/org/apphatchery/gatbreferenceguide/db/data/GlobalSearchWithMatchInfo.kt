// Query result wrapper pairing a GlobalSearchEntity FTS hit with its raw SQLite matchinfo() blob.
// Returned by GlobalSearchDao when the caller needs to rank or highlight search results beyond
// simple relevance ordering — the matchinfo ByteArray encodes per-column hit counts and phrase
// statistics as defined by the SQLite FTS matchinfo() function.
//
// Why ByteArray instead of a primitive: SQLite's matchinfo() returns a BLOB of 32-bit integers
// packed as little-endian bytes. The ViewModel unpacks this blob to compute a relevance score or
// to determine which fields (title vs body) matched the query, driving result ranking.
//
// equals() and hashCode() are manually overridden because Kotlin's data class auto-generation
// uses referential equality for ByteArray, which would break list diffing in DiffUtil. The
// overrides use contentEquals() / contentHashCode() for correct structural comparison.
//
// Related: GlobalSearchEntity, GlobalSearchDao, FAGlobalSearchViewModel, GlobalSearchFragment.
package org.apphatchery.gatbreferenceguide.db.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import org.apphatchery.gatbreferenceguide.db.entities.GlobalSearchEntity

data class GlobalSearchWithMatchInfo(
    @Embedded
    val globalSearchEntity: GlobalSearchEntity,
    @ColumnInfo(name = "matchInfo")
    val matchInfo: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GlobalSearchWithMatchInfo

        if (globalSearchEntity != other.globalSearchEntity) return false
        if (!matchInfo.contentEquals(other.matchInfo)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = globalSearchEntity.hashCode()
        result = 31 * result + matchInfo.contentHashCode()
        return result
    }
}
