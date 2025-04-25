package com.example.budgetbudgies_prog7313_poe

import android.app.Activity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageButton
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.budgetbudgies_prog7313_poe.databinding.ActivityMainWithNavDrawerBinding
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import android.content.Intent


class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainWithNavDrawerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainWithNavDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.navToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        var toggleOnOff = ActionBarDrawerToggle(this,
            binding.drawerLayout, binding.navToolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close)
        binding.drawerLayout.addDrawerListener(toggleOnOff)
        toggleOnOff.syncState()

        binding.navView.bringToFront()
        binding.navView.setNavigationItemSelectedListener(this)


        val analyticsBtn = findViewById<ImageButton>(R.id.analyticsbtn)
        val progressBtn = findViewById<ImageButton>(R.id.progressbtn)
        val budgetsBtn = findViewById<ImageButton>(R.id.budgetsbtn)
        val goalsBtn = findViewById<ImageButton>(R.id.goalsbtn)
        val accountBtn = findViewById<ImageButton>(R.id.accountbtn)

        accountBtn.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

    }

    override fun onBackPressed() {
        if(binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.title.toString()) {
            "Achievements" -> Toast.makeText(this, "Achievements clicked", Toast.LENGTH_SHORT).show()
            "Categorties" -> Toast.makeText(this, "Categories clicked", Toast.LENGTH_SHORT).show()
            "Currency" -> Toast.makeText(this, "Currency clicked", Toast.LENGTH_SHORT).show()
            "Settings" -> Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
            "Support" -> Toast.makeText(this, "Support clicked", Toast.LENGTH_SHORT).show()
            "Login" -> Toast.makeText(this, "Login clicked", Toast.LENGTH_SHORT).show()
            "Sign Up" -> Toast.makeText(this, "Sign Up clicked", Toast.LENGTH_SHORT).show()
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    }


