package com.opfis.shared.logging

import kotlin.test.Test
import kotlin.test.assertNotNull

class PlatformLoggerTest {

    @Test
    fun `platformLogger returns a usable logger instance`() {
        val logger = platformLogger()

        assertNotNull(logger)
        logger.debug("PlatformLoggerTest", "smoke test")
    }
}
