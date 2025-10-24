package com.interswitchng.smartpos.shared.services.kimono.models

import com.interswitchng.smartpos.shared.Constants
import com.interswitchng.smartpos.shared.models.core.CMS
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "allTerminalInfo", strict = false)
data class AllTerminalInfo (
        @field:Element(name = "responseCode", required = false)
        var responseCode: String = "",

        @field:Element(name = "responseMessage", required = false)
        var responseMessage: String = "",

        @field:ElementList(name = "terminalAllowedTxTypes", inline=true, required = false)
//        var terminalAllowedTxTypes: List<TerminalAllowedTxTypes>? = null,
        var terminalAllowedTxTypes: List<String>? = null,

        @field:Element(name = "terminalInfoBySerials", required = false)
        var terminalInfoBySerials: TerminalInfoBySerials? = null,

        @field:Element(name = "acquirerLogo", required = false)
        var acquirerLogo: AcquirerLogo? = null,

        @field:Element(name = "tmsRouteTypeConfig", required = false)
        var tmsRouteTypeConfig: TmsRouteTypeConfig? = null,

        @field:Element(name = "contactlessLimits", required = false)
        var contactlessLimits: ContactlessLimits? = null,

        @field:Element(name = "merchantSettings", required = false)
        var merchantSettings: MerchantSettings? = null
)

@Root(name = "terminalInfoBySerials", strict = false)
class TerminalInfoBySerials(

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

        @field:Element(name = "merchantAddress2", required = false)
        var merchantAddress2: String = "",

        @field:Element(name = "merchantPhoneNumber", required = false)
        var merchantPhoneNumber: String = "",

        @field:Element(name = "merchantEmail", required = false)
        var merchantEmail: String = "",

        @field:Element(name = "merchantState", required = false)
        var merchantState: String = "",

        @field:Element(name = "tmsRouteType", required = false)
        var tmsRouteType: String = "",

        @field:Element(name = "merchantCity", required = false)
        var merchantCity: String = "",

        @field:Element(name = "qtbMerchantCode", required = false)
        var qtbMerchantCode: String? = "",

        @field:Element(name = "qtbMerchantAlias", required = false)
        var qtbMerchantAlias: String? = "",

        @field:Element(name = "cardAcceptorNameLocation", required = false)
        var cardAcceptorNameLocation: String = "",

        @field:Element(name = "merchantCategoryCode", required = false)
        var merchantCategoryCode: String = "")

@Root(name = "terminalAllowedTxTypes", strict = false)
class TerminalAllowedTxTypes(
        @field:Element(name = "applicationDescription", required = false)
        var applicationDescription: String = ""
)

@Root(name = "tmsRouteTypeConfig", strict = false)
class TmsRouteTypeConfig(

        @field:Element(name = "port", required = false)
        var port: String = "${Constants.ISW_DEFAULT_TERMINAL_PORT}",

        @field:Element(name = "ip", required = false)
        var ip: String = Constants.ISW_DEFAULT_TERMINAL_IP,

        @field:Element(name = "key", required = false)
        var key: String = Constants.getCMS(CMS.NUS),

        @field:Element(name = "name", required = false)
        var name: String = "")


