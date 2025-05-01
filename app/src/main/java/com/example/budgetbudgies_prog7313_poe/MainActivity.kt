package com.example.budgetbudgies_prog7313_poe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbudgies_prog7313_poe.databinding.ActivityMainWithNavDrawerBinding
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainWithNavDrawerBinding

    // DAOs for accessing the database
    private lateinit var expenseDao: ExpenseDao
    private lateinit var incomeDao: IncomeDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var accountDao: AccountDao

    private var currentUserId: Int = -1
    private var currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH)

    private lateinit var transactionRecyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter

    private val ADD_TRANSACTION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check user login session
        currentUserId = SessionManager.getUserId(applicationContext)
        if (currentUserId == -1) {
            goToLogin()
            return
        }

        binding = ActivityMainWithNavDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeDatabase()
        setupNavigationDrawer()
        setupMonthNavigation()
        setupBottomNavigation()
        setupFabAddTransaction()
        setupRecyclerView()

        updateMonthYearTextView()
        fetchAndDisplayData()
    }

    private fun initializeDatabase() {
        val db = AppDatabase.getDatabase(applicationContext)
        expenseDao = db.expenseDao()
        incomeDao = db.incomeDao()
        categoryDao = db.categoryDao()
        accountDao = db.accountDao()
    }

    private fun setupNavigationDrawer() {
        setSupportActionBar(binding.navToolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            binding.HomePage,
            binding.navToolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.HomePage.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)
    }

    private fun setupMonthNavigation() {
        binding.prevMonthButton.setOnClickListener { navigateMonth(-1) }
        binding.nextMonthButton.setOnClickListener { navigateMonth(1) }
    }

    private fun navigateMonth(monthDelta: Int) {
        val cal = Calendar.getInstance()
        cal.set(currentYear, currentMonth, 1)
        cal.add(Calendar.MONTH, monthDelta)
        currentYear = cal.get(Calendar.YEAR)
        currentMonth = cal.get(Calendar.MONTH)
        updateMonthYearTextView()
        fetchAndDisplayData()
    }

    private fun setupBottomNavigation() {
        // Set up click listeners for bottom nav icons
        findViewById<ImageButton>(R.id.analyticsbtn)?.setOnClickListener { navigateToPlaceholder("Analytics"); overridePendingTransition(0, 0) }
        findViewById<ImageButton>(R.id.progressbtn)?.setOnClickListener { navigateToPlaceholder("Progress Dashboard"); overridePendingTransition(0, 0) }
        findViewById<ImageButton>(R.id.budgetsbtn)?.setOnClickListener { Toast.makeText(this, "You're already Home!", Toast.LENGTH_SHORT).show() }
        findViewById<ImageButton>(R.id.goalsbtn)?.setOnClickListener { startActivity(Intent(this, GoalsActivity::class.java)); overridePendingTransition(0, 0) }
        findViewById<ImageButton>(R.id.accountbtn)?.setOnClickListener { startActivity(Intent(this, AccountActivity::class.java)); overridePendingTransition(0, 0) }
    }

    private fun setupFabAddTransaction() {
        binding.fabAddTransaction.setOnClickListener {
            val intent = Intent(this, AddIncome::class.java)
            startActivityForResult(intent, ADD_TRANSACTION_REQUEST_CODE)
        }
    }

    private fun setupRecyclerView() {
        // Configure transaction RecyclerView
        transactionRecyclerView = binding.transactionsRecyclerView
        transactionAdapter = TransactionAdapter { transaction ->
            Toast.makeText(this, "Clicked: ${transaction.description}", Toast.LENGTH_SHORT).show()
            // View photo if expense has image
            if (transaction.type == "Expense" && transaction.photoPath != null) {
                viewPhoto(transaction.photoPath)
            }
        }
        transactionRecyclerView.layoutManager = LinearLayoutManager(this)
        transactionRecyclerView.adapter = transactionAdapter
    }

    private fun viewPhoto(photoPath: String) {
        // Open attached photo in viewer app
        try {
            val photoUri = Uri.parse(photoPath)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(photoUri, "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "No app found to view images", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error viewing photo: $photoPath", e)
            Toast.makeText(this, "Could not open photo", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Refresh transactions after adding new one
        if (requestCode == ADD_TRANSACTION_REQUEST_CODE && resultCode == RESULT_OK) {
            fetchAndDisplayData()
        }
    }

    private fun updateMonthYearTextView() {
        // Update month/year label
        val sdf = SimpleDateFormat("MMMM, yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.set(currentYear, currentMonth, 1)
        binding.monthYearTextView.text = sdf.format(cal.time)
    }

    private fun fetchAndDisplayData() {
        if (currentUserId == -1) return

        // Calculate date range
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

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get data from DB
                val expenses = expenseDao.getUserExpensesListInRange(currentUserId, startDate, endDate)
                val incomes = incomeDao.getUserIncomesListInRange(currentUserId, startDate, endDate)
                val categories = categoryDao.getUserCategoriesList(currentUserId).associateBy { it.categoryid }
                val accounts = accountDao.getUserAccountsList(currentUserId).associateBy { it.accountid }

                val totalExpenses = expenses.sumOf { it.amount }
                val totalIncome = incomes.sumOf { it.amount }
                val total = totalIncome - totalExpenses

                val displayList = mutableListOf<DisplayTransaction>()

                // Map incomes to display model
                incomes.mapTo(displayList) { income ->
                    val category = categories[income.categoryid]
                    DisplayTransaction(
                        id = income.incomeid,
                        type = "Income",
                        description = category?.categoryname ?: "Income",
                        amount = income.amount,
                        date = income.date,
                        categoryName = category?.categoryname,
                        accountName = accounts[income.accountid]?.accountname,
                        icon = category?.icon,
                        photoPath = null
                    )
                }

                // Map expenses to display model
                expenses.mapTo(displayList) { expense ->
                    val category = categories[expense.categoryid]
                    DisplayTransaction(
                        id = expense.expenseid,
                        type = "Expense",
                        description = expense.description,
                        amount = expense.amount,
                        date = expense.date,
                        categoryName = category?.categoryname,
                        accountName = accounts[expense.accountid]?.accountname,
                        icon = category?.icon,
                        photoPath = expense.photopath
                    )
                }

                // Sort by date
                displayList.sortByDescending { it.date }

                withContext(Dispatchers.Main) {
                    // Update UI
                    binding.expensesTextView.text = String.format(Locale.US, "R %.2f", totalExpenses)
                    binding.incomeTextView.text = String.format(Locale.US, "R %.2f", totalIncome)
                    binding.totalTextView.text = String.format(Locale.US, "R %.2f", total)
                    transactionAdapter.submitList(displayList)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching data", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Could not load data", Toast.LENGTH_SHORT).show()
                    binding.expensesTextView.text = "R -.--"
                    binding.incomeTextView.text = "R -.--"
                    binding.totalTextView.text = "R -.--"
                    transactionAdapter.submitList(emptyList())
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val clickedTitle = item.title.toString()
        Log.d("SideNav", "Item Clicked: Title='$clickedTitle'")

        // Handle navigation menu clicks
        val intent: Intent? = when (clickedTitle) {
            "Achievements {WIP Work in progress}" -> Intent(this, PlaceholderActivity::class.java).apply {
                putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, "Achievements")
            }
            "Categories" -> Intent(this, CategoryActivity::class.java)
            "Category Summary" -> Intent(this, MonthlySummaryActivity::class.java).apply {
                putExtra("userId", currentUserId)
                putExtra("year", currentYear)
                putExtra("month", currentMonth)
            }
            "Currency {WIP}" -> Intent(this, PlaceholderActivity::class.java).apply {
                putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, "Currency")
            }

            "Sign Out" -> {
                SessionManager.clearSession(applicationContext)
                goToLogin()
                null
            }
            else -> {
                Toast.makeText(this, "Option not ready yet!", Toast.LENGTH_SHORT).show()
                null
            }
        }

        intent?.let { startActivity(it) }
        binding.HomePage.closeDrawer(GravityCompat.START)
        return true
    }

    private fun navigateToPlaceholder(featureName: String) {
        val intent = Intent(this, PlaceholderActivity::class.java)
        intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, featureName)
        startActivity(intent)
    }

    private fun goToLogin() {
        // Redirect to login and clear task stack
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Close drawer on back press if open
        if (binding.HomePage.isDrawerOpen(GravityCompat.START)) {
            binding.HomePage.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}

// --- END MainActivity.kt (Reverted Base + Integrated Changes) ---