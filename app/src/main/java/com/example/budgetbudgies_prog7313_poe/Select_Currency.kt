package com.example.budgetbudgies_prog7313_poe

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Select_Currency : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_income_page)


        val spinnerCurrency = findViewById<Spinner>(R.id.spinnerCurrency)
        val tvCurrencySelected = findViewById<TextView>(R.id.tvCurrencySelected)

        val currencies = listOf(
            "ZAR",
            "USD",
            "EUR",
            "GBP",
            "JPY",
            "AUD"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = adapter


        spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {

                val selectedCurrency = parent.getItemAtPosition(position).toString()
                tvCurrencySelected.text = "Selected: $selectedCurrency"
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }
}
