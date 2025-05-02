package com.example.budgetbudgies_prog7313_poe

import android.os.Bundle
import android.view.MenuItem // Import MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar // Import Toolbar
import com.example.budgetbudgies_prog7313_poe.databinding.AddGoalsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.* // Import Locale if needed for String formatting

class AddGoalActivity : AppCompatActivity() {

    private lateinit var binding: AddGoalsBinding
    private lateinit var db: AppDatabase
    private var userId: Int = -1 // Initialize with invalid ID
    private var selectedCategoryId: Int = -1
    private val categories = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddGoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // *** Get userId from SessionManager ***
        userId = SessionManager.getUserId(applicationContext)
        if (userId == -1) {
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_LONG).show()
            finish() // Go back if not logged in
            return
        }

        db = AppDatabase.getDatabase(this)

        // *** Set up the Toolbar ***
        // Note: Toolbar ID in add_goals.xml is addGoalsToolbar
        setSupportActionBar(binding.addGoalsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Show back arrow
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Hide default title (using centered TextView)
        // The centered TextView title ("Add Goal") is in the XML

        // Load categories for the spinner
        loadCategories()

        // Set up category spinner listener
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Handle selection from custom adapter
                val selectedCategory = parent.getItemAtPosition(position) as? Category
                if (selectedCategory != null) {
                    selectedCategoryId = selectedCategory.categoryid
                    binding.tvCategoryType.text = "Type: ${selectedCategory.categoryType}"
                    binding.tvCategoryType.visibility = View.VISIBLE
                    binding.ivCategoryIcon.setImageResource(selectedCategory.icon)
                    binding.ivCategoryIcon.visibility = View.VISIBLE
                } else {
                    // Handle case where selection might be invalid (e.g., prompt "Select Category")
                    selectedCategoryId = -1
                    binding.tvCategoryType.visibility = View.GONE
                    binding.ivCategoryIcon.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCategoryId = -1
                binding.tvCategoryType.visibility = View.GONE
                binding.ivCategoryIcon.visibility = View.GONE
            }
        }

        binding.btnSaveGoal.setOnClickListener {
            saveGoal()
        }
    }

    private fun loadCategories() {
        if (userId == -1) return // Check userId

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get user categories
                categories.clear()
                // Fetch only EXPENSE categories if goals are only for expenses
                val expenseCategories = db.categoryDao().getUserCategoriesList(userId)
                    .filter { it.categoryType == "Expense" }
                categories.addAll(expenseCategories)

                withContext(Dispatchers.Main) {
                    if (categories.isEmpty()) {
                        Toast.makeText(this@AddGoalActivity, "Create Expense categories first!", Toast.LENGTH_LONG).show()
                        binding.btnSaveGoal.isEnabled = false
                        // Consider finishing the activity or disabling the spinner
                        binding.spinnerCategory.isEnabled = false
                    } else {
                        // Custom adapter to show category name and possibly icon in spinner
                        val categoryAdapter = CategorySpinnerAdapter(
                            this@AddGoalActivity,
                            R.layout.item_category_spinner, // Layout for selected item
                            categories
                        )
                        // Use a different layout for the dropdown if needed (e.g., R.layout.item_category_dropdown)
                        categoryAdapter.setDropDownViewResource(R.layout.item_category_dropdown)
                        binding.spinnerCategory.adapter = categoryAdapter
                        binding.spinnerCategory.isEnabled = true
                        binding.btnSaveGoal.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddGoalActivity, "Error loading categories: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnSaveGoal.isEnabled = false
                    binding.spinnerCategory.isEnabled = false
                }
            }
        }
    }

    private fun saveGoal() {
        if (userId == -1) return // Check userId

        val name = binding.etGoalName.text.toString().trim()
        val target = binding.etTarget.text.toString().toDoubleOrNull() // Still Double? here

        // Get the actual Category object selected
        val selectedCategoryObject = binding.spinnerCategory.selectedItem as? Category

        // --- Validation ---
        when {
            name.isEmpty() -> {
                Toast.makeText(this, "Please enter a goal name", Toast.LENGTH_SHORT).show()
                binding.etGoalName.requestFocus()
                return
            }
            selectedCategoryObject == null || selectedCategoryId == -1 -> {
                Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show()
                return
            }
            // Ensure the category type is indeed Expense if that's a rule
            selectedCategoryObject.categoryType != "Expense" -> {
                Toast.makeText(this, "Goals must be linked to an Expense category.", Toast.LENGTH_LONG).show()
                return
            }
            target == null || target <= 0 -> { // Validation ensures target is not null AND positive here
                Toast.makeText(this, "Please enter a valid positive target amount", Toast.LENGTH_SHORT).show()
                binding.etTarget.requestFocus()
                return
            }
        }
        // --- End Validation ---

        // If execution reaches here, target is guaranteed to be non-null and positive.

        // Create goal object
        val goal = Goal(
            goalname = name,
            notes = "", // No notes field in this layout
            categoryid = selectedCategoryId, // Use the ID obtained from selection
            target = target!!, // <-- FIX: Use non-null assertion (safe after validation)
            currency = "ZAR", // Static currency
            userid = userId, // Use the logged-in user's ID
            progress = 0.0, // Default progress
            completed = false, // Default to not completed
            savedAmount = 0.0 // Default savedAmount
        )

        // Save goal to database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resultId = db.goalDao().insertGoal(goal) // Get the inserted ID
                withContext(Dispatchers.Main) {
                    if (resultId != -1L) { // Check if insertion was successful
                        Toast.makeText(this@AddGoalActivity, "Goal saved successfully", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK) // Indicate success to previous activity if needed
                        finish() // Close activity
                    } else {
                        Toast.makeText(this@AddGoalActivity, "Error: Could not save goal (conflict?).", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddGoalActivity, "Error saving goal: ${e.message}", Toast.LENGTH_SHORT).show()
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