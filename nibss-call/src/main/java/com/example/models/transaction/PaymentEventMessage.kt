package com.interswitchng.smartpos.shared.models.transaction

import android.os.Parcel
import android.os.Parcelable

data class PaymentEventMessageBase(
    val root: ArrayList<PaymentEventMessage>
)

data class PaymentEventMessage(
    val messageId: String,
    val payload: PaymentPayload
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readParcelable(PaymentPayload::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(messageId)
        parcel.writeParcelable(payload, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PaymentEventMessage> {
        override fun createFromParcel(parcel: Parcel): PaymentEventMessage {
            return PaymentEventMessage(parcel)
        }

        override fun newArray(size: Int): Array<PaymentEventMessage?> {
            return arrayOfNulls(size)
        }
    }
}

data class PaymentPayload(
    val id: Long,
    val merchantCode: String,
    val payableId: Long,
    val payableCode: String? = "",
    val amount: Long,
    val remittanceAmount: Long,
    val dateOfPayment: Long,
    val currencyCode: String? = "",
    val transactionReference: String,
    val responseCode: String,
    val responseDescription: String,
    val surcharge: Long,
    val channel: String? = "",
    val encryptedAccountNumber: String? = "",
    val remittanceStatus: String? = "",
    val settlementStatus: String? = "",
    val acquirerCode: String? = "",
    val feeCode: String? = "",
    val feeType: String? = "",
    val bankCode: String? = "",
    val channelTransactionReference: String? = "",
    val remittanceBankCode: String? = "",
    val transactionAttemptCount: Long,
    val remittanceAccountNo: String? = "",
    val remittanceAccountType: String? = "",
    val accountType: String? = "",
    val retrievalReferenceNumber: String? = "",
    val merchantCustomerName: String? = "",
    val merchantCustomerId: String? = "",
    val merchantName: String? = "",
    val bankName: String? = "",
    val nonCardProviderId: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(merchantCode)
        parcel.writeLong(payableId)
        parcel.writeString(payableCode)
        parcel.writeLong(amount)
        parcel.writeLong(remittanceAmount)
        parcel.writeLong(dateOfPayment)
        parcel.writeString(currencyCode)
        parcel.writeString(transactionReference)
        parcel.writeString(responseCode)
        parcel.writeString(responseDescription)
        parcel.writeLong(surcharge)
        parcel.writeString(channel)
        parcel.writeString(encryptedAccountNumber)
        parcel.writeString(remittanceStatus)
        parcel.writeString(settlementStatus)
        parcel.writeString(acquirerCode)
        parcel.writeString(feeCode)
        parcel.writeString(feeType)
        parcel.writeString(bankCode)
        parcel.writeString(channelTransactionReference)
        parcel.writeString(remittanceBankCode)
        parcel.writeLong(transactionAttemptCount)
        parcel.writeString(remittanceAccountNo)
        parcel.writeString(remittanceAccountType)
        parcel.writeString(accountType)
        parcel.writeString(retrievalReferenceNumber)
        parcel.writeString(merchantCustomerName)
        parcel.writeString(merchantCustomerId)
        parcel.writeString(merchantName)
        parcel.writeString(bankName)
        parcel.writeString(nonCardProviderId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PaymentPayload> {
        override fun createFromParcel(parcel: Parcel): PaymentPayload {
            return PaymentPayload(parcel)
        }

        override fun newArray(size: Int): Array<PaymentPayload?> {
            return arrayOfNulls(size)
        }
    }
}

data class PaymentEventAck(
    val messageId: String,
    val acknowledged: Boolean = true
)

data class AcknowledgementWrapper(
    val event: String,
    val data: PaymentEventAck
)