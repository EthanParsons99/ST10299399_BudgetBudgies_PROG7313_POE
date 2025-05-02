// --- START LoginActivity.kt ---
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
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    // Activity lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        if (SessionManager.isLoggedIn(applicationContext)) {
            navigateToMain()
            return
        }

        val userDao = AppDatabase.getDatabase(applicationContext).userDao()

        val emailInput = findViewById<EditText>(R.id.editTextText)
        val passwordInput = findViewById<EditText>(R.id.editTextTextPassword)
        val loginButton = findViewById<Button>(R.id.button)
        val forgotPasswordText = findViewById<TextView>(R.id.textView9)
        val registerText = findViewById<TextView>(R.id.textView10)

        // Set up clickable text
        setupClickableText(forgotPasswordText, "Forgot your password? reset it here", "reset", Color.BLUE) {
            Toast.makeText(this, "Password reset isn't ready yet!", Toast.LENGTH_SHORT).show()
        }

        setupClickableText(registerText, "Not yet registered? register here", "register", Color.BLUE) {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Set up login button
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isEmpty()) {
                emailInput.error = "Need your email here!"
                emailInput.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                passwordInput.error = "Don't forget your password!"
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            // Check login details
            lifecycleScope.launch {
                try {
                    val foundUser = userDao.getUserByEmail(email)

                    // ** WARNING: Comparing plain password - use hashing in a real app! **
                    if (foundUser != null && foundUser.password == password) {
                        Log.d("Login", "Login ok for user ID: ${foundUser.userid}")
                        Toast.makeText(this@LoginActivity, "Welcome back!", Toast.LENGTH_SHORT).show()

                        SessionManager.saveUserId(applicationContext, foundUser.userid)

                        navigateToMain()

                    } else {
                        Log.w("Login", "Login failed for email $email")
                        Toast.makeText(this@LoginActivity, "Hmm, that email or password wasn't right.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("Login", "DB error on login", e)
                    Toast.makeText(this@LoginActivity, "Couldn't check details, something went wrong.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Navigate to main activity
    private fun navigateToMain() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Set up clickable text
    private fun setupClickableText(textView: TextView, fullText: String, clickableWord: String, color: Int, onClick: () -> Unit) {
        val spannableString = SpannableString(fullText)
        val start = fullText.indexOf(clickableWord)
        if (start == -1) return

        val end = start + clickableWord.length
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) { onClick() }
        }

        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
    }
}
// --- END LoginActivity.kt ---