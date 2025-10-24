package com.example.models.transaction.cardpaycode.response

import com.interswitchng.smartpos.shared.models.core.AdditionalInfo


/**
 * This class captures the transaction response from EPMS
 * for a given purchase request
 */
data class TransactionResponse(
        val responseCode: String, // response code
        val authCode: String, // authorization code
        val stan: String,
        val rrn: String,
        val date: Long,
        val scripts: String,
        var iad: String = "",
        var st1: String = "",
        var st2: String = "",
        var balance: String = "0",
        val responseDescription: String? = null,
        var transactionId: String? = null,
        val remoteResponseCode: String? = null,
        val additionalInfo: AdditionalInfo? = null
)
