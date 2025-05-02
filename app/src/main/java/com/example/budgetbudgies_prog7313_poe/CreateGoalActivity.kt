package com.example.budgetbudgies_prog7313_poe

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CreateGoalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_goals)

        // Finding views with correct IDs from the layout
        val etName = findViewById<EditText>(R.id.etGoalName)
        val etTarget = findViewById<EditText>(R.id.etTarget)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val btnSaveGoal = findViewById<Button>(R.id.btnSaveGoal)

        // The new views we just added
        val tvCategoryType = findViewById<TextView>(R.id.tvCategoryType)
        val ivCategoryIcon = findViewById<ImageView>(R.id.ivCategoryIcon)

        // We don't have these in the layout yet, so commenting them out
        // val etNotes = findViewById<EditText>(R.id.etNotes)
        // val etAmount = findViewById<EditText>(R.id.etAmount)
        // val etCurrency = findViewById<EditText>(R.id.etCurrency)

        btnSaveGoal.setOnClickListener {
            val name = etName.text.toString()
            val target = etTarget.text.toString()
            val category = spinnerCategory.selectedItem?.toString() ?: ""

            // Since we don't have these fields in the layout, we'll skip them
            // val notes = etNotes.text.toString()
            // val amount = etAmount.text.toString()
            // val currency = etCurrency.text.toString()

            if (name.isEmpty() || target.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Example of using the new views we added
            tvCategoryType.text = "Selected: $category"
            tvCategoryType.visibility = View.VISIBLE
            ivCategoryIcon.visibility = View.VISIBLE

            // Save logic here (e.g., database insert or API call)
            Toast.makeText(this, "Goal Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

// // _________________________________END OF FILE___________________________________  -
