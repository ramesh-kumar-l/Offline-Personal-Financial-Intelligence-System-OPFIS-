package com.opfis.data.di

import com.opfis.data.systemstatus.PersistentSystemStatusRepository
import com.opfis.domain.systemstatus.SystemStatusRepository
import com.opfis.shared.logging.Logger
import com.opfis.shared.logging.platformLogger
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Koin bindings owned by the Infrastructure layer that don't need a
 * platform `Context`/directory. The database driver, key provider, and
 * backup port are registered by the platform-specific
 * `androidDataModule` / `desktopDataModule` (see ADR 0005) and loaded
 * alongside this module from the composition root.
 */
val dataModule =
    module {
        single<Logger> { platformLogger() }
        singleOf(::PersistentSystemStatusRepository) { bind<SystemStatusRepository>() }
    }
