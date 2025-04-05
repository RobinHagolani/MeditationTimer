
package com.robinhagolani.meditationtimer.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job

@HiltViewModel
class TimerViewModel @Inject constructor() : ViewModel() {

    data class TimerState(
        val totalSeconds: Int = 900, // 15 minutes default
        val remainingSeconds: Int = 900,
        val isRunning: Boolean = false,
        val isPaused: Boolean = false
    )

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null

    fun setDuration(minutes: Int) {
        val seconds = minutes * 60
        _timerState.value = _timerState.value.copy(
            totalSeconds = seconds,
            remainingSeconds = seconds
        )
    }

    fun startTimer() {
        if (timerJob?.isActive == true) return

        timerJob = viewModelScope.launch {
            _timerState.value = _timerState.value.copy(
                isRunning = true,
                isPaused = false
            )

            while (_timerState.value.remainingSeconds > 0) {
                delay(1000L)
                _timerState.value = _timerState.value.copy(
                    remainingSeconds = _timerState.value.remainingSeconds - 1
                )
            }

            _timerState.value = _timerState.value.copy(isRunning = false)
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(
            isRunning = false,
            isPaused = true
        )
    }

    fun resumeTimer() {
        startTimer()
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(
            remainingSeconds = _timerState.value.totalSeconds,
            isRunning = false,
            isPaused = false
        )
    }
}