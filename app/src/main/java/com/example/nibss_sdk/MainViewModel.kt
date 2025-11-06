package com.example.nibss_sdk

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.interfaces.IsoServiceListener
import com.example.interfaces.library.IsoService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val isoFactory: IsoServiceFactoryWrapper
) : ViewModel() {


    private val _keyDownloadResponse = MutableLiveData<Boolean>()
    val keyDownloadResponse = MutableLiveData<Boolean>()


    // Holds your service instance
    private var isoService: IsoService? = null

    // Initialize service once
    fun initiate(listener: IsoServiceListener) {
        if (isoService == null) {
            isoService = isoFactory.create(listener)
        }
    }

    fun downloadKey(terminalId: String, ip: String, port:Int) {
        viewModelScope.launch(Dispatchers.IO) {
            println("downloading keys..........")
            val result = isoService!!.downloadKey(terminalId, ip, port)
            _keyDownloadResponse.postValue(result)
        }
    }



}