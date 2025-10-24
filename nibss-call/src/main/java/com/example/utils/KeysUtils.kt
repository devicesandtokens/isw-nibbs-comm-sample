package com.example.utils

import com.interswitchng.smartpos.shared.models.core.CMS


object KeysUtils {

    fun productionKSN() = "0000000002DDDDE00000" //FFFF000002DDDDE0
    fun testKSN() = "0000000006DDDDE00000"
    fun productionIPEK() = "3F2216D8297BCE9C"
    fun testIPEK() = "A9BA3B7822EC9462" //"9F8011E7E71E483B"

    fun productionCMS(cms: CMS): String {
        return when (cms) {
            CMS.NUS -> NUS.productionCMS()
            CMS.EMPS -> EPMS.productionCMS()
            CMS.UPSL -> UPSL.productionCMS()
            else -> CTMS.productionCMS()
        }
    }

    fun testCMS(cms: CMS): String{
        return when (cms) {
            CMS.NUS -> NUS.testCMS()
            CMS.EMPS -> EPMS.testCMS()
            CMS.UPSL -> UPSL.testCMS()
            else -> CTMS.testCMS()
        }
    }

    object EPMS {
        fun productionCMS() = "A050F63AFF366A4B0588D818D23C6C77"
        fun testCMS() = "DBEECACCB4210977ACE73A1D873CA59F"
    }

    object CTMS {
        fun productionCMS() = "3CDDE1CC6FDD225C9A8BC3EB065509A6"//"A050F63AFF366A4B0588D818D23C6C77"
        fun testCMS() = "DBEECACCB4210977ACE73A1D873CA59F" //EPMS=DBEECACCB4210977ACE73A1D873CA59F
    }

    object NUS {
        fun productionCMS() = "3CDDE1CC6FDD225C9A8BC3EB065509A6" //"56DBE87F76249B3CFC4D799681E47D09"//"3CDDE1CC6FDD225C9A8BC3EB065509A6"
        fun testCMS() = "DBEECACCB4210977ACE73A1D873CA59F"
    }

    object UPSL {
        fun productionCMS() = "A6D81DFFD2A5057E8B849F6C81273C42"
        fun testCMS() = "A6D81DFFD2A5057E8B849F6C81273C42"
    }
}
