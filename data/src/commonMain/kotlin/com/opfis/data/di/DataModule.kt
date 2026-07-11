package com.opfis.data.di

import com.opfis.data.systemstatus.LocalSystemStatusRepository
import com.opfis.domain.systemstatus.SystemStatusRepository
import com.opfis.shared.logging.Logger
import com.opfis.shared.logging.platformLogger
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Koin bindings owned by the Infrastructure layer. Only this module and
 * the composition root (`:composeApp`) depend on Koin - see ADR 0003.
 */
val dataModule = module {
    single<Logger> { platformLogger() }
    singleOf(::LocalSystemStatusRepository) { bind<SystemStatusRepository>() }
}
