package com.example.budgetbudgies_prog7313_poe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_page)

        val addAccountBtn = findViewById<Button>(R.id.addAccountbtn)

        addAccountBtn.setOnClickListener {
            val intent = Intent(this, AddAccount::class.java)
            startActivity(intent)
        }

    }
}