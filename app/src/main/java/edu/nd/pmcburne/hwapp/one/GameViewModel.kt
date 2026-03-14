package edu.nd.pmcburne.hwapp.one

import android.app.Application
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val gameDao = GameDatabase.getInstance(application).gameDao()

    var selectedDate by mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")))
    var isMens by mutableStateOf(true)
    var isLoading by mutableStateOf(false)

    private val _games = MutableStateFlow<List<GameObject>>(emptyList())
    val games: StateFlow<List<GameObject>> = _games

    fun onRefresh() {
        fetchGames()
    }

    fun fetchGames() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { isLoading = true }
            try {
                val client = OkHttpClient()
                val builtUrl = url()
                Log.d("GameViewModel", "Fetching URL: $builtUrl")

                val request = Request.Builder()
                    .url(builtUrl)
                    .build()

                val response = client.newCall(request).execute()
                Log.d("GameViewModel", "Response code: ${response.code}")
                Log.d("GameViewModel", "Response successful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val jsonString = response.body?.string()
                    Log.d("GameViewModel", "JSON received, length: ${jsonString?.length}")

                    if (jsonString != null) {
                        val gamesList = parseGames(jsonString)
                        Log.d("GameViewModel", "Parsed ${gamesList.size} games")

                        gameDao.upsertGames(gamesList)
                        withContext(Dispatchers.Main) { _games.value = gamesList }
                    }
                } else {
                    Log.e("GameViewModel", "Response not successful: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("GameViewModel", "Exception: ${e.message}")
                Log.e("GameViewModel", "Stack trace: ${e.stackTraceToString()}")
                try {
                    val cachedGames = gameDao.getGamesByDateAndGender(selectedDate, isMens).first()
                    withContext(Dispatchers.Main) { _games.value = cachedGames }
                } catch (roomEx: Exception) {
                    Log.e("GameViewModel", "Room fallback also failed: ${roomEx.message}")
                }
            } finally {
                withContext(Dispatchers.Main) { isLoading = false }
            }
        }
    }

    private fun parseGames(jsonString: String): List<GameObject> {
        val gamesList = mutableListOf<GameObject>()
        val root = JSONObject(jsonString)

        Log.d("GameViewModel", "Root keys: ${root.keys().asSequence().toList()}")

        val gamesArray = root.getJSONArray("games")
        Log.d("GameViewModel", "Games array length: ${gamesArray.length()}")

        for (i in 0 until gamesArray.length()) {
            try {
                val gameWrapper = gamesArray.getJSONObject(i)
                val game = gameWrapper.getJSONObject("game")

                Log.d("GameViewModel", "Game $i keys: ${game.keys().asSequence().toList()}")

                val id = game.getString("gameID")
                val away = game.getJSONObject("away")
                val home = game.getJSONObject("home")
                val awayscore = away.optString("score", null)
                val homescore = home.optString("score", null)
                val awayteam = away.getJSONObject("names").getString("short")
                val hometeam = home.getJSONObject("names").getString("short")
                val awaywin = away.optBoolean("winner", false)
                val homewin = home.optBoolean("winner", false)
                val state = game.getString("gameState")
                val startTime = game.optString("startTime", null)
                val timeleft = game.optString("contestClock", null)
                val period = game.optString("currentPeriod", null)

                Log.d("GameViewModel", "Game $i - id: $id, state: $state, home: $hometeam, away: $awayteam")

                if (state == "pre") {
                    gamesList.add(GameObject(
                        id = id,
                        finished = false,
                        current = false,
                        upcoming = true,
                        period = null,
                        timeLeft = null,
                        winner = null,
                        startTime = startTime,
                        awayTeam = awayteam,
                        homeTeam = hometeam,
                        awayScore = null,
                        homeScore = null,
                        mens = isMens,
                        date = selectedDate
                    ))
                }

                if (state == "live") {
                    gamesList.add(GameObject(
                        id = id,
                        finished = false,
                        current = true,
                        upcoming = false,
                        period = period,
                        timeLeft = timeleft,
                        winner = null,
                        startTime = startTime,
                        awayTeam = awayteam,
                        homeTeam = hometeam,
                        awayScore = awayscore,
                        homeScore = homescore,
                        mens = isMens,
                        date = selectedDate
                    ))
                }

                if (state == "final") {
                    val winningTeam = if (awaywin) awayteam else hometeam
                    gamesList.add(GameObject(
                        id = id,
                        finished = true,
                        current = false,
                        upcoming = false,
                        period = null,
                        timeLeft = null,
                        winner = winningTeam,
                        startTime = startTime,
                        awayTeam = awayteam,
                        homeTeam = hometeam,
                        awayScore = awayscore,
                        homeScore = homescore,
                        mens = isMens,
                        date = selectedDate
                    ))
                }

            } catch (e: Exception) {
                Log.e("GameViewModel", "Failed to parse game $i: ${e.message}")
                continue
            }
        }

        return gamesList
    }

    fun url(): String {
        val gender = if (isMens) "men" else "women"
        return "https://ncaa-api.henrygd.me/scoreboard/basketball-$gender/d1/$selectedDate"
    }

    internal fun formatDate(dateMillis: Long) {
        selectedDate = Instant.ofEpochMilli(dateMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        fetchGames()
    }
    fun onGenderToggled(mens: Boolean) {
        isMens = mens
        fetchGames()
    }
}