package com.example.models.kimono.models


import com.example.models.core.TerminalInfo
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.request.IccData
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.request.TransactionInfo
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "purchaseRequest", strict = false)
internal class PurchaseRequest : TransactionRequest()

@Root(name = "reservationRequest", strict = false)
internal class PreAuthRequest : TransactionRequest()

@Root(name = "completionRequest", strict = false)
internal class CompletionRequest : TransactionRequest()

@Root(name = "refundRequest", strict = false)
internal class RefundRequest : TransactionRequest()

@Root(name = "balanceInquiryRequest", strict = false)
internal class BalanceRequest : TransactionRequest()

@Root(name = "ifisBillPaymentCashoutRequest", strict = false)
internal class CashOutRequest : TransactionRequest()

@Root(name = "billPaymentRequest", strict = false)
internal class BillPaymentRequest : TransactionRequest()

@Root(name = "transferRequest", strict = false)
internal class TransferRequest : TransactionRequest()

internal sealed class TransactionRequest {
    @field:Element(name = "terminalInformation", required = false)
    var terminalInformation: TerminalInfo? = null

    @field:Element(name = "cardData", required = false)
    var cardData: CardData? = null

    @field:Element(name = "fromAccount", required = false)
    var fromAccount: String = ""

    @field:Element(name = "stan", required = false)
    var stan: String = ""

    @field:Element(name = "minorAmount", required = false)
    var minorAmount: String = ""

    @field:Element(name = "pinData", required = false)
    var pinData: PinData? = null

    @field:Element(name = "receivingInstitutionId", required = false)
    var receivingInstitutionId: String = ""

    @field:Element(name = "keyLabel", required = false)
    var keyLabel: String = ""

    @field:Element(name = "originalAuthRef", required = false)
    var originalAuthId: String = ""

    @field:Element(name = "originalStan", required = false)
    var originalStan: String = ""

    @field:Element(name = "originalDateTime", required = false)
    var originalDateTime: String = ""

    @field:Element(name = "purchaseType", required = false)
    var purchaseType: String = ""

    @field:Element(name = "transactionId", required = false)
    var transactionId: String = ""

    @field:Element(name = "app", required = false)
    var app: String = ""

    @field:Element(name = "retrievalReferenceNumber", required = false)
    var retrievalReferenceNumber: String = ""

    @field:Element(name = "bankCbnCode", required = false)
    var bankCbnCode: String = ""

    @field:Element(name = "cardPan", required = false)
    var cardPan: String = ""

    @field:Element(name = "customerEmail", required = false)
    var customerEmail: String = ""

    @field:Element(name = "customerId", required = false)
    var customerId: String = ""

    @field:Element(name = "customerMobile", required = false)
    var customerMobile: String = ""

    @field:Element(name = "paymentCode", required = false)
    var paymentCode: String = ""

    @field:Element(name = "terminalId", required = false)
    var terminalId: String = ""

    @field:Element(name = "customerName", required = false)
    var customerName: String = ""

    @field:Element(name = "requestType", required = false)
    var requestType: String = ""

    @field:Element(name = "transactionRef", required = false)
    var transactionRef: String = ""

    @field:Element(name = "uuid", required = false)
    var uuid: String = ""

    @field:Element(name = "additionalAmounts", required = false)
    var additionalAmounts: String = ""

    @field:Element(name = "surcharge", required = false)
    var surcharge: String = ""

    @field:Element(name = "destinationAccountNumber", required = false)
    var destinationAccountNumber: String = ""

    @field:Element(name = "originalTransmissionDateTime", required = false)
    var originalTransmissionDateTime: String = ""

    @field:Element(name = "extendedTransactionType", required = false)
    var extendedTransactionType: String = ""

    @field:Element(name = "additionalInfo", required = false)
    var additionalInfo: String = ""

    @field:Element(name = "settlementCurrencyCode", required = false)
    var settlementCurrencyCode: String = ""

    @field:Element(name = "rate", required = false)
    var rate: String = ""

    @field:Element(name = "narration", required = false)
    var customerRef: String = ""
}



