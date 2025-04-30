// --- START AppDatabase.kt (with Transaction Helpers) ---
package com.example.budgetbudgies_prog7313_poe

import android.content.Context
import android.util.Log // Import Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.withTransaction // Import for withTransaction

@Database(
    entities = [
        User::class,
        Category::class,
        Account::class,
        Expense::class,
        Income::class,
        Goal::class
    ],
    version = 3, // Keep version 3 as provided
    exportSchema = false
)
@TypeConverters(Convert::class)
abstract class AppDatabase : RoomDatabase() {

    // Abstract DAO accessors
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun goalDao(): GoalDao

    // --- Transaction Helper Functions ---

    /**
     * Inserts an Income record and updates the corresponding Account balance
     * within a single database transaction. Throws exception on failure.
     */
    suspend fun insertIncomeAndUpdateAccount(income: Income) {
        withTransaction {
            // 1. Insert Income
            val incomeId = incomeDao().insertIncome(income) // Use specific DAO instance
            if (incomeId == -1L) {
                Log.e("AppDatabase", "Income insertion failed (Conflict?), aborting transaction.")
                // Throwing an exception automatically rolls back the transaction
                throw IllegalStateException("Failed to insert income, transaction rolled back.")
            }

            // 2. Get Account
            val account = accountDao().getAccountById(income.accountid) // Use specific DAO instance
            if (account == null) {
                Log.e("AppDatabase", "Account not found for ID: ${income.accountid}, aborting transaction.")
                throw IllegalStateException("Account [ID: ${income.accountid}] not found, transaction rolled back.")
            }

            // 3. Calculate New Balance
            val newBalance = account.balance + income.amount

            // 4. Update Account Balance
            accountDao().updateAccountBalance(income.accountid, newBalance) // Use specific DAO instance
            Log.d("AppDatabase", "Updated account ${income.accountid} balance to $newBalance after income $incomeId")
        }
    }

    /**
     * Inserts an Expense record and updates the corresponding Account balance
     * within a single database transaction. Throws exception on failure.
     */
    suspend fun insertExpenseAndUpdateAccount(expense: Expense) {
        withTransaction {
            // 1. Insert Expense
            val expenseId = expenseDao().insertExpense(expense) // Use specific DAO instance
            if (expenseId == -1L) {
                Log.e("AppDatabase", "Expense insertion failed (Conflict?), aborting transaction.")
                throw IllegalStateException("Failed to insert expense, transaction rolled back.")
            }

            // 2. Get Account
            val account = accountDao().getAccountById(expense.accountid) // Use specific DAO instance
            if (account == null) {
                Log.e("AppDatabase", "Account not found for ID: ${expense.accountid}, aborting transaction.")
                throw IllegalStateException("Account [ID: ${expense.accountid}] not found, transaction rolled back.")
            }

            // 3. Calculate New Balance
            val newBalance = account.balance - expense.amount // Subtract expense

            // 4. Update Account Balance
            accountDao().updateAccountBalance(expense.accountid, newBalance) // Use specific DAO instance
            Log.d("AppDatabase", "Updated account ${expense.accountid} balance to $newBalance after expense $expenseId")
        }
    }

    // --- End Transaction Helper Functions ---


    // Companion object for singleton instance
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_budgies_prog7313_poe_database" // Database file name
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
// --- END AppDatabase.kt (with Transaction Helpers) ---