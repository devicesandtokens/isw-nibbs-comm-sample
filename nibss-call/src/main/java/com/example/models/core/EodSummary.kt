package com.interswitchng.smartpos.shared.models.core

import com.interswitchng.smartpos.shared.models.transaction.TransactionCurrencyType

data class EodSummary(
    val totalVolume: Int = 0,
    val successVolume: Int = 0,
    val failedVolume: Int = 0,
    val totalValue: Double = 0.00,
    val successValue: Double = 0.00,
    val failedValue: Double = 0.00,
    val summaryType: TransactionCurrencyType = TransactionCurrencyType.NAIRA
)