// --- START AddAccount.kt ---
package com.example.budgetbudgies_prog7313_poe

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.budgetbudgies_prog7313_poe.AccountDao
import com.example.budgetbudgies_prog7313_poe.AppDatabase
import kotlinx.coroutines.launch

class AddAccount : AppCompatActivity() {

    private lateinit var accountDbDao: AccountDao
    private var currentUserId: Int = -1

    private val bankNames = listOf("Select Bank", "FNB", "Absa", "Capitec", "Nedbank", "Standard Bank", "TymeBank", "Other")
    private val colorNames = listOf("Select Color", "Default", "Blue", "Green", "Red", "Purple", "Orange", "Grey")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_account_page)

        val toolbar: Toolbar = findViewById(R.id.toolbar) // Ensure toolbar ID exists in XML
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Add New Account"

        currentUserId = SessionManager.getUserId(applicationContext)
        if (currentUserId == -1) {
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        accountDbDao = AppDatabase.getDatabase(applicationContext).accountDao()

        val accountNameInput = findViewById<EditText>(R.id.accname)
        val accountAmountInput = findViewById<EditText>(R.id.accamount)
        val spinnerIcon = findViewById<Spinner>(R.id.spinnerAccountIcon)
        val accountTypeInput = findViewById<EditText>(R.id.acctype)
        val spinnerColor = findViewById<Spinner>(R.id.spinnerAccountColor)
        val accountNotesInput = findViewById<EditText>(R.id.accnotes)
        val addButton = findViewById<Button>(R.id.AddNewCatergory)
        val cancelButton = findViewById<Button>(R.id.button3)

        val iconAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bankNames)
        iconAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerIcon.adapter = iconAdapter

        val colorAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, colorNames)
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerColor.adapter = colorAdapter

        addButton.setOnClickListener {
            val name = accountNameInput.text.toString().trim()
            val amountStr = accountAmountInput.text.toString().trim()
            val selectedIconName = if (spinnerIcon.selectedItemPosition > 0) spinnerIcon.selectedItem.toString() else null
            val type = accountTypeInput.text.toString().trim()
            val selectedColorName = if (spinnerColor.selectedItemPosition > 0) spinnerColor.selectedItem.toString() else null
            val notes = accountNotesInput.text.toString().trim()

            if (name.isEmpty()) {
                accountNameInput.error = "Account needs a name!"
                accountNameInput.requestFocus(); return@setOnClickListener
            }
            if (amountStr.isEmpty()) {
                accountAmountInput.error = "Please enter a starting balance";
                accountAmountInput.requestFocus(); return@setOnClickListener
            }
            var balance: Double? = null
            try {
                balance = amountStr.toDouble()
            } catch (e: NumberFormatException) {
                accountAmountInput.error = "That doesn't look like a valid number";
                accountAmountInput.requestFocus(); return@setOnClickListener
            }

            val currency = "ZAR"

            val newAccount = Account(
                userid = currentUserId,
                accountname = name,
                accounttype = type.ifEmpty { null },
                balance = balance ?: 0.0,
                currency = currency,
                accountColor = selectedColorName,
                accountIconName = selectedIconName
            )

            lifecycleScope.launch {
                try {
                    val result = accountDbDao.insertAccount(newAccount)
                    if (result != -1L) {
                        Log.d("AddAccount", "Account saved! ID: $result")
                        Toast.makeText(this@AddAccount, "Account '$name' added!", Toast.LENGTH_SHORT).show()
                        finish() // Go back after successful save
                    } else {
                        Log.w("AddAccount", "Failed to save account.")
                        Toast.makeText(this@AddAccount, "Couldn't save account.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("AddAccount", "DB error saving account", e)
                    Toast.makeText(this@AddAccount, "Error adding account.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        cancelButton.setOnClickListener { finish() }
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
// --- END AddAccount.kt ---