package com.example.expenseapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.expenseapp.data.dao.TagsDao
import com.example.expenseapp.data.dao.TransactionsDao
import com.example.expenseapp.data.entities.CategoryEntity
import com.example.expenseapp.data.entities.TagEntity
import com.example.expenseapp.data.entities.TransactionEntity
import com.example.expenseapp.data.entities.TransactionTagCrossRef

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        TagEntity::class,
        TransactionTagCrossRef::class
    ],
    version = 1,
    exportSchema = true
)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun transactionsDao(): TransactionsDao

    abstract fun tagsDao(): TagsDao
}
