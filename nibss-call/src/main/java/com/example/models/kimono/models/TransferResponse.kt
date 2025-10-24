package com.interswitchng.smartpos.shared.services.kimono.models

import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Root

@Root(name = "transferResponse", strict = false)
@NamespaceList(
    Namespace( prefix = "ns2", reference = "http://interswitchng.com"),
    Namespace( prefix = "ns3", reference = "http://tempuri.org/ns.xsd")
)
internal data class TransferResponse(
    @field:Element(name = "description", required = false)
    var description: String = "",

    @field:Element(name = "field39", required = false)
    var responseCode: String = "",

    @field:Element(name = "authId", required = false)
    var authId: String = "",

    @field:Element(name = "hostEmvData", required = false)
    var hostEmvData: HostEmvData = HostEmvData(),

    @field:Element(name = "referenceNumber", required = false)
    var referenceNumber: String = "",

    @field:Element(name = "stan", required = false)
    var stan: String = "",

    @field:Element(name = "transactionChannelName", required = false)
    var transactionChannelName: String = "",

    @field:Element(name = "wasReceive", required = false)
    var wasReceive: Boolean = false,

    @field:Element(name = "wasSend", required = false)
    var wasSend: Boolean = false
)

@Root(name = "hostEmvData")
data class HostEmvData(
    @field:Element(name = "AmountAuthorized")
    var amountAuthorized: Int = 0,

    @field:Element(name = "AmountOther")
    var amountOther: Int = 0,

    @field:Element(name = "atc")
    var atc: String = "",

    @field:Element(name = "iad")
    var iad: String = "",

    @field:Element(name = "rc")
    var rc: String = ""
)