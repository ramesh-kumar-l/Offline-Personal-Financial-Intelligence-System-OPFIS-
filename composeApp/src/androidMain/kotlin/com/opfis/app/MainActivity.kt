package com.opfis.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity

/** [FragmentActivity] (not plain `ComponentActivity`) so `androidx.biometric.BiometricPrompt` can host its dialog. */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}
