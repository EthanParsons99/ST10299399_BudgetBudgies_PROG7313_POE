// --- START AddCategory.kt ---
package com.example.budgetbudgies_prog7313_poe

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AddCategory : AppCompatActivity() {

    private lateinit var categoryDbDao: CategoryDao
    private var currentUserId: Int = -1

    private lateinit var buttonIncome: Button
    private lateinit var buttonExpense: Button
    private lateinit var editTextCategoryName: EditText
    private lateinit var buttonAdd: Button
    private lateinit var buttonCancel: Button
    private lateinit var iconContainer: LinearLayout

    private var selectedType = "Income"
    private var selectedIconResId: Int? = null
    private var selectedImageView: ImageView? = null

    private val iconResIdList = listOf(
        R.drawable.ic_food,
        R.drawable.ic_transport,
        R.drawable.ic_salary,
        R.drawable.ic_disability,
        R.drawable.ic_cart,
        R.drawable.ic_home,
        R.drawable.ic_heartbeat,
        R.drawable.ic_gym,
        R.drawable.ic_loan
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_new_category)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Add New Category"

        currentUserId = SessionManager.getUserId(applicationContext)
        if (currentUserId == -1) {
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        categoryDbDao = AppDatabase.getDatabase(applicationContext).categoryDao()

        buttonIncome = findViewById(R.id.btn_income)
        buttonExpense = findViewById(R.id.btn_expenses)
        editTextCategoryName = findViewById(R.id.edit_category_name)
        buttonAdd = findViewById(R.id.btn_add)
        buttonCancel = findViewById(R.id.btn_cancel)
        iconContainer = findViewById(R.id.icon_container)

        setupTypeButtons()
        setupIcons()
        setupActionButtons()
        updateTypeSelectionUI()
    }

    private fun setupTypeButtons() {
        buttonIncome.setOnClickListener {
            selectedType = "Income"
            updateTypeSelectionUI()
        }
        buttonExpense.setOnClickListener {
            selectedType = "Expense"
            updateTypeSelectionUI()
        }
    }

    private fun updateTypeSelectionUI() {
        if (selectedType == "Income") {
            buttonIncome.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            buttonExpense.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        } else {
            buttonExpense.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            buttonIncome.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        }
    }

    private fun setupIcons() {
        iconResIdList.forEach { iconResId ->
            val imageView = ImageView(this).apply {
                setImageResource(iconResId)
                tag = iconResId
                layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                    setMargins(16, 8, 16, 8)
                }
                alpha = 0.5f
                setOnClickListener {
                    selectedIconResId = iconResId
                    highlightSelectedIcon(this)
                }
            }
            iconContainer.addView(imageView)
        }
    }

    private fun highlightSelectedIcon(viewToHighlight: ImageView) {
        selectedImageView?.alpha = 0.5f
        viewToHighlight.alpha = 1.0f
        selectedImageView = viewToHighlight
    }

    private fun setupActionButtons() {
        buttonAdd.setOnClickListener {
            val name = editTextCategoryName.text.toString().trim()

            if (name.isEmpty()) {
                editTextCategoryName.error = "Category name is needed"
                editTextCategoryName.requestFocus()
                return@setOnClickListener
            }

            if (selectedIconResId == null) {
                Toast.makeText(this, "Please select an icon", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newDbCategory = Category(
                userid = currentUserId,
                categoryname = name,
                categoryType = selectedType,
                icon = selectedIconResId!!
            )

            saveCategoryToDb(newDbCategory)
        }

        buttonCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun saveCategoryToDb(category: Category) {
        lifecycleScope.launch {
            try {
                val exists = categoryDbDao.categoryExists(currentUserId, category.categoryname) > 0
                if (exists) {
                    Toast.makeText(this@AddCategory, "'${category.categoryname}' already exists!", Toast.LENGTH_SHORT).show()
                    editTextCategoryName.error = "Name already taken"
                    editTextCategoryName.requestFocus()
                    return@launch
                }

                val result = categoryDbDao.insert(category)
                if (result != -1L) {
                    Log.d("AddCategory", "Category saved! ID: $result")
                    Toast.makeText(this@AddCategory, "Category '${category.categoryname}' added!", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@AddCategory, "Could not save category.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("AddCategory", "Error saving category", e)
                Toast.makeText(this@AddCategory, "Error adding category.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

// --- END AddCategory.kt ---
