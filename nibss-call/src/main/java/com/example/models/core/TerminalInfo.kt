package com.example.models.core

import android.os.Parcelable
import com.example.Constants
import com.interswitchng.smartpos.shared.interfaces.library.KeyValueStore


/**
 * A data model representing the terminal information downloaded from the server, which
 * is then used to configure the any given POS terminal
 *
 * - terminalId: the specific id of a terminal in a merchant's store
 * - merchantId: the merchant's id
 * - countryCode: a code identifying the country that the terminal is processing transactions in
 * - currencyCode: a code indicating the currency of the country
 * - callHomeTimeInMin: an integer indicating the time (in minutes) required to continuously call home
 * - serverTimeoutInSec: an integer indicating the time (in seconds) to specify as a connection timeout
 */

@Parcelize
data class TerminalInfo(
    val terminalId: String = "",
    val merchantId: String = "",
    val merchantNameAndLocation: String = "",
    var merchantName: String = "",
    val merchantCategoryCode: String = "",
    val merchantAddress: String = "",
    val countryCode: String = "",
    val currencyCode: String = "",
    val callHomeTimeInMin: Int = 60,
    val serverTimeoutInSec: Int = 60,
    var isKimono: Boolean = false,
    val capabilities: String? = "E0F8C8",
    var merchantAlias: String = "",
    var merchantCode: String = "",
    var nibbsKey: String? = "",
    var tmsRouteType: String = "KIMONO_DEFAULT",
    var serverUrl: String = "",
    var serverIp: String = "",
    var serverPort: Int = 2000
) : Parcelable {


    internal fun persist(store: KeyValueStore): Boolean {
        // set default values
        if (serverUrl.isEmpty()) serverUrl = ""
        if (serverIp.isEmpty()) serverIp = ""
        if (serverPort == 0) serverPort = 4000

        // get previous terminal info
        val prevInfo = get(store)

        // save only when config changed
        if (prevInfo != this) {
            val jsonString = Gson().toJson(this)
            store.saveString(PERSIST_KEY, jsonString)
            return true
        }

        return false
    }

    override fun toString(): String {
        return """
            | terminalId: $terminalId
            | merchantId: $merchantId
            | merchantName: $merchantName
            | merchantNameAndLocation: $merchantNameAndLocation
            | merchantCategory: $merchantCategoryCode
            | merchantAddress: $merchantAddress
            | currencyCode: $currencyCode
            | callHomeTimeInMin: $callHomeTimeInMin
            | serverTimeoutInSec: $serverTimeoutInSec
            | isKimono: $isKimono 
            | capabilities: $capabilities 
            | merchantAlias: $merchantAlias 
            | merchantCode: $merchantCode 
            | serverIp: $serverIp
            | serverPort: $serverPort
            | tmsRouteType: $tmsRouteType
        """.trimIndent()
    }

    override fun hashCode(): Int {
        var result = 17
        result =
            31 * result + (terminalId.hashCode() + merchantId.hashCode() + merchantNameAndLocation.hashCode()
                    + merchantName.hashCode() + merchantCategoryCode.hashCode() + merchantAddress.hashCode()
                    + currencyCode.hashCode() + serverTimeoutInSec.hashCode() + isKimono.hashCode() + capabilities.hashCode()
                    + merchantAlias.hashCode() + merchantCode.hashCode() + serverIp.hashCode() + serverPort.hashCode() + tmsRouteType.hashCode())

        return result
    }

    companion object {


        fun defaultTerminalInfo(): TerminalInfo {
            return TerminalInfo(
                terminalId = "",
                merchantId = "",
                merchantNameAndLocation = "",
                merchantName = "",
                merchantCategoryCode = "",
                merchantAddress = "",
                countryCode = "",
                currencyCode = "",
                callHomeTimeInMin = 0,
                serverTimeoutInSec = 0
            )
        }

        private const val PERSIST_KEY = "terminal_data"

        internal fun get(store: KeyValueStore): TerminalInfo? {
            val jsonString = store.getString(PERSIST_KEY, "")
            return when (jsonString) {
                "" -> TerminalInfo()
                else -> Gson().fromJson(jsonString, TerminalInfo::class.java)
            }
        }

        internal fun updateTransactionCurrencyCode(store: KeyValueStore, currencyCode: String) {
            val terminalInfoJsonString = store.getString(PERSIST_KEY, "")

            val terminalInfo = when (terminalInfoJsonString) {
                "" -> TerminalInfo()
                else -> Gson().fromJson(terminalInfoJsonString, TerminalInfo::class.java)
            }

            val updatedInfo = terminalInfo.copy(currencyCode = currencyCode)
            store.saveString(PERSIST_KEY, Gson().toJson(updatedInfo))
        }
    }

}

