package com.opfis.app.io

import androidx.compose.runtime.Composable

/**
 * Returns a callback that terminates the running process (ROADMAP
 * Phase 9: after a successful encrypted-backup restore,
 * `FileBackupPort` has closed the live database driver - every
 * Koin-held repository singleton is permanently bound to that now-closed
 * connection, so continuing to use the app would crash unrelated
 * screens). The UI warns the user first, then invokes this.
 */
@Composable
expect fun rememberAppExitLauncher(): () -> Unit
