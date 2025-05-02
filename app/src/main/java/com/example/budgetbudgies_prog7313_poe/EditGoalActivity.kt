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
    private var userId: Int = -1 // Added userId
    private lateinit var existingGoal: Goal // Renamed from 'existing' for clarity

    // Views
    private lateinit var etName: EditText
    private lateinit var etNotes: EditText
    private lateinit var etTargetAmount: EditText // Renamed from etAmt
    private lateinit var etSavedAmount: EditText // Renamed from etProgress
    private lateinit var spCategory: Spinner // Spinner to show category (likely read-only here)
    private lateinit var spCurrency: Spinner // Currency spinner
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button // Added Delete button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgressText: TextView
    private lateinit var tvCategoryDisplay: TextView // Added TextView to display category


    private var userCategories: List<Category> = listOf() // To hold categories for display

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_goals) // Use edit_goals.xml

        // *** Get userId from SessionManager ***
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
        etTargetAmount = findViewById(R.id.etAmount) // Correct ID from edit_goals.xml
        etSavedAmount = findViewById(R.id.etProgress) // Correct ID from edit_goals.xml
        spCategory = findViewById(R.id.spinnerCategory) // This spinner might be replaced or disabled
        tvCategoryDisplay = TextView(this) // Programmatically create or find if added to XML
        spCurrency = findViewById(R.id.spinnerCurrency)
        btnSave = findViewById(R.id.btnSetup) // Correct ID from edit_goals.xml ("Save Changes")
        btnDelete = findViewById(R.id.btnDelete) // Correct ID from edit_goals.xml
        progressBar = findViewById(R.id.progressBar)
        tvProgressText = findViewById(R.id.tvProgressText)

        // Since changing category might be complex (affecting saved amounts?),
        // let's disable the category spinner for editing for now.
        // We'll display the category name instead.
        spCategory.visibility = View.GONE // Hide the spinner
        // Add a TextView dynamically or find one if you add it to XML to show category
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
            // categoryid = existingGoal.categoryid, // Keep original category
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
        // Optional: Add confirmation dialog here
        // Example:
        // AlertDialog.Builder(this)
        //     .setTitle("Delete Goal")
        //     .setMessage("Are you sure you want to delete '${existingGoal.goalname}'?")
        //     .setPositiveButton("Delete") { _, _ ->
        //         // Proceed with deletion
        //         performDelete()
        //     }
        //     .setNegativeButton("Cancel", null)
        //     .show()

        // For now, delete directly:
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
}