package com.example.budgetbudgies_prog7313_poe

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetbudgies_prog7313_poe.AppDatabase
import com.example.budgetbudgies_prog7313_poe.UserDao
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        userDao = AppDatabase.getDatabase(applicationContext).userDao()

        val emailEditText = findViewById<EditText>(R.id.editTextText)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword)
        val loginButton = findViewById<Button>(R.id.button)
        val textView9 = findViewById<TextView>(R.id.textView9)
        val textView10 = findViewById<TextView>(R.id.textView10)

        setupClickableText(textView9, "Forgot your password? reset it here", "reset", Color.BLUE) {
            Toast.makeText(this, "Reset password clicked (Not Implemented)", Toast.LENGTH_SHORT).show()
        }

        setupClickableText(textView10, "Not yet registered? register here", "register", Color.BLUE) {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString() // Don't trim password

            if (email.isEmpty()) {
                emailEditText.error = "Email cannot be empty"
                emailEditText.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                passwordEditText.error = "Password cannot be empty"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val user = userDao.getUserByEmail(email)

                    if (user != null) {
                        if (user.password == password) {
                            Log.d("LoginActivity", "Login successful for user ID: ${user.userid}")
                            Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                            startActivity(intent)
                            finish()

                        } else {
                            Log.w("LoginActivity", "Login failed: Incorrect password for email $email")
                            Toast.makeText(this@LoginActivity, "Invalid email or password.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.w("LoginActivity", "Login failed: No user found with email $email")
                        Toast.makeText(this@LoginActivity, "Invalid email or password.", Toast.LENGTH_LONG).show()
                    }

                } catch (e: Exception) {
                    Log.e("LoginActivity", "Error during login database query", e)
                    Toast.makeText(this@LoginActivity, "Login failed: Database error.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupClickableText(textView: TextView, fullText: String, clickableWord: String, color: Int, onClick: () -> Unit) {
        val spannableString = SpannableString(fullText)
        val start = fullText.indexOf(clickableWord)
        if (start == -1) return

        val end = start + clickableWord.length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                onClick()
            }
        }

        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
    }
}