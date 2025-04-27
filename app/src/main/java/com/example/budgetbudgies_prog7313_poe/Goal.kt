package com.example.budgetbudgies_prog7313_poe

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "goals",

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
        )
    ]
)

data class Goal(
    @PrimaryKey(autoGenerate = true) val goalid: Int = 0,
    val goalname: String,
    val target: Double,
    val userid: Int,
    val categoryid: Int
)