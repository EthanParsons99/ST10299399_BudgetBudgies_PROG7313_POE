package com.example.budgetbudgies_prog7313_poe

import androidx.room.*
import com.example.budgetbudgies_prog7313_poe.Expense
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExpense(expense: Expense): Long

    @Query("SELECT * FROM expenses WHERE userid = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getUserExpensesInRange(userId: Int, startDate: Date, endDate: Date): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE userid = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getUserExpensesListInRange(userId: Int, startDate: Date, endDate: Date): List<Expense>

    @Query("SELECT * FROM expenses WHERE userid = :userId ORDER BY date DESC")
    fun getAllUserExpenses(userId: Int): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE expenseid = :expenseId LIMIT 1")
    suspend fun getExpenseById(expenseId: Int): Expense?

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)
}