package com.example.nibss_sdk.data

import com.example.interfaces.library.IsoService

class MainRepository(private val isoService: IsoService) {

    fun runKeyDownload(terminalId: String, ip: String, port: Int) {

        val response = isoService.downloadKey(terminalId, ip, port)

    }

}