package com.example.finconapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.finconapp.data.local.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("""
        SELECT COUNT(*) 
        FROM categories 
        WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name))
    """)
    suspend fun countByName(name: String): Int

    @Query("""
    SELECT COUNT(*)
    FROM categories
    WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name))
    AND id != :categoryId
""")
    suspend fun countByNameExcludingId(
        name: String,
        categoryId: Int
    ): Int

    @Query("""
        SELECT COUNT(*)
        FROM transactions
        WHERE categoryId = :categoryId
    """)
    suspend fun countTransactionsByCategory(
        categoryId: Int
    ): Int

    @Query("""
        DELETE FROM categories
        WHERE id = :categoryId
    """)
    suspend fun delete(categoryId: Int)
}
