# NIBSS SDK Integration Guide

This guide explains how to integrate with the NIBSS ISO service provided in this SDK.

**IMPORTANT:** The service is currently undergoing refactoring. The code as it stands has some issues that need to be resolved before it can be used in a production application. Specifically, the `NibssIsoServiceImpl` and its factory are not yet public, and its methods are synchronous, which will lead to runtime crashes on Android. The following guide describes the intended integration path once these issues are resolved.

## 1. Get an instance of IsoService

To interact with the NIBSS ISO service, you first need to obtain an instance of `IsoService` from the `IsoServiceFactory`.

```kotlin
import com.example.IsoServiceFactory
import com.example.interfaces.IsoServiceListener
import com.example.models.NibssConfig
import com.interswitchng.smartpos.shared.interfaces.library.KeyValueStore
import android.content.Context

// You need to provide implementations for these
val context: Context = // ... your application context
val keyValueStore: KeyValueStore = // ... your KeyValueStore implementation
val nibssConfig = NibssConfig("your_ip", 12345) // replace with actual IP and Port

// Create a listener to handle callbacks
val isoServiceListener = object : IsoServiceListener {
    override fun onStart(operation: String) {
        // Handle operation start
    }

    override fun onSuccess(operation: String, data: Any?) {
        // Handle successful operation
        // The 'data' parameter will contain the response
    }

    override fun onError(operation: String, message: String, throwable: Throwable?) {
        // Handle errors
    }

    override fun onComplete(operation: String) {
        // Handle operation completion
    }
}

// Create the service instance
val nibssIsoService = IsoServiceFactory.createNIBSS(
    context,
    keyValueStore,
    nibssConfig,
    listener = isoServiceListener
)
```

## 2. Using the Service

Once you have an instance of `nibssIsoService`, you can call its methods. These methods are asynchronous. The results will be delivered to the `IsoServiceListener` you provided.

### Example: Downloading Keys

To download the master, session, and PIN keys, you can call the `downloadKey` method.

```kotlin
import com.example.models.core.TerminalInfo

val terminalInfo = TerminalInfo(terminalId = "your_terminal_id") // provide your terminal info

nibssIsoService.downloadKey(terminalInfo, nibssConfig.ip, nibssConfig.port)
```

The result of this operation will be delivered to the `onSuccess` or `onError` methods of your `isoServiceListener`.

---

This documentation will be updated once the refactoring is complete.
