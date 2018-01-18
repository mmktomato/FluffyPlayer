package jp.gr.java_conf.mmktomato.fluffyplayer.db

import android.arch.persistence.room.TypeConverter
import jp.gr.java_conf.mmktomato.fluffyplayer.db.model.PlaylistItem

/**
 * The TypeConverters.
 */
class DbTypeConverters {
    /**
     * Converts `PlaylistItem.Status` to Int.
     */
    @TypeConverter
    fun toInt(status: PlaylistItem.Status): Int {
        return status.value
    }

    /**
     * Converts Int to 'PlaylistItem.Status`.
     */
    @TypeConverter
    fun fromInt(status: Int): PlaylistItem.Status {
        return PlaylistItem.Status.values().filter { it.value == status }.first()
    }
}