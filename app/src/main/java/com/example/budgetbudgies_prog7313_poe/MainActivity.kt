package com.example.budgetbudgies_prog7313_poe

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.budgetbudgies_prog7313_poe.databinding.ActivityMainWithNavDrawerBinding
import com.google.android.material.navigation.NavigationView
import com.example.budgetbudgies_prog7313_poe.PlaceholderActivity

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainWithNavDrawerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Call edge-to-edge function

        binding = ActivityMainWithNavDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.navToolbar)

        val toggleOnOff = ActionBarDrawerToggle(this,
            binding.drawerLayout, binding.navToolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close)
        binding.drawerLayout.addDrawerListener(toggleOnOff)
        toggleOnOff.syncState()

        binding.navView.bringToFront()
        binding.navView.setNavigationItemSelectedListener(this)

        val accountBtn = findViewById<ImageButton>(R.id.accountbtn)
        accountBtn?.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val analyticsBtn = findViewById<ImageButton>(R.id.analyticsbtn)
        analyticsBtn?.setOnClickListener {
            val intent = Intent(this, PlaceholderActivity::class.java)
            intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, "Analytics")
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val progressBtn = findViewById<ImageButton>(R.id.progressbtn)
        progressBtn?.setOnClickListener {
            val intent = Intent(this, PlaceholderActivity::class.java)
            intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, "Progress Dashboard")
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val budgetsBtn = findViewById<ImageButton>(R.id.budgetsbtn)
        budgetsBtn?.setOnClickListener {
            Toast.makeText(this, "Already on Home/Budgets", Toast.LENGTH_SHORT).show()
        }

        val goalsBtn = findViewById<ImageButton>(R.id.goalsbtn)
        goalsBtn?.setOnClickListener {
            val intent = Intent(this, PlaceholderActivity::class.java)
            intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, "Goals")
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var intent: Intent? = null

        when (item.title.toString()) {
            "Achievements" -> {
                intent = Intent(this, PlaceholderActivity::class.java)
                intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, "Achievements")
            }
            "Categorties" -> {
                intent = Intent(this, PlaceholderActivity::class.java)
                intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, "Categories")
            }
            "Currency" -> {
                intent = Intent(this, PlaceholderActivity::class.java)
                intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, "Currency")
            }
            "Settings" -> {
                intent = Intent(this, PlaceholderActivity::class.java)
                intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, "Settings")
            }
            "Support" -> {
                intent = Intent(this, PlaceholderActivity::class.java)
                intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, "Support")
            }
            "Login" -> {
                intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            "Sign Up" -> {
                intent = Intent(this, SignUpActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            "Home Page (Testing Purposes)" -> {
                Toast.makeText(this, "Already on Home Page", Toast.LENGTH_SHORT).show()
                intent = null
            }
            else -> {
                Toast.makeText(this, "Unknown item clicked", Toast.LENGTH_SHORT).show()
                intent = null
            }
        }

        intent?.let {
            startActivity(it)
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}

