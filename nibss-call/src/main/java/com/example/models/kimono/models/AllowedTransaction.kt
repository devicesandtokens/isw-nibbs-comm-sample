package com.interswitchng.smartpos.shared.services.kimono.models

import com.interswitchng.smartpos.shared.models.core.TransactionType

data class AllowedTransaction(
    val transactionType: TransactionType,
    val isDefaultType: Boolean = false,
)