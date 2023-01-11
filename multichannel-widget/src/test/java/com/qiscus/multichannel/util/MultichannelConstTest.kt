package com.qiscus.multichannel.util

import org.junit.jupiter.api.Test

internal class MultichannelConstTest {

    @Test
    fun runnerTest() {
        MultichannelConst.qiscusCore()
        MultichannelConst.setAllQiscusCore(arrayListOf())
        MultichannelConst.getAllQiscusCore()
    }

}