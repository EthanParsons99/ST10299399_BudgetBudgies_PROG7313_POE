package com.example.budgetbudgies_prog7313_poe

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem // Import MenuItem
import android.widget.Toast // Import Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetbudgies_prog7313_poe.databinding.GoalsPageBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GoalsActivity : AppCompatActivity() {

    private lateinit var binding: GoalsPageBinding
    private lateinit var adapter: GoalAdapter
    private lateinit var db: AppDatabase
    private var userId: Int = -1 // Initialize with invalid ID

    private val calendar = Calendar.getInstance() // Added calendar instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GoalsPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //  Get userId from SessionManager
        userId = SessionManager.getUserId(applicationContext)
        if (userId == -1) {
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_LONG).show()
            finish() // Go back if not logged in
            return
        }

        db = AppDatabase.getDatabase(this)
        adapter = GoalAdapter()

        // Set up the Toolbar
        setSupportActionBar(binding.goalsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Show back arrow
        supportActionBar?.setDisplayShowHomeEnabled(true)
        // Title is set in XML (app:title="Goals")

        binding.recyclerViewGoals.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewGoals.adapter = adapter

        binding.btnAddGoal.setOnClickListener {
            startActivity(Intent(this, AddGoalActivity::class.java))
        }

        // Set initial month/year
        updateMonthYearText()

        // Handle prev/next month buttons
        binding.prevMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateMonthYearText()

        }

        // Handle prev/next month buttons
        binding.nextMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateMonthYearText()

        }

        // Load goals
        loadGoals()
    }

    // Separate function to load goals
    private fun loadGoals() {
        if (userId == -1) return // Check userId again just in case

        lifecycleScope.launch {
            // Adjust DAO query if month filtering is needed for goals
            db.goalDao().getUserGoals(userId).collectLatest { goals ->
                adapter.submitList(goals)

                // Calculate total TARGET of all goals listed (adjust logic if needed)
                val totalTarget = goals.sumOf { it.target }
                binding.tvMonthlyGoal.text = String.format(Locale.US, "R %.2f", totalTarget) // Display total target
            }
        }
    }


    // Update month/year text
    private fun updateMonthYearText() {
        val formatter = SimpleDateFormat("MMMM, yyyy", Locale.getDefault())
        binding.monthYearTextView.text = formatter.format(calendar.time)
    }

    // *** Handle Toolbar Back Button Click ***
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish() // Close this activity
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Refresh list when returning to the activity (e.g., after adding/editing a goal)
    override fun onResume() {
        super.onResume()
        if (userId != -1) {
            loadGoals() // Reload goals
        }
    }
}