@Root(name = "CardData", strict = false)
internal class CardData {
    @field:Element(name = "cardSequenceNumber", required = false)
    var cardSequenceNumber: String = ""

    @field:Element(name = "emvData", required = false)
    var emvData: IccData? = null

    @field:Element(name = "mifareData", required = false)
    var mifareData: MiFareData? = null

    @field:Element(name = "track2", required = false)
    var track2: Track2? = null

    @field:Element(name = "wasFallback", required = false)
    var wasFallback: Boolean = false

    @field:Element(name = "cvv2", required = false)
    var cvv2: String? = null

    companion object {
        fun create(transactionInfo: TransactionInfo) = CardData().apply {
            cardSequenceNumber = transactionInfo.csn
            emvData = transactionInfo.iccData
            if(!transactionInfo.cvv2.isNullOrEmpty()){
                cvv2 = transactionInfo.cvv2
            }

            track2 = Track2().apply {
                pan = transactionInfo.cardPAN
                expiryMonth = transactionInfo.cardExpiry.takeLast(2)
                expiryYear = transactionInfo.cardExpiry.take(2)
                if (transactionInfo.cardTrack2.isNotEmpty()) {
                    track2 = let {

                        val neededLength = transactionInfo.cardTrack2.length - 2
                        val isVisa = transactionInfo.cardTrack2.startsWith('4')
                        val hasCharacter = transactionInfo.cardTrack2.last().isLetter()

                        // remove character suffix for visa
                        if (isVisa && hasCharacter) transactionInfo.cardTrack2.substring(0..neededLength)
                        else transactionInfo.cardTrack2
                    }
                }

            }

        }

        fun createCNP(transactionInfo: TransactionInfo) = CardData().apply {
            cardSequenceNumber = transactionInfo.csn
//            emvData = transactionInfo.iccData
            if(!transactionInfo.cvv2.isNullOrEmpty()){
                cvv2 = transactionInfo.cvv2
            }

            track2 = Track2().apply {
                pan = transactionInfo.cardPAN
                expiryMonth = transactionInfo.cardExpiry.takeLast(2)
                expiryYear = transactionInfo.cardExpiry.take(2)
                if (transactionInfo.cardTrack2.isNotEmpty()) {
                    track2 = let {

                        val neededLength = transactionInfo.cardTrack2.length - 2
                        val isVisa = transactionInfo.cardTrack2.startsWith('4')
                        val hasCharacter = transactionInfo.cardTrack2.last().isLetter()

                        // remove character suffix for visa
                        if (isVisa && hasCharacter) transactionInfo.cardTrack2.substring(0..neededLength)
                        else transactionInfo.cardTrack2
                    }
                }

            }

        }
    }
}

@Root(name = "MiFareData", strict = false)
internal class MiFareData {
    @field:Element(name = "cardSerialNo", required = false)
    var cardSerialNo: String = ""
}


@Root(name = "Track2", strict = false)
internal class Track2 {
    @field:Element(name = "pan", required = false)
    var pan: String = ""

    @field:Element(name = "expiryMonth", required = false)
    var expiryMonth: String = ""

    @field:Element(name = "expiryYear", required = false)
    var expiryYear: String = ""

    @field:Element(name = "track2", required = false)
    var track2: String? = ""
}


@Root(name = "Track2", strict = false)
internal class PinData {
    @field:Element(name = "ksnd", required = false)
    var ksnd: String = "605"

    @field:Element(name = "pinType", required = false)
    var pinType: String = "Dukpt"

    @field:Element(name = "ksn", required = false)
    var ksn: String = ""

    @field:Element(name = "pinBlock", required = false)
    var pinBlock: String = ""

    companion object {
        fun create(txnInfo: TransactionInfo, shortened: Boolean = false) = PinData().apply {
            ksn = if (shortened) {
                // For cash-out transfer requests, if the KSN is in full
                // it returns "Security Violation"
                txnInfo.pinKsn.substring(4)
            } else {
                txnInfo.pinKsn
            }
            pinBlock = txnInfo.cardPIN
        }

    }
}

