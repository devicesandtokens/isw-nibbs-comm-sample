package com.example.models.transaction



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

        return string.uppercase()
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
    }

}