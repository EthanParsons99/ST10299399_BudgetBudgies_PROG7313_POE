package com.example.budgetbudgies_prog7313_poe

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import java.util.Date

//Expense table
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userid"],
            childColumns = ["userid"], // Links to the 'userid' field below
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["categoryid"],
            childColumns = ["categoryid"], // Links to the 'categoryid' field below
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["accountid"],
            childColumns = ["accountid"], // *** Links to the NEW 'accountid' field below ***
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val expenseid: Int = 0,
    val amount: Double,     // Amount
    val date: Date,         // Date
    val userid: Int,      // user I.D
    val categoryid: Int,   // Foreign key for Category table
    val description: String, //stores the description as a string
    val photopath: String?, //For the photo
    val accountid: Int   // Account I.D
)