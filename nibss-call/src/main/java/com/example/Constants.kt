package com.example

import android.graphics.Bitmap


object Constants {

    var DEVICE_SERIAL_NUMBER = ""
    lateinit var COMPANY_LOGO: Bitmap
    var CANCEL_DIALOG_MESSAGE = "Would you like to change payment method, or try again?"
    var IS_CONTACT = true
    var CLSS_POS_DATA_CODE = "A10101711344101"

    // URL END POINTS
    internal const val CODE_END_POINT = "till.json"
    internal const val TRANSACTION_STATUS_QR = "transactions/qr"
    internal const val TRANSACTION_STATUS_USSD = "transactions/ussd.json"
    internal const val TRANSACTION_STATUS_TRANSFER = "virtualaccounts/transaction"
    internal const val BANKS_END_POINT = "till/short-codes/1"
    internal const val AUTH_END_POINT = "oauth/token"
    internal const val KIMONO_KEY_END_POINT = "/kmw/keydownloadservice"
    internal const val QUERY_END_POINT = "transactions"
    internal const val POST_BATCH_RECORDS_END_POINT = "transactions/pos/process-batch-records"
    internal const val SOCKET_IO = "socket.io/"
    const val THANKYOU_MERCHANT_CODE = "THANKYOU_MERCHANT_CODE"
    internal const val BILL_CATEGORY_ENDPOINT = "billerpurchase/api/v1/billers/categories"
    internal const val BILL_TYPE_ENDPOINT = "billerpurchase/api/v1/billers/categories/{categoryId}?withServices=true"
    internal const val BILL_SERVICE_OPTION_ENDPOINT = "billerpurchase/api/v1/billers/services/{serviceUrlName}"



    //    internal const val KIMONO_END_POINT = "kmw/v2/kimonoservice"
    internal const val KIMONO_END_POINT = "kmw/kimonoservice"
    internal const val KIMONO_MERCHANT_DETAILS_END_POINT_AUTO = "kmw/v2/serialid/{terminalSerialNo}"
    internal const val FINCH_USER_MANAGEMENT_END_POINT = "api/v2/finch/user-mgmt/device/serialid/{terminalSerialNo}"
    internal const val QTB_CONFIG_END_POINT = "till/config"

    // EMAIL
    internal const val EMAIL_END_POINT = "mail/send"
    internal const val EMAIL_TEMPLATE_ID = "d-c33c9a651cea40dd9b0ee4615593dcb4"

    internal const val KEY_PAYMENT_INFO = "payment_info_key"

    internal const val KEY_MASTER_KEY = "master_key"
    internal const val KEY_SESSION_KEY = "session_key"
    internal const val KEY_PIN_KEY = "pin_key"
    internal const val KIMONO_KEY = "kimono_key"

    const val KEY_MERCHANT_ADDRESS= "key_merchant_address"
    const val KEY_ADMIN_PIN = "terminal_admin_access_pin_key"
    internal const val TERMINAL_CONFIG_TYPE = "kimono_or_nibss"
    internal const val SETTINGS_TERMINAL_CONFIG_TYPE = "settings_kimono_or_nibss"
    // UTIL CONSTANTS
     const val CALL_HOME_TIME_IN_MIN = "30"
     const val SERVER_TIMEOUT_IN_SEC = "60"
     const val TERMINAL_CAPABILITIES = "E0F8C8"
     const val NAIRA_CURRENCY_CODE = "566"
     const val COUNTRY_CODE = "566"
     var POS_ENTRY_MODE = "051"

    const val DOLLAR_CURRENCY_CODE = "840"

    internal  const val  KIMONO_CALL_HOME="kimonoservice"

    const val EMPTY_STRING = ""
    const val PKMOD = "qAujj7YjqEihDiSMbEeGyqnib5YTi3SCyB57l8gV5nPjHd6kVvLImVZbmqjyixGPuIK4l5IASfhQ50VwRpKQ9x7VWD7DIOvu1%2bpdDDnbuzyFfVEINT/RpYBxYh6MooEUl/WvTh2Ym2snJ1GtfXLtQpeT3HnB60kbLjdLfs0k2%2bE%3d&pkex=AAEAAQ%3d%3d"
    const val PKEX = "AAEAAQ%3d%3d"

    const val THANKU_CASH_PROD = "https://api.thankucash.com/api/v1/thankuconnect/"
    const val THANKU_CASH_TEST = "https://testapi.thankucash.com/api/v1/thankuconnect/"
    const val THANKYOU_REWARD = "settletransaction"
    const val THANKYOU_BALANCE = "getbalance"
    const val THANKYOU_REDEEM = "redeem"
    const val THANKYOU_CONFIRM_REDEEM = "confirmredeem"
    const val THANKYOU_FAILED = "notifyfailedtransaction"
    const val THANKYOU_TEST_KEY = "7cffb3dd67d04770b713db09c8802d97"
    const val THANKYOU_LIVE_KEY = "bec8653108884269b5513c40e179b77e"

    const val AFRIGO_ACQUIRER_ID = "991002"

    // Amount Limits For Contactless Payments
    const val CONTACTLESS_ACQUIRER_CODE: String = "default"
    const val CONTACTLESS_CVM_LIMIT: Int = 500000
    const val CONTACTLESS_FLOOR_LIMIT: Int = 0
    const val CONTACTLESS_TRANS_LIMIT: Int = 2000000
    const val CONTACTLESS_TRANS_NO_DEVICE_LIMIT: Int = 2000000

    // contactless config load state

    var CTLS_CONFIG_LOADED = false

