package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "learned_words")
data class LearnedWordEntity(
    @PrimaryKey val word: String,
    @ColumnInfo(name = "frequency") val frequency: Int = 1,
    @ColumnInfo(name = "locale") val locale: String = "ar",
    @ColumnInfo(name = "last_used") val lastUsed: Long = System.currentTimeMillis()
)

@Dao
interface LearnedWordDao {
    @Query("SELECT * FROM learned_words WHERE locale = :locale ORDER BY frequency DESC LIMIT 500")
    suspend fun getTopWords(locale: String): List<LearnedWordEntity>

    @Query("SELECT * FROM learned_words WHERE word = :word LIMIT 1")
    suspend fun findWord(word: String): LearnedWordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(wordEntity: LearnedWordEntity)

    @Query("DELETE FROM learned_words")
    suspend fun clearAll()
}
