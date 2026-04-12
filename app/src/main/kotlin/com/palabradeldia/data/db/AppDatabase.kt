package com.palabradeldia.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favourites")
data class FavouriteEntity(
    @PrimaryKey val wordId: Int,
    val word: String,
    val pos: String,
    val gender: String?,
    val etymology: String?,
    // Full definition list serialised as JSON to keep the schema flat.
    val definitionsJson: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Dao
interface FavouritesDao {

    @Query("SELECT * FROM favourites ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavouriteEntity>>

    @Query("SELECT * FROM favourites ORDER BY addedAt DESC")
    suspend fun getAll(): List<FavouriteEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: FavouriteEntity)

    @Query("DELETE FROM favourites WHERE wordId = :wordId")
    suspend fun deleteById(wordId: Int)

    @Query("SELECT COUNT(*) FROM favourites WHERE wordId = :wordId")
    suspend fun countById(wordId: Int): Int
}

@Database(
    entities = [FavouriteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favouritesDao(): FavouritesDao
}
