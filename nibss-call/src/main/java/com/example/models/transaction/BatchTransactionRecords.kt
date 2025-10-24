package com.interswitchng.smartpos.shared.models.transaction

import com.interswitchng.smartpos.shared.models.transaction.database.TransactionLog

data class BatchTransactionRecords (val terminalId: String, val merchantId: String, val transactionRecords: List<TransactionLog>)