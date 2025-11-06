package com.example.nibss_sdk

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.IsoServiceFactory
import com.example.interfaces.IsoServiceListener
import com.example.interfaces.library.IsoService
import com.example.models.NibssConfig
import com.example.models.core.TerminalInfo
import com.example.nibss_sdk.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.log

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), IsoServiceListener {
    private val viewModel: MainViewModel by viewModels()
    private val terminalId = "2011E138"
    private val ip = "196.45.10.10"
    private val port = 5000

    private lateinit var binding: ActivityMainBinding




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 1️⃣ Attach the listener to ViewModel
        viewModel.initiate(this)

        // 2️⃣ Observe responses
        viewModel.keyDownloadResponse.observe(this) { success ->
            if (success) {
                Log.d("ISO", "Key download successful ✅")
                // Continue to download parameters
            } else {
                Log.e("ISO", "Key download failed ❌")
            }
        }

        setupUi()

        // 3️⃣ Trigger key download

    }

    private fun setupUi() {
        println("Setting up UI")
        binding.cardKeyDownload.setOnClickListener {
            // Handle key download
            println("I want to download key")
            viewModel.downloadKey(terminalId, ip, port)
        }

        binding.cardParamDownload.setOnClickListener {
            // Handle parameter download
        }

        binding.cardTestPurchase.setOnClickListener {
            // Handle test purchase
        }

        binding.cardTestPurchaseCashback.setOnClickListener {
            // Handle test purchase cashback
        }

        binding.cardTestRefund.setOnClickListener {
            // Handle refund
        }

        binding.cardTestBalance.setOnClickListener {
            // Handle balance enquiry
        }

        binding.cardTestCashAdvance.setOnClickListener {
            // Handle cash advance
        }

        binding.cardTestPreAuth.setOnClickListener {
            // Handle pre-auth
        }

        binding.cardTestSalesCompletion.setOnClickListener {
            // Handle sales completion
        }
    }

    override fun onStart(operation: String) {
        println("I have started listening to the operation: $operation")
    }

    override fun onSuccess(operation: String, data: Any?) {

    }

    override fun onError(
        operation: String,
        message: String,
        throwable: Throwable?
    ) {

    }

    override fun onComplete(operation: String) {

    }


}