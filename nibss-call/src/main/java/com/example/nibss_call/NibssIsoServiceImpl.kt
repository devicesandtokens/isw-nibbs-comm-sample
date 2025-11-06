package com.example.nibss_call

import android.content.Context
import com.example.Constants
import com.example.Constants.KEY_MASTER_KEY
import com.example.Constants.KEY_PIN_KEY
import com.example.Constants.KEY_SESSION_KEY
import com.example.interfaces.IsoServiceListener
import com.example.interfaces.device.POSDevice
import com.example.interfaces.library.IsoService
import com.example.models.billpayment.InquiryResponse
import com.interswitchng.smartpos.shared.interfaces.library.IsoSocket
import com.interswitchng.smartpos.shared.interfaces.library.KeyValueStore
import com.interswitchng.smartpos.shared.models.core.AdditionalInfo
import com.interswitchng.smartpos.shared.models.core.CMS
import com.example.models.core.TerminalInfo
import com.interswitchng.smartpos.shared.models.transaction.IswPaymentInfo
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.CardDetail
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.CardType
import com.interswitchng.smartpos.shared.models.transaction.cardpaycode.request.TransactionInfo
import com.example.models.transaction.cardpaycode.response.TransactionResponse
import com.example.utils.CardTypeUtils
import com.example.utils.DateUtils.dateFormatter
import com.example.utils.DateUtils.monthFormatter
import com.example.utils.DateUtils.timeAndDateFormatter
import com.example.utils.DateUtils.timeFormatter
import com.example.utils.DateUtils.yearAndMonthFormatter
import com.example.utils.IsoUtils
import com.example.utils.IsoUtils.TIMEOUT_CODE
import com.example.utils.IsoUtils.generatePan
import com.example.utils.NibssIsoMessage
import com.interswitchng.smartpos.shared.services.utils.TerminalInfoParser
import com.interswitchng.smartpos.shared.services.utils.TripleDES
import com.example.utils.FileUtils
import com.example.utils.KeysUtils
import com.example.utils.Logger
import com.solab.iso8583.IsoType
import com.solab.iso8583.IsoValue
import com.solab.iso8583.parse.ConfigParser
import java.io.StringReader
import java.io.UnsupportedEncodingException
import java.net.SocketTimeoutException
import java.text.ParseException
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal class NibssIsoServiceImpl(
    private val context: Context,
    private val store: KeyValueStore,
    private val socket: IsoSocket,
    private val listener: IsoServiceListener? = null,
    private val posDevice: POSDevice? = null,

    ) : IsoService {

    fun getCurrentTerminalInfo(): TerminalInfo? {
        return TerminalInfo.get(store)
    }

    private val logger by lazy { Logger.with("IsoServiceImpl") }
    private val messageFactory by lazy {
        try {

            val data = FileUtils.getFromAssets(context)
            val string = String(data!!)
            val stringReader = StringReader(string)
            val messageFactory = ConfigParser.createFromReader(stringReader)
            messageFactory.isUseBinaryBitmap = false //NIBSS usebinarybitmap = false
            messageFactory.characterEncoding = "UTF-8"

            return@lazy messageFactory

        } catch (e: Exception) {
            logger.logErr(e.localizedMessage)
            e.printStackTrace()
            throw e
        }
    }

    private fun makeKeyCall(
        terminalId: String,
        ip: String,
        port: Int,
        code: String,
        key: String
    ): String? {
        val operation = "downloadTerminalParameters"
        listener?.onStart(operation)

        try {

            val now = Date()
            val stan = getNextStan()
            println("starting to pack 0800 message")
            val message = NibssIsoMessage(messageFactory.newMessage(0x800))
            message
                .setValue(3, code)
                .setValue(7, timeAndDateFormatter.format(now))
                .setValue(11, stan)
                .setValue(12, timeFormatter.format(now))
                .setValue(13, monthFormatter.format(now))
                .setValue(41, terminalId)
                //.setValue(59, "SMARTPOS|$code|${terminalInfo.tmsRouteType}")
//            if (!isSSLContext() || terminalInfo.tmsRouteType != CMS.UPSL.name) {
//                message.message.removeFields(59)
//            }


            // remove unset fields
            message.message.removeFields(62, 64)
            message.dump(System.out, "request -- ")

            // set server Ip and port
            socket.setIpAndPort(ip, port)

            println("Socket port set to $ip $port")

            // open to socket endpoint
            val socketOpened = socket.open()
            println("Socket opened $socketOpened")

            val request = message.message.writeData()
            logger.log("Key Xch Request HEX ---> ${IsoUtils.bytesToHex(request)}")

            // send request and process response
            val response = socket.sendReceive(message.message.writeData())
            // close connection
            socket.close()

            logger.log("Key Xch Response HEX ---> ${IsoUtils.bytesToHex(response!!)}")

            // read message
            val msg = NibssIsoMessage(messageFactory.parseMessage(response, 0))
            msg.dump(System.out, "response -- ")


            // extract encrypted key with clear key
            val encryptedKey = msg.message.getField<String>(SRCI)
            val decryptedKey = TripleDES.soften(key, encryptedKey.value)

            println(decryptedKey)

            return decryptedKey
        } catch (e: UnsupportedEncodingException) {
            listener?.onError(operation, e.localizedMessage ?: "Failed to download terminal parameters", e)
            logger.logErr(
                e.localizedMessage ?: "UnsupportedEncodingException occurred in downloading keys"
            )
        } catch (e: ParseException) {
            listener?.onError(operation, e.localizedMessage ?: "Failed to download terminal parameters", e)
            logger.logErr(e.localizedMessage ?: "ParseException occurred in downloading keys")
        } catch (e: java.lang.Exception) {
            listener?.onError(operation, e.localizedMessage ?: "Failed to download terminal parameters", e)
            e.printStackTrace()
            logger.logErr(e.localizedMessage ?: "Exception occurred in downloading keys mmm")
        }

        return null
    }

    private fun getNextStan(): String {
        return "000001"
    }

    override fun downloadKey(terminalId: String, ip: String, port: Int): Boolean {

        val key = KeysUtils.testCMS(CMS.NUS)

        // println("cms_key: $key")
        // download and save encrypted master key
        // this will also load the master key into the POS terminal
        val isDownloaded = makeKeyCall(terminalId, ip, port, "9A0000", key)?.let { masterKey ->
            store.saveString(KEY_MASTER_KEY, masterKey)
            // load master key into pos
//            posDevice.loadMasterKey(masterKey)

            // getResult session key & save
            val isSessionSaved =
                makeKeyCall(terminalId, ip, port, "9B0000", masterKey)?.let { sessionKey ->
                    store.saveString(KEY_SESSION_KEY, sessionKey)
                    true
                }

            // getResult pin key & save
            val isPinSaved =
                makeKeyCall(terminalId, ip, port, "9G0000", masterKey)?.let { pinKey ->
                    store.saveString(KEY_PIN_KEY, pinKey)

                    // load pin key into pos device
//                    posDevice.loadPinKey(pinKey)
                    true
                }

            isPinSaved == true && isSessionSaved == true
        }

        return isDownloaded == true
    }


    override fun downloadTerminalParameters(
        terminalId: String,
        ip: String,
        port: Int
    ): Boolean {
        try {
            val code = "9C0000"
            val field62 = "01009280824266"

            val now = Date()
            val stan = getNextStan()

            val message = NibssIsoMessage(messageFactory.newMessage(0x800))
            message
                .setValue(3, code)
                .setValue(7, timeAndDateFormatter.format(now))
                .setValue(11, stan)
                .setValue(12, timeFormatter.format(now))
                .setValue(13, monthFormatter.format(now))
                .setValue(41, terminalId)
                //.setValue(59, "SMARTPOS|$code|${terminalInfo.tmsRouteType}")
                .setValue(62, field62)


//            if (!isSSLContext() || terminalInfo.tmsRouteType != CMS.UPSL.name) {
//                message.message.removeFields(59)
//            }

//            if (terminalInfo.tmsRouteType == CMS.UPSL.name) {
//                message.setValue(59, "SMARTPOS|$code|${terminalInfo.tmsRouteType}")
//            }

            val bytes = message.message.writeData()
            val length = bytes.size
            val temp = ByteArray(length - 64)
            if (length >= 64) {
                System.arraycopy(bytes, 0, temp, 0, length - 64)
            }


            // confirm that key was downloaded
            val key = store.getString(KEY_SESSION_KEY, "")
            if (key.isEmpty()) return false

            val hashValue = IsoUtils.getMac(key, temp) //SHA256
            message.setValue(64, hashValue)
            message.dump(System.out, "parameter request ---- ")

            // set server Ip and port
            socket.setIpAndPort(ip, port)

            // open socket connection
            socket.open()

            // send request and receive response
            val response = socket.sendReceive(message.message.writeData())
            // close connection
            socket.close()

            // read message
            val responseMessage = NibssIsoMessage(messageFactory.parseMessage(response, 0))
            responseMessage.dump(System.out, "parameter response ---- ")


            // getResult string formatted terminal info
            val terminalDataString = responseMessage.message.getField<String>(62).value

            // parse and save terminal info
            TerminalInfoParser.parse(
                terminalId,
                ip,
                port,
                terminalDataString,
                store
            )
                ?.also { it.persist(store) }

            return true
        } catch (e: Exception) {

            logger.log(
                e.localizedMessage ?: "Exception occurred performing download terminal parameters"
            )
            e.printStackTrace()
        }

        return false
    }

    override fun initiateCardPurchase(
        terminalInfo: TerminalInfo,
        transaction: TransactionInfo
    ): TransactionResponse? {
        val now = Date()
        val message = NibssIsoMessage(messageFactory.newMessage(0x200))
        val processCode = "00" + transaction.accountType.value + "00"
        val hasPin = transaction.cardPIN.isNotEmpty()
        val stan = transaction.stan
        val randomReference = transaction.rrn
        var field59 = "SMARTPOS|PURCHASE|${terminalInfo.tmsRouteType}"

        if (transaction.customerReference.isNullOrEmpty().not()) {
            field59 += "|${transaction.customerReference}"
        }

        if (transaction.transactionRemark.isNullOrEmpty().not()) {
            field59 += "|${transaction.transactionRemark}"
        }

        message
            .setValue(2, transaction.cardPAN)
            .setValue(3, processCode)
            .setValue(4, String.format(Locale.getDefault(), "%012d", transaction.amount))
            .setValue(7, timeAndDateFormatter.format(now))
            .setValue(11, stan)
            .setValue(12, timeFormatter.format(now))
            .setValue(13, monthFormatter.format(now))
            .setValue(14, transaction.cardExpiry)
            .setValue(18, terminalInfo.merchantCategoryCode)
            .setValue(22, "051")
            .setValue(23, transaction.csn)
            .setValue(25, "00")
            .setValue(26, "06")
            .setValue(28, "C00000000")
            .setValue(35, transaction.cardTrack2.replace("F", ""))
            .setValue(32, CardTypeUtils.getAcquirerID(transaction.cardPAN))
            .setValue(37, randomReference)
            .setValue(40, transaction.src)
            .setValue(41, terminalInfo.terminalId)
            .setValue(42, terminalInfo.merchantId)
            .setValue(43, terminalInfo.merchantNameAndLocation)
            .setValue(49, terminalInfo.currencyCode)
            .setValue(55, transaction.iccString)
            .setValue(59, field59)

        if (hasPin) {
            message.setValue(52, transaction.cardPIN)
                .setValue(
                    123,
                    if (Constants.IS_CONTACT) "510101511344101" else Constants.CLSS_POS_DATA_CODE
                )
            // remove unset fields
            // message.message.removeFields( 59)
        } else {
            message.setValue(
                123,
                if (Constants.IS_CONTACT) "511101511344101" else Constants.CLSS_POS_DATA_CODE
            )
            // remove unset fields
            message.message.removeFields(52)
        }

        // set message hash
        val bytes = message.message.writeData()
        logger.log(IsoUtils.bytesToHex(bytes))
        val length = bytes.size
        val temp = ByteArray(length - 64)
        if (length >= 64) {
            System.arraycopy(bytes, 0, temp, 0, length - 64)
        }

        val sessionKey = store.getString(KEY_SESSION_KEY, "")
        val hashValue = IsoUtils.getMac(sessionKey, temp) //SHA256
        message.setValue(128, hashValue)
        message.dump(System.out, "request -- ")

        try {
            // open connection
            val isConnected = socket.open()
            if (!isConnected) return TransactionResponse(
                TIMEOUT_CODE,
                authCode = "",
                stan = stan,
                scripts = "",
                date = now.time,
                rrn = ""
            )

            val request = message.message.writeData()
            logger.log("Purchase Request HEX ---> ${IsoUtils.bytesToHex(request)}")

            val response = socket.sendReceive(request)
            //throw Exception("This is a test exception")
            logger.log("Purchase Response HEX1 ---> ${response?.let { IsoUtils.bytesToHex(it) }}")
            // close connection`
            socket.close()

            if (response == null || response.isEmpty())
                throw Exception("No response from remote entity")

            logger.log("Purchase Response HEX ---> ${IsoUtils.bytesToHex(response)}")

            val responseMsg = NibssIsoMessage(messageFactory.parseMessage(response, 0))
            responseMsg.dump(System.out, "")

            // return response
            return responseMsg.message.let {
                val authCode = it.getObjectValue<String?>(38) ?: ""
                val code = it.getObjectValue<String>(39)
                val scripts = it.getObjectValue<String>(55) ?: ""
                val rrn = it.getObjectValue<String>(37) ?: ""
                return@let TransactionResponse(
                    responseCode = code,
                    authCode = authCode,
                    stan = stan,
                    scripts = scripts ?: "",
                    date = now.time,
                    rrn = rrn
                )
            }
        } catch (e: SocketTimeoutException) {
            
            return TransactionResponse(
                responseCode = "20",
                authCode = "",
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            )
        } catch (e: Exception) {
            
            // log error
            logger.log(e.localizedMessage ?: "Exception occurred performing Card Purchase")
            e.printStackTrace()

            return TransactionResponse(
                responseCode = "20",
                authCode = "",
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            )
        }
    }

    override fun initiateCardNotPresentPurchase(
        terminalInfo: TerminalInfo,
        transaction: TransactionInfo
    ): TransactionResponse? {
        return null
    }

    override fun initiatePaycodePurchase(
        terminalInfo: TerminalInfo,
        code: String,
        iswPaymentInfo: IswPaymentInfo
    ): Pair<CardDetail, TransactionResponse?> {

        val pan = generatePan(code)
        val amount = String.format(Locale.getDefault(), "%012d", iswPaymentInfo.amount)
        val now = Date()
        val message = NibssIsoMessage(messageFactory.newMessage(0x200))
        val processCode = "001000"
        val stan = getNextStan()
        val randomReference = iswPaymentInfo.rrn
        val date = dateFormatter.format(now)
        val src = "501"

        val expiryDate = Calendar.getInstance().let {
            it.time = now
            val currentYear = it.get(Calendar.YEAR)
            it.set(Calendar.YEAR, currentYear + 1)
            it.time
        }

        // format track 2
        val expiry = yearAndMonthFormatter.format(expiryDate)
        val track2 = "${pan}D$expiry"

        // create card detail
        val card = CardDetail(
            pan = pan,
            expiry = expiry,
            type = CardType.VERVE
        )

        // format iccString data
        val authorizedAmountTLV = String.format("9F02%02d%s", amount.length / 2, amount)
        val transactionDateTLV = String.format("9A%02d%s", date.length / 2, date)
        val iccData =
            "9F260831BDCBC7CFF6253B9F2701809F10120110A50003020000000000000000000000FF9F3704F435D8A29F3602052795050880000000" +
                    "${transactionDateTLV}9C0100${authorizedAmountTLV}5F2A020566820238009F1A0205669F34034103029F3303E0F8C89F3501229F0306000000000000"

        message
            .setValue(2, pan)
            .setValue(3, processCode)
            .setValue(4, amount)
            .setValue(7, timeAndDateFormatter.format(now))
            .setValue(11, stan)
            .setValue(12, timeFormatter.format(now))
            .setValue(13, monthFormatter.format(now))
            .setValue(14, expiry)
            .setValue(18, terminalInfo.merchantCategoryCode)
            .setValue(22, "051")
            .setValue(23, "000")
            .setValue(25, "00")
            .setValue(26, "06")
            .setValue(28, "C00000000")
            .setValue(35, track2.replace("F", ""))
            .setValue(37, randomReference)
            .setValue(40, src)
            .setValue(41, terminalInfo.terminalId)
            .setValue(42, terminalInfo.merchantId)
            .setValue(43, terminalInfo.merchantNameAndLocation)
            .setValue(49, terminalInfo.currencyCode)
            .setValue(55, iccData)
            .setValue(59, "00") //""90")
            .setValue(123, "510101561344101")

        message.message.removeFields(32, 52)


        // set message hash
        val bytes = message.message.writeData()
        val length = bytes.size
        val temp = ByteArray(length - 64)
        if (length >= 64) {
            System.arraycopy(bytes, 0, temp, 0, length - 64)
        }

        val sessionKey = store.getString(KEY_SESSION_KEY, "")
        val hashValue = IsoUtils.getMac(sessionKey, temp) //SHA256
        message.setValue(128, hashValue)
        message.dump(System.out, "request -- ")

        try {

            // open connection
            val isConnected = socket.open()
            if (!isConnected) return Pair(
                card, TransactionResponse(
                    TIMEOUT_CODE,
                    authCode = "",
                    stan = iswPaymentInfo.currentStan,
                    scripts = "",
                    date = now.time,
                    rrn = iswPaymentInfo.rrn
                )
            )

            val request = message.message.writeData()
            val response = socket.sendReceive(request)
            // close connection
            socket.close()

            if (response == null || response.isEmpty()) return Pair(
                card, TransactionResponse(
                    responseCode = "20",
                    authCode = "",
                    stan = stan,
                    scripts = "",
                    date = now.time,
                    rrn = randomReference
                )
            )

            val responseMsg = NibssIsoMessage(messageFactory.parseMessage(response, 0))
            responseMsg.dump(System.out, "")


            // return response
            return responseMsg.message.let {
                val authCode = it.getObjectValue<String?>(38) ?: ""
                val scripts = it.getObjectValue<String>(55) ?: ""
                val responseCode = it.getObjectValue<String>(39)
                val rrn = it.getObjectValue<String>(37) ?: ""

                return@let Pair(
                    card, TransactionResponse(
                        responseCode,
                        authCode = authCode,
                        stan = stan,
                        scripts = scripts,
                        date = now.time,
                        rrn = rrn
                    )
                )
            }

        } catch (e: SocketTimeoutException) {
            
            reversePurchase(message, now.time)
            return Pair(
                card, TransactionResponse(
                    TIMEOUT_CODE,
                    authCode = "",
                    stan = iswPaymentInfo.currentStan,
                    scripts = "",
                    date = now.time,
                    rrn = iswPaymentInfo.rrn
                )
            )
        } catch (e: Exception) {
            
            // error message
            logger.log(e.localizedMessage ?: "Exception occurred performing Paycode Purchase")
            e.printStackTrace()
            // auto reverse txn purchase
            //reversePurchase(message, now.time)
            // return response
            return Pair(
                card, TransactionResponse(
                    TIMEOUT_CODE,
                    authCode = "",
                    stan = iswPaymentInfo.currentStan,
                    scripts = "",
                    date = now.time,
                    rrn = iswPaymentInfo.rrn
                )
            )
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
        val now = Date()
        val transmissionDateTime = timeAndDateFormatter.format(now)

        val message = NibssIsoMessage(messageFactory.newMessage(0x100))
        try {
            val processCode = "60" + transaction.accountType.value + "00"
            val hasPin = transaction.cardPIN.isNotEmpty()
            val stan = transaction.stan
            val randomReference = transaction.rrn

            Logger.with("IsoServiceImpl").log(transaction.amount.toString())

            message
                .setValue(2, transaction.cardPAN)
                .setValue(3, processCode)
                .setValue(4, String.format(Locale.getDefault(), "%012d", transaction.amount))
                .setValue(7, transmissionDateTime)
                .setValue(11, stan)
                .setValue(12, timeFormatter.format(now))
                .setValue(13, monthFormatter.format(now))
                .setValue(14, transaction.cardExpiry)
                .setValue(18, terminalInfo.merchantCategoryCode)
                .setValue(22, "051")
                .setValue(23, transaction.csn)
                .setValue(25, "00")
                .setValue(26, "06")
                .setValue(28, "C00000000")
                .setValue(32, transaction.cardPAN.substring(0, 6))
                .setValue(35, transaction.cardTrack2.replace("F", ""))
                .setValue(37, randomReference)
                .setValue(40, transaction.src)
                .setValue(41, terminalInfo.terminalId)
                .setValue(42, terminalInfo.merchantId)
                .setValue(43, terminalInfo.merchantNameAndLocation)
                .setValue(49, terminalInfo.currencyCode)
                .setValue(55, transaction.iccString)
                .setValue(59, "SMARTPOS|PRE_AUTH|${terminalInfo.tmsRouteType}")


            if (hasPin) {
                message.setValue(52, transaction.cardPIN)
                    .setValue(123, "510101511344101")
                logger.log("transaction pin ${transaction.cardPIN}")
                // remove unset fields
                message.message.removeFields(53, 54, 56, 60, 62, 64, 124)
            } else {
                message.setValue(123, "511101511344101")
                // remove unset fields
                message.message.removeFields(52, 53, 54, 56, 60, 62, 64, 124)
            }

            // set message hash
            val bytes = message.message.writeData()
            val length = bytes.size
            val temp = ByteArray(length - 64)
            if (length >= 64) {
                System.arraycopy(bytes, 0, temp, 0, length - 64)
            }

            val sessionKey = store.getString(KEY_SESSION_KEY, "")
            val hashValue = IsoUtils.getMac(sessionKey, temp) //SHA256
            message.setValue(128, hashValue)
            message.dump(System.out, "preAuth request -- ")

            // open connection
            val isConnected = socket.open()
            if (!isConnected) return TransactionResponse(
                TIMEOUT_CODE,
                authCode = "",
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            )

            // send request and read response
            val request = message.message.writeData()
            val response = socket.sendReceive(request)

            // close connection
            socket.close()
            //throw Exception("This is a test exception")

            if (response == null || response.isEmpty())
                throw Exception("No response from remote entity")

            val responseMsg = NibssIsoMessage(messageFactory.parseMessage(response, 0))
            responseMsg.dump(System.out, "")

            logger.log("Message ---> Stan == $stan \n Timedate ==> $transmissionDateTime ")
            logger.log("Response code ==> ${responseMsg.message.getObjectValue<String>(39)}")

            // return response
            return responseMsg.message.let {
                val authCode = it.getObjectValue<String?>(38) ?: ""
                val code = it.getObjectValue<String>(39)
                val scripts = it.getObjectValue<String>(55) ?: ""
                val rrn = it.getObjectValue<String>(37) ?: ""
                return@let TransactionResponse(
                    responseCode = code,
                    authCode = authCode,
                    stan = stan,
                    scripts = scripts,
                    date = now.time,
                    rrn = rrn
                )
            }
        } catch (e: SocketTimeoutException) {
            
            logger.log(e.localizedMessage ?: "SocketTimeoutException occurred performing PreAuth")
            //return reversePurchase(message, now.time)
            return TransactionResponse(
                TIMEOUT_CODE,
                authCode = "",
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            )
        } catch (e: Exception) {
            
            logger.log(e.localizedMessage ?: "Exception occurred performing PreAuth")
            e.printStackTrace()
            return TransactionResponse(
                TIMEOUT_CODE,
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

        // time of transaction
        val now = Date()

        val message = NibssIsoMessage(messageFactory.newMessage(0x220))

        try {
            val processCode = "61" + transaction.accountType.value + "00"
            val hasPin = transaction.cardPIN.isNotEmpty()
            val stan = transaction.stan
            val acquiringInstitutionId = "00000111129"
            val forwardingInstitutionId = "00000111129"
            val randomReference = transaction.rrn
            val actualSettlementAmount = "000000000000"
            val actualSettlementFee = "C00000000"
            val actualTransactionFee = "C00000000"

            val originalDataElement =
                "0100$preAuthStan$preAuthDateTime$acquiringInstitutionId$forwardingInstitutionId"
            val replacementAmount = String.format(
                Locale.getDefault(),
                "%012d",
                transaction.amount
            ) + actualSettlementAmount + actualTransactionFee + actualSettlementFee

            message
                .setValue(2, transaction.cardPAN)
                .setValue(3, processCode)
                .setValue(4, String.format(Locale.getDefault(), "%012d", transaction.amount))
                .setValue(7, timeAndDateFormatter.format(now))
                .setValue(11, stan)

                .setValue(14, transaction.cardExpiry)
                .setValue(18, terminalInfo.merchantCategoryCode)
                .setValue(22, "051")
                .setValue(23, transaction.csn)
                .setValue(25, "00")
                .setValue(26, "06")
                .setValue(28, "C00000000")
                .setValue(32, transaction.cardPAN.substring(0, 6))
                .setValue(35, transaction.cardTrack2.replace("F", ""))
                .setValue(37, randomReference)
                .setValue(40, transaction.src)
                .setValue(41, terminalInfo.terminalId)
                .setValue(42, terminalInfo.merchantId)
                .setValue(43, terminalInfo.merchantNameAndLocation)
                .setValue(49, terminalInfo.currencyCode)
                .setValue(55, transaction.iccString)
                .setValue(59, "SMARTPOS|COMPLETION|${terminalInfo.tmsRouteType}")
                .setValue(90, originalDataElement)
                .setValue(95, replacementAmount)
                .setValue(123, "510101511344101")



            if (hasPin) {
                message.setValue(52, transaction.cardPIN)
                logger.log("transaction pin ${transaction.cardPIN}")
                // remove unset fields
                message.message.removeFields(
                    9,
                    29,
                    30,
                    31,
                    33,
                    50,
                    53,
                    54,
                    56,
                    58,
                    60,
                    62,
                    64,
                    67,
                    98,
                    100,
                    102,
                    103,
                    124
                )
            } else {
                // remove unset fields
                message.message.removeFields(
                    9,
                    29,
                    30,
                    31,
                    33,
                    50,
                    52,
                    53,
                    54,
                    56,
                    58,
                    60,
                    62,
                    64,
                    67,
                    98,
                    100,
                    102,
                    103,
                    124
                )
            }


            // set message hash
            val bytes = message.message.writeData()
            val length = bytes.size
            val temp = ByteArray(length - 64)
            if (length >= 64) {
                System.arraycopy(bytes, 0, temp, 0, length - 64)
            }

            val sessionKey = store.getString(KEY_SESSION_KEY, "")
            val hashValue = IsoUtils.getMac(sessionKey, temp) //SHA256

            message.setValue(128, hashValue)
            // remove unset fields
            //message.message.removeFields(9, 29, 30, 31, 32, 33, 50, 52, 53, 54, 56, 58, 59, 60, 62, 64, 67, 98, 100, 102, 103, 124)
            message.dump(System.out, "request -- ")

            logger.log("Called ---->Completion message packed")


            // open connection
            val isConnected = socket.open()
            if (!isConnected) return TransactionResponse(
                TIMEOUT_CODE,
                authCode = preAuthAuthId,
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            )


            // send request and read response
            val request = message.message.writeData()
            val response = socket.sendReceive(request)

            // close connection
            socket.close()

            logger.log("Completion Request HEX ---> ${IsoUtils.bytesToHex(request)}")
            logger.log("Completion Response HEX ---> ${response?.let { IsoUtils.bytesToHex(it) }}")

            if (response == null || response.isEmpty())
                throw Exception("No response from remote entity")

            val responseMsg = NibssIsoMessage(messageFactory.parseMessage(response, 0))
            responseMsg.dump(System.out, "")


            // return response
            return responseMsg.message.let {
                val authCode = it.getObjectValue<String?>(38) ?: preAuthAuthId
                val code = it.getObjectValue<String>(39)
                val scripts = it.getObjectValue<String>(55) ?: ""
                val rrn = it.getObjectValue<String>(37) ?: ""

                return@let TransactionResponse(
                    responseCode = code,
                    authCode = authCode,
                    stan = stan,
                    scripts = scripts,
                    date = now.time,
                    rrn = rrn
                )
            }
        } catch (e: SocketTimeoutException) {
            
            logger.log(
                e.localizedMessage ?: "SocketTimeoutException occurred performing Completion"
            )
            //return reversePurchase(message, now.time)
            return TransactionResponse(
                TIMEOUT_CODE,
                authCode = "",
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            )
        } catch (e: Exception) {
            
            logger.log(e.localizedMessage ?: "Error occurred performing Completion")
            e.printStackTrace()
            //return reversePurchase(message, now.time)
            return TransactionResponse(
                TIMEOUT_CODE,
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

        // time of txn
        val now = Date()

        val message = NibssIsoMessage(messageFactory.newMessage(0x200))
        try {
            val processCode = "20" + transaction.accountType.value + "00"
            val hasPin = transaction.cardPIN.isNotEmpty()
            val stan = transaction.stan
            val randomReference = transaction.rrn
            val timeDateNow = timeAndDateFormatter.format(now)

            message
                .setValue(2, transaction.cardPAN)
                .setValue(3, processCode)
                .setValue(4, String.format(Locale.getDefault(), "%012d", transaction.amount))
                .setValue(7, timeDateNow)
                .setValue(11, stan)
                .setValue(12, timeFormatter.format(now))
                .setValue(13, monthFormatter.format(now))
                .setValue(14, transaction.cardExpiry)
                .setValue(18, terminalInfo.merchantCategoryCode)
                .setValue(22, "051")
                .setValue(23, transaction.csn)
                .setValue(25, "00")
                .setValue(26, "06")
                .setValue(28, "C00000000")
                .setValue(32, transaction.cardPAN.substring(0, 6))
                .setValue(35, transaction.cardTrack2.replace("F", ""))
                .setValue(37, randomReference)
                .setValue(40, transaction.src)
                .setValue(41, terminalInfo.terminalId)
                .setValue(42, terminalInfo.merchantId)
                .setValue(43, terminalInfo.merchantNameAndLocation)
                .setValue(49, terminalInfo.currencyCode)
                .setValue(55, transaction.iccString)
                .setValue(59, "SMARTPOS|REFUND|${terminalInfo.tmsRouteType}")

            if (hasPin) {
                message.setValue(52, transaction.cardPIN)
                    .setValue(123, "510101511344101")

                // remove unset fields
//                message.message.removeFields( 59)
            } else {
                message.setValue(123, "511101511344101")
                // remove unset fields
                message.message.removeFields(52)
            }

            // set message hash
            val bytes = message.message.writeData()
            val length = bytes.size
            val temp = ByteArray(length - 64)
            if (length >= 64) {
                System.arraycopy(bytes, 0, temp, 0, length - 64)
            }

            val sessionKey = store.getString(KEY_SESSION_KEY, "")
            val hashValue = IsoUtils.getMac(sessionKey, temp) //SHA256
            message.setValue(128, hashValue)
            message.dump(System.out, "request -- ")

            // open connection
            val isConnected = socket.open()
            if (!isConnected) return TransactionResponse(
                TIMEOUT_CODE,
                authCode = "",
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            )


            // send request and read response
            val request = message.message.writeData()
            val response = socket.sendReceive(request)

            // close connection
            socket.close()

            if (response == null || response.isEmpty())
                throw Exception("No response from remote entity")

            val responseMsg = NibssIsoMessage(messageFactory.parseMessage(response, 0))
            responseMsg.dump(System.out, "")

            // return response
            return responseMsg.message.let {
                val authCode = it.getObjectValue<String?>(38) ?: ""
                val code = it.getObjectValue<String>(39)
                val scripts = it.getObjectValue<String>(55) ?: ""
                val rrn = it.getObjectValue<String>(37) ?: ""
                return@let TransactionResponse(
                    responseCode = code,
                    authCode = authCode,
                    stan = stan,
                    scripts = scripts,
                    date = now.time,
                    rrn = rrn
                )
            }
        } catch (e: SocketTimeoutException) {
            
            logger.log(
                e.localizedMessage ?: "SocketTimeoutException occurred performing BillPayment"
            )
            //return reversePurchase(message, now.time)
            return TransactionResponse(
                TIMEOUT_CODE,
                authCode = "",
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            )
        } catch (e: Exception) {
            
            // log error
            logger.log(e.localizedMessage)
            e.printStackTrace()
            // auto reverse txn purchase
            //return reversePurchase(message, now.time)
            // return response
            return TransactionResponse(
                TIMEOUT_CODE,
                authCode = "",
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            )
        }
    }



    override fun initiateBillPayment(
        terminalInfo: TerminalInfo,
        transaction: TransactionInfo,
        inquiryResponse: InquiryResponse
    ): TransactionResponse? {
        TODO("Not yet implemented")
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
        //TODO("Not Implemented at the moment, not sure if this method is needed")
        logger.log("D/IsoServiceImpl+++++ in reversal method")
        val now = Date()
        val formattedDate: String = timeAndDateFormatter.format(now)

        try {

            val txnAmount = String.format(Locale.getDefault(), "%012d", transaction.amount)

            val txnFee = "C00000000"
            val settlementFee = "C00000000"

            val originalTransactionMti =
                "0200" //TODO using 0200 for now since bulk of transaction is purchase

            val replacementAmount = txnAmount + txnAmount + txnFee + settlementFee

            val message = NibssIsoMessage(messageFactory.newMessage(0x420))
            message
                .setValue(2, transaction.cardPAN)
                .setValue(3, "00" + transaction.accountType.value + "00")
                .setValue(4, txnAmount)
                .setValue(7, formattedDate)
                .setValue(11, transaction.stan)
                .setValue(12, formattedDate.substring(4))
                .setValue(13, formattedDate.substring(0, 4))
                .setValue(14, transaction.cardExpiry)
                .setValue(18, terminalInfo.merchantCategoryCode)
                .setValue(22, "051")
                .setValue(23, transaction.csn)
                .setValue(25, "00")
                .setValue(26, "06")
                .setValue(28, "C00000000")
                .setValue(32, transaction.cardPAN.substring(0, 6))
                .setValue(35, transaction.cardTrack2.replace("F", ""))
                .setValue(37, transaction.rrn)
                .setValue(40, transaction.src)
                .setValue(41, terminalInfo.terminalId)
                .setValue(42, terminalInfo.merchantId)
                .setValue(43, terminalInfo.merchantNameAndLocation)
                .setValue(49, terminalInfo.currencyCode)
                .setValue(56, "4000")
                .setValue(59, "SMARTPOS|REVERSAL|${terminalInfo.tmsRouteType}")
                .setValue(
                    90,
                    originalTransactionMti + transaction.stan + formattedDate + "0000011112900000111129"
                )
                .setValue(95, replacementAmount)
                .setValue(123, "510101511344101")

            // set message hash
            val bytes = message.message.writeData()
            val length = bytes.size
            val temp = ByteArray(length - 64)
            if (length >= 64) {
                System.arraycopy(bytes, 0, temp, 0, length - 64)
            }

            val sessionKey = store.getString(KEY_SESSION_KEY, "")
            val hashValue = IsoUtils.getMac(sessionKey, temp) //SHA256
            message.setValue(128, hashValue)
            message.dump(System.out, "reversal request -- ")

            // open connection
            val isConnected = socket.open()
            if (!isConnected) return TransactionResponse(
                TIMEOUT_CODE,
                authCode = "",
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            )

            logger.log("D/IsoServiceImpl++++sending reversal")

            val request = message.message.writeData()
            val response = socket.sendReceive(request)
            // close connection
            socket.close()

            if (response == null || response.isEmpty()) return TransactionResponse(
                responseCode = "20",
                authCode = "",
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            )

            val responseMsg = NibssIsoMessage(messageFactory.parseMessage(response, 0))
            responseMsg.dump(System.out, "")


            // return response
            return responseMsg.message.let {
                val authCode = it.getObjectValue(38) ?: ""
                var code = it.getObjectValue<String>(39)
                val scripts = it.getObjectValue<String>(55) ?: ""
                val rrn = it.getObjectValue<String>(37) ?: ""


                return@let TransactionResponse(
                    responseCode = code,
                    authCode = authCode,
                    stan = transaction.stan,
                    scripts = scripts,
                    date = now.time,
                    rrn = rrn
                )
            }
        } catch (e: Exception) {
            
            logger.log(e.localizedMessage)
            e.printStackTrace()
            return TransactionResponse(
                TIMEOUT_CODE,
                authCode = "",
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            )
        }
    }



    override fun initiatePurchaseWithCashBack(
        terminalInfo: TerminalInfo,
        transaction: TransactionInfo
    ): TransactionResponse? {
        val now = Date()
        val message = NibssIsoMessage(messageFactory.newMessage(0x200))
        message.message.setField(54, IsoValue(IsoType.LLLVAR, 120))
        val processCode = "09" + transaction.accountType.value + "00"
        val hasPin = transaction.cardPIN.isNotEmpty()
        val stan = transaction.stan
        val randomReference = transaction.rrn

        val additionalAmounts = transaction.additionalAmounts.toString();
        val field54 =
            transaction.accountType.value + "40" + terminalInfo.currencyCode + "D" + additionalAmounts.padStart(
                12,
                '0'
            )
        val formattedSurcharge =
            'D' + String.format(Locale.getDefault(), "%08d", transaction.surcharge)

        message
            .setValue(2, transaction.cardPAN)
            .setValue(3, processCode)
            .setValue(4, String.format(Locale.getDefault(), "%012d", transaction.amount))
            .setValue(7, timeAndDateFormatter.format(now))
            .setValue(11, stan)
            .setValue(12, timeFormatter.format(now))
            .setValue(13, monthFormatter.format(now))
            .setValue(14, transaction.cardExpiry)
            .setValue(18, terminalInfo.merchantCategoryCode)
            .setValue(22, "051")
            .setValue(23, transaction.csn)
            .setValue(25, "00")
            .setValue(26, "06")
            .setValue(28, formattedSurcharge)
            .setValue(32, transaction.cardPAN.substring(0, 6))
            .setValue(35, transaction.cardTrack2.replace("F", ""))
            .setValue(37, randomReference)
            .setValue(40, transaction.src)
            .setValue(41, terminalInfo.terminalId)
            .setValue(42, terminalInfo.merchantId)
            .setValue(43, terminalInfo.merchantNameAndLocation)
            .setValue(49, terminalInfo.currencyCode)
            .setValue(54, field54)
            .setValue(55, transaction.iccString)

        if (hasPin) {
            message.setValue(52, transaction.cardPIN)
                .setValue(123, "510101511344101")
            // remove unset fields
//            message.message.removeFields( 59)
        } else {
            message.setValue(123, "511101511344101")
            // remove unset fields
            message.message.removeFields(52)
        }

        // set message hash
        val bytes = message.message.writeData()
        val length = bytes.size
        val temp = ByteArray(length - 64)
        if (length >= 64) {
            System.arraycopy(bytes, 0, temp, 0, length - 64)
        }

        val sessionKey = store.getString(KEY_SESSION_KEY, "")
        val hashValue = IsoUtils.getMac(sessionKey, temp) //SHA256
        message.setValue(128, hashValue)
        message.dump(System.out, "request -- ")

        try {
            // open connection
            val isConnected = socket.open()
            if (!isConnected) return TransactionResponse(
                TIMEOUT_CODE,
                authCode = "",
                stan = stan,
                scripts = "",
                date = now.time,
                rrn = ""
            )

            val request = message.message.writeData()
            logger.log("Purchase with cashback Request HEX ---> ${IsoUtils.bytesToHex(request)}")

            val response = socket.sendReceive(request)
            // close connection
            socket.close()

            if (response == null || response.isEmpty())
                throw Exception("No response from remote entity")

            val responseMsg = NibssIsoMessage(messageFactory.parseMessage(response, 0))
            responseMsg.dump(System.out, "")


            // return response
            return responseMsg.message.let {
                val authCode = it.getObjectValue<String?>(38) ?: ""
                val code = it.getObjectValue<String>(39)
                val scripts = it.getObjectValue<String>(55) ?: ""
                val rrn = it.getObjectValue<String>(37) ?: ""
                return@let TransactionResponse(
                    responseCode = code,
                    authCode = authCode,
                    stan = stan,
                    scripts = scripts,
                    date = now.time,
                    additionalInfo = AdditionalInfo(formattedSurcharge, additionalAmounts),
                    rrn = rrn
                )
            }
        } catch (e: SocketTimeoutException) {
            
            logger.log(
                e.localizedMessage
                    ?: "SocketTimeoutException occurred performing Purchase with cashback"
            )
            //return reversePurchase(message, now.time)
            return TransactionResponse(
                TIMEOUT_CODE,
                authCode = "",
                stan = transaction.stan,
                scripts = "",
                date = now.time,
                rrn = transaction.rrn
            )
        } catch (e: Exception) {
            
            // log error
            logger.log(e.localizedMessage)
            e.printStackTrace()
            logger.log("D/IsoServiceImpl++++attempting to send reversal")
            // auto reverse txn purchase
            //return reversePurchase(message, now.time)
            // return response
            return TransactionResponse(
                TIMEOUT_CODE,
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
        val now = Date()
        // NOT YET IMPLEMENTED
        return TransactionResponse(
            TIMEOUT_CODE,
            authCode = "",
            stan = transaction.stan,
            scripts = "",
            date = now.time,
            rrn = transaction.rrn
        )
    }




    private fun reversePurchase(
        txnMessage: NibssIsoMessage,
        prevTime: Long,
        prevResponseCode: String? = null
    ): Pair<TransactionResponse, TransactionResponse?> {
        logger.log("D/IsoServiceImpl+++++ in reversal method")
        val now = Date()
        val stan = getNextStan()

        val transactionResponse = TransactionResponse(
            prevResponseCode ?: TIMEOUT_CODE,
            authCode = "",
            stan = txnMessage.message.getField<Any>(11).toString(),
            scripts = "",
            date = prevTime,
            rrn = txnMessage.message.getField<Any>(37).toString()
        )

        try {
            val message = NibssIsoMessage(messageFactory.newMessage(0x420))
            val randomReference = "000000$stan"

            val txnAmount = txnMessage.message.getField<Any>(4).toString()
            val txnStan = txnMessage.message.getField<Any>(11).toString()
            val txnDateTime = txnMessage.message.getField<Any>(7).toString()
            val pinData = txnMessage.message.getField<Any>(52)

            // txn acquirer and forwarding code
            val txnCodes = "0000011112900000111129"
            // settlement amt
            val settlement = "000000000000"
            val txnFee = "C00000000"
            val settlementFee = "C00000000"

            val originalDataElements = "0200$txnStan$txnDateTime$txnCodes"
            val replacementAmount = txnAmount + settlement + txnFee + settlementFee

            message
                .copyFieldsFrom(txnMessage)

                .setValue(11, txnMessage.message.getField<Any>(11).toString())
                .setValue(37, txnMessage.message.getField<Any>(37).toString())
                .setValue(56, "4000") // timeout waiting for response
                .setValue(90, originalDataElements)
                .setValue(95, replacementAmount)
                // remove unused fields
                .message.removeFields(28, 32, 53, 55, 62)

            // set or remove pin field
            if (pinData != null) message.setValue(52, pinData.toString())
            else message.message.removeFields(52)


            // set message hash
            val bytes = message.message.writeData()
            val length = bytes.size
            val temp = ByteArray(length - 64)
            if (length >= 64) {
                System.arraycopy(bytes, 0, temp, 0, length - 64)
            }

            val sessionKey = store.getString(KEY_SESSION_KEY, "")
            val hashValue = IsoUtils.getMac(sessionKey, temp) //SHA256
            message.setValue(128, hashValue)
            message.dump(System.out, "reversal request -- ")

            // open connection
            val isConnected = socket.open()
            if (!isConnected) return Pair(
                transactionResponse, TransactionResponse(
                    TIMEOUT_CODE,
                    authCode = "",
                    stan = stan,
                    scripts = "",
                    date = now.time,
                    rrn = ""
                )
            )

            logger.log("D/IsoServiceImpl++++sending reversal")

            val request = message.message.writeData()
            val response = socket.sendReceive(request)
            // close connection
            socket.close()

            if (response == null || response.isEmpty()) return Pair(
                transactionResponse, TransactionResponse(
                    responseCode = "20",
                    authCode = "",
                    stan = stan,
                    scripts = "",
                    date = now.time,
                    rrn = ""
                )
            )

            val responseMsg = NibssIsoMessage(messageFactory.parseMessage(response, 0))
            responseMsg.dump(System.out, "")


            // return response
            return Pair(transactionResponse, responseMsg.message.let {
                val authCode = it.getObjectValue(38) ?: ""
                var code = it.getObjectValue<String>(39)
                val scripts = it.getObjectValue<String>(55) ?: ""
                val rrn = it.getObjectValue<String>(37) ?: ""

                return@let TransactionResponse(
                    responseCode = code,
                    authCode = authCode,
                    stan = stan,
                    scripts = scripts,
                    date = now.time,
                    rrn = rrn
                )
            })
        } catch (e: Exception) {
            
            logger.log(e.localizedMessage)
            e.printStackTrace()
            return Pair(
                transactionResponse, TransactionResponse(
                    TIMEOUT_CODE,
                    authCode = "",
                    stan = stan,
                    scripts = "",
                    date = now.time,
                    rrn = ""
                )
            )
        }
    }

    companion object {
        private const val SRCI = 53


    }

}