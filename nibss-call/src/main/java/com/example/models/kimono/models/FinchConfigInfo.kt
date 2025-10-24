package com.interswitchng.smartpos.shared.services.kimono.models

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "allTerminalInfo", strict = false)
data class FinchConfigInfo(
    @field:Element(name = "responseCode", required = false)
    var responseCode: String = "",

    @field:Element(name = "responseMessage", required = false)
    var responseMessage: String = "",

    @field:Element(name = "data", required = false)
    var data: FinchData? = null
)


data class FinchData(
    @field:Element(name = "terminalInfo", required = false)
    var terminalInfo: FinchTerminalInfo? = null,

    @field:ElementList(name = "terminalAllowedTxTypes", inline = true, required = false)
    var terminalAllowedTxTypes: List<String>? = null,

    @field:Element(name = "acquirerLogo", required = false)
    var acquirerLogo: AcquirerLogo? = null,
)

@Root(name = "terminalInfoBySerials", strict = false)
class FinchTerminalInfo(

    @field:Element(name = "terminalCode", required = false)
    var terminalCode: String = "",

    @field:Element(name = "cardAcceptorId", required = false)
    var cardAcceptorId: String = "",

    @field:Element(name = "merchantId", required = false)
    var merchantId: String = "",

    @field:Element(name = "merchantName", required = false)
    var merchantName: String = "",

    @field:Element(name = "merchantAddress1", required = false)
    var merchantAddress1: String = "",

    @field:Element(name = "merchantPhoneNumber", required = false)
    var merchantPhoneNumber: String = "",

    @field:Element(name = "merchantEmail", required = false)
    var merchantEmail: String = "",

    @field:Element(name = "merchantState", required = false)
    var merchantState: String = "",

    @field:Element(name = "merchantCity", required = false)
    var merchantCity: String = "",

    @field:Element(name = "cardAcceptorNameLocation", required = false)
    var cardAcceptorNameLocation: String = ""
)