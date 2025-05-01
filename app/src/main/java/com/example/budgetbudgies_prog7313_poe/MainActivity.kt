// --- START MainActivity.kt (Reverted Base + Integrated Changes) ---
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
// import androidx.drawerlayout.widget.DrawerLayout // Not explicitly needed if using binding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbudgies_prog7313_poe.databinding.ActivityMainWithNavDrawerBinding // Using ViewBinding
// import com.google.android.material.floatingactionbutton.FloatingActionButton // Not explicitly needed if using binding
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import com.example.budgetbudgies_prog7313_poe.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // View Binding
    private lateinit var binding: ActivityMainWithNavDrawerBinding

    // Database DAOs
    private lateinit var expenseDao: ExpenseDao
    private lateinit var incomeDao: IncomeDao
    private lateinit var categoryDao: CategoryDao // Added
    private lateinit var accountDao: AccountDao // Added
    private lateinit var GoalDao: GoalDao // Added
    // private lateinit var db: AppDatabase // Don't need the whole DB instance usually

    // Session/User ID
    private var currentUserId: Int = -1

    // Date variables
    private var currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH)

    // RecyclerView
    private lateinit var transactionRecyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter

    private val ADD_TRANSACTION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // --- Check Login Status ---
        currentUserId = SessionManager.getUserId(applicationContext)
        if (currentUserId == -1) {
            goToLogin() // Redirect to login if not logged in
            return
        }
        // --- ---

        // Initialize View Binding
        binding = ActivityMainWithNavDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Database DAOs
        initializeDatabase()

        // Set up UI Components
        setupNavigationDrawer() // Includes setSupportActionBar
        setupMonthNavigation()
        setupBottomNavigation()
        setupFabAddTransaction()
        setupRecyclerView() // Setup RecyclerView

        // Initial Data Load
        updateMonthYearTextView()
        fetchAndDisplayData() // Fetch totals AND list data
    }

    // Initialize Database DAOs
    private fun initializeDatabase() {
        val db = AppDatabase.getDatabase(applicationContext)
        expenseDao = db.expenseDao()
        incomeDao = db.incomeDao()
        categoryDao = db.categoryDao() // Init needed DAOs
        accountDao = db.accountDao()   // Init needed DAOs
    }

    // Set up Navigation Drawer
    private fun setupNavigationDrawer() {
        setSupportActionBar(binding.navToolbar) // Set the toolbar first

        val toggle = ActionBarDrawerToggle(
            this,
            binding.HomePage, // DrawerLayout ID from XML
            binding.navToolbar, // Toolbar ID
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.HomePage.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this) // Set the listener
    }

    // Set up Month Navigation
    private fun setupMonthNavigation() {
        binding.prevMonthButton.setOnClickListener { navigateMonth(-1) }
        binding.nextMonthButton.setOnClickListener { navigateMonth(1) }
    }

    private fun navigateMonth(monthDelta: Int) {
        val cal = Calendar.getInstance(); cal.set(currentYear, currentMonth, 1)
        cal.add(Calendar.MONTH, monthDelta)
        currentYear = cal.get(Calendar.YEAR)
        currentMonth = cal.get(Calendar.MONTH)
        updateMonthYearTextView()
        fetchAndDisplayData() // Refresh data
    }


    // Set up Bottom Navigation (using findViewById based on older working version)
    private fun setupBottomNavigation() {
        findViewById<ImageButton>(R.id.analyticsbtn)?.setOnClickListener { navigateToPlaceholder("Analytics"); overridePendingTransition(0,0) }
        findViewById<ImageButton>(R.id.progressbtn)?.setOnClickListener { navigateToPlaceholder("Progress Dashboard"); overridePendingTransition(0,0) }
        findViewById<ImageButton>(R.id.budgetsbtn)?.setOnClickListener { Toast.makeText(this, "You're already Home!", Toast.LENGTH_SHORT).show() }
        findViewById<ImageButton>(R.id.goalsbtn)?.setOnClickListener { startActivity(Intent(this, GoalsActivity::class.java)); overridePendingTransition(0,0) }
        findViewById<ImageButton>(R.id.accountbtn)?.setOnClickListener { startActivity(Intent(this, AccountActivity::class.java)); overridePendingTransition(0,0) }
    }

    // Set up Floating Action Button
    private fun setupFabAddTransaction() {
        binding.fabAddTransaction.setOnClickListener {
            val intent = Intent(this, AddIncome::class.java) // Assuming AddIncome handles both
            startActivityForResult(intent, ADD_TRANSACTION_REQUEST_CODE)
        }
    }

    // Setup RecyclerView
    private fun setupRecyclerView() {
        transactionRecyclerView = binding.transactionsRecyclerView // Use ViewBinding reference
        transactionAdapter = TransactionAdapter { transaction ->
            // Handle transaction click
            Toast.makeText(this, "Clicked: ${transaction.description}", Toast.LENGTH_SHORT).show()
            if (transaction.type == "Expense" && transaction.photoPath != null) {
                viewPhoto(transaction.photoPath)
            }
        }
        transactionRecyclerView.layoutManager = LinearLayoutManager(this)
        transactionRecyclerView.adapter = transactionAdapter
    }

    private fun viewPhoto(photoPath: String) {
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

    // Handle the result from AddTransactionActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_TRANSACTION_REQUEST_CODE && resultCode == RESULT_OK) {
            fetchAndDisplayData() // Refresh data
        }
    }

    // Update the monthYearTextView
    private fun updateMonthYearTextView() {
        val sdf = SimpleDateFormat("MMMM, yyyy", Locale.getDefault())
        val cal = Calendar.getInstance(); cal.set(currentYear, currentMonth, 1)
        binding.monthYearTextView.text = sdf.format(cal.time)
    }

    // Fetch totals AND transaction list
    private fun fetchAndDisplayData() {
        if (currentUserId == -1) return // Check user ID validity

        val calStart = Calendar.getInstance(); calStart.set(currentYear, currentMonth, 1, 0, 0, 0); calStart.set(Calendar.MILLISECOND, 0)
        val startDate = calStart.time
        val calEnd = Calendar.getInstance(); calEnd.time = startDate; calEnd.add(Calendar.MONTH, 1); calEnd.add(Calendar.MILLISECOND, -1)
        val endDate = calEnd.time

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val expenses = expenseDao.getUserExpensesListInRange(currentUserId, startDate, endDate)
                val incomes = incomeDao.getUserIncomesListInRange(currentUserId, startDate, endDate)
                val categories = categoryDao.getUserCategoriesList(currentUserId).associateBy { it.categoryid }
                val accounts = accountDao.getUserAccountsList(currentUserId).associateBy { it.accountid }

                val totalExpenses = expenses.sumOf { it.amount }
                val totalIncome = incomes.sumOf { it.amount }
                val total = totalIncome - totalExpenses

                val displayList = mutableListOf<DisplayTransaction>()
                incomes.mapTo(displayList) { income ->
                    val category = categories[income.categoryid]
                    DisplayTransaction(id = income.incomeid, type = "Income", description = category?.categoryname ?: "Income", amount = income.amount, date = income.date, categoryName = category?.categoryname, accountName = accounts[income.accountid]?.accountname, icon = category?.icon,photoPath = null)
                }
                expenses.mapTo(displayList) { expense ->
                    val category = categories[expense.categoryid]
                    DisplayTransaction(id = expense.expenseid, type = "Expense", description = expense.description, amount = expense.amount, date = expense.date, categoryName = category?.categoryname, accountName = accounts[expense.accountid]?.accountname, icon = category?.icon, photoPath = expense.photopath)
                }
                displayList.sortByDescending { it.date }

                withContext(Dispatchers.Main) {
                    binding.expensesTextView.text = String.format(Locale.US,"R %.2f", totalExpenses)
                    binding.incomeTextView.text = String.format(Locale.US,"R %.2f", totalIncome)
                    binding.totalTextView.text = String.format(Locale.US,"R %.2f", total)
                    transactionAdapter.submitList(displayList) // SUBMIT LIST TO ADAPTER
                    Log.d("MainActivity", "Fetched Month Data: E=%.2f, I=%.2f, T=%.2f, Items=${displayList.size}".format(totalExpenses, totalIncome, total))
                }
            } catch(e: Exception) {
                Log.e("MainActivity", "Error fetching data", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Could not load data", Toast.LENGTH_SHORT).show()
                    binding.expensesTextView.text = "R -.--"
                    binding.incomeTextView.text = "R -.--"
                    binding.totalTextView.text = "R -.--"
                    transactionAdapter.submitList(emptyList()) // Clear list
                }
            }
        }
    }

    // Handle navigation drawer item clicks (Using structure from older working version)
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val intent: Intent? // Declare intent variable locally
        val clickedTitle = item.title.toString() // Get title
        Log.d("SideNav", "Item Clicked: Title='$clickedTitle'") // Log click

        when (clickedTitle) {
            // --- Match Titles EXACTLY From navigation_menu.xml ---
            "Achievements {WIP Work in progress}" -> {
                intent = Intent(this, PlaceholderActivity::class.java)
                intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, "Achievements")
            }
            "Categories" -> { // Use Corrected Title
                intent = Intent(this, CategoryActivity::class.java)
            }
            "Currency {WIP}" -> {
                intent = Intent(this, PlaceholderActivity::class.java)
                intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, "Currency")
            }
            "Support {WIP}" -> {
                intent = Intent(this, PlaceholderActivity::class.java)
                intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, "Support")
            }
            "Login" -> {
                // Handle logout - Use helper function for clarity and flags
                SessionManager.clearSession(applicationContext)
                goToLogin() // This calls finish() and starts LoginActivity
                intent = null // Prevent double navigation
            }
            "Sign Up" -> {
                intent = Intent(this, SignUpActivity::class.java)
                // Decide on flags if needed
                // intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            "Sign Out" -> {
                // Handle logout - Use helper function for clarity and flags
                SessionManager.clearSession(applicationContext)
                goToLogin() // This calls finish() and starts LoginActivity
                intent = null // Prevent double navigation
            }
            else -> {
                Log.w("SideNav", "Unknown item title: $clickedTitle")
                Toast.makeText(this, "Option not ready yet!", Toast.LENGTH_SHORT).show()
                intent = null
            }
        }

        // Start activity only if intent was created
        intent?.let {
            startActivity(it)
        }

        binding.HomePage.closeDrawer(GravityCompat.START) // Close drawer
        return true // Indicate handled
    }

    // Helper function to navigate to PlaceholderActivity
    private fun navigateToPlaceholder(featureName: String) {
        Log.d("Navigation", "Navigating to Placeholder: $featureName")
        val intent = Intent(this, PlaceholderActivity::class.java)
        intent.putExtra(PlaceholderActivity.EXTRA_FEATURE_NAME, featureName)
        startActivity(intent)
    }

    // Helper function to navigate to LoginActivity (handles logout)
    private fun goToLogin() {
        Log.d("Navigation", "Navigating to Login Activity (Logout/SessionExpired)")
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Close MainActivity
    }

    // Handle back button press for Drawer
    override fun onBackPressed() {
        if (binding.HomePage.isDrawerOpen(GravityCompat.START)) {
            binding.HomePage.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
// --- END MainActivity.kt (Reverted Base + Integrated Changes) ---