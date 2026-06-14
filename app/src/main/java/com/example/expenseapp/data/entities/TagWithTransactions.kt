package com.example.expenseapp.data.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TagWithTransactions(
    @Embedded
    val tag: TagEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TransactionTagCrossRef::class,
            parentColumn = "tag_id",
            entityColumn = "transaction_id"
        )
    )
    val transactions: List<TransactionEntity>,
)
