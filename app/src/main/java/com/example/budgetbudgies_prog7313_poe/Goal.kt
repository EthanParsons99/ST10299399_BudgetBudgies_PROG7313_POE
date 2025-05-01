package com.example.budgetbudgies_prog7313_poe

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
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
    val notes: String,
    val categoryid: Int,
    val target: Double,
    val savedAmount: Double = 0.0,
    val currency: String,
    val userid: Int
) : Parcelable
