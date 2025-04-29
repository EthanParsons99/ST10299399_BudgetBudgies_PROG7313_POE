package com.example.budgetbudgies_prog7313_poe

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AddTransactionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        //  TODO: Add UI elements and logic for adding transactions
        //  For example, EditTexts for amount, description, etc.,
        //  Spinners for category, account, etc., and a Save button.

        //  When the user saves, you would:
        //  1. Get the data from the UI elements.
        //  2. Create an Expense or Income object.
        //  3. Insert it into the database using ExpenseDao or IncomeDao.
        //  4. Set the result and finish the activity:
        //  setResult(Activity.RESULT_OK)
        //  finish()
    }
}