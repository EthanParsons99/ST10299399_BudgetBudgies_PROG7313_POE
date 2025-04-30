package com.example.budgetbudgies_prog7313_poe

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.parcelize.Parcelize

@Parcelize
data class kCategory(
    val type: String,
    val name: String,
    val iconResId: Int
) : Parcelable

class AddCategory : AppCompatActivity() {

    private lateinit var buttonIncome: Button
    private lateinit var buttonExpense: Button
    private lateinit var editTextCategoryName: EditText
    private lateinit var buttonAdd: Button
    private lateinit var buttonCancel: Button
    private lateinit var iconContainer: LinearLayout

    private var selectedType: String = "Income"
    private var selectedIconResId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_new_category)

        buttonIncome = findViewById(R.id.btn_income)
        buttonExpense = findViewById(R.id.btn_expenses)
        editTextCategoryName = findViewById(R.id.edit_category_name)
        buttonAdd = findViewById(R.id.btn_add)
        buttonCancel = findViewById(R.id.btn_cancel)
        iconContainer = findViewById(R.id.icon_container)

        // Handle type buttons
        buttonIncome.setOnClickListener {
            selectedType = "Income"
            buttonIncome.isSelected = true
            buttonExpense.isSelected = false
        }

        buttonExpense.setOnClickListener {
            selectedType = "Expense"
            buttonIncome.isSelected = false
            buttonExpense.isSelected = true
        }

        // Add selectable icons
        val iconList = listOf(
            R.drawable.ic_food,
            R.drawable.ic_transport,
            R.drawable.ic_salary,
            R.drawable.ic_disability
        )

        iconList.forEach { iconRes ->
            val imageView = ImageView(this).apply {
                setImageResource(iconRes)
                layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                    setMargins(16, 8, 16, 8)
                }
                setPadding(12, 12, 12, 12)
                setOnClickListener {
                    selectedIconResId = iconRes
                    highlightSelectedIcon(this)
                }
            }
            iconContainer.addView(imageView)
        }

        buttonAdd.setOnClickListener {
            val name = editTextCategoryName.text.toString().trim()
            if (name.isEmpty() || selectedIconResId == 0) {
                Toast.makeText(this, "Please enter a name and select an icon.", Toast.LENGTH_SHORT).show()
            } else {
                val newCategory = kCategory(
                    type = selectedType,
                    name = name,
                    iconResId = selectedIconResId
                )
                val resultIntent = Intent().apply {
                    putExtra("NEW_CATEGORY", newCategory)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }

        buttonCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun highlightSelectedIcon(selectedImageView: ImageView) {
        // Clear highlight from all icons
        for (i in 0 until iconContainer.childCount) {
            val child = iconContainer.getChildAt(i)
            child.alpha = 0.5f
        }
        selectedImageView.alpha = 1.0f
    }
}
