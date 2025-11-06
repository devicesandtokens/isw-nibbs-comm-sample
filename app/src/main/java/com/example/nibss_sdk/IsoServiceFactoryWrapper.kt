package com.example.nibss_sdk

import android.content.Context
import com.example.IsoServiceFactory
import com.example.interfaces.IsoServiceListener
import com.example.interfaces.library.IsoService
import com.example.models.NibssConfig
import com.interswitchng.smartpos.shared.interfaces.library.KeyValueStore
import javax.inject.Inject

class IsoServiceFactoryWrapper @Inject constructor(
    private val context: Context,
    private val store: KeyValueStore,
    private val nibssConfig: NibssConfig
) {
    fun create(listener: IsoServiceListener): IsoService {
        return IsoServiceFactory.createNIBSS(
            context = context,
            store = store,
            nibssConfig = nibssConfig,
            listener = listener
        )
    }
}