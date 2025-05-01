//-- Start of ADDINCOME.kt --//
package com.example.budgetbudgies_prog7313_poe

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
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

    private var selectedDate: Date = Date()
    private var userAccounts: List<Account> = listOf()
    private var userCategories: List<Category> = listOf()
    private var currentFilteredCategories: List<Category> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_income_page)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
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
        attachLayout = findViewById(R.id.attachLayout)
        attachReceiptIcon = findViewById(R.id.receiptIcon)
        saveButton = findViewById(R.id.saveButton)
    }

    private fun setupInitialState() {
        updateDateLabel()
        dateInput.isFocusable = false
        dateInput.isClickable = true
        radioIncome.isChecked = true
    }

    private fun setupListeners() {
        dateInput.setOnClickListener { showDatePickerDialog() }

        attachLayout.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE
                )
            }
        }

        saveButton.setOnClickListener { saveTransaction() }
        radioGroupType.setOnCheckedChangeListener { _, _ -> updateCategorySpinnerBasedOnType() }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        DatePickerDialog(this, { _, year, month, day ->
            val newCalendar = Calendar.getInstance()
            newCalendar.set(year, month, day)
            selectedDate = newCalendar.time
            updateDateLabel()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateLabel() {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateInput.setText(format.format(selectedDate))
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val photo = data?.extras?.get("data") as? Bitmap
            if (photo != null) {
                attachReceiptIcon.setImageBitmap(photo)
                Toast.makeText(this, "Photo captured!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSpinnersData() {
        lifecycleScope.launch {
            try {
                userAccounts = accountDbDao.getUserAccountsList(currentUserId)
                val accountNames = userAccounts.map { it.accountname }
                spinnerAccount.adapter = ArrayAdapter(this@AddIncome, android.R.layout.simple_spinner_item, accountNames)

                userCategories = categoryDbDao.getUserCategoriesList(currentUserId)
                updateCategorySpinnerBasedOnType()
            } catch (e: Exception) {
                Toast.makeText(this@AddIncome, "Failed to load accounts/categories.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCategorySpinnerBasedOnType() {
        val isIncome = radioIncome.isChecked
        currentFilteredCategories = userCategories.filter {
            it.categoryType == if (isIncome) "Income" else "Expense"
        }
        val categoryNames = currentFilteredCategories.map { it.categoryname }
        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
    }

    private fun saveTransaction() {
        val amountStr = amountInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val selectedAccountPos = spinnerAccount.selectedItemPosition
        val selectedCategoryPos = spinnerCategory.selectedItemPosition

        val amountValue = amountStr.toDoubleOrNull()
        if (amountValue == null) {
            amountInput.error = "Enter a valid amount"
            return
        }
        if (selectedAccountPos < 0 || selectedCategoryPos < 0) {
            Toast.makeText(this, "Select account and category", Toast.LENGTH_SHORT).show()
            return
        }

        val account = userAccounts[selectedAccountPos]
        val category = currentFilteredCategories[selectedCategoryPos]

        lifecycleScope.launch {
            val result = if (radioIncome.isChecked) {
                val income = Income(
                    amount = amountValue,
                    date = selectedDate,
                    userid = currentUserId,
                    categoryid = category.categoryid,
                    accountid = account.accountid
                )
                incomeDbDao.insertIncome(income)
            } else {
                val expense = Expense(
                    amount = amountValue,
                    date = selectedDate,
                    userid = currentUserId,
                    categoryid = category.categoryid,
                    description = description,
                    photopath = null,
                    accountid = account.accountid
                )
                expenseDbDao.insertExpense(expense)
            }

            if (result != -1L) {
                Toast.makeText(this@AddIncome, "Transaction saved!", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                Toast.makeText(this@AddIncome, "Save failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
// --- END AddIncome.kt ---