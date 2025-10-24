package com.interswitchng.smartpos.shared.models.transaction

data class PosTransactionResponse(
    val currentPage: Long,
    val totalItems: Long,
    val totalPages: Long,
    val data: List<Data>,
)

data class Data(
    val id: Long,
    val stan: String,
    val rrn: String,
    val amount: String,
    val terminalId: String,
    val merchantId: String,
    val merchantNameAndLocation: String,
    val maskedPan: String,
    val responseCode: String,
    val responseMessage: String,
    val currencyCode: String,
    val receivingInstitutionId: String,
    val dateCreated: String,
    val authId: String,
    val tvr: String,
    val cbnCode: String,
    val issuerName: String,
    val cardTypeName: String,
    val accountType: String,
    val transactionType: String,
)

data class PostBatchRecordsResponse(val message: String)

