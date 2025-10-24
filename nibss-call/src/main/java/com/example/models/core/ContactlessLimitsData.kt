package com.interswitchng.smartpos.shared.models.core

import com.interswitchng.smartpos.shared.Constants

data class ContactlessLimitsData(
    var acquirerCode: String = Constants.CONTACTLESS_ACQUIRER_CODE,
    var contactlessTransLimit: String  = Constants.CONTACTLESS_TRANS_LIMIT.toString(),
    var contactlessCvmLimit: String  = Constants.CONTACTLESS_CVM_LIMIT.toString(),
    var contactlessFloorLimit: String  = Constants.CONTACTLESS_FLOOR_LIMIT.toString(),
    var contactlessTransNoDeviceLimit: String  = Constants.CONTACTLESS_TRANS_NO_DEVICE_LIMIT.toString(),
)