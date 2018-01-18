package jp.gr.java_conf.mmktomato.fluffyplayer.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import jp.gr.java_conf.mmktomato.fluffyplayer.db.model.PlaylistItem
import jp.gr.java_conf.mmktomato.fluffyplayer.db.model.PlaylistItemDao

@Database(entities = [PlaylistItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Returns PlaylistDao.
     */
    abstract val playlistDao: PlaylistItemDao
}