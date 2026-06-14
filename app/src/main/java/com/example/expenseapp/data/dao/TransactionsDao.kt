package com.example.expenseapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import androidx.room.Update
import com.example.expenseapp.data.entities.TransactionEntity
import com.example.expenseapp.data.entities.TransactionTypeConverter
import com.example.expenseapp.domain.model.TransactionType

@Dao
@TypeConverters(TransactionTypeConverter::class)
interface TransactionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM transactions ORDER BY created_at DESC")
    suspend fun getAll(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE category_id = :categoryId ORDER BY created_at DESC")
    suspend fun getByCategory(categoryId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY created_at DESC")
    suspend fun getByType(type: TransactionType): List<TransactionEntity>

    @Query("SELECT * FROM transactions ORDER BY created_at DESC")
    suspend fun listByDate(): List<TransactionEntity>

    @Query(
        """
        SELECT * FROM transactions
        WHERE created_at >= :startOfDayMillis
            AND created_at < :endOfDayMillis
        ORDER BY created_at DESC
        """
    )
    suspend fun getByDay(
        startOfDayMillis: Long,
        endOfDayMillis: Long,
    ): List<TransactionEntity>
}
