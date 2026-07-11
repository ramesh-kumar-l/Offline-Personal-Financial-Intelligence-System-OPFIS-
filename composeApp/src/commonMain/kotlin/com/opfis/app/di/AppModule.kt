package com.opfis.app.di

import com.opfis.domain.systemstatus.usecase.ObserveSystemStatusUseCase
import org.koin.dsl.module

/**
 * Composition-root bindings owned by the Presentation layer. Wires
 * Application-layer use cases on top of the repository bindings
 * `:data` provides - see ADR 0003.
 */
val appModule =
    module {
        factory { ObserveSystemStatusUseCase(repository = get()) }
    }
