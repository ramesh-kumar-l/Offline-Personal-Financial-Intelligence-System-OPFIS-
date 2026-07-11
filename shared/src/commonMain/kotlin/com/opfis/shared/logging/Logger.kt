package com.opfis.shared.logging

/**
 * The single structured logging port for the whole codebase.
 *
 * Callers must never pass financial values, credentials, keys, tokens
 * or other PII in [message] or [throwable] - see SystemPrompt Part 2,
 * "Logging must never leak sensitive data."
 */
interface Logger {
    fun debug(
        tag: String,
        message: String,
    )

    fun info(
        tag: String,
        message: String,
    )

    fun warn(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    )

    fun error(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    )
}

expect fun platformLogger(): Logger
