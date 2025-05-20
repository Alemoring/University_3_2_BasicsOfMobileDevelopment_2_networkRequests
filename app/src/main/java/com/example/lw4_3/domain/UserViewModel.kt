package com.example.lw4_3.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lw4_3.domain.data.RegisterRequest
import com.example.lw4_3.domain.data.UserResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val apiService: UserApiService) : ViewModel() {
    private val _userData = MutableStateFlow<UserResponse?>(null)
    val userData: StateFlow<UserResponse?> = _userData

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentJob: Job? = null

    fun registerUser(username: String, email: String) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = apiService.registerUser(RegisterRequest(username, email))
                _userData.value = UserResponse(response.username, response.mail)
            } catch (e: Exception) {
                _error.value = "Ошибка регистрации: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun getUser(username: String) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _userData.value = apiService.getUser(username)
            } catch (e: Exception) {
                _error.value = "Ошибка получения данных: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun cancelRequests() {
        currentJob?.cancel()
    }
}