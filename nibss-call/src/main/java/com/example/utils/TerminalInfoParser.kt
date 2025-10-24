package com.interswitchng.smartpos.shared.services.utils

import com.interswitchng.smartpos.shared.interfaces.library.KeyValueStore
import com.example.models.core.TerminalInfo

internal object TerminalInfoParser {

    internal data class TerminalData(
            internal val tag: String,
            internal val len: Int,
            internal val value: String)


    fun parse(terminalId: String, ip: String, port: Int, rawData: String, store: KeyValueStore): TerminalInfo? {

        val parametersLists = mutableListOf<MutableList<TerminalData>>()
        var terminalParameters: MutableList<TerminalData> = ArrayList()
        try {

            var tmp = rawData
            while (tmp.isNotEmpty()) {
                println("this is not empty")
                val tag = tmp.substring(0, 2)
                tmp = tmp.substring(2)

                val len = Integer.parseInt(tmp.substring(0, 3))
                tmp = tmp.substring(3)
                val value = tmp.substring(0, len)

                tmp = tmp.substring(len)
                val tlv = TerminalData(tag, len, value)
                terminalParameters.add(tlv)

                val tmpLen = tmp.length
                val delim = if (tmpLen > 0) tmp.substring(0, 1) else ""
                if (delim.equals("~", ignoreCase = true) || tmpLen == 0) {
                    parametersLists.add(terminalParameters)
                    tmp = if (tmpLen > 0) tmp.substring(1) else tmp
                    terminalParameters = ArrayList()
                }
            }

            if (parametersLists.size > 0) {
                terminalParameters = parametersLists[0]

                val map = mutableMapOf<String, Any>()

                for (tlv in terminalParameters) {
                    when {
                        "03" == tlv.tag -> map["03"] = tlv.value
                        "04" == tlv.tag -> map["04"] = Integer.parseInt(tlv.value)
                        "05" == tlv.tag -> map["05"] = "0" + tlv.value
                        "06" == tlv.tag -> map["06"] = "0" + tlv.value
                        "07" == tlv.tag -> map["07"] = Integer.parseInt(tlv.value) * 60
                        "08" == tlv.tag -> map["08"] = tlv.value
                        "52" == tlv.tag -> map["52"] = tlv.value
                    }
                }

                val savedTerminalInfo = TerminalInfo.get(store)
                var terminalInfo = createOrUpdateTerminalInfo(savedTerminalInfo, terminalId, ip, port, map)

                if (terminalInfo.countryCode.length >= 4) {
                    terminalInfo = terminalInfo.copy(countryCode = terminalInfo.countryCode.substring(1, terminalInfo.countryCode.length))
                }
                if (terminalInfo.currencyCode.length >= 4) {
                    terminalInfo = terminalInfo.copy(currencyCode = terminalInfo.currencyCode.substring(1, terminalInfo.currencyCode.length))
                }
                println("this is terminal info : $terminalInfo")
                return terminalInfo
            } else {
                println("this is getting here")
                return null
            }


        } catch (ex: Exception) {
            println("this is getting here for exception")
            println(ex)
            return null
        }
    }

    private fun createOrUpdateTerminalInfo(savedTerminalInfo: TerminalInfo?, terminalId: String,
                                           ip: String, port: Int, map: MutableMap<String, Any>): TerminalInfo {
        savedTerminalInfo?.let {
            return savedTerminalInfo.copy(
                    terminalId = terminalId,
                    merchantId = map["03"] as String,
                    currencyCode = map["05"] as String,
                    countryCode = map["06"] as String,
                    serverTimeoutInSec = map["04"] as Int,
                    callHomeTimeInMin = map["07"] as Int,
                    merchantCategoryCode = map["08"] as String,
                    merchantNameAndLocation = map["52"] as String,
                    merchantAddress = it.merchantAddress,
                    serverIp = ip,
                    serverPort = port,
                    capabilities = savedTerminalInfo.capabilities,
                    isKimono = false

            )
        } ?: run {
            return TerminalInfo(
                    terminalId = terminalId,
                    merchantId = map["03"] as String,
                    currencyCode = map["05"] as String,
                    countryCode = map["06"] as String,
                    serverTimeoutInSec = map["04"] as Int,
                    callHomeTimeInMin = map["07"] as Int,
                    merchantCategoryCode = map["08"] as String,
                    merchantNameAndLocation = map["52"] as String,
                    serverIp = ip,
                    serverPort = port,
                    capabilities = "E0F8C8",
                    isKimono = false
            )
        }
    }
}