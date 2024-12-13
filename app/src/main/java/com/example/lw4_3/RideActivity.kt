package com.example.lw4_3

import SwipeHelper
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lw4_3.databinding.ActivityRideBinding
import com.example.lw4_3.domain.Ride
import com.example.lw4_3.domain.RideListener
import com.example.lw4_3.domain.RideMockRepository
import kotlin.properties.Delegates

class RideActivity : AppCompatActivity() {
    private var _binding: ActivityRideBinding? = null
    val binding
        get() = _binding?: throw IllegalStateException("No binding!")
    private lateinit var adapter: RideAdapter // Объект Adapter
    private val repository: RideMockRepository = App.rideRepository
    private val listener: RideListener = {adapter.data = it}
    private lateinit var login: String
    private lateinit var distance: String
    private lateinit var type: String
    private var pos: Int = 0
    val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            intent = result.data
            login = intent.getStringExtra("LOGIN").toString()
            distance = intent.getStringExtra("DISTANCE").toString()
            if (type == "EDIT"){
                repository.editRide(repository.getRides().get(pos), login, distance)
            }else if (type == "ADD"){
                repository.createRide(login, distance)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityRideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false) // LayoutManager
        adapter = RideAdapter(object : RideActionListener { // Создание объекта
            override fun onRideGetId(ride: Ride) =
                Toast.makeText(this@RideActivity, "Rides ID: ${ride.id}", Toast.LENGTH_SHORT).show()

            override fun onRideAdd(login: String, distance: String) = repository.createRide(login, distance)

            override fun onRideRemove(ride: Ride) = repository.removeRide(ride)

            override fun onRideEdit(ride: Ride, login: String, distance: String) = repository.editRide(ride, login, distance)

        }) // Создание объекта
        adapter.data = repository.getRides() // Заполнение данными

        binding.recyclerView.layoutManager = manager // Назначение LayoutManager для RecyclerView
        binding.recyclerView.adapter = adapter // Назначение адаптера для RecyclerView
        repository.addListener(listener)

        val swipeHelper = object : SwipeHelper(binding.recyclerView) {
            override fun instantiateUnderlayButton(position: Int): List<UnderlayButton> {
                return listOf(
                    UnderlayButton(
                        this@RideActivity,
                        "Edit",
                        14f,
                        android.R.color.holo_blue_light,
                        R.drawable.icon_edit,
                        10,
                        object : UnderlayButtonClickListener {
                            override fun onClick() {
                                pos = position
                                type = "EDIT"
                                var ride = repository.getRides().get(pos)
                                var intent = Intent(binding.root.context, EditAddRideActivity::class.java)
                                intent.putExtra("LoginIN", ride.login)
                                intent.putExtra("DistanceIn", ride.distance)
                                startForResult.launch(intent)
                            }
                        }
                    ),
                    UnderlayButton(
                        this@RideActivity,
                        "Add",
                        14f,
                        android.R.color.holo_green_light,
                        R.drawable.icon_add,
                        10,
                        object : UnderlayButtonClickListener {
                            override fun onClick() {
                                type = "ADD"
                                var intent = Intent(binding.root.context, EditAddRideActivity::class.java)
                                startForResult.launch(intent)
                            }
                        }
                    )
                )
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHelper)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }
}