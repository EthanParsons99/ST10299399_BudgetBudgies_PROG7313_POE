package com.example.budgetbudgies_prog7313_poe

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddIncome : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_income_page)

        val radioGroupType = findViewById<RadioGroup>(R.id.radioGroupType)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val spinnerAccount = findViewById<Spinner>(R.id.spinnerAccount)
        val spinnerCurrency = findViewById<Spinner>(R.id.spinnerCurrency)
        val tvCurrencySelected = findViewById<TextView>(R.id.tvCurrencySelected)

        // Populate account spinner
        val accountOptions = listOf("FNB", "Absa", "Capitec")
        val accountAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, accountOptions)
        spinnerAccount.adapter = accountAdapter

        // Category options
        val incomeCategories = listOf("Salary", "Gift", "Interest", "Other")
        val expenseCategories = listOf("Food", "Transport", "Rent", "Bills", "Entertainment")

        // Update categories based on selected type
        fun updateCategorySpinner(isIncome: Boolean) {
            val categories = if (isIncome) incomeCategories else expenseCategories
            val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
            spinnerCategory.adapter = categoryAdapter
        }

        // Default to income
        updateCategorySpinner(true)

        radioGroupType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioIncome -> updateCategorySpinner(true)
                R.id.radioExpenditure -> updateCategorySpinner(false)
            }
        }

        // Currency spinner setup
        val currencies = listOf("ZAR", "USD", "EUR", "GBP", "JPY", "AUD")
        val currencyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = currencyAdapter

        // Currency selection listener
        spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedCurrency = parent.getItemAtPosition(position).toString()
                tvCurrencySelected.text = "Selected: $selectedCurrency"
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                tvCurrencySelected.text = "Selected: None" +
                        "Please select a currency"
            }
        }
    }
}