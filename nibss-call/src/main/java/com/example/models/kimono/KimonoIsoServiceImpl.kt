package com.interswitchng.smartpos.shared.services.kimono

import com.interswitchng.smartpos.IswPos
import android.util.Log
import com.interswitchng.smartpos.IswPos.Companion.getNextStan
import com.interswitchng.smartpos.shared.Constants
import com.interswitchng.smartpos.shared.interfaces.device.POSDevice
import com.interswitchng.smartpos.shared.interfaces.library.IsoService
import com.interswitchng.smartpos.shared.interfaces.library.KeyValueStore
import com.interswitchng.smartpos.shared.interfaces.retrofit.IKimonoConfigService
import com.interswitchng.smartpos.shared.interfaces.retrofit.IKimonoHttpService
import com.interswitchng.smartpos.shared.models.core.AdditionalInfo
import com.example.models.core.TerminalInfo
import com.interswitchng.smartpos.shared.models.core.TransactionType
import com.interswitchng.smartpos.shared.models.transaction.IswPaymentInfo
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.CardDetail
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.CardType
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.request.AccountType
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.request.IccData
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.request.PurchaseType
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.request.TransactionInfo
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.response.TransactionResponse
import com.interswitchng.smartpos.shared.models.transaction.payments.billpayment.InquiryResponse
import com.interswitchng.smartpos.shared.models.transaction.payments.billpayment.IswBillPaymentInfo
import com.interswitchng.smartpos.shared.services.kimono.models.AllTerminalInfo
import com.example.models.kimono.models.BalanceRequest
import com.example.models.kimono.models.BillPaymentRequest
import com.example.models.kimono.models.CashOutRequest
import com.example.models.kimono.models.CompletionRequest
import com.example.models.kimono.models.PreAuthRequest
import com.example.models.kimono.models.PurchaseRequest
import com.example.models.kimono.models.RefundRequest
import com.interswitchng.smartpos.shared.services.kimono.models.ReversalRequest
import com.example.models.kimono.models.TransactionRequest
import com.example.models.kimono.models.TransferRequest
import com.interswitchng.smartpos.shared.services.kimono.models.convertAutoToAllTerminalInfo
import com.interswitchng.smartpos.shared.services.utils.DateUtils
import com.interswitchng.smartpos.shared.services.utils.IsoUtils
import com.example.utils.Logger
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.util.Base64 as AndroidBase64

