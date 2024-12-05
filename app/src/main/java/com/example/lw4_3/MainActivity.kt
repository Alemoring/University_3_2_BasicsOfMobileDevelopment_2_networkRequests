package com.example.lw4_3

//noinspection SuspiciousImport
//import android.R
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.lw4_3.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    //private lateinit var appBarConfiguration: AppBarConfiguration
    private var _binding: ActivityMainBinding? = null
    val binding
        get() = _binding?: throw IllegalStateException("No binding!")
    private var isLogIn = false
    private lateinit var drawerLayout:DrawerLayout
    private lateinit var navigationView:NavigationView
    private lateinit var toolBar:Toolbar
    val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        val textView = binding.tvAccount
        if (result.resultCode == Activity.RESULT_OK) {
            isLogIn = true
            isLogged()
            intent = result.data
            var accessMessage = intent.getStringExtra("ACCESS_MESSAGE")
            textView.text = accessMessage
        } else {
            textView.text = "Ошибка доступа"
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = binding.drawerLayout
        navigationView = binding.navView
        toolBar = binding.toolbar
        isLogged()

        setSupportActionBar(toolBar)

        navigationView.bringToFront()
        var toggle:ActionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)

        navigationView.setCheckedItem(R.id.home)
    }
    public fun isLogged(){
        if (isLogIn){
            binding.navView.menu.findItem(R.id.logout).setVisible(true)
            binding.navView.menu.findItem(R.id.profile).setVisible(true)
            binding.navView.menu.findItem(R.id.logUp).setVisible(false)
            binding.navView.menu.findItem(R.id.logIn).setVisible(false)
        }else {
            binding.navView.menu.findItem(R.id.logout).setVisible(false)
            binding.navView.menu.findItem(R.id.profile).setVisible(false)
            binding.navView.menu.findItem(R.id.logUp).setVisible(true)
            binding.navView.menu.findItem(R.id.logIn).setVisible(true)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }else {
            super.onBackPressed()
        }
    }
    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.home -> return true
            R.id.profile -> {
                /*start<PersonDetailsActivity> {
                    putExtra("Car_Details", tvCountCars.text)
                }*/
                var intent = Intent(binding.root.context, ProfileActivity::class.java)
                startActivity(intent)
            }
            R.id.logIn -> {
                var intent = Intent(binding.root.context, LogInActivity::class.java)
                startForResult.launch(intent)
            }
            R.id.logUp -> {
                var intent = Intent(binding.root.context, LogUpActivity::class.java)
                startActivity(intent)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}



