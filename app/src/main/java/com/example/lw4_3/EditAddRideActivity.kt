package com.example.lw4_3

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.core.text.set
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lw4_3.databinding.ActivityEditAddRideBinding
import com.example.lw4_3.databinding.ActivityLogInBinding

class EditAddRideActivity : AppCompatActivity() {
    private var _binding: ActivityEditAddRideBinding? = null
    val binding
        get() = _binding?: throw IllegalStateException("No binding!")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityEditAddRideBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.editOrAdd.text = "Добавьте новую запись"
        if (intent.getStringExtra("LoginIN") != null){
            binding.editOrAdd.text = "Отредактируйте запись"
            val login = intent.getStringExtra("LoginIN").toString()
            val distance = intent.getStringExtra("DistanceIn").toString()
            binding.login.setText(login)
            binding.distance.setText(distance)
        }

        binding.sendEditOrAddData.setOnClickListener({
            val login = binding.login.text.toString()
            val distance = binding.distance.text.toString()
            sendMessage(login, distance)
        })
    }
    private fun sendMessage(login: String, distance: String) {
        val data = Intent()
        data.putExtra("LOGIN", login)
        data.putExtra("DISTANCE", distance)
        setResult(RESULT_OK, data)
        finish()
    }
}