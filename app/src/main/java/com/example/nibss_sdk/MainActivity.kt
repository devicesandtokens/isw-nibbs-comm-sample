package com.example.nibss_sdk

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.IsoServiceFactory
import com.example.interfaces.IsoServiceListener
import com.example.interfaces.library.IsoService
import com.example.models.NibssConfig
import com.example.models.core.TerminalInfo

class MainActivity : AppCompatActivity() {
    private lateinit var isoService: IsoService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1️⃣ Initialize dependencies
        val keyValueStore = KeyValueStoreImpl()

        // 2️⃣ Create listener
        val listener = object : IsoServiceListener {
            override fun onStart(operation: String) {
                Log.d("ISO", "Started: $operation")
            }

            override fun onSuccess(operation: String, data: Any?) {
                Log.d("ISO", "Success: $operation - $data")
            }

            override fun onError(operation: String, message: String, throwable: Throwable?) {
                Log.e("ISO", "Error: $operation - $message", throwable)
            }

            override fun onComplete(operation: String) {
                Log.d("ISO", "$operation complete")
            }
        }

        val ip = "196.45.10.10"
        val port = 5000

        // 3️⃣ Build the service from factory
        isoService = IsoServiceFactory.createNIBSS(
            context = this,
            store = keyValueStore,
            nibssConfig = NibssConfig(ip = ip, port = port),
            listener = listener
        )

        // 4️⃣ Kick off operations (on a background thread)
        runKeyDownload()
    }

    private fun runKeyDownload() {
        Thread {
            try {
                val terminalInfo = TerminalInfo(
                    terminalId = "12345678",
                    tmsRouteType = "NIBSS_NUS"
                )

                val ip = "196.45.10.10"
                val port = 5000

                val success = isoService.downloadKey(terminalInfo, ip, port)
                Log.d("ISO", "Download Key Result: $success")

                if (success) {
                    val paramSuccess = isoService.downloadTerminalParameters(terminalInfo, ip, port)
                    Log.d("ISO", "Download Params Result: $paramSuccess")
                }
            } catch (e: Exception) {
                Log.e("ISO", "Exception in key download", e)
            }
        }.start()
    }
}