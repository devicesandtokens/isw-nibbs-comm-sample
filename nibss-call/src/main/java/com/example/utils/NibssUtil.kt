package com.interswitchng.smartpos.shared.utilities

import com.interswitchng.smartpos.shared.Constants

interface NibssSelector {
    fun onNibssSelected(selected: NibssSelected)
}

enum class NibssSelection(val selection: NibssSelected) {
    NUS_PROD ( NibssSelected(
        serverPort = "${Constants.ISW_TERMINAL_PORT_NUS_FOR_SETTINGS}",
        serverIp = Constants.ISW_TERMINAL_IP_NUS_FOR_SETTINGS, name = "NIBSS NUS",
        key = KeysUtils.NUS.productionCMS(), tmsRouteType = "NIBSS_NUS"
    )),
    NUS_TEST (NibssSelected(
        serverPort = "${Constants.ISW_TERMINAL_PORT_NUS_FOR_SETTINGS}",
        serverIp = Constants.ISW_TERMINAL_IP_NUS_FOR_SETTINGS, name = "NIBSS NUS TEST",
        key = KeysUtils.NUS.testCMS(), tmsRouteType = "NIBSS_NUS"
    )),
    UPSL_PROD ( NibssSelected(
        serverPort = "${Constants.ISW_TERMINAL_PORT_NUS_FOR_SETTINGS}",
        serverIp = Constants.ISW_TERMINAL_IP_NUS_FOR_SETTINGS, name = "UPSL",
        key = KeysUtils.NUS.productionCMS(), tmsRouteType = "UPSL"
    )),
    UPSL_TEST (NibssSelected(
        serverPort = "${Constants.ISW_TERMINAL_PORT_NUS_FOR_SETTINGS}",
        serverIp = Constants.ISW_TERMINAL_IP_NUS_FOR_SETTINGS, name = "UPSL TEST",
        key = KeysUtils.NUS.testCMS(), tmsRouteType = "UPSL"
    )),
     CTMS_PROD ( NibssSelected(
        serverPort = "${Constants.ISW_DEFAULT_TERMINAL_PORT}",
        serverIp = Constants.ISW_DEFAULT_TERMINAL_IP, name = "NIBSS CTMS",
        key = KeysUtils.CTMS.productionCMS(), tmsRouteType = "NIBSS_CTMS"
    )),
     CTMS_TEST (NibssSelected(
        serverPort = "${Constants.ISW_DEFAULT_TERMINAL_PORT}",
        serverIp = Constants.ISW_DEFAULT_TERMINAL_IP, name = "NIBSS CTMS TEST",
        key = KeysUtils.CTMS.testCMS(), tmsRouteType = "NIBSS_CTMS"
    )),
     EPMS_PROD (NibssSelected(
        serverPort = "${Constants.ISW_TERMINAL_PORT}",
        serverIp = Constants.ISW_TERMINAL_IP, name = "NIBSS EPMS",
        key = KeysUtils.EPMS.productionCMS(), tmsRouteType = "NIBSS_EPMS"
    )),
     EPMS_TEST (NibssSelected(
        serverPort = "${Constants.ISW_TERMINAL_PORT}",
        serverIp = Constants.ISW_TERMINAL_IP, name = "NIBSS EPMS TEST",
        key = KeysUtils.EPMS.testCMS(), tmsRouteType = "NIBSS_EPMS"
    ))
}

data class NibssSelected(
    var serverIp: String = "",
    var serverPort: String = "",
    var name : String = "",
    var key : String = "",
    var tmsRouteType : String = ""
)

fun checkWhichNibss(ip: String, port: String, route: String): NibssSelection {
    val selectedX = NibssSelected(
        serverIp = ip,
        serverPort = port,
        tmsRouteType = route
    )

    val listSelected = NibssSelection.values().filter {
        it.selection.serverIp == selectedX.serverIp
                && it.selection.serverPort == selectedX.serverPort
                && it.selection.tmsRouteType == selectedX.tmsRouteType
    }

    if (!listSelected.isNullOrEmpty()) {
        return listSelected.get(0)
    } else {
        return NibssSelection.NUS_PROD
    }
}
