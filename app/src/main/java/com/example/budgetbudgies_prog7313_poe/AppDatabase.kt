package com.example.budgetbudgies_prog7313_poe

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.withTransaction

@Database(
    entities = [
        User::class,
        Category::class,
        Account::class,
        Expense::class,
        Income::class,
        Goal::class
    ],
    version = 5, // 🔼 Version updated from 4 to 5
    exportSchema = false
)
@TypeConverters(Convert::class)
abstract class AppDatabase : RoomDatabase() {

    // --- Abstract DAO accessors ---
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
            val incomeId = incomeDao().insertIncome(income)
            if (incomeId == -1L) {
                Log.e("AppDatabase", "Income insertion failed (Conflict?), aborting transaction.")
                throw IllegalStateException("Failed to insert income, transaction rolled back.")
            }

            // 2. Get Account
            val account = accountDao().getAccountById(income.accountid)
            if (account == null) {
                Log.e("AppDatabase", "Account not found for ID: ${income.accountid}, aborting transaction.")
                throw IllegalStateException("Account [ID: ${income.accountid}] not found, transaction rolled back.")
            }

            // 3. Calculate New Balance
            val newBalance = account.balance + income.amount

            // 4. Update Account Balance
            accountDao().updateAccountBalance(income.accountid, newBalance)
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
            val expenseId = expenseDao().insertExpense(expense)
            if (expenseId == -1L) {
                Log.e("AppDatabase", "Expense insertion failed (Conflict?), aborting transaction.")
                throw IllegalStateException("Failed to insert expense, transaction rolled back.")
            }

            // 2. Get Account
            val account = accountDao().getAccountById(expense.accountid)
            if (account == null) {
                Log.e("AppDatabase", "Account not found for ID: ${expense.accountid}, aborting transaction.")
                throw IllegalStateException("Account [ID: ${expense.accountid}] not found, transaction rolled back.")
            }

            // 3. Calculate New Balance
            val newBalance = account.balance - expense.amount

            // 4. Update Account Balance
            accountDao().updateAccountBalance(expense.accountid, newBalance)
            Log.d("AppDatabase", "Updated account ${expense.accountid} balance to $newBalance after expense $expenseId")
        }
    }

    // --- End Transaction Helper Functions ---

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * MIGRATION: v4 to v5
         * Adds a new column 'savedAmount' to the goals table with default value 0.0
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE goals ADD COLUMN savedAmount REAL NOT NULL DEFAULT 0.0"
                )
                Log.d("Migration", "Migrated DB from version 4 to 5: added savedAmount to goals.")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_budgies_prog7313_poe_database"
                )
                    // .addMigrations(MIGRATION_4_5)
                    // ✅ Use migration instead of destructive fallback
                    //.fallbackToDestructiveMigration() ❌ Do NOT use destructive migration anymore
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
// _________________________________END OF FILE___________________________________  