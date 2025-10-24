package com.interswitchng.smartpos.shared.models.transaction

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.Locale


/**
 * This class represents the purchase request
 * triggered by external source that depends the SDK
 */
@Parcelize
data class IswPaymentInfo(
    val amount: Long,
    val currentStan: String,
    val rrn: String,
    val surcharge: Int = 0,
    val additionalAmounts: Int = 0,
    val currencyType: TransactionCurrencyType = TransactionCurrencyType.NAIRA,
    val merchantUniqueRef: String = "",
    val transactionRemark: String = "",
) : Parcelable {


    val amountString: String
        get() = String.format(
            Locale.getDefault(), "%,.2f",
            amount.toDouble() / 100.toDouble()
        )

    val additionalAmountsString: String
        get() = String.format(
            Locale.getDefault(), "%,.2f",
            additionalAmounts.toDouble() / 100.toDouble()
        )

    val surchargeString: String
        get() = String.format(
            Locale.getDefault(), "%,.2f",
            surcharge.toDouble() / 100.toDouble()
        )
}