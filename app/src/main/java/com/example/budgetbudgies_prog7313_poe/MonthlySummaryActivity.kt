package com.example.budgetbudgies_prog7313_poe

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// Data class to represent spending per category
data class CategorySpending(
    val categoryName: String,
    val totalAmount: Double,
    val iconResId: Int? = null
)

class MonthlySummaryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategorySpendingAdapter
    private lateinit var monthYearTextView: TextView

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

        // Set up RecyclerView with adapter
        adapter = CategorySpendingAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Initialize DAOs
        expenseDao = AppDatabase.getDatabase(applicationContext).expenseDao()
        categoryDao = AppDatabase.getDatabase(applicationContext).categoryDao()

        // Get data from Intent
        userId = intent.getIntExtra("userId", -1)
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
        }
    }
}
