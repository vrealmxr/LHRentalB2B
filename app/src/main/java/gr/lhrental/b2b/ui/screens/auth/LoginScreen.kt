package gr.lhrental.b2b.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import gr.lhrental.b2b.R
import gr.lhrental.b2b.data.local.BiometricAuthenticator
import gr.lhrental.b2b.data.local.BiometricOutcome
import gr.lhrental.b2b.data.repo.B2bRepository
import gr.lhrental.b2b.ui.theme.LhInk
import gr.lhrental.b2b.ui.util.viewModelFactoryOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    repository: B2bRepository,
    onLoggedIn: () -> Unit,
) {
    val viewModel: LoginViewModel = viewModel(factory = viewModelFactoryOf { LoginViewModel(repository) })
    val activity = LocalContext.current as FragmentActivity
    val biometrics = remember { BiometricAuthenticator(activity) }
    val coroutineScope = rememberCoroutineScope()
    var showBiometricOffer by remember { mutableStateOf(false) }
    var biometricOfferError by remember { mutableStateOf<String?>(null) }

    /** Called right after a successful password login — decides whether to
     *  offer biometric unlock before actually handing off to onLoggedIn(). */
    fun afterLogin() {
        coroutineScope.launch {
            val alreadyEnabled = repository.biometricEnabledFlow.first()
            if (!alreadyEnabled && biometrics.isAvailable()) {
                showBiometricOffer = true
            } else {
                onLoggedIn()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Dark header, matching the site's own navbar — the wordmark is a
        // white PNG meant to sit on ink, not on a light ground.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.38f)
                .background(LhInk),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_lh_logo),
                contentDescription = "LH Rental",
                modifier = Modifier.size(width = 220.dp, height = 66.dp),
            )
        }

        Column(
            modifier = Modifier
                .weight(0.62f)
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = stringResource(R.string.login_title), style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = viewModel.username,
                onValueChange = viewModel::onUsernameChange,
                label = { Text(stringResource(R.string.login_username)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )

            OutlinedTextField(
                value = viewModel.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text(stringResource(R.string.login_password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )

            viewModel.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Button(
                onClick = { viewModel.submit(::afterLogin) },
                enabled = !viewModel.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .padding(top = 20.dp),
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                } else {
                    Text(stringResource(R.string.login_submit))
                }
            }
        }
    }

    if (showBiometricOffer) {
        AlertDialog(
            onDismissRequest = { showBiometricOffer = false; onLoggedIn() },
            title = { Text("Βιομετρικό ξεκλείδωμα") },
            text = {
                Column {
                    Text("Θέλετε να χρησιμοποιείτε δακτυλικό αποτύπωμα, πρόσωπο ή PIN αντί για κωδικό την επόμενη φορά που θα ανοίξετε την εφαρμογή;")
                    biometricOfferError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        when (val outcome = biometrics.authenticate(
                            title = "Ενεργοποίηση βιομετρικού ξεκλειδώματος",
                            subtitle = "Επιβεβαιώστε για να το ενεργοποιήσετε",
                        )) {
                            is BiometricOutcome.Success -> {
                                repository.setBiometricEnabled(true)
                                showBiometricOffer = false
                                onLoggedIn()
                            }
                            is BiometricOutcome.Failed -> {
                                if (!outcome.cancelled) {
                                    biometricOfferError = outcome.message
                                } else {
                                    showBiometricOffer = false
                                    onLoggedIn()
                                }
                            }
                        }
                    }
                }) { Text("Ναι, ενεργοποίηση") }
            },
            dismissButton = {
                TextButton(onClick = { showBiometricOffer = false; onLoggedIn() }) { Text("Όχι τώρα") }
            },
        )
    }
}
