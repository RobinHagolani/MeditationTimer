
package com.robinhagolani.meditationtimer.viewmodels

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    data class TimerState(
        val totalSeconds: Int = 900, // 15 minutes default
        val remainingSeconds: Int = 900,
        val isRunning: Boolean = false,
        val isPaused: Boolean = false
    )

    private val _totalSeconds = savedStateHandle.getStateFlow("total_seconds", 900)
    private val _remainingSeconds = savedStateHandle.getStateFlow("remaining_seconds", 900)
    private val _isRunning = savedStateHandle.getStateFlow("is_running", false)
    private val _isPaused = savedStateHandle.getStateFlow("is_paused", false)

    val timerState = combine(
        _totalSeconds,
        _remainingSeconds,
        _isRunning,
        _isPaused
    ) { total, remaining, running, paused ->
        TimerState(
            totalSeconds = total,
            remainingSeconds = remaining,
            isRunning = running,
            isPaused = paused
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimerState()
    )

    private var timerJob: Job? = null

    fun setDuration(minutes: Int) {
        val seconds = minutes * 60
        savedStateHandle["total_seconds"] = seconds
        savedStateHandle["remaining_seconds"] = seconds
    }

    fun startTimer() {
        if (timerJob?.isActive == true) return

        timerJob = viewModelScope.launch {
            savedStateHandle["is_running"] = true
            savedStateHandle["is_paused"] = false

            while (_remainingSeconds.value > 0) {
                delay(1000L)
                savedStateHandle["remaining_seconds"] = _remainingSeconds.value - 1
            }

            savedStateHandle["is_running"] = false
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        savedStateHandle["is_running"] = false
        savedStateHandle["is_paused"] = true
    }

    fun resumeTimer() {
        startTimer()
    }

    fun resetTimer() {
        timerJob?.cancel()
        savedStateHandle["remaining_seconds"] = _totalSeconds.value
        savedStateHandle["is_running"] = false
        savedStateHandle["is_paused"] = false
    }
}