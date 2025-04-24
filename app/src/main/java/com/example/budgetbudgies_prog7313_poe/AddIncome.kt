package com.example.budgetbudgies_prog7313_poe
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddIncome : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_income_page)

        val radioGroupType = findViewById<RadioGroup>(R.id.radioGroupType)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val spinnerAccount = findViewById<Spinner>(R.id.spinnerAccount)


        val accountOptions = listOf("Account 1", "Account 2", "Account 3")
        val accountAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, accountOptions)
        spinnerAccount.adapter = accountAdapter


        val incomeCategories = listOf("Salary", "Gift", "Interest", "Other")
        val expenseCategories = listOf("Food", "Transport", "Rent", "Bills", "Entertainment")


        fun updateCategorySpinner(isIncome: Boolean) {
            val categories = if (isIncome) incomeCategories else expenseCategories
            val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
            spinnerCategory.adapter = categoryAdapter
        }

        updateCategorySpinner(true)


        radioGroupType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioIncome -> updateCategorySpinner(true)
                R.id.radioExpenditure -> updateCategorySpinner(false)
            }
        }
    }
}
