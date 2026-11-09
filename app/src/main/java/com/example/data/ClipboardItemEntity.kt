package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "clipboard_items")
data class ClipboardItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "copied_at") val copiedAt: Long = System.currentTimeMillis()
)

@Dao
interface ClipboardDao {
    @Query("SELECT * FROM clipboard_items ORDER BY copied_at DESC LIMIT 10")
    fun getRecentItemsFlow(): Flow<List<ClipboardItemEntity>>

    @Query("SELECT * FROM clipboard_items ORDER BY copied_at DESC LIMIT 10")
    suspend fun getRecentItems(): List<ClipboardItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ClipboardItemEntity): Long

    @Query("DELETE FROM clipboard_items WHERE id NOT IN (SELECT id FROM clipboard_items ORDER BY copied_at DESC LIMIT 10)")
    suspend fun trimToTen()

    @Query("DELETE FROM clipboard_items WHERE text = :text")
    suspend fun deleteByText(text: String)

    @Query("DELETE FROM clipboard_items")
    suspend fun clearAll()
}
