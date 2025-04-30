package com.example.budgetbudgies_prog7313_poe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class CategoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.category_page)

        val addCategoryBtn = findViewById<Button>(R.id.btn_add_category)

        addCategoryBtn.setOnClickListener {
            val intent = Intent(this, AddCategory::class.java)
            startActivity(intent)
        }
    }
}
