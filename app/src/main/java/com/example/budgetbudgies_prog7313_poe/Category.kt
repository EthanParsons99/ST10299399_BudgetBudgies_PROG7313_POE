// --- START Category.kt ---
package com.example.budgetbudgies_prog7313_poe

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index // Import Index

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userid"],
            childColumns = ["userid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Add index to prevent duplicate category names per user
    indices = [Index(value = ["userid", "categoryname"], unique = true)]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val categoryid: Int = 0,
    val userid: Int,
    val categoryname: String,
    val categoryType: String, // "Income" or "Expense"
    val icon: Int // Store drawable resource name as int (e.g., "ic_food")

)
// --- END Category.kt ---