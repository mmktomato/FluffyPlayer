package jp.gr.java_conf.mmktomato.fluffyplayer.db.model

import android.arch.persistence.room.*
import jp.gr.java_conf.mmktomato.fluffyplayer.db.DbTypeConverters
import java.io.Serializable

/**
 * A playlist item.
 *
 * @param path the Dropbox's path.
 * @param status the playing status.
 */
@Entity(tableName = "playlist")
@TypeConverters(DbTypeConverters::class)
class PlaylistItem(
        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: String,

        @ColumnInfo(name = "path")
        var path: String,

        @ColumnInfo(name = "status")
        var status: Status) : Serializable {

    companion object {
        /**
         * the now playing status value.
         */
        const val PLAYING_STATUS_VALUE = 1
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
        WAIT(0),

        /**
         * Now playing.
         */
        PLAYING(PLAYING_STATUS_VALUE),

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
     * Returns first item of playlist.
     */
    @Query("select * from playlist order by id limit 1")
    fun getFirst(): PlaylistItem?

    /**
     * Returns now playing item of playlist.
     */
    @Query("select * from playlist where status = ${PlaylistItem.PLAYING_STATUS_VALUE}")
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
