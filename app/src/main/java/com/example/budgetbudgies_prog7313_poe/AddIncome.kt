// --- START AddIncome.kt (with Photo File Saving) ---
package com.example.budgetbudgies_prog7313_poe

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.budgetbudgies_prog7313_poe.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddIncome : AppCompatActivity() {

    // Launcher to request camera permisson when used
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission received , a user can access the camera
                Log.d("AddIncome", "Camera permission granted")
                openCamera()
            } else {
                // Permission denied, user cant access the camera
                Log.w("AddIncome", "Camera permission denied")
                Toast.makeText(this, "Camera permission is required to take photos.", Toast.LENGTH_SHORT).show()
            }
        }

    // Launcher to handle the result from the camera
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                // Photo was successfully taken
                if (currentPhotoUri != null) {
                    Log.d("AddIncome", "Photo captured successfully to URI: $currentPhotoUri")
                    photoUriPath = currentPhotoUri.toString() // Store the URI String
                    attachReceiptIcon.setImageURI(currentPhotoUri) // Display the captured image
                    attachReceiptIcon.setColorFilter(ContextCompat.getColor(this, R.color.holo_green_dark))
                    Toast.makeText(this, "Photo attached!", Toast.LENGTH_SHORT).show()
                } else {
                    // Photo failed
                    Log.e("AddIncome", "currentPhotoUri was null after camera returned OK")
                    Toast.makeText(this, "Failed to get photo URI.", Toast.LENGTH_SHORT).show()
                    photoUriPath = null
                    attachReceiptIcon.clearColorFilter()
                }
            } else {
                Log.w("AddIncome", "Camera activity cancelled or failed. Result Code: ${result.resultCode}")
                Toast.makeText(this, "Photo capture cancelled.", Toast.LENGTH_SHORT).show()
                photoUriPath = null // Clear path if cancelled
                attachReceiptIcon.clearColorFilter()

                if (currentPhotoUri != null) {

                }
            }
            // Clear the current URI when done
                currentPhotoUri = null
        }


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
    private var photoUriPath: String? = null
    private var currentPhotoUri: Uri? = null
    private var userAccounts: List<Account> = listOf()
    private var userCategories: List<Category> = listOf()
    private var currentFilteredCategories: List<Category> = listOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_income_page)

        // Setup the toolbar with a back button and title
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Transaction"
        // gets current user
        currentUserId = SessionManager.getUserId(applicationContext)
        if (currentUserId == -1) {
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_LONG).show()
            finish(); return // Exit if user is not logged in
        }

        initializeDaos()
        findViews()         // Bind views from the layout
        setupInitialState()
        setupListeners()
        loadSpinnersData() // Load account and category spinner data
    }
    // Initialize the database access
    private fun initializeDaos() {
        val db = AppDatabase.getDatabase(applicationContext)
        incomeDbDao = db.incomeDao()
        expenseDbDao = db.expenseDao()
        categoryDbDao = db.categoryDao()
        accountDbDao = db.accountDao()
    }
    // Links all view components from the layout file to variables
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
        // Ensure deleteButton is removed or handled if needed for edit mode later(part 3)
        findViewById<Button>(R.id.deleteButton)?.visibility = View.GONE
    }

    private fun setupInitialState() {
        updateDateLabel()  // Set current date
        dateInput.isFocusable = false
        dateInput.isClickable = true
        radioIncome.isChecked = true // Default for income
    }

    private fun setupListeners() {
        dateInput.setOnClickListener { showDatePickerDialog() }
        attachLayout.setOnClickListener { checkCameraPermissionAndOpenCamera() } // Call check function
        saveButton.setOnClickListener { saveTransaction() }
        radioGroupType.setOnCheckedChangeListener { _, _ -> updateCategorySpinnerBasedOnType() }
        setupCurrencySpinner() // populate currency options
    }

    private fun checkCameraPermissionAndOpenCamera() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted, open camera
                openCamera()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                // Explain why you need the permission (optional)
                Toast.makeText(this, "Camera permission is needed to attach receipt photos.", Toast.LENGTH_LONG).show()
                // Then request the permission
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                // Directly request the permission
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }


    // Creates a temporary file URI and launches the camera intent
    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Create the File where the photo should go
        var photoFile: File? = null
        try {
            photoFile = createImageFile()
        } catch (ex: IOException) {
            // Error occurred while creating the File
            Log.e("AddIncome", "Error creating image file", ex)
            Toast.makeText(this, "Error preparing camera.", Toast.LENGTH_SHORT).show()
            return
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            // Get the content URI using FileProvider
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider", // Matches authority in Manifest
                photoFile
            )
            currentPhotoUri = photoURI // Save the URI temporarily for onActivityResult
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI) // Tell camera to save here
            Log.d("AddIncome", "Launching camera. Output URI: $photoURI")
            takePictureLauncher.launch(cameraIntent) // Use the launcher
        }
    }

    // Creates a unique image file in the app's cache directory
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        // Get directory (use cache dir - system can clean it up)
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        // val storageDir: File? = cacheDir // Alternative: internal cache dir

        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs() // Create directory if it doesn't exist
        }
        Log.d("AddIncome", "Storage directory: ${storageDir?.absolutePath}")

        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir     /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents -- this isn't used here directly
            // currentPhotoPath = absolutePath
            Log.d("AddIncome", "Created image file: ${absolutePath}")
        }
    }

    // onActivityResult is handled by the takePictureLauncher defined earlier


    private fun setupCurrencySpinner() {
        val currencies = listOf("ZAR", "USD", "EUR", "GBP", "JPY", "AUD")
        val currencyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = currencyAdapter
        // Optional: Add listener or default selection
    }
    // Loads user accounts and categories into the respective spinner
    private fun loadSpinnersData() {
        lifecycleScope.launch {
            try {
                // Fetchs user from db
                userAccounts = accountDbDao.getUserAccountsList(currentUserId)

               // Prepare a list of account names with a default "Select Account" prompt

                val accountDisplayNames = mutableListOf<String>("Select Account")
                userAccounts.mapTo(accountDisplayNames) { it.accountname }
                val accountAdapter = ArrayAdapter(this@AddIncome, android.R.layout.simple_spinner_item, accountDisplayNames)
                accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerAccount.adapter = accountAdapter

                userCategories = categoryDbDao.getUserCategoriesList(currentUserId)
                updateCategorySpinnerBasedOnType() // Update spinner with loaded categories
                //error handling
            } catch (e: Exception) {
                Log.e("AddIncome", "Error loading spinner data", e)
                Toast.makeText(this@AddIncome, "Failed to load accounts/categories.", Toast.LENGTH_SHORT).show()
            }
        }
    }
