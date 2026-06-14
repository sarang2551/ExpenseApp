package com.example.expenseapp.data.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TransactionWithTags(
    @Embedded
    val transaction: TransactionEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TransactionTagCrossRef::class,
            parentColumn = "transaction_id",
            entityColumn = "tag_id"
        )
    )
    val tags: List<TagEntity>,
)
