package com.example.budgetbudgies_prog7313_poe

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

// PlaceholderActivity.kt
class PlaceholderActivity : AppCompatActivity() {

    // Companion object to define constants
    companion object {
        const val EXTRA_FEATURE_NAME = "FEATURE_NAME"
    }

    // Activity lifecycle methods
    // onCreate is called when the activity is first created
    // It sets the layout and initializes the toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_placeholder)

        val toolbar: Toolbar = findViewById(R.id.placeholder_toolbar)
        setSupportActionBar(toolbar)

        val featureName = intent.getStringExtra(EXTRA_FEATURE_NAME) ?: "Feature"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = featureName
    }

    // onOptionsItemSelected is called when an item in the options menu is selected
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