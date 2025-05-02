// --- START of CategorySummaryActivity.kt ---

package com.example.budgetbudgies_prog7313_poe
//Imports
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CategorySummaryActivity : AppCompatActivity() {

    // UI components
    private lateinit var categorySpendingRecyclerView: RecyclerView
    private lateinit var adapter: CategorySpendingAdapter
    private lateinit var monthYearTextView: TextView

    // Current selected year and month
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH)

    // DAO to access expense data
    private lateinit var expenseDao: ExpenseDao

    // User ID for data filtering
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.category_summary)

        // Initialize views from layout
        monthYearTextView = findViewById(R.id.monthYearTextView)
        categorySpendingRecyclerView = findViewById(R.id.categorySpendingRecyclerView)

        // Setup RecyclerView with adapter
        adapter = CategorySpendingAdapter()
        categorySpendingRecyclerView.layoutManager = LinearLayoutManager(this)
        categorySpendingRecyclerView.adapter = adapter

        // Get DAO and user ID from session
        expenseDao = AppDatabase.getDatabase(applicationContext).expenseDao()
        userId = SessionManager.getUserId(applicationContext)

        // Set click listeners for previous and next month buttons
        findViewById<ImageButton>(R.id.prevMonthButton).setOnClickListener { navigateMonth(-1) }
        findViewById<ImageButton>(R.id.nextMonthButton).setOnClickListener { navigateMonth(1) }

        // Display current month and load summary data
        updateMonthYearTextView()
        loadCategorySpending()
    }

    // Navigate between months (delta: -1 for previous, 1 for next)
    private fun navigateMonth(delta: Int) {
        val cal = Calendar.getInstance().apply {
            set(currentYear, currentMonth, 1)
            add(Calendar.MONTH, delta)
        }
        currentYear = cal.get(Calendar.YEAR)
        currentMonth = cal.get(Calendar.MONTH)
        updateMonthYearTextView()
        loadCategorySpending()
    }

    // Update the text displaying the current month and year
    private fun updateMonthYearTextView() {
        val calendar = Calendar.getInstance().apply { set(currentYear, currentMonth, 1) }
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthYearTextView.text = sdf.format(calendar.time)
    }

    // Load and group expenses by category for the selected month
    private fun loadCategorySpending() {
        // Calculate start and end of the selected month
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

        // Get category DAO
        val categoryDao = AppDatabase.getDatabase(applicationContext).categoryDao()

        // Perform database operations in background thread
        lifecycleScope.launch(Dispatchers.IO) {
            // Fetch expenses in date range
            val expenses = expenseDao.getUserExpensesListInRange(userId, startDate, endDate)

            // Fetch and map categories by ID
            val categories = categoryDao.getUserCategoriesList(userId).associateBy { it.categoryid }

            // Group expenses by category and compute total per category
            val grouped = expenses.groupBy { it.categoryid }.mapNotNull { (catId, list) ->
                val category = categories[catId] ?: return@mapNotNull null
                CategorySpending(
                    categoryName = category.categoryname,
                    totalAmount = list.sumOf { it.amount },
                    iconResId = category.icon
                )
            }.sortedByDescending { it.totalAmount } // Sort by amount spent

            // Update UI on the main thread
            launch(Dispatchers.Main) {
                adapter.submitList(grouped)
            }
        }
    }
}
// _________________________________END OF FILE___________________________________