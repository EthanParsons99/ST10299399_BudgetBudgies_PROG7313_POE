// --- START AccountActivity.kt ---
package com.example.budgetbudgies_prog7313_poe

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

import kotlinx.coroutines.launch
import java.util.Locale

//
class AccountActivity : AppCompatActivity() {

    // Initialize the dao
    private lateinit var accountDbDao: AccountDao
    private var currentUserId: Int = -1

    // Initialize views
    private lateinit var totalBalanceTextView: TextView
    private lateinit var accountsRecyclerView: RecyclerView
    private lateinit var accountAdapter: AccountAdapter

    // Check if user has logged in if not an error message displays
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_page)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Accounts"

        currentUserId = SessionManager.getUserId(applicationContext)
        if (currentUserId == -1) {
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        accountDbDao = AppDatabase.getDatabase(applicationContext).accountDao()
        totalBalanceTextView = findViewById(R.id.totalbalance)
        accountsRecyclerView = findViewById(R.id.accountsRecyclerView)

        // Add button logic
        val addAccountBtn = findViewById<Button>(R.id.addAccountbtn)
        addAccountBtn.setOnClickListener {
            val intent = Intent(this, AddAccount::class.java)
            startActivity(intent)
        }

        setupRecyclerView()
        loadAccountsAndBalance()
    }

    // Reload data when returning to the screen
    override fun onResume() {
        super.onResume()
        if (currentUserId != -1) {
            loadAccountsAndBalance() // Reload data when returning to the screen
        }
    }

    // Set up the RecyclerView
    private fun setupRecyclerView() {
        accountAdapter = AccountAdapter { account ->
            // Handle account click here if needed (e.g., navigate to details/edit)
            Toast.makeText(this, "Clicked on ${account.accountname}", Toast.LENGTH_SHORT).show()
        }
        accountsRecyclerView.layoutManager = LinearLayoutManager(this)
        accountsRecyclerView.adapter = accountAdapter
    }

    // Load accounts and calculate the total balance
    private fun loadAccountsAndBalance() {
        lifecycleScope.launch {
            try {
                val userAccounts = accountDbDao.getUserAccountsList(currentUserId)
                var totalBalance = 0.0
                userAccounts.forEach { account ->
                    totalBalance += account.balance
                }
                // Update UI on the main thread
                runOnUiThread {
                    totalBalanceTextView.text = String.format(Locale.US, "R %.2f", totalBalance)
                    accountAdapter.submitList(userAccounts) // Update RecyclerView
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@AccountActivity, "Error loading accounts", Toast.LENGTH_SHORT).show()
                    totalBalanceTextView.text = "R -.--"
                }
            }
        }
    }

    // Back button logic
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
// --- END AccountActivity.kt ---