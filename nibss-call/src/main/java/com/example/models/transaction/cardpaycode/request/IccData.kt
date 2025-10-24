package com.interswitchng.smartpos.shared.models.transaction.cardpaycode.request

import com.example.models.core.TerminalInfo
import org.simpleframework.xml.Element

class IccData(
        @field:Element(name = "AmountAuthorized")
        var TRANSACTION_AMOUNT: String = "",
        @field:Element(name = "AmountOther")
        var ANOTHER_AMOUNT: String = "000000000000",
        @field:Element(name = "ApplicationInterchangeProfile")
        var APPLICATION_INTERCHANGE_PROFILE: String = "",
        @field:Element(name = "atc")
        var APPLICATION_TRANSACTION_COUNTER: String = "",
        @field:Element(name = "Cryptogram")
        var AUTHORIZATION_REQUEST: String = "",
        @field:Element(name = "CryptogramInformationData")
        var CRYPTOGRAM_INFO_DATA: String = "",
        @field:Element(name = "CvmResults")
        var CARD_HOLDER_VERIFICATION_RESULT: String = "",
        @field:Element(name = "iad")
        var ISSUER_APP_DATA: String = "",
        @field:Element(name = "TransactionCurrencyCode")
        var TRANSACTION_CURRENCY_CODE: String = "",
        @field:Element(name = "TerminalVerificationResult")
        var TERMINAL_VERIFICATION_RESULT: String = "",
        @field:Element(name = "TerminalCountryCode")
        var TERMINAL_COUNTRY_CODE: String = "",
        @field:Element(name = "TerminalType")
        var TERMINAL_TYPE: String = "",
        @field:Element(name = "TerminalCapabilities")
        var TERMINAL_CAPABILITIES: String = "",
        @field:Element(name = "TransactionDate")
        var TRANSACTION_DATE: String = "",
        @field:Element(name = "TransactionType")
        var TRANSACTION_TYPE: String = "",
        @field:Element(name = "UnpredictableNumber")
        var UNPREDICTABLE_NUMBER: String = "",
        @field:Element(name = "DedicatedFileName")
        var DEDICATED_FILE_NAME: String = "",
        @field:Element(name = "CardHolderName")
        var CARD_HOLDER_NAME: String = "",
        @field:Element(name = "FormFactorIndicator")
        var FORM_FACTOR_INDICATOR: String = "") {

        var INTERFACE_DEVICE_SERIAL_NUMBER: String = ""
        var APP_VERSION_NUMBER: String = ""
        var APP_PAN_SEQUENCE_NUMBER: String = ""
        var iccAsString: String = ""
        var TERMINAL_ENTRY_POINT: String = ""


        companion object {
                 fun getIcc(terminalInfo: TerminalInfo, amount: String, date: String): IccData {
                        val authorizedAmountTLV = String.format("9F02%02d%s", amount.length / 2, amount)
                        val transactionDateTLV = String.format("9A%02d%s", date.length / 2, date)
                        val iccData =
                                "9F260831BDCBC7CFF6253B9F2701809F3704F435D8A29F36020527950508800000009F10120110A50003020000000000000000000000FF" +
                                        "${transactionDateTLV}9C0100${authorizedAmountTLV}5F2A020566820238009F1A0205669F34034103029F3303E0D0F89F3501229F0306000000000000"

                        // remove leadin zero if exits
                        val currencyCode =
                                if (terminalInfo.currencyCode.length > 3) terminalInfo.currencyCode.substring(1) else terminalInfo.currencyCode
                        val countryCode =
                                if (terminalInfo.countryCode.length > 3) terminalInfo.countryCode.substring(1) else terminalInfo.countryCode



                        return IccData().apply {
                                TRANSACTION_AMOUNT = amount
                                ANOTHER_AMOUNT = "000000000000"
                                APPLICATION_INTERCHANGE_PROFILE = "3800"
                                APPLICATION_TRANSACTION_COUNTER = "0527"
                                CRYPTOGRAM_INFO_DATA = "80"
                                CARD_HOLDER_VERIFICATION_RESULT = "410302"
                                ISSUER_APP_DATA = "0110A50003020000000000000000000000FF"
                                TRANSACTION_CURRENCY_CODE = currencyCode
                                TERMINAL_VERIFICATION_RESULT = "0880000000"
                                TERMINAL_COUNTRY_CODE = countryCode
                                TERMINAL_TYPE = "22"
                                TERMINAL_CAPABILITIES = terminalInfo.capabilities ?: "E050C8"
                                TRANSACTION_DATE = date
                                TRANSACTION_TYPE = "00"
                                UNPREDICTABLE_NUMBER = "F435D8A2"
                                DEDICATED_FILE_NAME = ""
                                AUTHORIZATION_REQUEST = "31BDCBC7CFF6253B"

                                iccAsString = iccData
                        }

                }
        }
}