    // Pref Keys For Contactless Payments
    const val KEY_ACQUIRER_CODE = "acquirer_code"
    const val KEY_CONTACTLESS_CVM_LIMIT = "contactless_cvm_limit"
    const val KEY_CONTACTLESS_FLOOR_LIMIT = "contactless_floor_limit"
    const val KEY_CONTACTLESS_TRANS_LIMIT = "contactless_trans_limit"
    const val KEY_CONTACTLESS_TRANS_NO_DEVICE_LIMIT = "contactless_trans_no_device_limit"

    // Pref keys for Qr code config
    const val KEY_ENABLE_RECEIPT_QR_CODE = "KEY_ENABLE_RECEIPT_QR_CODE"
    const val RECEIPT_QR_ITEMS_CONFIG = "RECEIPT_QR_ITEMS_CONFIG"
    const val PREFER_REMOTE_CONFIG = "PREFER_REMOTE_CONFIG"
    const val KEY_FORCE_PRINT_BARCODE = "KEY_FORCE_PRINT_BARCODE"
    const val KEY_DISABLE_FIELD_LABEL = "KEY_DISABLE_FIELD_LABEL"
    const val KEY_BARCODE_SEPARATOR = "KEY_BARCODE_SEPARATOR"

    // Other Pref Keys
    const val AGENT_PHONE_NUMBER = "AGENT_PHONE_NUMBER"
    const val IS_FINCH_AGENT = "IS_FINCH_AGENT"
    const val KEY_ALLOWED_TRANSACTIONS = "allowed_transactions"
    const val KEY_QTB_MERCHANT_DATA = "qtb_merchant_data"

    const val KEY_SSL_SOCKET = "KEY_SSL_SOCKET"

    val ISW_KIMONO_KEY_URL: String
        get() {
            return Production.ISW_KEY_DOWNLOAD_URL
        }

    private object Production {

        const val ISW_USSD_QR_BASE_URL = "https://api.interswitchng.com/paymentgateway/api/v1/"
        const val ISW_TOKEN_BASE_URL = "https://passport.interswitchng.com/passport/"
        const val ISW_IMAGE_BASE_URL = "https://mufasa.interswitchng.com/p/paymentgateway/"
       const val ISW_KIMONO_URL = "https://kimono.interswitchng.com/kmw/v2/kimonoservice"
        // const val ISW_KIMONO_URL = "https://154.72.34.105:7075/kmw/v2/kimonoservice"
        const val ISW_KIMONO_BASE_URL = "https://kimono.interswitchng.com/"
        const val ISW_FINCH_BASE_URL = "https://api-gateway.interswitchng.com/" // REMOVE_THIS_BRO
        const val ISW_SOCKET_BASE_URL = "https://socket.interswitchng.com"
        const val ISW_POS_REPORTING_BASE_URL = "https://api.interswitchng.com/pos-transaction-reporting/api/v1/" //"https://ca37-102-88-83-56.ngrok-free.app/pos-transaction-reporting/api/v1/"
        const val ISW_TERMINAL_IP_EPMS = "196.6.103.73"
        const val ISW_TERMINAL_PORT_EPMS = 5043

        const val ISW_TERMINAL_IP_CTMS = "196.6.103.18"
        const val ISW_TERMINAL_PORT_CTMS = 5008

        const val ISW_TERMINAL_IP_NUS = "196.6.103.18"
        const val ISW_TERMINAL_PORT_NUS = 4008
        const val ISW_TERMINAL_IP_PPM = "41.223.145.223"
        const val ISW_TERMINAL_PORT_PPM = 80
        const val ISW_KEY_DOWNLOAD_URL = "http://kimono.interswitchng.com/kmw/keydownloadservice"
        const val PAYMENT_CODE = "04358001"
        const val BILLER_PURCHASE_SERVICES = "https://biller-purchase-service.k8.isw.la/"
    }

    private object Test {

        //        const val ISW_USSD_QR_BASE_URL = "https://api.interswitchng.com/paymentgateway/api/v1/"
        const val ISW_USSD_QR_BASE_URL = "https://project-x-merchant.k9.isw.la/paymentgateway/api/v1/"
        const val ISW_TOKEN_BASE_URL = "https://passport.interswitchng.com/passport/"
        const val ISW_IMAGE_BASE_URL = "https://mufasa.interswitchng.com/p/paymentgateway/"
        const val ISW_KIMONO_URL = "https://qa.interswitchng.com/kmw/v2/kimonoservice/amex"
        const val ISW_KIMONO_BASE_URL = "https://qa.interswitchng.com/"
        const val ISW_FINCH_BASE_URL = "https://finch-user-mgmt-service.k8.isw.la/"
        const val ISW_SOCKET_BASE_URL = "https://isw-socket-io.k8.isw.la"
        const val ISW_POS_REPORTING_BASE_URL = "https://pos-transaction-reporting.k8.isw.la/pos-transaction-reporting/api/v1/" //"https://ca37-102-88-83-56.ngrok-free.app/pos-transaction-reporting/api/v1/"
        const val ISW_TERMINAL_IP_EPMS = "196.6.103.72"
        const val ISW_TERMINAL_PORT_EPMS = 5043

        const val ISW_TERMINAL_IP_CTMS = "196.6.103.126"
        const val ISW_TERMINAL_PORT_CTMS = 55533

        const val ISW_TERMINAL_IP_NUS = "196.6.103.126"
        const val ISW_TERMINAL_PORT_NUS = 55533
        const val ISW_TERMINAL_IP_PPM = "41.223.145.223"
        const val ISW_TERMINAL_PORT_PPM = 80
        const val PAYMENT_CODE = "051626554287"
        const val BILLER_PURCHASE_SERVICES = "https://biller-purchase-service.k8.isw.la/"
    }

}