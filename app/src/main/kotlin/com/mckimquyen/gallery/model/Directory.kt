package com.mckimquyen.gallery.model

import android.content.Context
import androidx.annotation.Keep
import androidx.room.*
import com.bumptech.glide.signature.ObjectKey
import org.fossify.commons.extensions.formatDate
import org.fossify.commons.extensions.formatSize
import org.fossify.commons.helpers.*
import com.mckimquyen.gallery.helper.RECYCLE_BIN

@Keep
@Entity(
    tableName = "directories",
    indices = [Index(value = ["path"], unique = true)]
)
data class Directory(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "path") var path: String,
    @ColumnInfo(name = "thumbnail") var tmb: String,
    @ColumnInfo(name = "filename") var name: String,
    @ColumnInfo(name = "media_count") var mediaCnt: Int,
    @ColumnInfo(name = "last_modified") var modified: Long,
    @ColumnInfo(name = "date_taken") var taken: Long,
    @ColumnInfo(name = "size") var size: Long,
    @ColumnInfo(name = "location") var location: Int,
    @ColumnInfo(name = "media_types") var types: Int,
    @ColumnInfo(name = "sort_value") var sortValue: String,

    // used with "Group direct subfolders" enabled
    @Ignore var subfoldersCount: Int = 0,
    @Ignore var subfoldersMediaCount: Int = 0,
    @Ignore var containsMediaFilesDirectly: Boolean = true
) {

    constructor() : this(
        id = null,
        path = "",
        tmb = "",
        name = "",
        mediaCnt = 0,
        modified = 0L,
        taken = 0L,
        size = 0L,
        location = 0,
        types = 0,
        sortValue = "",
        subfoldersCount = 0,
        subfoldersMediaCount = 0
    )

    fun getBubbleText(
        sorting: Int,
        context: Context,
        dateFormat: String? = null,
        timeFormat: String? = null,
    ) = when {
        sorting and SORT_BY_NAME != 0 -> name
        sorting and SORT_BY_PATH != 0 -> path
        sorting and SORT_BY_SIZE != 0 -> size.formatSize()
        sorting and SORT_BY_DATE_MODIFIED != 0 -> modified.formatDate(
            context = context,
            dateFormat = dateFormat,
            timeFormat = timeFormat
        )
        sorting and SORT_BY_RANDOM != 0 -> name
        else -> taken.formatDate(context)
    }

    fun areFavorites() = path == FAVORITES

    fun isRecycleBin() = path == RECYCLE_BIN

    fun getKey() = ObjectKey("$path-$modified")
}
