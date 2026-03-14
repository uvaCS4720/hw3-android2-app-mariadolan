package edu.nd.pmcburne.hwapp.one

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Upsert
    suspend fun upsertGames(games: List<GameObject>)

    @Query("SELECT * FROM games WHERE date = :date AND mens = :mens")
    fun getGamesByDateAndGender(date: String, mens: Boolean): Flow<List<GameObject>>
}
