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

        val tvCategoryType = findViewById<TextView>(R.id.tvCategoryType)
        val ivCategoryIcon = findViewById<ImageView>(R.id.ivCategoryIcon)


        btnSaveGoal.setOnClickListener {
            val name = etName.text.toString()
            val target = etTarget.text.toString()
            val category = spinnerCategory.selectedItem?.toString() ?: ""


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
