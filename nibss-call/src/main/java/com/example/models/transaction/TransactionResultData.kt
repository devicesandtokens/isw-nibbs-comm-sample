package com.interswitchng.smartpos.shared.models.transaction

import android.os.Parcelable
import com.example.models.core.TerminalInfo
import com.example.models.transaction.PaymentType
import com.interswitchng.smartpos.shared.models.core.TransactionType
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.CardType
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.request.TransactionInfo
import kotlinx.android.parcel.Parcelize

/**
 * This class represents the final result
 * of the triggered purchase transaction.
 * This is what captures the transaction's result
 */
@Parcelize
data class TransactionResultData(
    val paymentType: PaymentType,
    var stan: String,
    val dateTime: String,
    val amount: String,
    val type: TransactionType,
    val cardPan: String,
    val cardType: CardType,
    val cardExpiry: String,
    var authorizationCode: String,
    val pinStatus: String,
    var responseMessage: String,
    var responseCode: String,
    val AID: String,
    val code: String,
    val telephone: String,
    val txnDate: Long,
    val transactionId: String = "",
    val cardHolderName: String,
    val remoteResponseCode: String = "",
    val biller: String? = "",
    val customerDescription: String? = "",
    val surcharge: String? = "",
    val additionalAmounts: String? = "",
    var customerName: String? = "",
    var ref: String? = "",
    var accountNumber: String? = "",
    val prevStan: String? = "",
    val prevAuthId: String? = "",
    var prevDateTime: Long = 0L,
    var transactionCurrencyType: TransactionCurrencyType = TransactionCurrencyType.NAIRA,
    var isReversed: Int = 0,
    var rrn: String,
    val route: String,
    val cardTrack2: String? = "",
    val serviceRestrictionCode: String? = "",
    val cardSequenceNumber: String? = "",
    val uniqueReference: String? = "",
    val transactionRemark: String? = ""
) : Parcelable {

}