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
    val accounttype: String,
    val balance: Double,
    val currency: String,

)