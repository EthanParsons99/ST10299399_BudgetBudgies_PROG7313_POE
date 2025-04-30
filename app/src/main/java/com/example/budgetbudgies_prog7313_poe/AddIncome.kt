// --- START AddIncome.kt (with Spinner Prompts) ---
package com.example.budgetbudgies_prog7313_poe

import android.Manifest // Import Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.icu.util.Calendar // Import ICU Calendar
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log // Import Log
import android.view.MenuItem
import android.view.View // Import View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.budgetbudgies_prog7313_poe.* // Import local data package
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddIncome : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 123

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
    private lateinit var attachLayout: LinearLayout
    private lateinit var attachReceiptIcon: ImageView
    private lateinit var saveButton: Button

    // Data holders
    private var selectedDate: Date = Date()
    private var userAccounts: List<Account> = listOf()
    private var userCategories: List<Category> = listOf()
    private var currentFilteredCategories: List<Category> = listOf()
    private var capturedPhotoBitmap: Bitmap? = null // Store captured bitmap temporarily

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_income_page)

        val toolbar: Toolbar = findViewById(R.id.toolbar) // Ensure toolbar ID exists
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
        loadSpinnersData() // Load data including prompts
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
        attachLayout = findViewById(R.id.attachLayout)
        attachReceiptIcon = findViewById(R.id.receiptIcon)
        saveButton = findViewById(R.id.saveButton)
    }

    private fun setupInitialState() {
        updateDateLabel()
        dateInput.isFocusable = false
        dateInput.isClickable = true
        radioIncome.isChecked = true
        // Category spinner initially updated in loadSpinnersData
    }

    private fun setupListeners() {
        dateInput.setOnClickListener { showDatePickerDialog() }

        attachLayout.setOnClickListener {
            // Check for camera permission before opening
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE
                )
            }
        }

        saveButton.setOnClickListener { saveTransaction() }
        radioGroupType.setOnCheckedChangeListener { _, _ -> updateCategorySpinnerBasedOnType() }
        setupCurrencySpinner() // Setup currency spinner separately
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        DatePickerDialog(this, { _, year, month, day ->
            val newCalendar = Calendar.getInstance(); newCalendar.set(year, month, day)
            selectedDate = newCalendar.time
            updateDateLabel()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateLabel() {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateInput.setText(format.format(selectedDate))
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Check if there's an app to handle the camera intent
        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        } else {
            Toast.makeText(this, "No camera app found.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera() // Permission granted, open camera
            } else {
                Toast.makeText(this, "Camera permission is required to take photos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Get the bitmap (thumbnail) - NOTE: This is often low quality
            capturedPhotoBitmap = data?.extras?.get("data") as? Bitmap
            if (capturedPhotoBitmap != null) {
                attachReceiptIcon.setImageBitmap(capturedPhotoBitmap) // Show thumbnail
                Toast.makeText(this, "Photo captured! Ready to save.", Toast.LENGTH_SHORT).show()
                attachReceiptIcon.setColorFilter(ContextCompat.getColor(this, R.color.holo_green_dark))
                // TODO: Implement saving this bitmap to a file to get a persistent path/URI
                // For now, we only have the bitmap in memory. The actual 'photopath' saved will be null.
            } else {
                Toast.makeText(this, "Failed to capture photo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCurrencySpinner() {
        val currencies = listOf("ZAR", "USD", "EUR", "GBP", "JPY", "AUD") // Example list
        val currencyAdapter = ArrayAdapter(this@AddIncome, android.R.layout.simple_spinner_item, currencies)
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = currencyAdapter
    }

    private fun loadSpinnersData() {
        lifecycleScope.launch {
            try {
                // --- Load Accounts with Prompt ---
                userAccounts = accountDbDao.getUserAccountsList(currentUserId)
                val accountDisplayNames = mutableListOf<String>("Select Account") // Add prompt
                userAccounts.mapTo(accountDisplayNames) { it.accountname }
                val accountAdapter = ArrayAdapter(this@AddIncome, android.R.layout.simple_spinner_item, accountDisplayNames)
                accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerAccount.adapter = accountAdapter
                Log.d("AddIncome", "Loaded Accounts + Prompt. Display size: ${accountDisplayNames.size}")
                // --- End Account Load ---

                // --- Load Categories & Update Spinner ---
                userCategories = categoryDbDao.getUserCategoriesList(currentUserId)
                Log.d("AddIncome", "Loaded ${userCategories.size} total categories")
                updateCategorySpinnerBasedOnType() // Initial update with prompt
                // --- End Category Load ---

            } catch (e: Exception) {
                Log.e("AddIncome", "Error loading spinner data", e)
                Toast.makeText(this@AddIncome, "Failed to load accounts/categories.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCategorySpinnerBasedOnType() {
        val isIncome = radioIncome.isChecked
        currentFilteredCategories = userCategories.filter {
            // Ensure Category entity has categoryType: String ("Income" or "Expense")
            (it.categoryType) == if (isIncome) "Income" else "Expense"
        }

        // --- Add Prompt to Category List ---
        val categoryDisplayNames = mutableListOf<String>("Select Category") // Add prompt
        currentFilteredCategories.mapTo(categoryDisplayNames) { it.categoryname }
        // --- End Add Prompt ---

        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryDisplayNames)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter
        Log.d("AddIncome", "Updated category spinner. IsIncome: $isIncome. Display size: ${categoryDisplayNames.size}")
    }

    private fun saveTransaction() {
        val amountStr = amountInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val selectedAccountPos = spinnerAccount.selectedItemPosition
        val selectedCategoryPos = spinnerCategory.selectedItemPosition

        // --- Validation ---
        val amountValue = amountStr.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) { // Amount must be positive
            amountInput.error = "Enter a valid positive amount"
            amountInput.requestFocus(); return
        }
        if (selectedAccountPos <= 0) { // Check if prompt "Select Account" is selected
            Toast.makeText(this, "Please select an account", Toast.LENGTH_SHORT).show(); return
        }
        if (selectedCategoryPos <= 0) { // Check if prompt "Select Category" is selected
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show(); return
        }
        // Description is optional for income, potentially required for expense
        if (description.isEmpty() && !radioIncome.isChecked) {
            // Optionally force description for expense, or use category name as default
            // descriptionInput.error = "Description required for expenses"; descriptionInput.requestFocus(); return
        }
        // --- End Validation ---

        // --- Get Correct Selected Objects ---
        // Subtract 1 from position because index 0 is the prompt
        val selectedAccount = userAccounts[selectedAccountPos - 1]
        val selectedCategory = currentFilteredCategories[selectedCategoryPos - 1]
        // --- End Get Objects ---

        // --- TODO: Handle Photo Saving ---
        // The current camera logic only gets a Bitmap thumbnail in onActivityResult.
        // To save a proper path, you need to:
        // 1. Modify openCamera() to specify a file URI using FileProvider for ACTION_IMAGE_CAPTURE
        // 2. In onActivityResult, get the URI of the *full-sized saved image* (not just the 'data' bitmap).
        // 3. Store that URI's String representation in a variable (like photoUriPath)
        // 4. Use that variable when creating the Expense object below.
        // For now, photopath will be null.
        val photoPathToSave: String? = null // Placeholder until proper file saving is implemented
        // --- End Photo Handling ---


        lifecycleScope.launch {
            try {
                // Use the AppDatabase transaction helpers for atomicity
                val db = AppDatabase.getDatabase(applicationContext)
                var success = false

                if (radioIncome.isChecked) {
                    val newIncome = Income(
                        amount = amountValue, date = selectedDate, userid = currentUserId,
                        categoryid = selectedCategory.categoryid, accountid = selectedAccount.accountid
                    )
                    db.insertIncomeAndUpdateAccount(newIncome) // Call transaction helper
                    Log.d("AddIncome", "Called insertIncomeAndUpdateAccount")
                    success = true
                } else {
                    val newExpense = Expense(
                        amount = amountValue, date = selectedDate, userid = currentUserId,
                        categoryid = selectedCategory.categoryid,
                        description = description.ifEmpty { selectedCategory.categoryname }, // Default description
                        photopath = photoPathToSave, // Use the variable holding the saved photo path/URI
                        accountid = selectedAccount.accountid
                    )
                    db.insertExpenseAndUpdateAccount(newExpense) // Call transaction helper
                    Log.d("AddIncome", "Called insertExpenseAndUpdateAccount with photo: $photoPathToSave")
                    success = true
                }

                if (success) {
                    Toast.makeText(this@AddIncome, "Transaction saved!", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                // Errors inside transaction helpers will throw exceptions caught below

            } catch (e: Exception) {
                Log.e("AddIncome", "Error saving transaction & updating balance", e)
                Toast.makeText(this@AddIncome, "Error saving: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish() // Handle Up button
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
// --- END AddIncome.kt (with Spinner Prompts) ---