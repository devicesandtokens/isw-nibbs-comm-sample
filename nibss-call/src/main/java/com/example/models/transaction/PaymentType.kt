package com.interswitchng.smartpos.shared.models.transaction

import com.example.isw_smart_till.enums.TillPaymentOptions


//import com.isw_smart_bluetooth.enums.TillPaymentOptions


/**
 * This enum class represents the different
 * payment methods the SDK supports
 */
enum class PaymentType {
    PayCode,
    ThankYouCash,
    Card,
    QR,
    USSD,
    Options,
    Cash,
    Transfer,
    CNP,
    Web;

    override fun toString(): String {
        val string = when (QR) {
            this -> "QR Code"
            else -> super.toString()
        }

        return string.toUpperCase()
    }

    companion object {
        fun fromString(stringType: String): PaymentType {
            return when(stringType) {
                "PayCode" -> PayCode
                "ThankYouCash" -> ThankYouCash
                "Card" -> Card
                "QR" -> QR
                "USSD" -> USSD
                "Cash" -> Cash
                "Transfer" -> Transfer
                "CNP" -> CNP
                "QR Code" -> QR
                else -> Options
            }
        }

        fun fromTillPaymentType(type: TillPaymentOptions): PaymentType {
            return when(type) {
                TillPaymentOptions.CARD -> Card
                TillPaymentOptions.PAYCODE -> PayCode
                TillPaymentOptions.TRANSFER -> Transfer
                TillPaymentOptions.QR -> QR
                TillPaymentOptions.USSD -> USSD
                TillPaymentOptions.CASH -> Cash
                else -> Card
            }
        }
    }

}