package com.example.budgetbudgies_prog7313_poe

import android.content.Intent
import android.os.Bundle
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
    private val userId = 1

    private val calendar = Calendar.getInstance() // Added calendar instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GoalsPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)
        adapter = GoalAdapter()

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

        binding.nextMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateMonthYearText()
        }

        // Load goals
        lifecycleScope.launch {
            db.goalDao().getUserGoals(userId).collectLatest { goals ->
                adapter.submitList(goals)

                val total = goals.sumOf { it.target }
                binding.tvMonthlyGoal.text = "R%.2f".format(total)
            }
        }
    }

    private fun updateMonthYearText() {
        val formatter = SimpleDateFormat("MMMM, yyyy", Locale.getDefault())
        binding.monthYearTextView.text = formatter.format(calendar.time)
    }
}
