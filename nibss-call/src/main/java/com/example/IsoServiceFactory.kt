package com.example

import android.content.Context
import com.example.interfaces.IsoServiceListener
import com.example.interfaces.device.POSDevice
import com.example.interfaces.library.IsoService
import com.example.models.NibssConfig
import com.example.nibss_call.NibssIsoServiceImpl
import com.example.nibss_call.tcp.IsoSocketImpl
import com.interswitchng.smartpos.shared.interfaces.library.IsoSocket
import com.interswitchng.smartpos.shared.interfaces.library.KeyValueStore

object IsoServiceFactory {

    fun createNIBSS(
        context: Context,
        store: KeyValueStore,
        nibssConfig: NibssConfig,
        posDevice: POSDevice? = null,
        listener: IsoServiceListener? = null
    ): IsoService {
        val socketToUse = IsoSocketImpl(nibssConfig.ip, nibssConfig.port, 6000)
        return NibssIsoServiceImpl(context, store, socketToUse, listener, posDevice)
    }

}