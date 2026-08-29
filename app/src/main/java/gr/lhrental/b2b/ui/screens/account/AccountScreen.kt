package gr.lhrental.b2b.ui.screens.account

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import gr.lhrental.b2b.R
import gr.lhrental.b2b.data.model.Invoice
import gr.lhrental.b2b.data.repo.B2bRepository
import gr.lhrental.b2b.ui.util.viewModelFactoryOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    repository: B2bRepository,
    onLoggedOut: () -> Unit,
) {
    val viewModel: AccountViewModel = viewModel(factory = viewModelFactoryOf { AccountViewModel(repository) })
    val context = LocalContext.current

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

        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(20.dp)) {
            viewModel.user?.let { user ->
                Text(user.companyName ?: user.username, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(user.email.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                user.phoneNumber?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                user.discountPercent?.let {
                    Text("Έκπτωση: $it%", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))
            Text(stringResource(R.string.account_invoices), style = MaterialTheme.typography.titleMedium)

            if (viewModel.invoices.isEmpty()) {
                Text(
                    "Δεν υπάρχουν τιμολόγια.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(viewModel.invoices, key = { it.id }) { invoice ->
                        InvoiceRow(
                            invoice = invoice,
                            isDownloading = viewModel.downloadingInvoiceId == invoice.id,
                            onDownload = { viewModel.downloadInvoice(invoice) },
                        )
                    }
                }
            }

            viewModel.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            TextButton(onClick = viewModel::logout, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Text(stringResource(R.string.account_logout), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun InvoiceRow(invoice: Invoice, isDownloading: Boolean, onDownload: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(invoice.fileName)
            Text(invoice.dateUploaded, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (isDownloading) {
            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
        } else {
            OutlinedButton(onClick = onDownload) { Text("Λήψη") }
        }
    }
}
