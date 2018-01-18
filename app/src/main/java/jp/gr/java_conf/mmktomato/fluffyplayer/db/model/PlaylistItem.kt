package jp.gr.java_conf.mmktomato.fluffyplayer.db.model

import android.arch.persistence.room.*
import jp.gr.java_conf.mmktomato.fluffyplayer.db.DbTypeConverters

/**
 * A playlist item.
 *
 * @param path the Dropbox's path.
 * @param status the playing status.
 */
@Entity(tableName = "playlist")
@TypeConverters(DbTypeConverters::class)
class PlaylistItem(
        @ColumnInfo(name = "path")
        var path: String,

        @ColumnInfo(name = "status")
        var status: Status) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id = 0

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
        PLAYING(1),

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
    fun getFirst(): PlaylistItem

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
