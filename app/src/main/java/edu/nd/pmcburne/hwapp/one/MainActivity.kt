package edu.nd.pmcburne.hwapp.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.Button
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import java.time.ZoneOffset
import androidx.compose.material3.Switch
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val viewModel: GameViewModel by viewModels()
        super.onCreate(savedInstanceState)
        setContent {
            ScoreboardScreen(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreboardScreen(viewModel: GameViewModel) {
    val games by viewModel.games.collectAsState()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = LocalDate.now()
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    )
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "NCAA Basketball Game Scores",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // mens/womens toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Women's",
                    fontSize = 13.sp,
                    fontWeight = if (!viewModel.isMens) FontWeight.Bold else FontWeight.Normal
                )
                Switch(
                    checked = viewModel.isMens,
                    onCheckedChange = { viewModel.onGenderToggled(it) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(
                    text = "Men's",
                    fontSize = 13.sp,
                    fontWeight = if (viewModel.isMens) FontWeight.Bold else FontWeight.Normal
                )
            }

            // date display with calendar icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = viewModel.selectedDate,
                    fontSize = 13.sp
                )
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Pick Date"
                    )
                }
            }
        }

        // load button row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { viewModel.onRefresh() },
                enabled = !viewModel.isLoading,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Load/Reload")
            }
        }

        // date picker dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.formatDate(it)
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // content area
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (games.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Select a date and press Load/Reload",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(games) { game ->
                        GameCard(game = game)
                    }
                }
            }
        }
    }
}
@Composable
fun GameCard(game: GameObject) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // game title
            Text(
                text = "${game.homeTeam} vs ${game.awayTeam}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // upcoming
            if (game.upcoming) {
                Text(
                    text = "Start Time: ${game.startTime}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // current
            if (game.current) {
                Spacer(modifier = Modifier.height(4.dp))

                // home team row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = game.homeTeam,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = game.homeScore ?: "-",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // time left
                Text(
                    text = getTimeLeftText(game),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // away team row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = game.awayTeam,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = game.awayScore ?: "-",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // finished
            if (game.finished) {
                Spacer(modifier = Modifier.height(4.dp))

                // home team row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = game.homeTeam,
                        fontSize = 16.sp,
                        fontWeight = if (game.winner == game.homeTeam)
                            FontWeight.ExtraBold else FontWeight.Normal
                    )
                    Text(
                        text = game.homeScore ?: "-",
                        fontSize = 16.sp,
                        fontWeight = if (game.winner == game.homeTeam)
                            FontWeight.ExtraBold else FontWeight.Normal
                    )
                }

                // final label
                Text(
                    text = "Final",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // away team row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = game.awayTeam,
                        fontSize = 16.sp,
                        fontWeight = if (game.winner == game.awayTeam)
                            FontWeight.ExtraBold else FontWeight.Normal
                    )
                    Text(
                        text = game.awayScore ?: "-",
                        fontSize = 16.sp,
                        fontWeight = if (game.winner == game.awayTeam)
                            FontWeight.ExtraBold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // winner
                Text(
                    text = "Winner: ${game.winner}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

fun getTimeLeftText(game: GameObject): String {

    val periodText = if (game.mens) {
        when (game.period) {
            "1st" -> "1st Half"
            "2nd" -> "2nd Half"
            "OT" -> "Overtime"
            else -> "Half Time"
        }
    } else {
        when (game.period) {
            "1st" -> "1st Quarter"
            "2nd" -> "2nd Quarter"
            "3rd" -> "3rd Quarter"
            "4th" -> "4th Quarter"
            "OT" -> "Overtime"
            else -> "Half Time"
        }
    }

    return "${game.timeLeft} left in $periodText"
}
