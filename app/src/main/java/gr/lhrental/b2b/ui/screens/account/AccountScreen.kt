package gr.lhrental.b2b.ui.screens.account

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import gr.lhrental.b2b.R
import gr.lhrental.b2b.data.local.BiometricAuthenticator
import gr.lhrental.b2b.data.local.BiometricOutcome
import gr.lhrental.b2b.data.model.Invoice
import gr.lhrental.b2b.data.model.User
import gr.lhrental.b2b.data.repo.B2bRepository
import gr.lhrental.b2b.ui.theme.lhTextFieldColors
import gr.lhrental.b2b.ui.util.viewModelFactoryOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    repository: B2bRepository,
    onLoggedOut: () -> Unit,
) {
    val viewModel: AccountViewModel = viewModel(factory = viewModelFactoryOf { AccountViewModel(repository) })
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val biometrics = remember { BiometricAuthenticator(activity) }
    val coroutineScope = rememberCoroutineScope()
    var biometricToggleError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel.loggedOut) {
        if (viewModel.loggedOut) onLoggedOut()
    }

    LaunchedEffect(viewModel.downloadedFile) {
        viewModel.downloadedFile?.let { file ->
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            viewModel.consumeDownloadedFile()
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.account_title)) }) }) { padding ->
        if (viewModel.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(padding).fillMaxSize())
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
        ) {
            item {
                val user = viewModel.user
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = user?.companyName?.takeIf { it.isNotBlank() } ?: user?.username.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        // Without this, a long company name (unweighted, measured before its
                        // sibling) can leave the button almost no width, forcing its label to
                        // wrap one letter per line — see the screenshot that reported this.
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                    )
                    if (!viewModel.isEditing) {
                        TextButton(onClick = viewModel::startEditing) { Text("Επεξεργασία") }
                    }
                }
            }

            item {
                if (viewModel.isEditing) {
                    EditProfileForm(viewModel)
                } else if (viewModel.user != null) {
                    ProfileDetails(viewModel.user!!)
                }
            }

            item {
                TextButton(onClick = viewModel::openChangePassword, modifier = Modifier.padding(top = 4.dp)) {
                    Text("Αλλαγή κωδικού πρόσβασης")
                }

                HorizontalDivider(modifier = Modifier.padding(top = 12.dp, bottom = 16.dp))
                Text("Ασφάλεια", style = MaterialTheme.typography.titleMedium)

                val unavailableReason = biometrics.availabilityReason()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text("Βιομετρικό ξεκλείδωμα", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Δακτυλικό, πρόσωπο ή PIN αντί για κωδικό κάθε φορά που ανοίγετε την εφαρμογή.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Switch(
                        checked = viewModel.biometricEnabled,
                        enabled = unavailableReason == null,
                        onCheckedChange = { turnOn ->
                            if (!turnOn) {
                                viewModel.updateBiometricPreference(false)
                            } else {
                                // Confirm it actually works on this device before persisting —
                                // otherwise "enabled" could lock the customer out on next launch.
                                coroutineScope.launch {
                                    when (val outcome = biometrics.authenticate(
                                        title = "Ενεργοποίηση βιομετρικού ξεκλειδώματος",
                                        subtitle = "Επιβεβαιώστε για να το ενεργοποιήσετε",
                                    )) {
                                        is BiometricOutcome.Success -> {
                                            biometricToggleError = null
                                            viewModel.updateBiometricPreference(true)
                                        }
                                        is BiometricOutcome.Failed -> {
                                            if (!outcome.cancelled) biometricToggleError = outcome.message
                                        }
                                    }
                                }
                            }
                        },
                    )
                }
                (unavailableReason ?: biometricToggleError)?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (unavailableReason != null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(top = 20.dp, bottom = 20.dp))
                Text(stringResource(R.string.account_invoices), style = MaterialTheme.typography.titleMedium)
            }

            if (viewModel.invoices.isEmpty()) {
                item {
                    Text(
                        "Δεν υπάρχουν τιμολόγια.",
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            } else {
                items(viewModel.invoices, key = { it.id }) { invoice ->
                    InvoiceRow(
                        invoice = invoice,
                        isDownloading = viewModel.downloadingInvoiceId == invoice.id,
                        onDownload = { viewModel.downloadInvoice(invoice) },
                    )
                }
            }

            item {
                viewModel.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }
                TextButton(onClick = viewModel::logout, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                    Text(stringResource(R.string.account_logout), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (viewModel.showChangePassword) {
        ChangePasswordDialog(viewModel)
    }
}

@Composable
private fun ProfileDetails(user: User) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        DetailRow("Όνομα χρήστη", user.username)
        DetailRow("Email", user.email)
        DetailRow("Τηλέφωνο", user.phoneNumber)
        DetailRow("Επάγγελμα / Δραστηριότητα", user.profession)
        DetailRow("Α.Φ.Μ.", user.vatNumber?.takeIf { it != 0 }?.toString())
        DetailRow("Διεύθυνση", user.address)
        DetailRow("Πόλη", user.city)
        DetailRow("Τ.Κ.", user.postcode?.takeIf { it != 0 }?.toString())
        DetailRow("Χώρα", user.country)
        user.discountPercent?.let { DetailRow("Έκπτωση", "$it%") }
    }
}

@Composable
private fun DetailRow(label: String, value: String?) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(end = 12.dp),
        )
        Text(
            value?.takeIf { it.isNotBlank() } ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun EditProfileForm(viewModel: AccountViewModel) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        FormField("Επωνυμία εταιρείας", viewModel.form.companyName) { viewModel.form.companyName = it }
        FormField("Email", viewModel.form.email, keyboardType = KeyboardType.Email) { viewModel.form.email = it }
        FormField("Τηλέφωνο", viewModel.form.phoneNumber, keyboardType = KeyboardType.Phone) { viewModel.form.phoneNumber = it }
        FormField("Επάγγελμα / Δραστηριότητα", viewModel.form.profession) { viewModel.form.profession = it }
        FormField("Α.Φ.Μ.", viewModel.form.vatNumber, keyboardType = KeyboardType.Number) { viewModel.form.vatNumber = it }
        FormField("Διεύθυνση", viewModel.form.address) { viewModel.form.address = it }
        FormField("Πόλη", viewModel.form.city) { viewModel.form.city = it }
        FormField("Τ.Κ.", viewModel.form.postcode, keyboardType = KeyboardType.Number) { viewModel.form.postcode = it }
        FormField("Χώρα", viewModel.form.country) { viewModel.form.country = it }

        viewModel.saveError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = viewModel::cancelEditing, modifier = Modifier.weight(1f), enabled = !viewModel.isSaving) {
                Text("Άκυρο")
            }
            Button(onClick = viewModel::saveProfile, modifier = Modifier.weight(1f), enabled = !viewModel.isSaving) {
                if (viewModel.isSaving) CircularProgressIndicator(modifier = Modifier.padding(2.dp)) else Text("Αποθήκευση")
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        colors = lhTextFieldColors(),
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
    )
}

