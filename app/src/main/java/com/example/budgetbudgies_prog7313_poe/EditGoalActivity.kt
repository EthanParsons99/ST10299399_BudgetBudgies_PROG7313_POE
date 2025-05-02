package com.example.budgetbudgies_prog7313_poe

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditGoalActivity : AppCompatActivity() {
    private lateinit var dao: GoalDao
    private var goalId: Int = 0
    private lateinit var existing: Goal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_goals)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        dao = AppDatabase.getDatabase(this).goalDao()
        goalId = intent.getIntExtra("GOAL_ID", 0)

        val etName     = findViewById<EditText>(R.id.etName)
        val etNotes    = findViewById<EditText>(R.id.etNotes)
        val etAmt      = findViewById<EditText>(R.id.etAmount)
        val etProgress = findViewById<EditText>(R.id.etProgress)
        val spCat      = findViewById<Spinner>(R.id.spinnerCategory)
        val spCur      = findViewById<Spinner>(R.id.spinnerCurrency)
        val btnSave    = findViewById<Button>(R.id.btnSetup)

        val cats = listOf("Supermarket", "Fuel", "Other")
        spCat.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cats)

        val curs = listOf("ZAR", "USD")
        spCur.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, curs)

        lifecycleScope.launch {
            existing = withContext(Dispatchers.IO) { dao.getGoalById(goalId)!! }

            etName.setText(existing.goalname)
            etNotes.setText(existing.notes)
            etAmt.setText(existing.target.toString())
            etProgress.setText(existing.savedAmount.toString())
            spCat.setSelection(existing.categoryid)
            spCur.setSelection(curs.indexOf(existing.currency))
        }

        btnSave.setOnClickListener {
            val updated = existing.copy(
                goalname    = etName.text.toString().trim(),
                notes       = etNotes.text.toString().trim(),
                categoryid  = spCat.selectedItemPosition,
                target      = etAmt.text.toString().toDoubleOrNull() ?: existing.target,
                savedAmount = etProgress.text.toString().toDoubleOrNull() ?: existing.savedAmount,
                currency    = spCur.selectedItem.toString()
            )

            lifecycleScope.launch {
                dao.updateGoal(updated)
                finish()
            }
        }
    }
}
