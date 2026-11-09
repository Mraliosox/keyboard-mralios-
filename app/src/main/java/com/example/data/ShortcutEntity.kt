package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "shortcuts")
data class ShortcutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "trigger_text") val triggerText: String,
    @ColumnInfo(name = "expanded_text") val expandedText: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcuts ORDER BY trigger_text ASC")
    fun getAllFlow(): Flow<List<ShortcutEntity>>

    @Query("SELECT * FROM shortcuts")
    suspend fun getAllList(): List<ShortcutEntity>

    @Query("SELECT * FROM shortcuts WHERE trigger_text = :trigger LIMIT 1")
    suspend fun findByTrigger(trigger: String): ShortcutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shortcut: ShortcutEntity): Long

    @Update
    suspend fun update(shortcut: ShortcutEntity)

    @Delete
    suspend fun delete(shortcut: ShortcutEntity)
}
