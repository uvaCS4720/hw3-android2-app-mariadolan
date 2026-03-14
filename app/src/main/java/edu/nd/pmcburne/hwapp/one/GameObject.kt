package edu.nd.pmcburne.hwapp.one

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName ="games")
data class GameObject(
    @PrimaryKey
    val id: String,
    val finished: Boolean,
    val upcoming: Boolean,
    val current: Boolean,
    val date: String,
    val period: String?,
    val timeLeft: String?,
    val winner: String?,
    val startTime: String ,
    val awayTeam: String,
    val homeTeam: String,
    val awayScore: String?,
    val homeScore: String?,
    val mens: Boolean
)