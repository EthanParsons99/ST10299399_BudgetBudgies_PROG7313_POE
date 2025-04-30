// --- START AppDatabase.kt ---
package com.example.budgetbudgies_prog7313_poe

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// !!! INCREMENT VERSION NUMBER !!! because Account and Category entities changed
@Database(
    entities = [
        User::class,
        Category::class,
        Account::class,
        Expense::class,
        Income::class,
        Goal::class
    ],
    version = 3, // <-- Incremented from 1
    exportSchema = false
)
@TypeConverters(Convert::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_budgies_prog7313_poe_database"
                )
                    // WARNING: This will delete all data on version upgrade!
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
// --- END AppDatabase.kt ---