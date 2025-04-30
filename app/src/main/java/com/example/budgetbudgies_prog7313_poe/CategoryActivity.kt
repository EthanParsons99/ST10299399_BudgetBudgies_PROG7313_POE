// --- START CategoryActivity.kt ---
package com.example.budgetbudgies_prog7313_poe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoryActivity : AppCompatActivity() {

    private lateinit var categoryDbDao: CategoryDao
    private var currentUserId: Int = -1

    private lateinit var incomeAdapter: DbCategoryAdapter
    private lateinit var expenseAdapter: DbCategoryAdapter
    private lateinit var recyclerViewIncome: RecyclerView
    private lateinit var recyclerViewExpense: RecyclerView

    private val addCategoryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d("CategoryActivity", "Returned from AddCategory, Flow should update list.")
            // List updates via Flow, no manual add needed here
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.category_page)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Manage Categories"

        currentUserId = SessionManager.getUserId(applicationContext)
        if (currentUserId == -1) {
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        categoryDbDao = AppDatabase.getDatabase(applicationContext).categoryDao()

        val addCategoryBtn = findViewById<Button>(R.id.btn_add_category)
        recyclerViewIncome = findViewById(R.id.recyclerViewIncome)
        recyclerViewExpense = findViewById(R.id.recyclerViewExpense)

        setupRecyclerViews()

        insertDefaultCategoriesIfEmpty()

        observeCategories()

        addCategoryBtn.setOnClickListener {
            val intent = Intent(this, AddCategory::class.java)
            addCategoryLauncher.launch(intent)
        }
    }

    //adding default categories
    private fun insertDefaultCategoriesIfEmpty() {
        lifecycleScope.launch {
            val existingCategories = categoryDbDao.getUserCategoriesOnce(currentUserId)
            if (existingCategories.isEmpty()) {
                val defaultCategories = listOf(
                    Category(0, currentUserId, "Salary", "Income", R.drawable.ic_salary),
                    Category(0, currentUserId, "Food", "Expense", R.drawable.ic_food),
                    Category(0, currentUserId, "Gift", "Income", R.drawable.ic_cart),
                    Category(0, currentUserId, "Transport", "Expense", R.drawable.ic_transport)
                )
                defaultCategories.forEach {
                    categoryDbDao.insert(it)
                }
                Log.d("CategoryActivity", "Inserted default categories")
            }
        }
    }

    private fun setupRecyclerViews() {
        incomeAdapter = DbCategoryAdapter { categoryToDelete -> deleteCategory(categoryToDelete) }
        recyclerViewIncome.layoutManager = LinearLayoutManager(this)
        recyclerViewIncome.adapter = incomeAdapter

        expenseAdapter = DbCategoryAdapter { categoryToDelete -> deleteCategory(categoryToDelete) }
        recyclerViewExpense.layoutManager = LinearLayoutManager(this)
        recyclerViewExpense.adapter = expenseAdapter
    }

    private fun observeCategories() {
        lifecycleScope.launch {

            categoryDbDao.getUserCategories(currentUserId).collectLatest { categories ->
                val incomeList = categories.filter { it.categoryType == "Income" }
                val expenseList = categories.filter { it.categoryType == "Expense" }

                incomeAdapter.submitList(incomeList)
                expenseAdapter.submitList(expenseList)

                Log.d("CategoryActivity", "Updated category lists: ${incomeList.size} Income, ${expenseList.size} Expense")
            }
        }
    }

    private fun deleteCategory(category: Category) {
        lifecycleScope.launch {
            try {
                categoryDbDao.deleteCategory(category) // Assumes deleteCategory method exists in DAO
                Toast.makeText(this@CategoryActivity, "Category '${category.categoryname}' deleted", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("CategoryActivity", "Error deleting category ${category.categoryid}", e)
                Toast.makeText(this@CategoryActivity, "Error deleting category", Toast.LENGTH_SHORT).show()
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
// --- END CategoryActivity.kt ---