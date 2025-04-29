package com.example.budgetbudgies_prog7313_poe

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetbudgies_prog7313_poe.AccountDao
import com.example.budgetbudgies_prog7313_poe.AppDatabase
import kotlinx.coroutines.launch

class AddAccount : AppCompatActivity() {

    private lateinit var accountDbDao: AccountDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_account_page)

        accountDbDao = AppDatabase.getDatabase(applicationContext).accountDao()

        val accountNameInput = findViewById<EditText>(R.id.accname)
        val accountAmountInput = findViewById<EditText>(R.id.accamount)
        val accountIconInput = findViewById<EditText>(R.id.accicon)
        val accountTypeInput = findViewById<EditText>(R.id.acctype)
        val accountColorInput = findViewById<EditText>(R.id.acccolor)
        val accountNotesInput = findViewById<EditText>(R.id.accnotes)
        val addButton = findViewById<Button>(R.id.button2)
        val cancelButton = findViewById<Button>(R.id.button3)
        val backButton = findViewById<Button>(R.id.backtoaccountbtn)

        addButton.setOnClickListener {
            val name = accountNameInput.text.toString().trim()
            val amountStr = accountAmountInput.text.toString().trim()
            val icon = accountIconInput.text.toString().trim()
            val type = accountTypeInput.text.toString().trim()
            val color = accountColorInput.text.toString().trim()
            val notes = accountNotesInput.text.toString().trim()

            // --- Validation ---
            if (name.isEmpty()) {
                accountNameInput.error = "Account needs a name!"
                accountNameInput.requestFocus()
                return@setOnClickListener
            }
            if (amountStr.isEmpty()) {
                accountAmountInput.error = "Please enter a starting balance"
                accountAmountInput.requestFocus()
                return@setOnClickListener
            }

            var balance: Double? = null
            try {
                balance = amountStr.toDouble()
            } catch (e: NumberFormatException) {
                accountAmountInput.error = "That doesn't look like a valid number"
                accountAmountInput.requestFocus()
                return@setOnClickListener
            }


            val currentUserId = 1
            val currency = "ZAR"

            val newAccount = Account(
                userid = currentUserId,
                accountname = name,
                accounttype = type,
                balance = balance ?: 0.0,
                currency = currency
            )

            lifecycleScope.launch {
                try {
                    val result = accountDbDao.insertAccount(newAccount)
                    if (result != -1L) {
                        Log.d("AddAccount", "Account saved! ID: $result")
                        Toast.makeText(this@AddAccount, "Account added!", Toast.LENGTH_SHORT).show()
                        finish() // Go back to the previous screen
                    } else {
                        Log.w("AddAccount", "Failed to save account (maybe conflict).")
                        Toast.makeText(this@AddAccount, "Couldn't save account.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("AddAccount", "DB error saving account", e)
                    Toast.makeText(this@AddAccount, "Error adding account.", Toast.LENGTH_SHORT).show()
                }
            }
            // --- ---
        }

        cancelButton.setOnClickListener {
            finish()
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}