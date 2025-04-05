package com.robinhagolani.meditationtimer.ui.screens

import android.R.drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
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
        // New Duration selector
        DurationSelector(
            onDurationSelected = { viewModel.setDuration(it) }
        )

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

@Composable
private fun DurationSelector(
    onDurationSelected: (Int) -> Unit
) {
    var showCustomInput by remember { mutableStateOf(false) }
    var customMinutes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Quick select chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(5, 10, 15, 20).forEach { minutes ->
                FilterChip(
                    selected = false,
                    onClick = { onDurationSelected(minutes) },
                    label = { Text("$minutes min") },
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        TextButton(
            onClick = { showCustomInput = !showCustomInput }
        ) {
            Text(if (showCustomInput) "Hide custom" else "Custom duration")
        }

        AnimatedVisibility(showCustomInput) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customMinutes,
                    onValueChange = {
                        if (it.isEmpty() || it.toIntOrNull() != null) {
                            customMinutes = it
                        }
                    },
                    label = { Text("Minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(120.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        customMinutes.toIntOrNull()?.let {
                            if (it > 0) {
                                onDurationSelected(it)
                                showCustomInput = false
                                customMinutes = ""
                            }
                        }
                    },
                    enabled = customMinutes.isNotEmpty() && customMinutes.toIntOrNull() != null && customMinutes.toIntOrNull()!! > 0
                ) {
                    Text("Set")
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