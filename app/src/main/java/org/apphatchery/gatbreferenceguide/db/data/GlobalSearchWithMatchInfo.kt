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
