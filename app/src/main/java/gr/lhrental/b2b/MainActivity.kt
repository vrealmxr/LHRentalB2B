package gr.lhrental.b2b

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import gr.lhrental.b2b.data.local.BiometricAuthenticator
import gr.lhrental.b2b.data.local.BiometricOutcome
import gr.lhrental.b2b.ui.nav.LhNavGraph
import gr.lhrental.b2b.ui.theme.LhRentalB2bTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * FragmentActivity, not the usual ComponentActivity, because
 * BiometricPrompt requires one — it's the only reason for the change and
 * it's a strict superset (FragmentActivity extends ComponentActivity), so
 * setContent/enableEdgeToEdge keep working exactly as before.
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as LhB2bApplication
        val biometrics = BiometricAuthenticator(this)

        setContent {
            var sessionChecked by mutableStateOf(false)
            var startLoggedIn by mutableStateOf(false)
            // Only an existing (returning) session is gated — a fresh
            // username/password login on this launch doesn't need it twice.
            var biometricRequired by mutableStateOf(false)
            var biometricPassed by mutableStateOf(false)
            var biometricError by mutableStateOf<String?>(null)

            suspend fun runBiometricGate() {
                biometricError = null
                when (val outcome = biometrics.authenticate(
                    title = "Ξεκλείδωμα LH Rental B2B",
                    subtitle = "Επιβεβαιώστε ότι είστε εσείς για να συνεχίσετε",
                )) {
                    is BiometricOutcome.Success -> biometricPassed = true
                    is BiometricOutcome.Failed -> {
                        if (!outcome.cancelled) biometricError = outcome.message
                    }
                }
            }

            lifecycleScope.launch {
                startLoggedIn = app.tokenStore.tokenFlow.first() != null
                val biometricEnabled = app.tokenStore.biometricEnabledFlow.first()
                biometricRequired = startLoggedIn && biometricEnabled && biometrics.isAvailable()
                sessionChecked = true
                if (biometricRequired) runBiometricGate()
            }

            LhRentalB2bTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when {
                        !sessionChecked -> LoadingScreen()
                        biometricRequired && !biometricPassed -> BiometricGateScreen(
                            errorMessage = biometricError,
                            onRetry = { lifecycleScope.launch { runBiometricGate() } },
                            onLogout = {
                                lifecycleScope.launch {
                                    app.repository.logout()
                                    app.cartStore.clear()
                                    app.eventDatesStore.clear()
                                    startLoggedIn = false
                                    biometricRequired = false
                                }
                            },
                        )
                        else -> LhNavGraph(
                            repository = app.repository,
                            cartStore = app.cartStore,
                            eventDatesStore = app.eventDatesStore,
                            startLoggedIn = startLoggedIn,
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@androidx.compose.runtime.Composable
private fun BiometricGateScreen(errorMessage: String?, onRetry: () -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.padding(bottom = 16.dp))
        Text("Η εφαρμογή είναι κλειδωμένη", style = MaterialTheme.typography.titleLarge)
        Text(
            "Χρησιμοποιήστε το δακτυλικό σας αποτύπωμα, το πρόσωπό σας ή το PIN της συσκευής για να συνεχίσετε.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
        }
        Button(onClick = onRetry) { Text("Ξεκλείδωμα") }
        TextButton(onClick = onLogout, modifier = Modifier.padding(top = 8.dp)) { Text("Αποσύνδεση") }
    }
}
