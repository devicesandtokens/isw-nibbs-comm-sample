package com.example.nibss_sdk

import com.interswitchng.smartpos.shared.interfaces.library.KeyValueStore
import com.pixplicity.easyprefs.library.Prefs

class KeyValueStoreImpl(): KeyValueStore {

    override fun saveString(key: String, value: String) {
        val result = Prefs.putString(key, value)
    }

    override fun getString(key: String, default: String): String {
        return Prefs.getString(key, default) ?: ""
    }

    override fun saveNumber(key: String, value: Long) {
        Prefs.putLong(key, value)
    }

    override fun getNumber(key: String, default: Long): Long {
        return Prefs.getLong(key, default)
    }


    override fun saveBoolean(key: String, value: Boolean) {
        Prefs.putBoolean(key, value)
    }

    override fun getBoolean(key: String): Boolean {
        return Prefs.getBoolean(key)
    }

     override fun getBoolean(key: String, default: Boolean): Boolean {
         return if (Prefs.contains(key)) {
             Prefs.getBoolean(key)
         } else {
             default
         }
     }

     override fun contains(key: String): Boolean {
         return Prefs.contains(key)
     }
}