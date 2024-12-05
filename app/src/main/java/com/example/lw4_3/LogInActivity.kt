package com.example.lw4_3

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lw4_2.domain.AccountMockRepository
import com.example.lw4_2.domain.AssertPasswordUseCase
import com.example.lw4_3.databinding.ActivityLogInBinding


class LogInActivity : AppCompatActivity() {
    private val accounts = AccountMockRepository()
    private val assertPasswordUseCase = AssertPasswordUseCase()
    private var _binding: ActivityLogInBinding? = null
    val binding
        get() = _binding?: throw IllegalStateException("No binding!")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.button.setOnClickListener({
            val name = binding.ettLogin.text.toString()
            val account = accounts.findAccoundByName(name)
            if (account != null){
                sendMessage(account.name)
            }
        })
    }
    private fun sendMessage(message: String) {
        val data = Intent()
        data.putExtra("ACCESS_MESSAGE", message)
        setResult(RESULT_OK, data)
        finish()
    }
}