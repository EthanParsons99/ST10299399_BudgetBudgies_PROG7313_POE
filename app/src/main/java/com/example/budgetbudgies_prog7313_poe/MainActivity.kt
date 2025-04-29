package com.example.budgetbudgies_prog7313_poe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // View Binding (if you're using it, otherwise, use findViewById)
    // private lateinit var binding: ActivityMainWithNavDrawerBinding

    // UI elements
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var navToolbar: androidx.appcompat.widget.Toolbar
    private lateinit var menuButton: ImageButton
    private lateinit var budgetNameTextView: TextView
    private lateinit var prevMonthButton: ImageButton
    private lateinit var monthYearTextView: TextView
    private lateinit var nextMonthButton: ImageButton
    private lateinit var expensesTextView: TextView
    private lateinit var incomeTextView: TextView
    private lateinit var totalTextView: TextView
    private lateinit var fabAddTransaction: FloatingActionButton
    private lateinit var analyticsBtn: ImageButton
    private lateinit var progressBtn: ImageButton
    private lateinit var budgetsBtn: ImageButton
    private lateinit var goalsBtn: ImageButton
    private lateinit var accountBtn: ImageButton

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
        setContentView(R.layout.activity_main_with_nav_drawer)

        // Initialize UI elements
        initializeViews()

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

    // Initialize UI elements
    private fun initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navToolbar = findViewById(R.id.nav_toolbar)
        menuButton = findViewById(R.id.menuButton)
        budgetNameTextView = findViewById(R.id.budgetNameTextView)
        prevMonthButton = findViewById(R.id.prevMonthButton)
        monthYearTextView = findViewById(R.id.monthYearTextView)
        nextMonthButton = findViewById(R.id.nextMonthButton)
        expensesTextView = findViewById(R.id.expensesTextView)
        incomeTextView = findViewById(R.id.incomeTextView)
        totalTextView = findViewById(R.id.totalTextView)
        fabAddTransaction = findViewById(R.id.fabAddTransaction)
        analyticsBtn = findViewById(R.id.analyticsbtn)
        progressBtn = findViewById(R.id.progressbtn)
        budgetsBtn = findViewById(R.id.budgetsbtn)
        goalsBtn = findViewById(R.id.goalsbtn)
        accountBtn = findViewById(R.id.accountbtn)
    }

    // Initialize Database
    private fun initializeDatabase() {
        db = AppDatabase.getDatabase(applicationContext)
        expenseDao = db.expenseDao()
        incomeDao = db.incomeDao()
    }

    // Set up Navigation Drawer
    private fun setupNavigationDrawer() {
        setSupportActionBar(navToolbar)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            navToolbar,
            R.string.navigation_drawer_open,  // Make sure these string resources exist
            R.string.navigation_drawer_close // and are correctly defined in your strings.xml
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    // Set up Month Navigation
    private fun setupMonthNavigation() {
        prevMonthButton.setOnClickListener {
            currentMonth--
            if (currentMonth < 0) {
                currentMonth = 11
                currentYear--
            }
            updateMonthYearTextView()
            fetchMonthlyData()
        }

        nextMonthButton.setOnClickListener {
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
        analyticsBtn.setOnClickListener { navigateToPlaceholder("Analytics") }
        progressBtn.setOnClickListener { navigateToPlaceholder("Progress Dashboard") }
        budgetsBtn.setOnClickListener { Toast.makeText(this, "Already on Home/Budgets", Toast.LENGTH_SHORT).show() }
        goalsBtn.setOnClickListener { navigateToPlaceholder("Goals") }
        accountBtn.setOnClickListener { startActivity(Intent(this, AccountActivity::class.java)) }
    }

    private fun navigateToPlaceholder(featureName: String) {
        val intent = Intent(this, PlaceholderActivity::class.java)
        intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, featureName)
        startActivity(intent)
    }

    // Set up Floating Action Button
    private fun setupFabAddTransaction() {
        fabAddTransaction.setOnClickListener {
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
        monthYearTextView.text = sdf.format(cal.time)
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
                expensesTextView.text = String.format("R %.2f", totalExpenses)
                incomeTextView.text = String.format("R %.2f", totalIncome)
                totalTextView.text = String.format("R %.2f", total)

                // Log the data for debugging
                Log.d("MainActivity", "Expenses: $totalExpenses, Income: $totalIncome, Total: $total")
            }
        }
    }

    // Handle navigation drawer item clicks
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var intent: Intent? = null

        when (item.itemId) {
            R.id.nav_achievements -> {
                intent = navigateToPlaceholderIntent("Achievements")
            }
            R.id.nav_categories -> {
                intent = Intent(this, CategoryActivity::class.java)
            }
            R.id.nav_currency -> {
                intent = navigateToPlaceholderIntent("Currency")
            }
            R.id.nav_settings -> {
                intent = navigateToPlaceholderIntent("Settings")
            }
            R.id.nav_support -> {
                intent = navigateToPlaceholderIntent("Support")
            }
            R.id.nav_login -> {
                intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            R.id.nav_signup -> {
                intent = Intent(this, SignUpActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            R.id.nav_home -> {
                Toast.makeText(this, "Already on Home", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Unknown item", Toast.LENGTH_SHORT).show()
            }
        }

        intent?.let { startActivity(it) }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun navigateToPlaceholderIntent(featureName: String): Intent {
        val intent = Intent(this, PlaceholderActivity::class.java)
        intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, featureName)
        return intent
    }

    // Handle back button press
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}