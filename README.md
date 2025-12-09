# NIBSS SDK Integration Guide

This guide explains how to integrate with the NIBSS ISO service provided in this SDK.

**IMPORTANT:** The service is currently undergoing refactoring. The code as it stands has some issues that need to be resolved before it can be used in a production application. Specifically, the `NibssIsoServiceImpl` and its factory are not yet public, and its methods are synchronous, which will lead to runtime crashes on Android. The following guide describes the intended integration path once these issues are resolved.

---

## 0. Add the Library to Your Project

### Option 1: Using Version Catalog (`libs.versions.toml`)

1.  In your `gradle/libs.versions.toml` file, add the following to the `[versions]` and `[libraries]` sections:

    ```toml
    [versions]
    nibbsCaller = "1.0.0"

    [libraries]
    nibbs-caller = { group = "io.github.devicesandtokens", name="isw-nibss-caller", version.ref="nibbsCaller" }
    ```

2.  Then, in your module-level `build.gradle.kts` file, add the dependency:

    ```kotlin
    dependencies {
        implementation(libs.nibbs.caller)
    }
    ```

### Option 2: Manual Gradle Configuration

1.  Add credentials to `~/.gradle/gradle.properties`:

    ```properties
    gpr.user=YOUR_GITHUB_USERNAME
    gpr.key=YOUR_PERSONAL_ACCESS_TOKEN
    ```

2.  Add the repository to your `settings.gradle.kts`:
    ```kotlin
    dependencyResolutionManagement {
        repositories {
            google()
            mavenCentral()

            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/YOUR_GITHUB_USERNAME/YOUR_LIBRARY_REPO")
                credentials {
                    username = findProperty("gpr.user") as String?
                    password = findProperty("gpr.key") as String?
                }
            }
        }
    }
    ```

3.  Add the dependency to your app's `build.gradle.kts`:
    ```kotlin
    dependencies {
        implementation("io.github.devicesandtokens:isw-nibss-caller:1.0.0")
    }
    ```

## 1. Get an instance of IsoService

To interact with the NIBSS ISO service, you first need to obtain an instance of `IsoService` from the `IsoServiceFactory`.

```kotlin
import com.example.IsoServiceFactory
import com.example.interfaces.library.IsoService
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
val nibssIsoService: IsoService = IsoServiceFactory.createNIBSS(
    context,
    keyValueStore,
    nibssConfig,
    listener = isoServiceListener
)
```

## 2. Using the Service

Once you have an instance of `nibssIsoService`, you can call its methods. The `IsoService` provides the following methods for performing ISO-8583 transactions:

-   `downloadKey(terminalId: String, ip: String, port: Int): Boolean`: Downloads the master and session keys from the EPMS server.
-   `downloadTerminalParameters(terminalId: String, ip: String, port: Int): Boolean`: Downloads terminal parameters.
-   `initiateCardPurchase(terminalInfo: TerminalInfo, transaction: TransactionInfo): TransactionResponse?`: Initiates a card purchase.
-   `initiateCardNotPresentPurchase(terminalInfo: TerminalInfo, transaction: TransactionInfo): TransactionResponse?`: Initiates a card-not-present purchase.
-   `initiatePaycodePurchase(terminalInfo: TerminalInfo, code: String, iswPaymentInfo: IswPaymentInfo): Pair<CardDetail, TransactionResponse?>`: Initiates a paycode purchase.
-   `initiatePreAuthorization(terminalInfo: TerminalInfo, transaction: TransactionInfo): TransactionResponse?`: Initiates a pre-authorization.
-   `initiateCompletion(terminalInfo: TerminalInfo, transaction: TransactionInfo, preAuthStan: String, preAuthDateTime: String, preAuthAuthId: String): TransactionResponse?`: Completes a pre-authorized transaction.
-   `initiateReversal(terminalInfo: TerminalInfo, transaction: TransactionInfo): TransactionResponse?`: Initiates a reversal.
-   `initiateRefund(terminalInfo: TerminalInfo, transaction: TransactionInfo): TransactionResponse?`: Initiates a refund.
-   `initiateBillPayment(terminalInfo: TerminalInfo, transaction: TransactionInfo, inquiryResponse: InquiryResponse): TransactionResponse?`: Initiates a bill payment.
-   `initiatePurchaseWithCashBack(terminalInfo: TerminalInfo, transaction: TransactionInfo): TransactionResponse?`: Initiates a purchase with cash back.
-   `initiateBalance(terminalInfo: TerminalInfo, transaction: TransactionInfo): TransactionResponse?`: Initiates a balance inquiry.

Please refer to the interface definition for details on the required parameters and return types for each method. Asynchronous results will be delivered to the `IsoServiceListener` you provided.

### Example: Downloading Keys

To download the master, session, and PIN keys, you can call the `downloadKey` method.

```kotlin
import com.example.models.core.TerminalInfo

val terminalInfo = TerminalInfo(terminalId = "your_terminal_id") // provide your terminal info

nibssIsoService.downloadKey(terminalInfo.terminalId, nibssConfig.ip, nibssConfig.port)
```

The result of this operation will be delivered to the `onSuccess` or `onError` methods of your `isoServiceListener`.

---

This documentation will be updated once the refactoring is complete.
