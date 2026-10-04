package dev.flondy.checkyourlove.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ResultDao {
    @Query("SELECT * FROM love_results ORDER BY created_at DESC, id DESC")
    fun observeAll(): Flow<List<LoveResultEntity>>

    @Query("SELECT * FROM love_results WHERE favorite = 1 ORDER BY created_at DESC, id DESC")
    fun observeFavorites(): Flow<List<LoveResultEntity>>

    @Query("SELECT * FROM love_results WHERE id = :id")
    fun observeById(id: Long): Flow<LoveResultEntity?>

    @Query("SELECT * FROM love_results WHERE pair_key = :pairKey LIMIT 1")
    suspend fun findByPairKey(pairKey: String): LoveResultEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: LoveResultEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restore(entity: LoveResultEntity): Long

    @Query("UPDATE love_results SET name_a = :nameA, name_b = :nameB, created_at = :createdAt WHERE id = :id")
    suspend fun touch(id: Long, nameA: String, nameB: String, createdAt: Long)

    @Query("UPDATE love_results SET favorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("DELETE FROM love_results WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM love_results")
    suspend fun deleteAll()

    @Query("DELETE FROM love_results WHERE favorite = 0")
    suspend fun deleteNonFavorites()

    /** Speichert ein Ergebnis; ein bereits vorhandenes Paar wird nach oben geholt (Favorit bleibt). */
    @Transaction
    suspend fun upsertResult(entity: LoveResultEntity): LoveResultEntity {
        val existing = findByPairKey(entity.pairKey)
        return if (existing != null) {
            touch(existing.id, entity.nameA, entity.nameB, entity.createdAt)
            existing.copy(nameA = entity.nameA, nameB = entity.nameB, createdAt = entity.createdAt)
        } else {
            entity.copy(id = insert(entity))
        }
    }

    @Query("SELECT COUNT(*) FROM legacy_name_values")
    suspend fun legacyCount(): Int
}
