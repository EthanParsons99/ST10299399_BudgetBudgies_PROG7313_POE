package com.example.budgetbudgies_prog7313_poe

import android.os.Bundle
import android.view.MenuItem // Import MenuItem
import android.view.View      // <--- ADD THIS IMPORT
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar // Import Toolbar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.* // Import Locale

class EditGoalActivity : AppCompatActivity() {
    private lateinit var dao: GoalDao
    private var goalId: Int = -1
    private var userId: Int = -1
    private lateinit var existingGoal: Goal

    // Views
    private lateinit var etName: EditText
    private lateinit var etNotes: EditText
    private lateinit var etTargetAmount: EditText
    private lateinit var etSavedAmount: EditText
    private lateinit var spCategory: Spinner
    private lateinit var spCurrency: Spinner // Currency spinner
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgressText: TextView
    private lateinit var tvCategoryDisplay: TextView


    private var userCategories: List<Category> = listOf() // To hold categories for display

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_goals)

        // Get userId from SessionManager
        userId = SessionManager.getUserId(applicationContext)
        goalId = intent.getIntExtra("GOAL_ID", -1) // Get goal ID from intent

        if (userId == -1 || goalId == -1) {
            Toast.makeText(this, "Error: Invalid user or goal ID.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        dao = AppDatabase.getDatabase(this).goalDao()

        // *** Set up the Toolbar ***
        val toolbar: Toolbar = findViewById(R.id.editGoalsToolbar) // Find toolbar by ID
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Show back arrow
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Hide default title (using centered TextView)
        // The centered TextView title ("Edit Goal") is in the XML

        // Find Views by ID
        findViews()

        // Set up listeners
        btnSave.setOnClickListener { saveChanges() }
        btnDelete.setOnClickListener { deleteGoal() } // Added listener for delete

        // Load initial data
        loadGoalData()
    }

    private fun findViews() {
        etName = findViewById(R.id.etName)
        etNotes = findViewById(R.id.etNotes)
        etTargetAmount = findViewById(R.id.etAmount)
        etSavedAmount = findViewById(R.id.etProgress)
        spCategory = findViewById(R.id.spinnerCategory)
        tvCategoryDisplay = TextView(this)
        spCurrency = findViewById(R.id.spinnerCurrency)
        btnSave = findViewById(R.id.btnSetup)
        btnDelete = findViewById(R.id.btnDelete)
        progressBar = findViewById(R.id.progressBar)
        tvProgressText = findViewById(R.id.tvProgressText)


        spCategory.visibility = View.GONE // Hide the spinner
        val parentLayout = etNotes.parent as? LinearLayout // Get parent layout
        tvCategoryDisplay.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 16 } // Add some margin
        tvCategoryDisplay.textSize = 16f
        // Check if parentLayout is not null and if tvCategoryDisplay is not already added
        if (parentLayout != null && tvCategoryDisplay.parent == null) {
            parentLayout.addView(tvCategoryDisplay, parentLayout.indexOfChild(spCategory)) // Add TextView where spinner was
        }
    }


    private fun loadGoalData() {
        lifecycleScope.launch {
            try {
                // Fetch Goal
                val goal = withContext(Dispatchers.IO) { dao.getGoalById(goalId) }
                if (goal == null || goal.userid != userId) { // Security check
                    Toast.makeText(this@EditGoalActivity, "Goal not found or access denied.", Toast.LENGTH_LONG).show()
                    finish()
                    return@launch
                }
                existingGoal = goal

                // Fetch Category Name
                val category = withContext(Dispatchers.IO) {
                    AppDatabase.getDatabase(applicationContext).categoryDao().getCategoryById(existingGoal.categoryid)
                }

                // Populate UI on Main Thread
                withContext(Dispatchers.Main) {
                    etName.setText(existingGoal.goalname)
                    etNotes.setText(existingGoal.notes)
                    etTargetAmount.setText(String.format(Locale.US, "%.2f", existingGoal.target))
                    etSavedAmount.setText(String.format(Locale.US, "%.2f", existingGoal.savedAmount))

                    // Display category name (read-only)
                    tvCategoryDisplay.text = "Category: ${category?.categoryname ?: "Unknown"}"


                    // Setup Currency Spinner
                    val currencies = listOf("ZAR", "USD", "EUR", "GBP") // Add more if needed
                    val currencyAdapter = ArrayAdapter(this@EditGoalActivity, android.R.layout.simple_spinner_item, currencies)
                    currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spCurrency.adapter = currencyAdapter
                    val currencyPosition = currencies.indexOf(existingGoal.currency)
                    if (currencyPosition >= 0) {
                        spCurrency.setSelection(currencyPosition)
                    }

                    // Update Progress Bar
                    updateProgressBarUI()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditGoalActivity, "Error loading goal data.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateProgressBarUI() {
        val target = existingGoal.target
        val saved = existingGoal.savedAmount
        val progressPercent = if (target > 0) (saved / target * 100).toInt() else 0
        progressBar.progress = progressPercent.coerceIn(0, 100) // Ensure progress is between 0-100
        tvProgressText.text = String.format(Locale.US, "%d%% Complete (R%.2f / R%.2f)",
            progressPercent, saved, target)
    }


    private fun saveChanges() {
        val name = etName.text.toString().trim()
        val notes = etNotes.text.toString().trim()
        val targetStr = etTargetAmount.text.toString()
        val savedStr = etSavedAmount.text.toString()
        val currency = spCurrency.selectedItem.toString()

        // --- Validation ---
        if (name.isEmpty()) {
            etName.error = "Goal name cannot be empty"
            etName.requestFocus()
            return
        }
        val target = targetStr.toDoubleOrNull()
        if (target == null || target <= 0) {
            etTargetAmount.error = "Invalid target amount"
            etTargetAmount.requestFocus()
            return
        }
        val saved = savedStr.toDoubleOrNull()
        if (saved == null || saved < 0) {
            etSavedAmount.error = "Invalid saved amount"
            etSavedAmount.requestFocus()
            return
        }
        if (saved > target) {
            etSavedAmount.error = "Saved amount cannot exceed target"
            etSavedAmount.requestFocus()
            return
        }
        // --- End Validation ---


        // Create updated goal object - Keep categoryId the same
        val updatedGoal = existingGoal.copy(
            goalname = name,
            notes = notes,
            target = target,
            savedAmount = saved,
            currency = currency,
            completed = (saved >= target) // Mark completed if saved amount reaches target
        )

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) { dao.updateGoal(updatedGoal) }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditGoalActivity, "Goal updated!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK) // Indicate success to previous activity
                    finish() // Close activity
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditGoalActivity, "Error updating goal.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteGoal() {

        performDelete()
    }

    private fun performDelete() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) { dao.deleteGoal(existingGoal) }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditGoalActivity, "Goal deleted.", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK) // Indicate success/change to previous activity
                    finish()
                }
            } catch(e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditGoalActivity, "Error deleting goal.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}