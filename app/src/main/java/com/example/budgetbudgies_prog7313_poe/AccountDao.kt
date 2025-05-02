// --- START AccountDao.kt ---

package com.example.budgetbudgies_prog7313_poe

import androidx.room.*
import com.example.budgetbudgies_prog7313_poe.Account
import kotlinx.coroutines.flow.Flow

//DAO for all account operations
@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAccount(account: Account): Long

    @Query("SELECT * FROM accounts WHERE userid = :userId ORDER BY accountname ASC")
    fun getUserAccounts(userId: Int): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE userid = :userId ORDER BY accountname ASC")
    suspend fun getUserAccountsList(userId: Int): List<Account>

    @Query("SELECT * FROM accounts WHERE accountid = :accountId LIMIT 1")
    suspend fun getAccountById(accountId: Int): Account?

    @Update
    suspend fun updateAccount(account: Account)     // Update an existing account

    @Delete
    suspend fun deleteAccount(account: Account)

    @Query("UPDATE accounts SET balance = :newBalance WHERE accountid = :accountId")
    suspend fun updateAccountBalance(accountId: Int, newBalance: Double)
}


// _________________________________END OF FILE___________________________________