package com.example.lw4_3.domain.data

// Модель для регистрации пользователя
data class RegisterRequest(
    val username: String,
    val mail: String
)