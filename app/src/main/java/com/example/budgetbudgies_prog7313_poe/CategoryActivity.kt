package com.example.budgetbudgies_prog7313_poe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CategoryActivity : AppCompatActivity() {

    private lateinit var incomeAdapter: CategoryAdapter
    private lateinit var expenseAdapter: CategoryAdapter
//Splits the income and expense into differnat categories
    private val incomeCategories = mutableListOf<kCategory>()
    private val expenseCategories = mutableListOf<kCategory>()

    private val addCategoryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val newCategory = result.data?.getParcelableExtra<kCategory>("NEW_CATEGORY")
            if (newCategory != null) {
                if (newCategory.type == "Income") {
                    incomeCategories.add(newCategory)
                    incomeAdapter.notifyItemInserted(incomeCategories.size - 1)
                } else {
                    expenseCategories.add(newCategory)
                    expenseAdapter.notifyItemInserted(expenseCategories.size - 1)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.category_page)

        val addCategoryBtn = findViewById<Button>(R.id.btn_add_category)
        val recyclerViewIncome = findViewById<RecyclerView>(R.id.recyclerViewIncome)
        val recyclerViewExpense = findViewById<RecyclerView>(R.id.recyclerViewExpense)

        // Setup Income RecyclerView
        recyclerViewIncome.layoutManager = LinearLayoutManager(this)
        incomeAdapter = CategoryAdapter(incomeCategories) { categoryToDelete ->
            val index = incomeCategories.indexOf(categoryToDelete)
            incomeCategories.remove(categoryToDelete)
            incomeAdapter.notifyItemRemoved(index)
        }
        recyclerViewIncome.adapter = incomeAdapter

        // Setup Expense RecyclerView
        recyclerViewExpense.layoutManager = LinearLayoutManager(this)
        expenseAdapter = CategoryAdapter(expenseCategories) { categoryToDelete ->
            val index = expenseCategories.indexOf(categoryToDelete)
            expenseCategories.remove(categoryToDelete)
            expenseAdapter.notifyItemRemoved(index)
        }
        recyclerViewExpense.adapter = expenseAdapter

        incomeCategories.add(kCategory("Income", "Salary", R.drawable.ic_salary))
        incomeAdapter.notifyItemInserted(incomeCategories.size - 1)

        expenseCategories.add(kCategory("Expense", "Food", R.drawable.ic_food))
        expenseAdapter.notifyItemInserted(expenseCategories.size - 1)

        // Launch add category screen
        addCategoryBtn.setOnClickListener {
            val intent = Intent(this, AddCategory::class.java)
            addCategoryLauncher.launch(intent)
        }
    }
}
