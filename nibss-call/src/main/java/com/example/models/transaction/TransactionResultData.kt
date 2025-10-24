package com.interswitchng.smartpos.shared.models.transaction

import android.os.Parcelable
import com.example.models.core.TerminalInfo
import com.interswitchng.smartpos.shared.models.core.TransactionType
import com.interswitchng.smartpos.shared.models.printer.info.TransactionInfo
import com.interswitchng.smartpos.shared.models.printer.info.TransactionStatus
import com.interswitchng.smartpos.shared.models.printer.slips.CardSlip
import com.interswitchng.smartpos.shared.models.printer.slips.CashSlip
import com.interswitchng.smartpos.shared.models.printer.slips.TransactionSlip
import com.interswitchng.smartpos.shared.models.printer.slips.UssdQrTransferSlip
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.CardType
import com.interswitchng.smartpos.shared.services.utils.IsoUtils
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

    private var _isSuccessfulOverride: Boolean? = null

    var isSuccessful: Boolean
        get() = _isSuccessfulOverride ?: (responseCode == IsoUtils.OK)
        set(value) {
            _isSuccessfulOverride = value
        }

    fun getSlip(terminal: TerminalInfo): TransactionSlip {
        return when (paymentType) {
            PaymentType.USSD, PaymentType.QR, PaymentType.Transfer, PaymentType.Web, PaymentType.Options -> UssdQrTransferSlip(
                terminal,
                getTransactionStatus(),
                getTransactionInfo()
            )
            PaymentType.Card, PaymentType.PayCode, PaymentType.ThankYouCash, PaymentType.CNP -> CardSlip(
                terminal,
                getTransactionStatus(),
                getTransactionInfo()
            )
            PaymentType.Cash -> CashSlip(
                terminal,
                getTransactionStatus(),
                getTransactionInfo()
            )
        }
    }

    /// function to extract
    /// print slip transaction info
    fun getTransactionInfo() = TransactionInfo(
        paymentType = paymentType,
        stan = stan,
        dateTime = dateTime,
        amount = amount,
        type = type,
        cardPan = cardPan,
        cardType = cardType.name,
        cardExpiry = cardExpiry,
        authorizationCode = authorizationCode,
        pinStatus = pinStatus,
        responseCode = responseCode,
        transactionId = transactionId,
        cardHolderName = cardHolderName,
        remoteResponseCode = remoteResponseCode,
        biller = biller,
        customerDescription = customerDescription,
        surcharge = surcharge,
        additionalAmounts = additionalAmounts,
        customerName = customerName ?: "",
        ref = ref ?: "",
        accountNumber = accountNumber ?: "",
        transactionCurrencyType = transactionCurrencyType,
        rrn = rrn,
        route = route,
        uniqueRef = uniqueReference ?: "",
        transactionRemark = transactionRemark ?: ""
    )

    /// function to extract
    /// print slip transaction status
    fun getTransactionStatus() = TransactionStatus(
        responseMessage = responseMessage,
        responseCode = responseCode,
        AID = AID,
        telephone = telephone,
        stan = stan,
        rrn = rrn
    )
}