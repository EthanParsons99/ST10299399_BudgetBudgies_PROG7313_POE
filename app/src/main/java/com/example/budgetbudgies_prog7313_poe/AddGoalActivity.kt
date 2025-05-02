package com.example.budgetbudgies_prog7313_poe

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetbudgies_prog7313_poe.databinding.AddGoalsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddGoalActivity : AppCompatActivity() {

    private lateinit var binding: AddGoalsBinding
    private lateinit var db: AppDatabase
    private val userId = 1 // Replace with actual session-based ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddGoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        binding.btnSaveGoal.setOnClickListener {
            val name = binding.etGoalName.text.toString()
            val notes = binding.etNotes.text.toString()
            val target = binding.etTarget.text.toString().toDoubleOrNull()
            val currency = binding.etCurrency.text.toString()

            if (name.isEmpty() || target == null || currency.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val goal = Goal(
                goalname = name,
                notes = notes,
                categoryid = 1, // Replace or make dynamic
                target = target,
                currency = currency,
                userid = userId
            )

            CoroutineScope(Dispatchers.IO).launch {
                db.goalDao().insertGoal(goal)
                finish()
            }
        }
    }
}