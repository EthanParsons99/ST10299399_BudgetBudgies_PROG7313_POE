// --- CreateGoalActivity.kt ---
package com.example.budgetbudgies_prog7313_poe

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateGoalActivity : AppCompatActivity() {
    private lateinit var dao: GoalDao
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_goals)

        // back arrow
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        dao = AppDatabase.getDatabase(this).goalDao()
        userId = intent.getIntExtra("USER_ID", 0)

        val etName = findViewById<EditText>(R.id.etName)
        val etNotes = findViewById<EditText>(R.id.etNotes)
        val spCat = findViewById<Spinner>(R.id.spinnerCategory)
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val spCur = findViewById<Spinner>(R.id.spinnerCurrency)
        val btnSetup = findViewById<Button>(R.id.btnSetup)

        spCat.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Supermarket", "Fuel", "Other")
        )
        spCur.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("ZAR", "USD")
        )

        btnSetup.setOnClickListener {
            val goal = Goal(
                goalname = etName.text.toString().trim(),
                notes = etNotes.text.toString().trim(),
                categoryid = spCat.selectedItemPosition,
                target = etAmount.text.toString().toDoubleOrNull() ?: 0.0,
                currency = spCur.selectedItem.toString(),
                userid = userId
            )

            lifecycleScope.launch {
                // Insert goal
                val newId = dao.insertGoal(goal)

                // Query the updated goals list
                val allGoals = dao.getUserGoalsList(userId)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@CreateGoalActivity,
                        "Inserted id=$newId; total goals=${allGoals.size}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                finish()
            }
        }
    }
}
