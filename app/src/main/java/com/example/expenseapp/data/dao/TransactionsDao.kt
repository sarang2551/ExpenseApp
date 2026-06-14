package com.example.expenseapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import androidx.room.Update
import com.example.expenseapp.data.entities.TransactionEntity
import com.example.expenseapp.data.entities.TransactionTypeConverter
import com.example.expenseapp.domain.model.TransactionType

@Dao
@TypeConverters(TransactionTypeConverter::class)
abstract class TransactionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    abstract suspend fun update(transaction: TransactionEntity)

    @Delete
    abstract suspend fun delete(transaction: TransactionEntity)

    @Transaction
    suspend fun deleteAll() {
        deleteAllTransactionTagLinks()
        deleteAllTransactions()
    }

    @Query("DELETE FROM transaction_tags")
    abstract suspend fun deleteAllTransactionTagLinks()

    @Query("DELETE FROM transactions")
    abstract suspend fun deleteAllTransactions()

    @Query("DELETE FROM transactions WHERE id = :id")
    abstract suspend fun deleteById(id: Long)

    @Query("SELECT * FROM transactions ORDER BY created_at DESC")
    abstract suspend fun getAll(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    abstract suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE category_id = :categoryId ORDER BY created_at DESC")
    abstract suspend fun getByCategory(categoryId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY created_at DESC")
    abstract suspend fun getByType(type: TransactionType): List<TransactionEntity>

    @Query("SELECT * FROM transactions ORDER BY created_at DESC")
    abstract suspend fun listByDate(): List<TransactionEntity>

    @Query(
        """
        SELECT * FROM transactions
        WHERE created_at >= :startOfDayMillis
            AND created_at < :endOfDayMillis
        ORDER BY created_at DESC
        """
    )
    abstract suspend fun getByDay(
        startOfDayMillis: Long,
        endOfDayMillis: Long,
    ): List<TransactionEntity>
}
