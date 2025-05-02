package com.example.budgetbudgies_prog7313_poe

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetbudgies_prog7313_poe.databinding.GoalsPageBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GoalsActivity : AppCompatActivity() {

    private lateinit var binding: GoalsPageBinding
    private lateinit var adapter: GoalAdapter
    private lateinit var db: AppDatabase
    private val userId = 1

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

        lifecycleScope.launch {
            db.goalDao().getUserGoals(userId).collectLatest { goals ->
                adapter.submitList(goals)

                val total = goals.sumOf { it.target }
                binding.tvMonthlyGoal.text = "R%.2f".format(total)
            }
        }
    }
}