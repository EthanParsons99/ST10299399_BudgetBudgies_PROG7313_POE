package com.example.budgetbudgies_prog7313_poe

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import java.util.Date

@Entity(
    tableName = "incomes",

    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userid"],
            childColumns = ["userid"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["categoryid"],
            childColumns = ["categoryid"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["accountid"],
            childColumns = ["accountid"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class Income(
    @PrimaryKey(autoGenerate = true) val incomeid: Int = 0,
    val amount: Double,
    val date: Date,
    val userid: Int,
    val categoryid: Int,
    val accountid: Int
)