package com.example.interfaces

interface IsoServiceListener {
    fun onStart(operation: String)
    fun onSuccess(operation: String, data: Any? = null)
    fun onError(operation: String, message: String, throwable: Throwable? = null)
    fun onComplete(operation: String)
}