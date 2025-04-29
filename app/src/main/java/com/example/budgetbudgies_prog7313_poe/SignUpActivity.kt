package com.example.budgetbudgies_prog7313_poe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetbudgies_prog7313_poe.AppDatabase
import com.example.budgetbudgies_prog7313_poe.UserDao
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_page)

        userDao = AppDatabase.getDatabase(applicationContext).userDao()

        val nameEditText = findViewById<EditText>(R.id.editTextName)
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val termsCheckBox = findViewById<CheckBox>(R.id.cbForTerms)
        val signUpButton = findViewById<Button>(R.id.signUpBtn)

        signUpButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()

            val nameParts = name.split(" ", limit = 2)
            val firstName = nameParts.getOrElse(0) { "" }
            val lastName = nameParts.getOrElse(1) { "" }

            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            if (firstName.isEmpty()) {
                nameEditText.error = "Name cannot be empty"
                nameEditText.requestFocus()
                return@setOnClickListener
            }
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "Enter a valid email address"
                emailEditText.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty() || password.length < 6) {
                passwordEditText.error = "Password must be at least 6 characters"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }
            if (!termsCheckBox.isChecked) {
                Toast.makeText(this, "You must agree to the terms and conditions", Toast.LENGTH_SHORT).show()
                termsCheckBox.requestFocus()
                return@setOnClickListener
            }

            val hashedPassword = password

            val newUser = User(
                email = email,
                password = hashedPassword,
                firstName = firstName,
                lastName = lastName
            )

            lifecycleScope.launch {
                try {
                    val result = userDao.insert(newUser)

                    if (result != -1L) {
                        Toast.makeText(this@SignUpActivity, "Registration successful!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()

                    } else {
                        emailEditText.error = "This email is already registered"
                        emailEditText.requestFocus()
                        Toast.makeText(this@SignUpActivity, "Registration has failed: The email already exists.", Toast.LENGTH_LONG).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(this@SignUpActivity, "Registration has failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}