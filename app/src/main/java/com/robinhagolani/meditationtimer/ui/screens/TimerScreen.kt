package com.robinhagolani.meditationtimer.ui.screens

import android.R.drawable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.robinhagolani.meditationtimer.ui.components.CircularTimer
import com.robinhagolani.meditationtimer.viewmodels.TimerViewModel

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel()
) {
    val timerState by viewModel.timerState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Duration selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(5, 10, 15, 20).forEach { minutes ->
                OutlinedButton(
                    onClick = { viewModel.setDuration(minutes) },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text("$minutes min")
                }
            }
        }

        // Timer
        CircularTimer(
            progress = 1f - (timerState.remainingSeconds.toFloat() / timerState.totalSeconds.toFloat()),
            time = formatTime(timerState.remainingSeconds)
        )

        // Control buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FloatingActionButton(
                onClick = { viewModel.resetTimer() }
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Reset"
                )
            }

            FloatingActionButton(
                onClick = {
                    if (timerState.isRunning) {
                        viewModel.pauseTimer()
                    } else {
                        if (timerState.isPaused) viewModel.resumeTimer() else viewModel.startTimer()
                    }
                }
            ) {
                if (timerState.isRunning) {
                    Icon(
                        painter = painterResource(id = drawable.ic_media_pause),
                        contentDescription = "Pause"
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Start"
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}