package com.qiscus.multichannel.util

import android.os.Build

class BuildVersionProviderUtil {

    companion object {
        @Volatile
        private var INSTANCE: BuildVersionProviderUtil? = null

        @JvmStatic
        private val instance: BuildVersionProviderUtil
            get() {
                if (INSTANCE == null) {
                    synchronized(BuildVersionProviderUtil::class.java) {
                        INSTANCE = BuildVersionProviderUtil()
                    }
                }

                return INSTANCE!!
            }

        fun get() = instance
    }

    private var sdkVersion: Int =  Build.VERSION.SDK_INT

    fun changeVersionTest(version: Int) {
        this.sdkVersion = version
    }

    fun isSame(version: Int): Boolean = sdkVersion == version

    fun isAbove(version: Int): Boolean = sdkVersion > version

    fun isUnder(version: Int): Boolean = sdkVersion < version

    fun isSamesOrAbove(version: Int): Boolean = sdkVersion >= version

    fun isSamesOrUnder(version: Int): Boolean = sdkVersion <= version
}