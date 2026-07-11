package com.opfis.shared.logging

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private class DesktopLogger : Logger {
    private val timestampFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun debug(tag: String, message: String) {
        println(formatLine("DEBUG", tag, message))
    }

    override fun info(tag: String, message: String) {
        println(formatLine("INFO", tag, message))
    }

    override fun warn(tag: String, message: String, throwable: Throwable?) {
        System.err.println(formatLine("WARN", tag, message))
        throwable?.printStackTrace()
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        System.err.println(formatLine("ERROR", tag, message))
        throwable?.printStackTrace()
    }

    private fun formatLine(level: String, tag: String, message: String): String {
        val timestamp = LocalDateTime.now().format(timestampFormatter)
        return "$timestamp $level [$tag] $message"
    }
}

actual fun platformLogger(): Logger = DesktopLogger()
