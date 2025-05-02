// --- START of AddGoalActivity

package com.example.budgetbudgies_prog7313_poe

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetbudgies_prog7313_poe.databinding.AddGoalsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddGoalActivity : AppCompatActivity() {

    private lateinit var binding: AddGoalsBinding
    private lateinit var db: AppDatabase
    private val userId = 1 // Replace with actual session-based ID
    private var selectedCategoryId: Int = -1
    private val categories = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddGoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        // Set up back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Load categories for the spinner
        loadCategories()

        // Set up category spinner listener
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < categories.size) {
                    selectedCategoryId = categories[position].categoryid

                    // You might want to display the category type or icon somewhere
                    val selectedCategory = categories[position]
                    binding.tvCategoryType.text = "Type: ${selectedCategory.categoryType}"

                    // If you have an ImageView to display the category icon
                    binding.ivCategoryIcon.setImageResource(selectedCategory.icon)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCategoryId = -1
            }
        }

        binding.btnSaveGoal.setOnClickListener {
            saveGoal()
        }
    }

    private fun loadCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get user categories - you might want to filter by categoryType (e.g., only "Expense" for goals)
                categories.clear()
                val allCategories = db.categoryDao().getUserCategoriesList(userId)

                // Optional: Filter categories by type if needed
                // categories.addAll(allCategories.filter { it.categoryType == "Expense" })

                // Or use all categories
                categories.addAll(allCategories)

                withContext(Dispatchers.Main) {
                    // Custom adapter to show category name and possibly icon in spinner
                    val categoryAdapter = CategorySpinnerAdapter(
                        this@AddGoalActivity,
                        android.R.layout.simple_spinner_item,
                        categories
                    )
                    categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerCategory.adapter = categoryAdapter

                    // Enable the save button after categories are loaded
                    binding.btnSaveGoal.isEnabled = categories.isNotEmpty()

                    // Show message if no categories
                    if (categories.isEmpty()) {
                        Toast.makeText(
                            this@AddGoalActivity,
                            "You need to create categories first",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AddGoalActivity,
                        "Error loading categories: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun saveGoal() {
        val name = binding.etGoalName.text.toString().trim()
        val target = binding.etTarget.text.toString().toDoubleOrNull()

        // Validation
        when {
            name.isEmpty() -> {
                Toast.makeText(this, "Please enter a goal name", Toast.LENGTH_SHORT).show()
                return
            }
            selectedCategoryId == -1 -> {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
                return
            }
            target == null || target <= 0 -> {
                Toast.makeText(this, "Please enter a valid target amount", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Get the selected category to check its type
        val selectedCategory = categories.find { it.categoryid == selectedCategoryId }

        // Optional: Validate category type if goals should only be for specific types
        if (selectedCategory?.categoryType != "Expense") {
            Toast.makeText(this, "Goals can only be created for expense categories", Toast.LENGTH_SHORT).show()
            return
        }

        // Create goal object
        val goal = Goal(
            goalname = name,
            notes = "",  // No notes as per our requirements
            categoryid = selectedCategoryId,
            target = target ?: 0.0, // Ensure target is non-null, use 0.0 as fallback
            currency = "ZAR",  // Static currency as per our requirements
            userid = userId,
            progress = 0.0,  // Default progress
            completed = false, // Default to not completed
            savedAmount = 0.0 // Default savedAmount
        )

        // Save goal to database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.goalDao().insertGoal(goal)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddGoalActivity, "Goal saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AddGoalActivity,
                        "Error saving goal: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}

// _________________________________END OF FILE___________________________________