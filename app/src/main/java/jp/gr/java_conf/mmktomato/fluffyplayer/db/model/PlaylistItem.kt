package jp.gr.java_conf.mmktomato.fluffyplayer.db.model

import android.arch.persistence.room.*
import jp.gr.java_conf.mmktomato.fluffyplayer.db.DbTypeConverters
import java.io.Serializable

/**
 * A playlist item.
 *
 * @param id the primary key.
 * @param order the order of this item.
 * @param path the Dropbox's path.
 * @param status the playing status.
 */
@Entity(tableName = "playlist")
@TypeConverters(DbTypeConverters::class)
class PlaylistItem(
        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: String,

        @ColumnInfo(name = "order")
        var order: Int,

        @ColumnInfo(name = "path")
        var path: String,

        @ColumnInfo(name = "status")
        var status: Status) : Serializable {

    companion object {
        /**
         * the waiting status value.
         */
        const val STATUS_VALUE_WAIT = 0

        /**
         * the now playing status value.
         */
        const val STATUS_VALUE_PLAYING = 1
    }

    /**
     * A playlist item status.
     *
     * @param value the status value.
     */
    enum class Status(val value: Int) {
        /**
         * Not played.
         */
        WAIT(STATUS_VALUE_WAIT),

        /**
         * Now playing.
         */
        PLAYING(STATUS_VALUE_PLAYING),

        /**
         * Already played.
         */
        PLAYED(2)
    }
}

/**
 * A data access object of PlaylistItem.
 */
@Dao
interface PlaylistItemDao {
    /**
     * Returns next waiting item of playlist.
     */
    @Query("select * from playlist where status = ${PlaylistItem.STATUS_VALUE_WAIT} order by [order] limit 1")
    fun getNext(): PlaylistItem?

    /**
     * Returns now playing item of playlist.
     */
    @Query("select * from playlist where status = ${PlaylistItem.STATUS_VALUE_PLAYING}")
    fun getNowPlaying(): PlaylistItem?

    /**
     * Inserts a PlaylistItem.
     *
     * @param item the inserted item.
     */
    @Insert
    fun insert(item: PlaylistItem)

    /**
     * Updates a PlaylistItem.
     *
     * @param item the updated item.
     */
    @Update
    fun update(item: PlaylistItem)

    /**
     * Deletes all items.
     */
    //@Delete
    @Query("delete from playlist")
    fun deleteAll()
}
