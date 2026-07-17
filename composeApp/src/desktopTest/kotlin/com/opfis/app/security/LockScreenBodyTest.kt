package com.opfis.app.security

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * [LockScreenBody] is pure/stateless (no Koin injection), so it's directly
 * renderable in a headless Compose UI test without faking the DI graph -
 * unlike [LockScreen] itself, which needs `RecordAuditEventUseCase` and the
 * platform biometric launcher (ROADMAP Phase 11).
 */
class LockScreenBodyTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `tapping Unlock invokes onUnlockTap`() =
        runComposeUiTest {
            var tapped = false
            setContent {
                LockScreenBody(
                    authFailed = false,
                    biometricUnavailable = false,
                    onUnlockTap = { tapped = true },
                    onManualConfirm = {},
                )
            }

            onNodeWithText("Unlock").performClick()

            assertTrue(tapped)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `shows the failure message when authFailed is true`() =
        runComposeUiTest {
            setContent {
                LockScreenBody(
                    authFailed = true,
                    biometricUnavailable = false,
                    onUnlockTap = {},
                    onManualConfirm = {},
                )
            }

            onNodeWithText("Authentication failed - try again.").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `shows the manual confirm fallback and invokes onManualConfirm when biometric is unavailable`() =
        runComposeUiTest {
            var confirmed = false
            setContent {
                LockScreenBody(
                    authFailed = false,
                    biometricUnavailable = true,
                    onUnlockTap = {},
                    onManualConfirm = { confirmed = true },
                )
            }

            onNodeWithText("Biometric authentication is not available on this device.").assertIsDisplayed()
            onNodeWithText("Confirm to unlock").performClick()

            assertTrue(confirmed)
        }
}
