package com.example.budgetbudgies_prog7313_poe

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CreateGoalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_goals)

        // Corrected ID reference here
        val etName = findViewById<EditText>(R.id.etGoalName)
        val etNotes = findViewById<EditText>(R.id.etNotes)
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val etTarget = findViewById<EditText>(R.id.etTarget)
        val etCurrency = findViewById<EditText>(R.id.etCurrency)
        val btnSaveGoal = findViewById<Button>(R.id.btnSaveGoal)

        btnSaveGoal.setOnClickListener {
            val name = etName.text.toString()
            val notes = etNotes.text.toString()
            val amount = etAmount.text.toString()
            val target = etTarget.text.toString()
            val currency = etCurrency.text.toString()

            if (name.isEmpty() || amount.isEmpty() || target.isEmpty() || currency.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save logic here (e.g., database insert or API call)
            Toast.makeText(this, "Goal Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
