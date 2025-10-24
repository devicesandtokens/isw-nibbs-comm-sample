package com.interswitchng.smartpos.shared.models.core

data class AdditionalInfo(val surcharge: String?, val additionalAmounts: String?){

    companion object{
        internal fun toHashMap(additionalInfo: AdditionalInfo): HashMap<String, String?> {
            val keyValue: HashMap<String, String?> = HashMap()
            keyValue["surcharge"] = additionalInfo.surcharge
            keyValue["additionalAmounts"] = additionalInfo.additionalAmounts

            return  keyValue
        }
    }
}