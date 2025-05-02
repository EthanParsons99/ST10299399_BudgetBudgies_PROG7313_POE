package com.example.budgetbudgies_prog7313_poe

import androidx.room.Entity
import androidx.room.PrimaryKey
// Create the User table
@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true) val userid: Int = 0,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)