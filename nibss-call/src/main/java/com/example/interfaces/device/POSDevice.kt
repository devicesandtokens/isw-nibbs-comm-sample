package com.example.interfaces.device

import android.graphics.Bitmap

/**
 * This interface represents the POS device to be used by the library,
 * providing access to its printer and card reader.
 */
interface POSDevice {

    /**
     * The name of the POS terminal device
     */
    val name: String


    /**
     * The serial number of the POS terminal device
     */
    fun serialNumber(): String {
        return ""
    }

    /**
     * The flag to check if POSDevice supports finger print
     */
    val hasFingerPrintReader: Boolean


    /**
     * Function to load DUKPT Key (BDK) into the pos device
     *
     * @param initialKey the hex string containing the DUKPT initial key to be loaded into the terminal
     * @param ksn the initial key's serial number
     */
    fun loadInitialKey(initialKey: String, ksn: String)


    /**
     * Function to load NIBSS MASTER Key into the pos device
     *
     * @param masterKey the hex string containing the Master key
     */
    fun loadMasterKey(masterKey: String)


    /**
     * Function to load NIBSS PIN Key into the pos device
     *
     * @param pinKey the hex string containing the PIN key
     */
    fun loadPinKey(pinKey: String)


    /**
     * Function to set the company logo into the pos device
     *
     * @param bitmap the bitmap image of the logo
     */
    fun setCompanyLogo(bitmap: Bitmap){}

    /**
     * This function checks if the device has a keyboard module.

     * @return The ordinal of the returned Enum response:
     *   - 0: Yes - Device has keyboard module.
     *   - 1: No - Device has NO keyboard module.
     *   - 2: Invalid
     *   - 3: Unknown
     */
    fun doesDeviceHaveKeyboard(): Int
}