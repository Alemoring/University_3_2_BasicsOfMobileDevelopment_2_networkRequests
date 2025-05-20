package com.example.lw4_3

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.lw4_3.databinding.ActivityLogInBinding
import com.example.lw4_3.databinding.ActivityLogUpBinding
import com.example.lw4_3.domain.UserViewModel
import com.example.lw4_3.domain.UserApiService
import com.example.lw4_3.domain.UserViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LogUpActivity : AppCompatActivity() {
    private var _binding: ActivityLogUpBinding? = null
    val binding
        get() = _binding?: throw IllegalStateException("No binding!")
    private val viewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserApiService.create(application))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityLogUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressBarLogUp.isVisible = false
        setupObservers()
        setupListeners()
    }
    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.userData.collect { user ->
                    user?.let {
                        binding.resultTextView.text =
                            "Пользователь: ${it.username}\nEmail: ${it.mail}"
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.error.collect { error ->
                    error?.let {
                        Toast.makeText(this@LogUpActivity, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.loading.collect { isLoading ->
                    binding.progressBarLogUp.isVisible = isLoading
                    binding.btnLogup.isEnabled = !isLoading
                    //binding.getUserButton.isEnabled = !isLoading
                }
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo?.isConnected == true
    }

    private fun setupListeners() {
        binding.btnLogup.setOnClickListener {
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "Нет интернет-соединения", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val username = binding.ettUsername.text.toString()
            val email = binding.ettMail.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty()) {
                viewModel.registerUser(username, email)
            } else {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }

        binding.getUserButton.setOnClickListener {
            val username = binding.ettUsername.text.toString()

            if (username.isNotEmpty()) {
                viewModel.getUser(username)
            } else {
                Toast.makeText(this, "Введите имя пользователя", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelRequests()
    }
}