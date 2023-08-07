package com.qiscus.multichannel.util

import android.os.Build
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BuildVersionProviderUtilTest {

    private lateinit var version: BuildVersionProviderUtil

    @BeforeEach
    fun before() {
        version = BuildVersionProviderUtil.get()
    }

    @Test
    fun isSame() {
        version.changeVersionTest(Build.VERSION_CODES.M)

        assertTrue(
            version.isSame(Build.VERSION_CODES.M)
        )
    }

    @Test
    fun isSameFalse() {
        version.changeVersionTest(Build.VERSION_CODES.LOLLIPOP)

        assertFalse(
            version.isSame(Build.VERSION_CODES.M)
        )
    }

    @Test
    fun isAbove() {
        version.changeVersionTest(Build.VERSION_CODES.M)

        assertTrue(
            version.isAbove(Build.VERSION_CODES.LOLLIPOP)
        )
    }

    @Test
    fun isAboveFalse() {
        version.changeVersionTest(Build.VERSION_CODES.LOLLIPOP)

        assertFalse(
            version.isAbove(Build.VERSION_CODES.LOLLIPOP)
        )
    }

    @Test
    fun isUnder() {
        version.changeVersionTest(Build.VERSION_CODES.LOLLIPOP)

        assertTrue(
            version.isUnder(Build.VERSION_CODES.M)
        )
    }

    @Test
    fun isUnderFalse() {
        version.changeVersionTest(Build.VERSION_CODES.M)

        assertFalse(
            version.isUnder(Build.VERSION_CODES.M)
        )
    }

    @Test
    fun isSamesOrAbove() {
        version.changeVersionTest(Build.VERSION_CODES.FROYO)

        assertTrue(
            version.isSamesOrAbove(Build.VERSION_CODES.ECLAIR)
        )
    }

    @Test
    fun isSamesOrAboveFalse() {
        version.changeVersionTest(Build.VERSION_CODES.ECLAIR)

        assertFalse(
            version.isSamesOrAbove(Build.VERSION_CODES.FROYO)
        )
    }

    @Test
    fun isSamesOrUnder() {
        version.changeVersionTest(Build.VERSION_CODES.KITKAT)

        assertTrue(
            version.isSamesOrUnder(Build.VERSION_CODES.LOLLIPOP)
        )
    }

    @Test
    fun isSamesOrUnderFalse() {
        version.changeVersionTest(Build.VERSION_CODES.LOLLIPOP)

        assertFalse(
            version.isSamesOrUnder(Build.VERSION_CODES.KITKAT)
        )
    }
}