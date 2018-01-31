package jp.gr.java_conf.mmktomato.fluffyplayer.dropbox

import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.Metadata
import java.io.Serializable

/**
 * A data class of Dropbox's Metadata. This class is serializable.
 *
 * @param isFile if true, this metadata is a file. Not, a directory.
 * @param name the file name.
 * @param path the file path.
 */
data class DbxNodeMetadata(val isFile: Boolean,
                           val name: String,
                           val path: String) : Serializable {

    companion object {
        /**
         * Returns instance of this class from raw Metadata.
         */
        internal fun createFrom(raw: Metadata): DbxNodeMetadata {
            return DbxNodeMetadata((raw is FileMetadata), raw.name, raw.pathLower)
        }

        /**
         * Returns root folder instance.
         */
        internal val root: DbxNodeMetadata
                get() = DbxNodeMetadata(false, "/", "")
    }
}