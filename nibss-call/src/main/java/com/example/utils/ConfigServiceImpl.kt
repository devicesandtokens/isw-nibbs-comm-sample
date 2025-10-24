package com.interswitchng.smartpos.shared.services.utils

import com.interswitchng.smartpos.shared.interfaces.library.ConfigService
import com.interswitchng.smartpos.shared.interfaces.retrofit.IFinchConfigService
import com.interswitchng.smartpos.shared.interfaces.retrofit.IKimonoConfigService
import com.interswitchng.smartpos.shared.interfaces.retrofit.IQtbConfigService
import com.interswitchng.smartpos.shared.services.kimono.models.AutoConfigInfo
import com.interswitchng.smartpos.shared.services.kimono.models.FinchConfigInfo
import com.interswitchng.smartpos.shared.services.qtb.TillConfigResponse
import com.example.utils.Logger
import com.interswitchng.smartpos.shared.utils.None
import com.interswitchng.smartpos.shared.utils.Optional
import com.interswitchng.smartpos.shared.utils.Some
import com.interswitchng.smartpos.simplecalladapter.Simple
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class ConfigServiceImpl(
    private val kimonoConfigService: IKimonoConfigService,
    private val qtbConfigService: IQtbConfigService,
    private val finchConfigService: IFinchConfigService
): ConfigService {

    val logger by lazy { Logger.with(this.javaClass.name) }

    override suspend fun callHome(serialNo: String): Optional<AutoConfigInfo> {
        val response = kimonoConfigService.getMerchantDetailsAuto(serialNo).await()
        val configResponse = response.first

        return when (configResponse) {
            null -> None
            else -> Some(configResponse)
        }
    }

    override suspend fun callQTB(merchantAlias: String): Optional<TillConfigResponse> {
        val response = qtbConfigService.getQTBConfig(merchantAlias).await()
        val configResponse = response.first

        return when (configResponse) {
            null -> None
            else -> Some(configResponse)
        }
    }

    override suspend fun callFinch(serialNo: String): Optional<FinchConfigInfo> {
        val response = finchConfigService.getFinchConfig(serialNo).await()
        val configResponse = response.first

        return when (configResponse) {
            null -> None
            else -> Some(configResponse)
        }
    }

    private suspend fun <T> Simple<T>.await(): Pair<T?, String?> {
        return suspendCoroutine { continuation ->
            process { response, t ->
                val message =  t?.message ?: t?.localizedMessage

                // log errors
                if (message != null) logger.log(message)
                // pair result and error
                val result = Pair(response, message)
                // return response
                continuation.resume(result)
            }
        }
    }
}