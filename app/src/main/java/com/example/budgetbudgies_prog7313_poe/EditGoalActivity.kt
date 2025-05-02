package com.example.budgetbudgies_prog7313_poe

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditGoalActivity : AppCompatActivity() {
    private lateinit var dao: GoalDao
    private var goalId: Int = 0
    private lateinit var existing: Goal

    // UI Elements
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgressText: TextView
    private lateinit var etName: EditText
    private lateinit var etNotes: EditText
    private lateinit var etAmount: EditText
    private lateinit var etProgress: EditText
    private lateinit var spCategory: Spinner
    private lateinit var spCurrency: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_goals)

        // Initialize UI components
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.toolbarTitle).text = "Edit Goal"

        progressBar = findViewById(R.id.progressBar)
        tvProgressText = findViewById(R.id.tvProgressText)
        etName = findViewById(R.id.etName)
        etNotes = findViewById(R.id.etNotes)
        etAmount = findViewById(R.id.etAmount)
        etProgress = findViewById(R.id.etProgress)
        spCategory = findViewById(R.id.spinnerCategory)
        spCurrency = findViewById(R.id.spinnerCurrency)

        val btnSave = findViewById<Button>(R.id.btnSetup)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        // Initialize database access
        dao = AppDatabase.getDatabase(this).goalDao()
        goalId = intent.getIntExtra("GOAL_ID", 0)

        // Set up spinners
        val categories = listOf("Supermarket", "Fuel", "Other")
        spCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        val currencies = listOf("ZAR", "USD")
        spCurrency.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, currencies)

        // Load existing goal data
        loadGoalData()

        // Setup save button
        btnSave.setOnClickListener {
            saveGoalData()
        }

        // Setup delete button
        btnDelete.setOnClickListener {
            confirmDelete()
        }

        // Add text watchers to update progress bar when values change
        etAmount.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updateProgressBar()
            }
        })

        etProgress.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updateProgressBar()
            }
        })
    }

    private fun loadGoalData() {
        lifecycleScope.launch {
            try {
                existing = withContext(Dispatchers.IO) { dao.getGoalById(goalId)!! }

                // Populate form fields
                etName.setText(existing.goalname)
                etNotes.setText(existing.notes)
                etAmount.setText(existing.target.toString())
                etProgress.setText(existing.savedAmount.toString())

                // Set spinner selections
                val categoryIndex = existing.categoryid
                if (categoryIndex in 0..2) {
                    spCategory.setSelection(categoryIndex)
                }

                val currencyIndex = if (existing.currency == "USD") 1 else 0
                spCurrency.setSelection(currencyIndex)

                // Update progress display
                updateProgressBar()
            } catch (e: Exception) {
                Toast.makeText(this@EditGoalActivity, "Error loading goal data", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun updateProgressBar() {
        try {
            val target = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val progress = etProgress.text.toString().toDoubleOrNull() ?: 0.0

            if (target > 0) {
                val percentage = (progress / target * 100).toInt().coerceIn(0, 100)
                progressBar.progress = percentage
                tvProgressText.text = "$percentage% Complete"
            } else {
                progressBar.progress = 0
                tvProgressText.text = "0% Complete"
            }
        } catch (e: Exception) {
            progressBar.progress = 0
            tvProgressText.text = "Error calculating progress"
        }
    }

    private fun saveGoalData() {
        val name = etName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a goal name", Toast.LENGTH_SHORT).show()
            return
        }

        val targetAmount = etAmount.text.toString().toDoubleOrNull()
        if (targetAmount == null || targetAmount <= 0) {
            Toast.makeText(this, "Please enter a valid target amount", Toast.LENGTH_SHORT).show()
            return
        }

        val progressAmount = etProgress.text.toString().toDoubleOrNull() ?: 0.0
        if (progressAmount < 0) {
            Toast.makeText(this, "Progress amount cannot be negative", Toast.LENGTH_SHORT).show()
            return
        }

        // Create updated goal object
        val updated = existing.copy(
            goalname = name,
            notes = etNotes.text.toString().trim(),
            categoryid = spCategory.selectedItemPosition,
            target = targetAmount,
            savedAmount = progressAmount,
            currency = spCurrency.selectedItem.toString()
        )

        // Save to database
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dao.updateGoal(updated)
                }
                Toast.makeText(this@EditGoalActivity, "Goal updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@EditGoalActivity, "Error updating goal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete this goal? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteGoal()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteGoal() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dao.deleteGoal(existing)
                }
                Toast.makeText(this@EditGoalActivity, "Goal deleted", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@EditGoalActivity, "Error deleting goal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}