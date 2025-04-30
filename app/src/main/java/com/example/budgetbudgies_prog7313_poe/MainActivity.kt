package com.example.budgetbudgies_prog7313_poe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.budgetbudgies_prog7313_poe.databinding.ActivityMainWithNavDrawerBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // View Binding
    private lateinit var binding: ActivityMainWithNavDrawerBinding

    // Database DAOs
    private lateinit var expenseDao: ExpenseDao
    private lateinit var incomeDao: IncomeDao
    private lateinit var db: AppDatabase

    // Date variables
    private var currentYear: Int = 0
    private var currentMonth: Int = 0

    private val ADD_TRANSACTION_REQUEST_CODE = 123 // Request code for adding transaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Call edge-to-edge function

        // Initialize View Binding
        binding = ActivityMainWithNavDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Database
        initializeDatabase()

        // Set up Navigation Drawer
        setupNavigationDrawer()

        // Set up Month Navigation
        setupMonthNavigation()

        // Set up Bottom Navigation
        setupBottomNavigation()

        // Set up Floating Action Button
        setupFabAddTransaction()

        // Initial Data Load
        getCurrentDate()
        updateMonthYearTextView()
        fetchMonthlyData()
    }

    // Initialize Database
    private fun initializeDatabase() {
        db = AppDatabase.getDatabase(applicationContext)
        expenseDao = db.expenseDao()
        incomeDao = db.incomeDao()
    }

    // Set up Navigation Drawer
    private fun setupNavigationDrawer() {
        setSupportActionBar(binding.navToolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            binding.HomePage, // Using the actual ID from your layout
            binding.navToolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.HomePage.addDrawerListener(toggle) // Using the actual ID from your layout
        toggle.syncState()

        binding.navView.bringToFront()
        binding.navView.setNavigationItemSelectedListener(this)
    }

    // Set up Month Navigation
    private fun setupMonthNavigation() {
        binding.prevMonthButton.setOnClickListener {
            currentMonth--
            if (currentMonth < 0) {
                currentMonth = 11
                currentYear--
            }
            updateMonthYearTextView()
            fetchMonthlyData()
        }

        binding.nextMonthButton.setOnClickListener {
            currentMonth++
            if (currentMonth > 11) {
                currentMonth = 0
                currentYear++
            }
            updateMonthYearTextView()
            fetchMonthlyData()
        }
    }

    // Set up Bottom Navigation
    private fun setupBottomNavigation() {
        val analyticsBtn = findViewById<ImageButton>(R.id.analyticsbtn)
        analyticsBtn?.setOnClickListener {
            navigateToPlaceholder("Analytics")
            overridePendingTransition(0, 0)
        }

        val progressBtn = findViewById<ImageButton>(R.id.progressbtn)
        progressBtn?.setOnClickListener {
            navigateToPlaceholder("Progress Dashboard")
            overridePendingTransition(0, 0)
        }

        val budgetsBtn = findViewById<ImageButton>(R.id.budgetsbtn)
        budgetsBtn?.setOnClickListener {
            Toast.makeText(this, "Already on Home/Budgets", Toast.LENGTH_SHORT).show()
        }

        val goalsBtn = findViewById<ImageButton>(R.id.goalsbtn)
        goalsBtn?.setOnClickListener {
            navigateToPlaceholder("Goals")
            overridePendingTransition(0, 0)
        }

        val accountBtn = findViewById<ImageButton>(R.id.accountbtn)
        accountBtn?.setOnClickListener {
            startActivity(Intent(this, AccountActivity::class.java))
            overridePendingTransition(0, 0)
        }
    }

    private fun navigateToPlaceholder(featureName: String) {
        val intent = Intent(this, PlaceholderActivity::class.java)
        intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, featureName)
        startActivity(intent)
    }

    // Set up Floating Action Button
    private fun setupFabAddTransaction() {
        binding.fabAddTransaction.setOnClickListener {
            val intent = Intent(this, AddIncome::class.java)
            startActivityForResult(intent, ADD_TRANSACTION_REQUEST_CODE)
        }
    }

    // Handle the result from AddTransactionActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_TRANSACTION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Refresh data after adding a transaction
                fetchMonthlyData()
            }
        }
    }

    // Get the current date and set the initial month and year
    private fun getCurrentDate() {
        val calendar = Calendar.getInstance()
        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH) // Month is 0-indexed (0 = January, 11 = December)
    }

    // Update the monthYearTextView with the current month and year
    private fun updateMonthYearTextView() {
        val sdf = SimpleDateFormat("MMMM, yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.set(currentYear, currentMonth, 1) // Set to the first day of the month
        binding.monthYearTextView.text = sdf.format(cal.time)
    }

    // Fetch the expenses and income data for the current month and update the UI
    private fun fetchMonthlyData() {
        val startDate = Calendar.getInstance()
        startDate.set(currentYear, currentMonth, 1, 0, 0, 0) // Start of the month (00:00:00)
        val endDate = Calendar.getInstance()
        endDate.set(currentYear, currentMonth + 1, 1, 0, 0, 0) // Start of the NEXT month
        endDate.add(Calendar.DAY_OF_MONTH, -1) // Go back one day to get the last day of the current month

        val startDateObj = startDate.time
        val endDateObj = endDate.time

        val userId = 1 // Replace with the actual user ID (e.g., get it from SharedPreferences or a logged-in user)

        lifecycleScope.launch(Dispatchers.IO) {
            val expenses = expenseDao.getUserExpensesListInRange(userId, startDateObj, endDateObj)
            val incomes = incomeDao.getUserIncomesListInRange(userId, startDateObj, endDateObj)

            val totalExpenses = expenses.sumOf { it.amount.toDouble() }
            val totalIncome = incomes.sumOf { it.amount.toDouble() }
            val total = totalIncome - totalExpenses

            withContext(Dispatchers.Main) {
                binding.expensesTextView.text = String.format("R %.2f", totalExpenses)
                binding.incomeTextView.text = String.format("R %.2f", totalIncome)
                binding.totalTextView.text = String.format("R %.2f", total)

                // Log the data for debugging
                Log.d("MainActivity", "Expenses: $totalExpenses, Income: $totalIncome, Total: $total")
            }
        }
    }

    // Handle navigation drawer item clicks
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var intent: Intent? = null

        when (item.title.toString()) {
            "Achievements" -> {
                intent = Intent(this, PlaceholderActivity::class.java)
                intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, "Achievements")
            }
            "Categorties" -> {
                intent = Intent(this, CategoryActivity::class.java)
                intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, "Categories")
            }
            "Currency" -> {
                intent = Intent(this, PlaceholderActivity::class.java)
                intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, "Currency")
            }
            "Settings" -> {
                intent = Intent(this, PlaceholderActivity::class.java)
                intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, "Settings")
            }
            "Support" -> {
                intent = Intent(this, PlaceholderActivity::class.java)
                intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, "Support")
            }
            "Login" -> {
                intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            "Sign Up" -> {
                intent = Intent(this, SignUpActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            "Home Page (Testing Purposes)" -> {
                Toast.makeText(this, "Already on Home Page", Toast.LENGTH_SHORT).show()
                intent = null
            }
            else -> {
                Toast.makeText(this, "Unknown item clicked", Toast.LENGTH_SHORT).show()
                intent = null
            }
        }

        intent?.let {
            startActivity(it)
        }

        binding.HomePage.closeDrawer(GravityCompat.START)
        return true
    }

    // Handle back button press
    override fun onBackPressed() {
        if (binding.HomePage.isDrawerOpen(GravityCompat.START)) {
            binding.HomePage.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}