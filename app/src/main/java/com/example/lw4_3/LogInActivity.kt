package com.example.lw4_3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lw4_2.domain.Account
import com.example.lw4_2.domain.AccountMockRepository
import com.example.lw4_2.domain.AssertPasswordUseCase
import com.example.lw4_3.databinding.ActivityLogInBinding
import java.util.concurrent.Executors

class LogInActivity : AppCompatActivity() {
    private val accounts = AccountMockRepository()
    private val assertPasswordUseCase = AssertPasswordUseCase()
    private var _binding: ActivityLogInBinding? = null
    private val executor = Executors.newFixedThreadPool(2).also {
        Log.i("ThreadPool", "ExecutorService created with 2 threads")
    }

    companion object {
        private const val TAG = "AuthFlow"
    }

    val binding
        get() = _binding ?: throw IllegalStateException("No binding!")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Activity created")
        enableEdgeToEdge()
        _binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.progressBar.visibility = View.INVISIBLE
        binding.button.setOnClickListener {
            Log.i(TAG, "Login button clicked")
            val name = binding.ettLogin.text.toString()
            val password = binding.etpPassword.text.toString()

            binding.progressBar.visibility = View.VISIBLE
            binding.button.isEnabled = false

            // Поток 1: Валидация данных
            executor.execute {
                Log.i(TAG, "ValidationThread started | ${Thread.currentThread().name}")
                try {
                    val isInputValid = validateInput(name, password)
                    Log.i(TAG, "Validation completed: $isInputValid")

                    runOnUiThread {
                        if (!isInputValid) {
                            Log.i(TAG, "Invalid input format")
                            binding.button.isEnabled = true
                            binding.progressBar.visibility = View.INVISIBLE
                            return@runOnUiThread
                        }
                    }

                    // Поток 2: Аутентификация
                    executor.execute {
                        Log.i(TAG, "AuthThread started | ${Thread.currentThread().name}")
                        try {
                            val account = accounts.findAccoundByName(name).also {
                                Log.i(TAG, "Account search result: ${it?.name ?: "null"}")
                            }

                            val userProfile = prepareUserData(account).also {
                                Log.i(TAG, "User profile prepared: $it")
                            }

                            runOnUiThread {
                                binding.button.isEnabled = true

                                if (account != null) {
                                    Log.i(TAG, "Authentication successful for ${account.name}")
                                    sendMessage(account.name, userProfile)
                                } else {
                                    Log.i(TAG, "Authentication failed")
                                    val dialog = NotInProfilesDialog()
                                    binding.progressBar.visibility = View.INVISIBLE
                                    dialog.show(supportFragmentManager, "custom")
                                }
                            }
                        } catch (e: Exception) {
                            Log.i(TAG, "Error in AuthThread: ${e.message}", e)
                            runOnUiThread {
                                binding.progressBar.visibility = View.INVISIBLE
                                binding.button.isEnabled = true
                            }
                        } finally {
                            Log.i(TAG, "AuthThread finished | ${Thread.currentThread().name}")
                        }
                    }
                } catch (e: Exception) {
                    Log.i(TAG, "Error in ValidationThread: ${e.message}", e)
                    runOnUiThread {
                        binding.progressBar.visibility = View.INVISIBLE
                        binding.button.isEnabled = true
                    }
                } finally {
                    Log.i(TAG, "ValidationThread finished | ${Thread.currentThread().name}")
                }
            }
        }
    }

    private fun validateInput(name: String, password: String): Boolean {
        Log.i(TAG, "Validating input: name.length=${name.length}, password.length=${password.length}")
        return name.length >= 4 && password.length >= 6
    }

    private fun prepareUserData(account: Account?): UserProfile {
        Log.i(TAG, "Preparing user data...")
        try {
            Thread.sleep(5000) // Имитация долгой операции
            return UserProfile(
                account?.name ?: "",
                account?.password ?: "",
                account?.mail ?: "",
                lastLogin = System.currentTimeMillis()
            ).also {
                Log.i(TAG, "User data prepared successfully")
            }
        } catch (e: InterruptedException) {
            Log.i(TAG, "User data preparation interrupted")
            throw e
        }
    }

    private fun sendMessage(username: String, profile: UserProfile) {
        Log.i(TAG, "Sending auth result for user: $username")
        val data = Intent().apply {
            putExtra("ACCESS_MESSAGE", username)
            putExtra("USER_PROFILE", profile.email)
        }
        setResult(RESULT_OK, data)
        finish()
        Log.i(TAG, "Activity finished with success result")
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
        Log.i(TAG, "Activity destroyed, executor shutdown")
    }
}

data class UserProfile(
    val username: String,
    val password: String,
    val email: String,
    val lastLogin: Long
) {
    override fun toString(): String {
        return "UserProfile(username='$username', email='$email', password=$password)"
    }
}