// Depending what the user selects a certain list of categorys display
    private fun updateCategorySpinnerBasedOnType() {
        val isIncome = radioIncome.isChecked
        currentFilteredCategories = userCategories.filter {
            (it.categoryType) == if (isIncome) "Income" else "Expense"
        }
        val categoryDisplayNames = mutableListOf<String>("Select Category")
        currentFilteredCategories.mapTo(categoryDisplayNames) { it.categoryname }

        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryDisplayNames)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter
    }

//Dialog for the date selection
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        DatePickerDialog(this, { _, year, month, day ->
            val newCalendar = Calendar.getInstance(); newCalendar.set(year, month, day)
            selectedDate = newCalendar.time
            updateDateLabel()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
// for the date
    private fun updateDateLabel() {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateInput.setText(format.format(selectedDate))
    }
//To save the transaction
    private fun saveTransaction() {
        val amountStr = amountInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val selectedAccountPos = spinnerAccount.selectedItemPosition
        val selectedCategoryPos = spinnerCategory.selectedItemPosition

        // Validation
        val amountValue = amountStr.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) { amountInput.error = "Enter a valid positive amount"; amountInput.requestFocus(); return }
        if (selectedAccountPos <= 0) { Toast.makeText(this, "Please select an account", Toast.LENGTH_SHORT).show(); return }
        if (selectedCategoryPos <= 0) { Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show(); return }

        // Get Correct Selected Objects
        val selectedAccount = userAccounts[selectedAccountPos - 1]
        val selectedCategory = currentFilteredCategories[selectedCategoryPos - 1]

        // Use the photoUriPath stored when camera returned OK
        val finalPhotoPath = photoUriPath // Use the class member variable

        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                var success = false
                // Check if the user selected "Income"
                if (radioIncome.isChecked) {
                    // Create a new Income object with entered details
                    val newIncome = Income(
                        amount = amountValue, date = selectedDate, userid = currentUserId,
                        categoryid = selectedCategory.categoryid, accountid = selectedAccount.accountid
                    )
                    // Insert income and update account balance
                    db.insertIncomeAndUpdateAccount(newIncome)
                    Log.d("AddIncome", "Saved Income")
                    success = true
                } else {
                    // Create a new Expense
                    val newExpense = Expense(
                        amount = amountValue, date = selectedDate, userid = currentUserId,
                        categoryid = selectedCategory.categoryid,
                        description = description.ifEmpty { selectedCategory.categoryname },
                        photopath = finalPhotoPath, // *** Use the stored photo URI string ***
                        accountid = selectedAccount.accountid
                    )
                    // Insert expense and update account balance
                    db.insertExpenseAndUpdateAccount(newExpense)
                    Log.d("AddIncome", "Saved Expense with photo: $finalPhotoPath")
                    success = true
                }
                // Tells user and close activity if saving was successful
                if (success) {
                    Toast.makeText(this@AddIncome, "Transaction saved!", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }

            } catch (e: Exception) {
                Log.e("AddIncome", "Error saving transaction & updating balance", e)
                Toast.makeText(this@AddIncome, "Error saving: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    // Handles action bar item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish() // Close this activity and return to the previous one
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
// --- END AddIncome.kt (with Photo File Saving) ---