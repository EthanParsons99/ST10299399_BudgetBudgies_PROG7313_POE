package com.example.budgetbudgies_prog7313_poe

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page) // THIS links to your login_page.xml

        val textView9 = findViewById<TextView>(R.id.textView9)
        val textView10 = findViewById<TextView>(R.id.textView10)

        setupClickableText(textView9, "Forgot your password? reset it here", "reset", Color.BLUE) {
            Toast.makeText(this, "Reset password clicked", Toast.LENGTH_SHORT).show()
            // You could navigate to password_reset_page.xml here!
        }

        setupClickableText(textView10, "Not yet registered? register here", "register", Color.BLUE) {
            Toast.makeText(this, "Register clicked", Toast.LENGTH_SHORT).show()
            // You could navigate to signup_page.xml here!
        }
    }

    private fun setupClickableText(textView: TextView, fullText: String, clickableWord: String, color: Int, onClick: () -> Unit) {
        val spannableString = SpannableString(fullText)

        val start = fullText.indexOf(clickableWord)
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