@Composable
private fun ChangePasswordDialog(viewModel: AccountViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::dismissChangePassword,
        title = { Text("Αλλαγή κωδικού πρόσβασης") },
        text = {
            if (viewModel.passwordChanged) {
                Text("Ο κωδικός σας άλλαξε με επιτυχία.")
            } else {
                Column {
                    OutlinedTextField(
                        value = viewModel.currentPassword,
                        onValueChange = viewModel::onCurrentPasswordChange,
                        label = { Text("Τρέχων κωδικός") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = lhTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = viewModel.newPassword,
                        onValueChange = viewModel::onNewPasswordChange,
                        label = { Text("Νέος κωδικός") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = lhTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                    OutlinedTextField(
                        value = viewModel.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        label = { Text("Επιβεβαίωση νέου κωδικού") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = lhTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                    viewModel.passwordError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            if (viewModel.passwordChanged) {
                TextButton(onClick = viewModel::dismissChangePassword) { Text("Κλείσιμο") }
            } else {
                TextButton(onClick = viewModel::submitPasswordChange, enabled = !viewModel.isChangingPassword) {
                    Text("Αλλαγή")
                }
            }
        },
        dismissButton = {
            if (!viewModel.passwordChanged) {
                TextButton(onClick = viewModel::dismissChangePassword) { Text("Άκυρο") }
            }
        },
    )
}

@Composable
private fun InvoiceRow(invoice: Invoice, isDownloading: Boolean, onDownload: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(invoice.fileName, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text(invoice.dateUploaded, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        if (isDownloading) {
            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
        } else {
            OutlinedButton(onClick = onDownload) { Text("Λήψη") }
        }
    }
}
