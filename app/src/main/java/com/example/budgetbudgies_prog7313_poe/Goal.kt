// --- START of Goal

package com.example.budgetbudgies_prog7313_poe
//Import
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Declares a Room entity for the "goals" table
@Entity(
    tableName = "goals",
    foreignKeys = [     //Establish foreign keys in the table
        ForeignKey(
            entity = Category::class,
            parentColumns = ["categoryid"],
            childColumns = ["categoryid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryid")]
)
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val goalid: Int = 0,
    val goalname: String,
    val notes: String,
    val categoryid: Int,
    val target: Double,
    val currency: String,
    val userid: Int,
    val progress: Double = 0.0,
    val completed: Boolean = false,
    val savedAmount: Double = 0.0 // To-DO: add some more "Doubles" for part 3's functionality
)
// _________________________________END OF FILE___________________________________