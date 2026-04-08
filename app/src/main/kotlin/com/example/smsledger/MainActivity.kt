package com.example.smsledger

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smsledger.feature.ledger.LedgerViewModel
import com.example.smsledger.feature.ledger.LedgerScreen
import com.example.smsledger.feature.ledger.LedgerIntent
import com.example.smsledger.ui.theme.SmsLedgerTheme
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    
    private val viewModel: LedgerViewModel by viewModels {
        val app = application as SmsLedgerApp
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LedgerViewModel(
                    app.getTransactionsUseCase,
                    app.addTransactionUseCase,
                    app.updateTransactionUseCase,
                    app.deleteTransactionUseCase,
                    app.getParsingRulesUseCase,
                    app.addParsingRuleUseCase,
                    app.updateParsingRuleUseCase,
                    app.deleteParsingRuleUseCase,
                    app.getCategoriesUseCase,
                    app.addCategoryUseCase,
                    app.updateCategoryUseCase,
                    app.deleteCategoryUseCase,
                    app.getGeminiApiKeyUseCase,
                    app.saveGeminiApiKeyUseCase,
                    app.getUseSmartAiUseCase,
                    app.setUseSmartAiUseCase,
                    app.getRecurringTransactionsUseCase,
                    app.addRecurringTransactionUseCase,
                    app.updateRecurringTransactionUseCase,
                    app.deleteRecurringTransactionUseCase
                ) as T
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            // Permission granted
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        MobileAds.initialize(this) {}
        checkPermissions()

        setContent {
            SmsLedgerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LedgerScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
}
