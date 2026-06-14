package com.example.expenseapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.expenseapp.data.entities.TagEntity
import com.example.expenseapp.data.entities.TagWithTransactions
import com.example.expenseapp.data.entities.TransactionEntity
import com.example.expenseapp.data.entities.TransactionTagCrossRef
import com.example.expenseapp.data.entities.TransactionWithTags

@Dao
interface TagsDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(tag: TagEntity): Long

    @Update
    suspend fun update(tag: TagEntity)

    @Delete
    suspend fun delete(tag: TagEntity)

    @Query("SELECT * FROM tags ORDER BY name ASC")
    suspend fun getAll(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TagEntity?

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: TransactionTagCrossRef)

    suspend fun linkTagToTransaction(transactionId: Long, tagId: Long) {
        insertCrossRef(TransactionTagCrossRef(transactionId = transactionId, tagId = tagId))
    }

    @Query("DELETE FROM transaction_tags WHERE transaction_id = :transactionId AND tag_id = :tagId")
    suspend fun unlinkTagFromTransaction(transactionId: Long, tagId: Long)

    @Query("DELETE FROM transaction_tags WHERE transaction_id = :transactionId")
    suspend fun clearTagsForTransaction(transactionId: Long)

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    suspend fun getTransactionWithTags(transactionId: Long): TransactionWithTags?

    @Transaction
    @Query("SELECT * FROM tags WHERE id = :tagId LIMIT 1")
    suspend fun getTagWithTransactions(tagId: Long): TagWithTransactions?

    @Query(
        """
        SELECT transactions.*
        FROM transactions
        INNER JOIN transaction_tags ON transactions.id = transaction_tags.transaction_id
        WHERE transaction_tags.tag_id = :tagId
        ORDER BY transactions.created_at DESC
        """
    )
    suspend fun getTransactionsByTag(tagId: Long): List<TransactionEntity>
}
