package com.example.budgetbudgies_prog7313_poe

import androidx.room.*
import com.example.budgetbudgies_prog7313_poe.Category // Import your Category entity
import kotlinx.coroutines.flow.Flow

interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category) : Long

    @Query ("SELECT * FROM categories WHERE userid = :userId ORDER BY categoryname ASC")
    fun getUserCategories(userId: Int): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE userid = :userId ORDER BY categoryname ASC")
    suspend fun getUserCategoriesList(userId: Int): List<Category>

    @Query("SELECT * FROM categories WHERE categoryid = :categoryId LIMIT 1")
    suspend fun getCategoryById(categoryId: Int): Category?

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT COUNT(*) FROM categories WHERE userid = :userId AND categoryname = :name COLLATE NOCASE")
    suspend fun categoryExists(userId: Int, name: String): Int
}