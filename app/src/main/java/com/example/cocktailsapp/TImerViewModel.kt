package com.example.cocktailsapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

class TimerViewModel : ViewModel() {

    // Dzięki użyciu mutableStateOf composable będzie się automatycznie odświeżać w trakcie działania timera
    private val _currentTime = mutableStateOf(0)
    val currentTime: State<Int> get() = _currentTime

    private var job: Job? = null
    var isRunning = false
        private set

    fun initTimer(startValue: Int) {
        // Inicjalizacja licznika tylko raz
        if (_currentTime.value == 0) {
            _currentTime.value = startValue
        }
    }

    fun startTimer() {
        if (!isRunning) {
            isRunning = true
            job = viewModelScope.launch {
                while (isRunning && _currentTime.value > 0) {
                    delay(1000L)
                    _currentTime.value = _currentTime.value - 1
                }
            }
        }
    }

    fun pauseTimer() {
        isRunning = false
        job?.cancel()
    }

    fun resumeTimer() {
        if (!isRunning && _currentTime.value > 0) {
            isRunning = true
            job = viewModelScope.launch {
                while (isRunning && _currentTime.value > 0) {
                    delay(1000L)
                    _currentTime.value = _currentTime.value - 1
                }
            }
        }
    }

    fun resetTimer(newStartValue: Int) {
        pauseTimer()
        _currentTime.value = newStartValue
    }

}