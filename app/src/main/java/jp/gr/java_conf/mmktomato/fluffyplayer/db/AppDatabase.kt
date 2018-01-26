package jp.gr.java_conf.mmktomato.fluffyplayer.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import jp.gr.java_conf.mmktomato.fluffyplayer.db.model.PlaylistItem
import jp.gr.java_conf.mmktomato.fluffyplayer.db.model.PlaylistItemDao

@Database(entities = [PlaylistItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Returns PlaylistDao.
     */
    abstract val playlistDao: PlaylistItemDao

    //companion object Factory {  This causes `java.lang.NoSuchFieldError` because `AppDatabase` class is abstract.
    object Factory {
        private lateinit var instance: AppDatabase

        /**
         * Factory of AppDatabase.
         */
        fun create(ctx: Context): AppDatabase {
            if (!::instance.isInitialized) {
                instance = Room.databaseBuilder(ctx, AppDatabase::class.java, "fluffy.db").build()
            }
            return instance
        }
    }
}
