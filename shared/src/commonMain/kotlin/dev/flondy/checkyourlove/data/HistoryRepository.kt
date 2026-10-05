package dev.flondy.checkyourlove.data

import dev.flondy.checkyourlove.core.LoveCalculation
import dev.flondy.checkyourlove.core.LoveCalculator
import dev.flondy.checkyourlove.data.db.LoveResultEntity
import dev.flondy.checkyourlove.data.db.ResultDao
import dev.flondy.checkyourlove.platform.currentTimeMillis
import kotlinx.coroutines.flow.Flow

class HistoryRepository(
    private val dao: ResultDao,
    private val clock: () -> Long = ::currentTimeMillis,
) {
    fun observeAll(): Flow<List<LoveResultEntity>> = dao.observeAll()
    fun observeFavorites(): Flow<List<LoveResultEntity>> = dao.observeFavorites()
    fun observe(id: Long): Flow<LoveResultEntity?> = dao.observeById(id)

    suspend fun save(nameA: String, nameB: String, calculation: LoveCalculation): LoveResultEntity =
        dao.upsertResult(
            LoveResultEntity(
                nameA = LoveCalculator.displayName(nameA),
                nameB = LoveCalculator.displayName(nameB),
                pairKey = calculation.pairKey,
                percent = calculation.percent,
                variant = calculation.variant,
                algorithmVersion = LoveCalculator.ALGORITHM_VERSION,
                createdAt = clock(),
            )
        )

    suspend fun setFavorite(id: Long, favorite: Boolean) = dao.setFavorite(id, favorite)
    suspend fun delete(id: Long) = dao.delete(id)
    suspend fun restore(entity: LoveResultEntity) { dao.restore(entity) }
    suspend fun clear(keepFavorites: Boolean) = if (keepFavorites) dao.deleteNonFavorites() else dao.deleteAll()
}
