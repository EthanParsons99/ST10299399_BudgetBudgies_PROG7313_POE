// --- START SessionManager.kt ---
package com.example.budgetbudgies_prog7313_poe

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREF_NAME = "BudgetBudgiePrefs"
    private const val KEY_USER_ID = "user_id"
    private const val DEFAULT_USER_ID = -1 // Indicates no user logged in

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Session management methods
    // Save user ID to session
    fun saveUserId(context: Context, userId: Int) {
        val editor = getPreferences(context).edit()
        editor.putInt(KEY_USER_ID, userId)
        editor.apply()
    }

    // Retrieve user ID from session
    fun getUserId(context: Context): Int {
        return getPreferences(context).getInt(KEY_USER_ID, DEFAULT_USER_ID)
    }

    // Clear session data
    fun clearSession(context: Context) {
        val editor = getPreferences(context).edit()
        editor.remove(KEY_USER_ID)
        editor.apply()
    }

    // Check if user is logged in
    fun isLoggedIn(context: Context): Boolean {
        return getUserId(context) != DEFAULT_USER_ID
    }
}
// --- END SessionManager.kt ---