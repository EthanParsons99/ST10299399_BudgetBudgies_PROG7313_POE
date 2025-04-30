// --- START AddIncome.kt ---
package com.example.budgetbudgies_prog7313_poe

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color // Keep if needed for photo indicator tinting
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.budgetbudgies_prog7313_poe.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddIncome : AppCompatActivity() {

    private lateinit var incomeDbDao: IncomeDao
    private lateinit var expenseDbDao: ExpenseDao
    private lateinit var categoryDbDao: CategoryDao
    private lateinit var accountDbDao: AccountDao
    private var currentUserId: Int = -1

    // UI Elements
    private lateinit var radioGroupType: RadioGroup
    private lateinit var radioIncome: RadioButton
    private lateinit var radioExpense: RadioButton
    private lateinit var dateInput: EditText
    private lateinit var spinnerAccount: Spinner
    private lateinit var amountInput: EditText
    private lateinit var spinnerCurrency: Spinner
    private lateinit var spinnerCategory: Spinner
    private lateinit var descriptionInput: EditText
    private lateinit var attachLayout: LinearLayout // Clickable layout for attaching photo
    private lateinit var attachReceiptIcon: ImageView
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button // Still unused for new items

    // Data holders
    private var selectedDate: Date = Date()
    private var photoUriPath: String? = null
    private var userAccounts: List<Account> = listOf()
    private var userCategories: List<Category> = listOf()
    // Store currently displayed categories for ID lookup
    private var currentFilteredCategories: List<Category> = listOf()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
                photoUriPath = it.toString()
                Log.d("AddIncome", "Photo selected with persistent URI: $photoUriPath")
                Toast.makeText(this, "Photo attached!", Toast.LENGTH_SHORT).show()
                attachReceiptIcon.setColorFilter(ContextCompat.getColor(this, R.color.holo_green_dark)) // Green tint
            } catch (e: SecurityException) {
                Log.e("AddIncome", "Failed to get persistent permission for image URI", e)
                Toast.makeText(this, "Could not attach photo.", Toast.LENGTH_SHORT).show()
                photoUriPath = null
                attachReceiptIcon.clearColorFilter() // Remove tint
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_income_page)

        val toolbar: Toolbar = findViewById(R.id.toolbar) // Ensure toolbar ID exists in XML
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Add Transaction"

        currentUserId = SessionManager.getUserId(applicationContext)
        if (currentUserId == -1) {
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_LONG).show()
            finish(); return
        }

        initializeDaos()
        findViews()
        setupInitialState()
        setupListeners()
        loadSpinnersData()
    }

    private fun initializeDaos() {
        val db = AppDatabase.getDatabase(applicationContext)
        incomeDbDao = db.incomeDao()
        expenseDbDao = db.expenseDao()
        categoryDbDao = db.categoryDao()
        accountDbDao = db.accountDao()
    }

    private fun findViews() {
        radioGroupType = findViewById(R.id.radioGroupType)
        radioIncome = findViewById(R.id.radioIncome)
        radioExpense = findViewById(R.id.radioExpenditure)
        dateInput = findViewById(R.id.currentDate)
        spinnerAccount = findViewById(R.id.spinnerAccount)
        amountInput = findViewById(R.id.amount)
        spinnerCurrency = findViewById(R.id.spinnerCurrency)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        descriptionInput = findViewById(R.id.description)
        attachLayout = findViewById(R.id.attachLayout) // Find the LinearLayout
        attachReceiptIcon = findViewById(R.id.receiptIcon)
        saveButton = findViewById(R.id.saveButton)
        deleteButton = findViewById(R.id.deleteButton)
    }

    private fun setupInitialState() {
        updateDateLabel()
        dateInput.isFocusable = false
        dateInput.isClickable = true
        deleteButton.visibility = View.GONE
        radioIncome.isChecked = true
        updateCategorySpinnerBasedOnType() // Initial update before data loads
    }

    private fun setupListeners() {
        dateInput.setOnClickListener { showDatePickerDialog() }
        attachLayout.setOnClickListener { pickImageLauncher.launch("image/*") } // Click layout
        saveButton.setOnClickListener { saveTransaction() }
        radioGroupType.setOnCheckedChangeListener { _, _ -> updateCategorySpinnerBasedOnType() }
        setupCurrencySpinnerListener()
    }

    private fun setupCurrencySpinnerListener() {
        val currencies = listOf("ZAR", "USD", "EUR", "GBP")
        val currencyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = currencyAdapter
        val tvCurrencySelected = findViewById<TextView>(R.id.tvCurrencySelected)
        spinnerCurrency.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                tvCurrencySelected?.text = "Selected: ${spinnerCurrency.selectedItem}"
                tvCurrencySelected?.visibility = View.VISIBLE // Show it
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                tvCurrencySelected?.visibility = View.GONE // Hide it
            }
        }
        tvCurrencySelected?.visibility = View.GONE // Initially hidden
    }


    private fun loadSpinnersData() {
        lifecycleScope.launch {
            try {
                userAccounts = accountDbDao.getUserAccountsList(currentUserId)
                val accountNames = userAccounts.map { it.accountname }
                val accountAdapter = ArrayAdapter(this@AddIncome, android.R.layout.simple_spinner_item, accountNames)
                accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerAccount.adapter = accountAdapter
                Log.d("AddIncome", "Loaded ${userAccounts.size} accounts")

                userCategories = categoryDbDao.getUserCategoriesList(currentUserId)
                Log.d("AddIncome", "Loaded ${userCategories.size} categories")
                updateCategorySpinnerBasedOnType() // Update spinner with loaded data

            } catch (e: Exception) {
                Log.e("AddIncome", "Error loading spinner data", e)
                Toast.makeText(this@AddIncome, "Could not load accounts/categories", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCategorySpinnerBasedOnType() {
        val isIncome = radioIncome.isChecked
        // Ensure Category entity has 'categoryType': "Income" or "Expense"
        currentFilteredCategories = userCategories.filter {
            (it.categoryType) == if (isIncome) "Income" else "Expense"
        }
        val categoryNames = currentFilteredCategories.map { it.categoryname }

        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter
    }


    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate

        DatePickerDialog(
            this,
            { _, year, month, day ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(year, month, day)
                selectedDate = newCalendar.time
                updateDateLabel()
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateLabel() {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateInput.setText(format.format(selectedDate))
    }

    private fun saveTransaction() {
        val amountStr = amountInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val selectedAccountPos = spinnerAccount.selectedItemPosition
        val selectedCategoryPos = spinnerCategory.selectedItemPosition

        // Validation
        var amountValue: Double? = null
        if (amountStr.isEmpty()) { amountInput.error = "Need an amount!"; amountInput.requestFocus(); return }
        try { amountValue = amountStr.toDouble() }
        catch (e: NumberFormatException) { amountInput.error = "Not a valid number"; amountInput.requestFocus(); return }

        if (selectedAccountPos < 0 || userAccounts.isEmpty()) { Toast.makeText(this, "Please select an account", Toast.LENGTH_SHORT).show(); return }
        if (selectedCategoryPos < 0 || currentFilteredCategories.isEmpty()) { Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show(); return }

        // Description mandatory for Expense, optional for Income? Let's make it optional for now.
        // if (description.isEmpty() && !radioIncome.isChecked) { descriptionInput.error = "Add a description?"; descriptionInput.requestFocus(); return }

        // Get selected IDs
        val selectedAccount = userAccounts[selectedAccountPos]
        val selectedCategory = currentFilteredCategories[selectedCategoryPos]
        val transactionAmount = amountValue ?: 0.0

        lifecycleScope.launch {
            try {
                // Get database instance once
                val db = AppDatabase.getDatabase(applicationContext)
                var success = false

                if (radioIncome.isChecked) { // Use the variable checking radio button state
                    val newIncome = Income(
                        userid = currentUserId, amount = transactionAmount, date = selectedDate,
                        categoryid = selectedCategory.categoryid, accountid = selectedAccount.accountid
                    )
                    // *** CALL DATABASE TRANSACTION HELPER ***
                    db.insertIncomeAndUpdateAccount(newIncome)
                    Log.d("AddIncome", "Called insertIncomeAndUpdateAccount")
                    success = true // Assume success if no exception thrown by transaction

                } else { // Expense selected
                    val newExpense = Expense(
                        userid = currentUserId, amount = transactionAmount, date = selectedDate,
                        categoryid = selectedCategory.categoryid, description = description.ifEmpty { selectedCategory.categoryname },
                        photopath = photoUriPath, accountid = selectedAccount.accountid
                    )
                    // *** CALL DATABASE TRANSACTION HELPER ***
                    db.insertExpenseAndUpdateAccount(newExpense)
                    Log.d("AddIncome", "Called insertExpenseAndUpdateAccount with photo: $photoUriPath")
                    success = true // Assume success if no exception thrown by transaction
                }

                // Handle UI feedback after transaction attempt
                if (success) {
                    Toast.makeText(this@AddIncome, "Transaction saved!", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                // If an exception occurred in the transaction helper, the catch block below handles it

            } catch (e: Exception) { // Catch exceptions from transaction helpers
                Log.e("AddIncome", "Error saving transaction & updating balance", e)
                // Provide more specific feedback if possible based on exception type
                Toast.makeText(this@AddIncome, "Error saving: ${e.message}", Toast.LENGTH_LONG).show()
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
// --- END AddIncome.kt ---