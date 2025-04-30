// --- START Account.kt ---
package com.example.budgetbudgies_prog7313_poe

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userid"],
            childColumns = ["userid"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Account(
    @PrimaryKey(autoGenerate = true) val accountid: Int = 0,
    val userid: Int,
    val accountname: String,
    val accounttype: String?, // Optional original type field
    val balance: Double,
    val currency: String,
    // --- Added Fields ---
    val accountColor: String?, // Store color name/hex as String
    val accountIconName: String? // Store bank/icon name as String
    // --- End Added Fields ---
)
// --- END Account.kt ---