internal class  KimonoIsoServiceImpl(
    private val device: POSDevice,
    private val store: KeyValueStore,
    private val httpService: IKimonoHttpService,
    private val iKimonoConfigService: IKimonoConfigService
) : IsoService {


    private val logger by lazy { Logger.with("KimonoIsoServiceImpl") }


    /**
     * Uses the provided terminalId to perform key exchange with the EPMS server
     *
     * @param terminalId  a string representing the configured terminal id
     * @param ip the ip address to download terminal parameters from
     * @param port the port number for the ip address
     * @return     boolean expression indicating the success or failure status of the key exchange
     */
    override fun downloadKey(terminalInfo: TerminalInfo, ip: String, port: Int): Boolean {

        // load test keys
        val tik = Constants.ISW_DUKPT_IPEK
        val ksn = Constants.ISW_DUKPT_KSN

        // load keys
        device.loadInitialKey(tik, ksn)
        return true
    }


    /**
     * Initiates a card transaction using the provided terminal and transaction info, and returns the
     * transaction response provided by EPMS
     *
     * @param terminalInfo  the necessary information that identifies the current POS terminal
     * @param transaction  the purchase information required to perform the transaction
     * @return   response status indicating transaction success or failure
     */
    override fun initiateCardPurchase(
        terminalInfo: TerminalInfo,
        transaction: TransactionInfo
    ): TransactionResponse? {
        // generate purchase request
        val request = TransactionRequest.create(
            TransactionType.Purchase,
            device,
            terminalInfo,
            transaction
        ) as PurchaseRequest


        val now = Date()
        request.pinData?.apply {
            // remove first 2 bytes to make 8 bytes
            if (ksn.isNotEmpty()) {
                ksn = ksn.substring(4)
            }
        }

        try {
            val response = httpService.makePurchase(terminalInfo.serverUrl, request).run()
            val data = response.body()
            //throw Exception("This is a test exception")
            return if (!response.isSuccessful || data == null) {
                TransactionResponse(
                    responseCode = response.code().toString(),
                    authCode = "",
                    stan = transaction.stan,
                    scripts = "",
                    responseDescription = response.message(),
                    date = now.time,
                    rrn = transaction.rrn
                )
            } else {
                TransactionResponse(
                    responseCode = data.responseCode,
                    stan = transaction.stan,
                    authCode = data.authCode,
                    scripts = "",
                    responseDescription = data.description,
                    date = now.time,
                    rrn = data.referenceNumber
                )
            }

        } catch (e: Exception) {
            reportException(e)
            logger.log(e.localizedMessage)
            e.printStackTrace()
            //return reversePurchase(terminalInfo, request)
            return TransactionResponse(
                IsoUtils.TIMEOUT_CODE,
                authCode = "",
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            )
//            return TransactionResponse(
//                responseCode = "20",
//                authCode = "",
//                stan = transaction.stan,
//                scripts = "",
//                date = now.time,
//            )
        }
    }

    override fun initiateCardNotPresentPurchase(
        terminalInfo: TerminalInfo,
        transaction: TransactionInfo
    ): TransactionResponse? {
        val request = TransactionRequest.createCNP(
            TransactionType.Purchase,
            device,
            terminalInfo,
            transaction
        ) as PurchaseRequest


        val now = Date()
        request.pinData?.apply {
            // remove first 2 bytes to make 8 bytes
            if (ksn.isNotEmpty()) {
                ksn = ksn.substring(4)
            }
        }

        try {
            val response = httpService.makePurchase(terminalInfo.serverUrl, request).run()
            val data = response.body()
            //throw Exception("This is a test exception")
            return if (!response.isSuccessful || data == null) {
                TransactionResponse(
                    responseCode = response.code().toString(),
                    authCode = "",
                    stan = transaction.stan,
                    scripts = "",
                    responseDescription = response.message(),
                    date = now.time,
                    rrn = transaction.rrn
                )
            } else {
                TransactionResponse(
                    responseCode = data.responseCode,
                    stan = transaction.stan,
                    authCode = data.authCode,
                    scripts = "",
                    responseDescription = data.description,
                    date = now.time,
                    rrn = data.referenceNumber
                )
            }

        } catch (e: Exception) {
            reportException(e)
            logger.log(e.localizedMessage)
            e.printStackTrace()
            //return reversePurchase(terminalInfo, request)
            return TransactionResponse(
                IsoUtils.TIMEOUT_CODE,
                authCode = "",
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            )
//            return TransactionResponse(
//                responseCode = "20",
//                authCode = "",
//                stan = transaction.stan,
//                scripts = "",
//                date = now.time,
//            )
        }
    }


    /**
     * Initiates a paycode transaction using the provided code, terminal and payment info, and returns
     * a status response provided by the EPMS
     *
     * @param terminalInfo the necessary information that identifies the current POS terminal
     * @param code  the paycode that is generated by the customer's bank
     * @param iswPaymentInfo the information required to make the current purchase
     * @return  response status indicating transaction success or failure
     */
    override fun initiatePaycodePurchase(
        terminalInfo: TerminalInfo,
        code: String,
        iswPaymentInfo: IswPaymentInfo
    ): Pair<CardDetail, TransactionResponse?> {

        val now = Date()
        val pan = IsoUtils.generatePan(code)
        val amount = String.format(Locale.getDefault(), "%012d", iswPaymentInfo.amount)
        val stan = getNextStan()
        val date = DateUtils.dateFormatter.format(now)
        val src = "501"

        // generate expiry date using current date
        val expiryDate = Calendar.getInstance().let {
            it.time = now
            val currentYear = it.get(Calendar.YEAR)
            it.set(Calendar.YEAR, currentYear + 1)
            it.time
        }

        // format track 2
        val expiry = DateUtils.yearAndMonthFormatter.format(expiryDate)
        val track2 = "${pan}D$expiry"


        // create card detail
        val card = CardDetail(
            pan = pan,
            expiry = expiry,
            type = CardType.VERVE
        )

        // get icc data
        val iccData = IccData.getIcc(terminalInfo, amount, date)

        // create transaction info
        val transaction = TransactionInfo(
            cardExpiry = expiry,
            cardPIN = "",
            cardPAN = pan,
            cardTrack2 = track2,
            iccData = iccData,
            iccString = iccData.iccAsString,
            stan = stan,
            accountType = AccountType.Default,
            amount = iswPaymentInfo.amount,
            csn = "",
            src = src,
            purchaseType = PurchaseType.PayCode,
            pinKsn = "",
            surcharge = iswPaymentInfo.surcharge,
            rrn = iswPaymentInfo.rrn
        )


        // generate purchase request
        val request = TransactionRequest.create(
            TransactionType.Purchase,
            device,
            terminalInfo,
            transaction
        ) as PurchaseRequest

        try {
            val response = httpService
                .makePurchase(terminalInfo.serverUrl, request)
                .run()


            val data = response.body()

            return if (!response.isSuccessful || data == null) {
               Pair(card, TransactionResponse(
                    responseCode = IsoUtils.TIMEOUT_CODE,
                    authCode = "",
                    stan = transaction.stan,
                    scripts = "",
                    date = now.time,
                    rrn = transaction.rrn
                ))
            } else {
                Pair(card, TransactionResponse(
                    responseCode = data.responseCode,
                    stan = transaction.stan,
                    authCode = data.authCode,
                    scripts = "",
                    date = now.time,
                    rrn = data.referenceNumber
                ))
            }

        } catch (e: Exception) {
            reportException(e)
            logger.logErr(e.localizedMessage)
            e.printStackTrace()
            reversePurchase(terminalInfo, request)
            return Pair(card, TransactionResponse(
                IsoUtils.TIMEOUT_CODE,
                authCode = "",
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            ))
        }
    }

    /**
     * Initiates a pre-authorization transaction using the provided terminal and transaction info, and returns the
     * transaction response provided by EPMS
     *
     * @param terminalInfo  the necessary information that identifies the current POS terminal
     * @param transaction  the purchase information required to perform the transaction
     * @return   response status indicating transaction success or failure
     */
    override fun initiatePreAuthorization(
        terminalInfo: TerminalInfo,
        transaction: TransactionInfo
    ): TransactionResponse? {
        // generate purchase request
        val request = TransactionRequest.create(
            TransactionType.PreAuth,
            device,
            terminalInfo,
            transaction
        ) as PreAuthRequest


        val now = Date()
        request.pinData?.apply {
            // remove first 2 bytes to make 8 bytes
            ksn = ksn.substring(4)
        }

        try {
            val response = httpService.preAuthorization(terminalInfo.serverUrl, request).run()
            val data = response.body()
            //throw Exception("This is a test exception")
            return if (!response.isSuccessful || data == null) {
                TransactionResponse(
                    responseCode = response.code().toString(),
                    authCode = data?.authCode ?: "",
                    stan = transaction.stan,
                    scripts = "",
                    responseDescription = response.message(),
                    date = now.time,
                    rrn = transaction.rrn
                )
            } else {
                TransactionResponse(
                    responseCode = data.responseCode,
                    stan = transaction.stan,
                    authCode = data.authCode,
                    scripts = "",
                    responseDescription = data.description,
                    date = now.time,
                    rrn = data.referenceNumber
                )
            }

        } catch (e: Exception) {
            reportException(e)
            logger.log(e.localizedMessage)
            e.printStackTrace()

            //return reversePurchase(terminalInfo, request)
            return TransactionResponse(
                IsoUtils.TIMEOUT_CODE,
                authCode = "",
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            )
        }
    }


    /**
     * Initiates a completion transaction using the provided terminal and transaction info, and returns the
     * transaction response provided by EPMS
     *
     * @param terminalInfo  the necessary information that identifies the current POS terminal
     * @param transaction  the information required to perform the transaction
     * @return   response status indicating transaction success or failure
     */
    override fun initiateCompletion(
        terminalInfo: TerminalInfo,
        transaction: TransactionInfo,
        preAuthStan: String,
        preAuthDateTime: String,
        preAuthAuthId: String
    ): TransactionResponse? {

        // generate purchase request
        val request = TransactionRequest.create(
            TransactionType.Completion,
            device,
            terminalInfo,
            transaction,
            preAuthStan = preAuthStan,
            preAuthDateTime = preAuthDateTime,
            preAuthAuthId = preAuthAuthId
        ) as CompletionRequest


        val now = Date()
        request.pinData?.apply {
            // remove first 2 bytes to make 8 bytes
            ksn = ksn.substring(4)
        }

        try {
            val response = httpService.completion(terminalInfo.serverUrl, request).run()
            val data = response.body()

            return if (!response.isSuccessful || data == null) {
                TransactionResponse(
                    responseCode = response.code().toString(),
                    authCode = preAuthAuthId,
                    stan = transaction.stan,
                    scripts = "",
                    responseDescription = response.message(),
                    date = now.time,
                    rrn = transaction.rrn
                )
            } else {
                TransactionResponse(
                    responseCode = data.responseCode,
                    stan = transaction.stan,
                    authCode = preAuthAuthId,
                    scripts = "",
                    responseDescription = data.description,
                    date = now.time,
                    rrn = data.referenceNumber
                )
            }

        } catch (e: Exception) {
            reportException(e)
            logger.log(e.localizedMessage)
            e.printStackTrace()
            //return reversePurchase(terminalInfo, request)
            return TransactionResponse(
                IsoUtils.TIMEOUT_CODE,
                authCode = preAuthAuthId,
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            )
        }
    }

    /**
     * Initiates a refund transaction using the provided terminal and transaction info, and returns the
     * transaction response provided by EPMS
     *
     * @param terminalInfo  the necessary information that identifies the current POS terminal
     * @param transaction  the information required to perform the transaction
     * @return   response status indicating transaction success or failure
     */
    override fun initiateRefund(
        terminalInfo: TerminalInfo,
        transaction: TransactionInfo
    ): TransactionResponse? {
        // generate purchase request
        val request = TransactionRequest.create(
            TransactionType.Refund,
            device,
            terminalInfo,
            transaction
        ) as RefundRequest


        val now = Date()
        request.pinData?.apply {
            // remove first 2 bytes to make 8 bytes
            ksn = ksn.substring(4)
        }

        try {
            val response = httpService.refund(terminalInfo.serverUrl, request).run()
            val data = response.body()

            return if (!response.isSuccessful || data == null) {
                TransactionResponse(
                    responseCode = response.code().toString(),
                    authCode = "",
                    stan = transaction.stan,
                    scripts = "",
                    responseDescription = response.message(),
                    date = now.time,
                    rrn = transaction.rrn
                )
            } else {
                TransactionResponse(
                    responseCode = data.responseCode,
                    stan = transaction.stan,
                    authCode = data.authCode,
                    scripts = "",
                    responseDescription = data.description,
                    date = now.time,
                    rrn = data.referenceNumber
                )
            }

        } catch (e: Exception) {
            reportException(e)
            logger.log(e.localizedMessage)
            e.printStackTrace()
            //return reversePurchase(terminalInfo, request)
            return TransactionResponse(
                IsoUtils.TIMEOUT_CODE,
                authCode = "",
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            )
        }
    }


    /**
     * Initiates a reversal transaction using the provided terminal and transaction info, and returns the
     * transaction response provided by EPMS
     *
     * @param terminalInfo  the necessary information that identifies the current POS terminal
     * @param transaction  the information required to perform the transaction
     * @return   response status indicating transaction success or failure
     */
    override fun initiateReversal(
        terminalInfo: TerminalInfo,
        transaction: TransactionInfo
    ): TransactionResponse? {
        val request = TransactionRequest.create(
            TransactionType.Purchase,
            device,
            terminalInfo,
            transaction
        ) as PurchaseRequest

        val rep = reversePurchase(terminalInfo, request)
        //TODO: modify this to return the right response
        return rep.second
    }

    /**
     * Initiates a cash transaction using the provided terminal and transaction info, and returns the
     * transaction response provided by EPMS
     *
     * @param terminalInfo  the necessary information that identifies the current POS terminal
     * @param transaction  the information required to perform the transaction
     * @return   response status indicating transaction success or failure
     */
    fun initiateCashPurchase(
        terminalInfo: TerminalInfo,
        transaction: TransactionInfo
    ): TransactionResponse {
        // generate purchase request
        val request = TransactionRequest.create(
            TransactionType.Purchase,
            device,
            terminalInfo,
            transaction
        ) as PurchaseRequest

        val now = Date()

        try {
            val response = httpService.makePurchase(terminalInfo.serverUrl, request).run()
            val data = response.body()

            return if (!response.isSuccessful || data == null) {
                TransactionResponse(
                    responseCode = response.code().toString(),
                    authCode = "",
                    stan = transaction.stan,
                    scripts = "",
                    responseDescription = response.message(),
                    date = now.time,
                    transactionId = request.transactionId,
                    rrn = transaction.rrn
                )
            } else {
                TransactionResponse(
                    responseCode = data.responseCode,
                    stan = transaction.stan,
                    authCode = data.authCode,
                    scripts = "",
                    responseDescription = data.description,
                    date = now.time,
                    transactionId = request.transactionId,
                    rrn = data.referenceNumber
                )
            }
        } catch (e: Exception) {
            reportException(e)
            logger.log(e.localizedMessage)
            e.printStackTrace()
            return TransactionResponse(
                IsoUtils.TIMEOUT_CODE,
                authCode = "",
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                transactionId = request.transactionId,
                rrn = transaction.rrn
            )
        }
    }

    /**
     * Initiates a IFIS CashOut transaction using the provided terminal and transaction info, and returns the
     * transaction response provided by EPMS
     *
     * @param terminalInfo  the necessary information that identifies the current POS terminal
     * @param transaction  the information required to perform the transaction
     * @param allTerminalInfo  the merchant information required to perform the transaction
     * @return   response status indicating transaction success or failure
     */
    fun initiateCashOut(
            terminalInfo: TerminalInfo,
            transaction: TransactionInfo,
            allTerminalInfo: AllTerminalInfo
    ): TransactionResponse{

        // generate cashOut request
        val request = TransactionRequest.createCashOut(
                TransactionType.IFIS,
                device,
                terminalInfo,
                transaction,
                allTerminalInfo
        ) as CashOutRequest
        //hack for IFIS
        request.pinData?.ksn = request.pinData?.ksn?.drop(4).toString()
        val now = Date()

        try {
            val response = httpService.doCashOut(terminalInfo.serverUrl, request).run()
            val data = response.body()

            return if (!response.isSuccessful || data == null || data.remoteResponseCode != IsoUtils.OK) {
                TransactionResponse(
                        responseCode =  data?.let { it.remoteResponseCode.ifEmpty { it.responseCode } } ?: "96",
                        authCode = "",
                        stan = transaction.stan,
                        scripts = "",
                        responseDescription = data?.responseMessage?.ifEmpty{ data.description },
                        date = now.time,
                        rrn = transaction.rrn
                )
            } else {
                TransactionResponse(
                        responseCode = data.responseCode,
                        stan = transaction.stan,
                        authCode = data.authCode,
                        scripts = "",
                        responseDescription = data.responseMessage.ifEmpty { data.description },
                        date = now.time,
                        transactionId = data.transactionRef,
                        remoteResponseCode = data.remoteResponseCode,
                        rrn = data.referenceNumber
                )
            }
        } catch (e: Exception) {
            reportException(e)
            logger.log(e.localizedMessage)
            e.printStackTrace()
            return TransactionResponse(
                    IsoUtils.TIMEOUT_CODE,
                    authCode = "",
                    stan = transaction.stan,
                    scripts = "",
                    date = now.time,
                    rrn = transaction.rrn
            )
        }
    }

    fun initiateCardTransfer(
        terminalInfo: TerminalInfo,
        transactionInfo: TransactionInfo,
        destinationAccount: String,
        receivingInstitutionId: String,
        extendedTransactionType: String,
        retrievalReferenceNumber: String = ""
    ): TransactionResponse {

        val date = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val transferDateTime = formatter.format(date)

        val request = TransactionRequest.buildCardTransfer(
            deviceName = device,
            terminalInfo = terminalInfo,
            transactionInfo = transactionInfo,
            transferDateTime = transferDateTime,
            destinationAccount = destinationAccount,
            receivingInstitutionId = receivingInstitutionId,
            extendedTransactionType = extendedTransactionType,
            rrn = retrievalReferenceNumber
        ) as TransferRequest

        val now = Date()
        val rrn = if (IswPos.getInstance().isFinchAgent()) retrievalReferenceNumber else transactionInfo.rrn

        try {
            val response = httpService.doTransfer(terminalInfo.serverUrl, request).run()
            val data = response.body()

            return if (!response.isSuccessful || data == null || data.responseCode != IsoUtils.OK) {
                TransactionResponse(
                    responseCode =  data?.responseCode ?: "96",
                    authCode = data?.authId ?: "",
                    stan = transactionInfo.stan,
                    scripts = "",
                    responseDescription = data?.description ?: "",
                    date = now.time,
                    rrn = rrn
                )
            } else {
                TransactionResponse(
                    responseCode = data.responseCode,
                    stan = transactionInfo.stan,
                    authCode = data.authId,
                    scripts = "",
                    responseDescription = data.description,
                    date = now.time,
                    transactionId = "",
                    remoteResponseCode = "",
                    rrn = rrn // check this, if to replace with the rrn passed in from Finch
                )
            }
        } catch (e: Exception) {
            reportException(e)
            logger.log(e.localizedMessage)
            e.printStackTrace()

            return TransactionResponse(
                IsoUtils.TIMEOUT_CODE,
                authCode = "",
                stan = transactionInfo.stan,
                scripts = "",
                date = now.time,
                rrn = rrn
            )
        }

    }

    /**
     * Initiates a BillPayment Inquiry transaction using the provided terminal and transaction info, and returns the
     * transaction response provided by EPMS
     *
     * @param terminalInfo  the necessary information that identifies the current POS terminal
     * @param paymentInfo  the payment information required to perform the transaction
     * @param billInfo  the billpaymeny related information required to perform the transaction
     * @param cardPan  the user's card pan required to perform the transaction
     * @return   response status indicating transaction success or failure
     */
    fun doInquiry(
            paymentInfo: IswPaymentInfo,
            terminalInfo: TerminalInfo,
            billInfo: IswBillPaymentInfo,
            cardPan: String
    ): TransactionResponse {

        // generate bill inquiry request
        val request = TransactionRequest.buildInquiry(
                device,
                terminalInfo,
                billInfo,
                cardPan,
                paymentInfo
        ) as BillPaymentRequest

        val now = Date()

        try {
            val response = httpService.billPayment(terminalInfo.serverUrl, request).run()
            val data = response.body()
            Log.d("INQUIRY RESPONSE", "${response}")
            Log.d("INQUIRY RESPONSE2", "${data}")

            return if (!response.isSuccessful || data == null) {
                TransactionResponse(
                        responseCode = data?.responseCode ?: "96",
                        authCode = "",
                        stan = paymentInfo.currentStan,
                        scripts = "",
                        responseDescription = data?.responseMessage?.ifEmpty{ data.description },
                        date = now.time,
                        rrn = paymentInfo.rrn
                )
            } else {
                TransactionResponse(
                        responseCode = data.responseCode,
                        stan = paymentInfo.currentStan,
                        scripts = "",
                        responseDescription = data.responseMessage.ifEmpty { data.description },
                        date = now.time,
                        transactionId = data.transactionRef,
                        authCode = "",
                        inquiryResponse = InquiryResponse.fromBillPaymentData(data, request),
                        rrn = data.referenceNumber
                )
            }
        } catch (e: Exception) {
            reportException(e)
            logger.log(e.localizedMessage)
            e.printStackTrace()
            return TransactionResponse(
                    IsoUtils.TIMEOUT_CODE,
                    authCode = "",
                    stan = paymentInfo.currentStan,
                    scripts = "",
                    date = now.time,
                    rrn = paymentInfo.rrn
            )
        }
    }

    override fun initiateBillPayment(
            terminalInfo: TerminalInfo,
            txnInfo: TransactionInfo,
            inquiryResponse: InquiryResponse
    ): TransactionResponse? {
        // generate bill inquiry request
        val request = TransactionRequest.buildCompletion(
                device,
                terminalInfo,
                txnInfo,
                inquiryResponse
        ) as BillPaymentRequest

        val now = Date()

        request.pinData?.apply {
            // remove first 2 bytes to make 8 bytes
            ksn = ksn.substring(4)
        }

        try {
            val response = httpService.billPayment(terminalInfo.serverUrl, request).run()
            val data = response.body()

            return if (!response.isSuccessful || data == null) {
                TransactionResponse(
                        responseCode = data?.responseCode ?: "96",
                        authCode = "",
                        stan = txnInfo.stan,
                        scripts = "",
                        responseDescription = data?.responseMessage?.ifEmpty{ data.description },
                        date = now.time,
                        rrn = txnInfo.rrn
                )
            } else {
                TransactionResponse(
                        responseCode = data.responseCode,
                        stan = txnInfo.stan,
                        scripts = "",
                        responseDescription = data.responseMessage.ifEmpty { data.description },
                        date = now.time,
                        transactionId = data.transactionRef,
                        authCode = "",
                        inquiryResponse = inquiryResponse,
                        rrn = data.referenceNumber
                )
            }
        } catch (e: Exception) {
            reportException(e)
            logger.log(e.localizedMessage)
            e.printStackTrace()
            //return reversePurchase(terminalInfo, request)
            return TransactionResponse(
                    IsoUtils.TIMEOUT_CODE,
                    authCode = "",
                    stan = txnInfo.stan,
                    scripts = "",
                    date = now.time,
                    rrn = txnInfo.rrn
            )
        }
    }



    fun initiatePaymentAdvice(
            terminalInfo: TerminalInfo,
            txnInfo: TransactionInfo,
            inquiryResponse: InquiryResponse
    ): TransactionResponse {

        // generate bill advice request
        val request = TransactionRequest.buildAdvice(
                device,
                terminalInfo,
                txnInfo,
                inquiryResponse
        ) as BillPaymentRequest

        val now = Date()

        try {
            val response = httpService.billPayment(terminalInfo.serverUrl, request).run()
            val data = response.body()

            return if (!response.isSuccessful || data == null) {
                TransactionResponse(
                        responseCode = data?.responseCode ?: "96",
                        authCode = "",
                        stan = txnInfo.stan,
                        scripts = "",
                        responseDescription = data?.responseMessage?.ifEmpty{ data.description },
                        date = now.time,
                        rrn = txnInfo.rrn
                )
            } else {
                TransactionResponse(
                        responseCode = data.responseCode,
                        stan = txnInfo.stan,
                        scripts = "",
                        responseDescription = data.responseMessage.ifEmpty { data.description },
                        date = now.time,
                        transactionId = data.transactionRef,
                        authCode = "",
                        inquiryResponse = InquiryResponse.fromBillPaymentData(data, request),
                        rrn = data.referenceNumber
                )
            }
        } catch (e: Exception) {
            reportException(e)
            logger.log(e.localizedMessage)
            e.printStackTrace()
            return TransactionResponse(
                    IsoUtils.TIMEOUT_CODE,
                    authCode = "",
                    stan = txnInfo.stan,
                    scripts = "",
                    date = now.time,
                    rrn = txnInfo.rrn
            )
        }
    }


    override fun initiatePurchaseWithCashBack(
            terminalInfo: TerminalInfo,
            transaction: TransactionInfo
    ): TransactionResponse {
        // generate purchase request
        val request = TransactionRequest.create(
                TransactionType.CashBack,
                device,
                terminalInfo,
                transaction
        ) as PurchaseRequest

        //add cash-back fields
        request.additionalAmounts = transaction.additionalAmounts.toString()
        request.purchaseType = "CashBack"
        request.surcharge = transaction.surcharge.toString()

        val now = Date()
        request.pinData?.apply {
            // remove first 2 bytes to make 8 bytes
            ksn = ksn.substring(4)
        }

        try {
            val response = httpService.makePurchase(terminalInfo.serverUrl, request).run()
            val data = response.body()

            return if (!response.isSuccessful || data == null) {
                TransactionResponse(
                        responseCode = response.code().toString(),
                        authCode = "",
                        stan = transaction.stan,
                        scripts = "",
                        responseDescription = response.message(),
                        date = now.time,
                        additionalInfo = AdditionalInfo(request.surcharge, request.additionalAmounts),
                        rrn = transaction.rrn
                )
            } else {
                TransactionResponse(
                        responseCode = data.responseCode,
                        stan = transaction.stan,
                        authCode = data.authCode,
                        scripts = "",
                        responseDescription = data.description,
                        date = now.time,
                        additionalInfo = AdditionalInfo(request.surcharge, request.additionalAmounts),
                        rrn = data.referenceNumber
                )
            }

        } catch (e: Exception) {
            reportException(e)
            logger.log(e.localizedMessage)
            e.printStackTrace()
            //return reversePurchase(terminalInfo, request)
            return TransactionResponse(
                    IsoUtils.TIMEOUT_CODE,
                    authCode = "",
                    stan = transaction.stan,
                    scripts = "",
                    date = now.time,
                    rrn = transaction.rrn
            )
        }
    }

    override fun initiateBalance(
        terminalInfo: TerminalInfo,
        transaction: TransactionInfo
    ): TransactionResponse? {
        val request = TransactionRequest.create(
            TransactionType.Balance,
            device,
            terminalInfo,
            transaction
        ) as BalanceRequest


        val now = Date()
        request.pinData?.apply {
            if (ksn.isNotEmpty()) {
                ksn = ksn.substring(4)
            }
        }

        try {
            val response = httpService.makeBalance(terminalInfo.serverUrl, request).run()
            val data = response.body()
            return if (!response.isSuccessful || data == null) {
                TransactionResponse(
                    responseCode = response.code().toString(),
                    authCode = "",
                    stan = transaction.stan,
                    scripts = "",
                    responseDescription = response.message(),
                    date = now.time,
                    rrn = transaction.rrn
                )
            } else {
                TransactionResponse(
                    responseCode = data.responseCode,
                    stan = transaction.stan,
                    balance = data.availableBalance,
                    authCode = data.authCode,
                    scripts = "",
                    responseDescription = data.description,
                    date = now.time,
                    rrn = data.referenceNumber
                )
            }

        } catch (e: Exception) {
            reportException(e)
            logger.log(e.localizedMessage)
            e.printStackTrace()
            return TransactionResponse(
                IsoUtils.TIMEOUT_CODE,
                authCode = "",
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            )
        }
    }

    fun downloadMerchantDetailsForAuto( terminalSerialNo: String): AllTerminalInfo? {

        return try {
            val response = iKimonoConfigService.getMerchantDetailsAuto(terminalSerialNo).run()
            val data = response.body()
            if (!response.isSuccessful || data == null) {
                null
            } else {
                val allInfo = convertAutoToAllTerminalInfo(data)
                allInfo.tmsRouteTypeConfig?.name?.let { store.saveString("TMS_ROUTE", it) }
                return allInfo
            }

        } catch (e: Exception) {
            reportException(e)
            logger.log(e.localizedMessage)
            e.printStackTrace()
            null
        }

    }


    // TODO: remove this method after successful QA
    private fun getIcc(terminalInfo: TerminalInfo, amount: String, date: String): IccData {
        val authorizedAmountTLV = String.format("9F02%02d%s", amount.length / 2, amount)
        val transactionDateTLV = String.format("9A%02d%s", date.length / 2, date)
        val iccData =
            "9F260831BDCBC7CFF6253B9F2701809F10120110A50003020000000000000000000000FF9F3704F435D8A29F3602052795050880000000" +
                    "${transactionDateTLV}9C0100${authorizedAmountTLV}5F2A020566820238009F1A0205669F34034103029F3303E0D0F89F3501229F0306000000000000"

        // remove leadin zero if exits
        val currencyCode =
            if (terminalInfo.currencyCode.length > 3) terminalInfo.currencyCode.substring(1) else terminalInfo.currencyCode
        val countryCode =
            if (terminalInfo.countryCode.length > 3) terminalInfo.countryCode.substring(1) else terminalInfo.countryCode



        return IccData().apply {
            TRANSACTION_AMOUNT = amount
            ANOTHER_AMOUNT = "000000000000"
            APPLICATION_INTERCHANGE_PROFILE = "3800"
            APPLICATION_TRANSACTION_COUNTER = "0527"
            CRYPTOGRAM_INFO_DATA = "80"
            CARD_HOLDER_VERIFICATION_RESULT = "410302"
            ISSUER_APP_DATA = "0110A50003020000000000000000000000FF"
            TRANSACTION_CURRENCY_CODE = currencyCode
            TERMINAL_VERIFICATION_RESULT = "0880000000"
            TERMINAL_COUNTRY_CODE = countryCode
            TERMINAL_TYPE = "22"
            TERMINAL_CAPABILITIES = terminalInfo.capabilities ?: "E050C8"
            TRANSACTION_DATE = date
            TRANSACTION_TYPE = "00"
            UNPREDICTABLE_NUMBER = "F435D8A2"
            DEDICATED_FILE_NAME = ""
            AUTHORIZATION_REQUEST = "31BDCBC7CFF6253B"

            iccAsString = iccData
        }

    }


    private fun reversePurchase(
        terminalInfo: TerminalInfo,
        txn: TransactionRequest
    ): Pair<TransactionResponse, TransactionResponse> {
        // create reversal request
        val request = ReversalRequest.create(txn)
        val now = Date()

        val transactionResponse = TransactionResponse(
            responseCode = IsoUtils.TIMEOUT_CODE,
            authCode = "",
            stan = txn.stan,
            scripts = "",
            date = now.time,
            rrn = txn.retrievalReferenceNumber
        )

        try {
            // initiate reversal request
            val response = httpService
                .reversePurchase(terminalInfo.serverUrl, request)
                .run()


            val data = response.body()

            return if (!response.isSuccessful || data == null) {
                Pair(transactionResponse, TransactionResponse(
                    responseCode = IsoUtils.TIMEOUT_CODE,
                    authCode = "",
                    stan = txn.stan,
                    scripts = "",
                    date = now.time,
                    rrn = txn.retrievalReferenceNumber
                ))
            } else {
                Pair(transactionResponse,TransactionResponse(
                    responseCode = data.responseCode,
                    stan = txn.stan,
                    authCode = data.authCode,
                    scripts = "",
                    date = now.time,
                    rrn = txn.retrievalReferenceNumber
                ))
            }

        } catch (e: Exception) {
            reportException(e)
            logger.logErr(e.localizedMessage)
            e.printStackTrace()
            return Pair(transactionResponse, TransactionResponse(
                IsoUtils.TIMEOUT_CODE,
                authCode = "",
                stan = txn.stan,
                scripts = "",
                date = now.time,
                rrn = txn.retrievalReferenceNumber
            ))
        }

    }

    private fun ByteArray.base64encode(): String {
        return String(AndroidBase64.encode(this, AndroidBase64.NO_WRAP))
    }
}