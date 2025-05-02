package com.example.budgetbudgies_prog7313_poe

import android.os.Bundle
import android.view.MenuItem // Import MenuItem
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast // Import Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar // Import Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// Data class to represent spending per category (Keep this if not already in a separate file)
data class CategorySpending(
    val categoryName: String,
    val totalAmount: Double,
    val iconResId: Int? = null
)

class MonthlySummaryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategorySpendingAdapter
    private lateinit var monthYearTextView: TextView
    private lateinit var toolbar: Toolbar // Added Toolbar variable

    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var userId = -1

    private lateinit var expenseDao: ExpenseDao
    private lateinit var categoryDao: CategoryDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.category_summary)

        // Initialize views
        recyclerView = findViewById(R.id.categorySpendingRecyclerView)
        monthYearTextView = findViewById(R.id.monthYearTextView)
        toolbar = findViewById(R.id.nav_toolbar) // Find the Toolbar

        // *** Set up the Toolbar ***
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Show back arrow
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Category Spending Summary" // Set title

        // Set up RecyclerView with adapter
        adapter = CategorySpendingAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Initialize DAOs
        expenseDao = AppDatabase.getDatabase(applicationContext).expenseDao()
        categoryDao = AppDatabase.getDatabase(applicationContext).categoryDao()

        // Get data from Intent or SessionManager (Using SessionManager is better)
        userId = SessionManager.getUserId(applicationContext) // Get userId from SessionManager
        // userId = intent.getIntExtra("userId", -1) // Keep if you specifically pass it via Intent

        // Check if user ID is valid
        if (userId == -1) {
            Toast.makeText(this, "Error: User not logged in.", Toast.LENGTH_LONG).show()
            finish() // Close activity if user not logged in
            return
        }

        // Get potential year/month from Intent, otherwise use current
        currentYear = intent.getIntExtra("year", currentYear)
        currentMonth = intent.getIntExtra("month", currentMonth)

        // Set listeners for month navigation buttons
        findViewById<ImageButton>(R.id.prevMonthButton).setOnClickListener {
            navigateMonth(-1)
        }
        findViewById<ImageButton>(R.id.nextMonthButton).setOnClickListener {
            navigateMonth(1)
        }

        updateMonthDisplay()
        loadCategorySpending()
    }

    // Change month and reload data
    private fun navigateMonth(delta: Int) {
        val cal = Calendar.getInstance().apply {
            set(currentYear, currentMonth, 1)
            add(Calendar.MONTH, delta)
        }
        currentYear = cal.get(Calendar.YEAR)
        currentMonth = cal.get(Calendar.MONTH)
        updateMonthDisplay()
        loadCategorySpending()
    }

    // Display current month and year
    private fun updateMonthDisplay() {
        val cal = Calendar.getInstance().apply {
            set(currentYear, currentMonth, 1)
        }
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthYearTextView.text = sdf.format(cal.time)
    }

    // Load spending per category for the selected month
    private fun loadCategorySpending() {
        if (userId == -1) return // Don't load if userId is invalid

        val calStart = Calendar.getInstance().apply {
            set(currentYear, currentMonth, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startDate = calStart.time

        val calEnd = Calendar.getInstance().apply {
            time = startDate
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }
        val endDate = calEnd.time

        // Fetch data from database on background thread
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val expenses = expenseDao.getUserExpensesListInRange(userId, startDate, endDate)
                val categories = categoryDao.getUserCategoriesList(userId).associateBy { it.categoryid }

                // Group expenses by category and calculate totals
                val grouped = expenses.groupBy { it.categoryid }.mapNotNull { (catId, list) ->
                    val category = categories[catId] ?: return@mapNotNull null
                    CategorySpending(
                        categoryName = category.categoryname,
                        totalAmount = list.sumOf { it.amount },
                        iconResId = category.icon
                    )
                }.sortedByDescending { it.totalAmount }

                // Update UI with results
                withContext(Dispatchers.Main) {
                    adapter.submitList(grouped)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MonthlySummaryActivity, "Error loading summary.", Toast.LENGTH_SHORT).show()
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