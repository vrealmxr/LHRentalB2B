package gr.lhrental.b2b.ui.screens.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.lhrental.b2b.data.model.Invoice
import gr.lhrental.b2b.data.model.User
import gr.lhrental.b2b.data.repo.ApiResult
import gr.lhrental.b2b.data.repo.B2bRepository
import kotlinx.coroutines.launch
import java.io.File

class AccountViewModel(private val repository: B2bRepository) : ViewModel() {
    var user by mutableStateOf<User?>(null)
        private set
    var invoices by mutableStateOf<List<Invoice>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set
    var loggedOut by mutableStateOf(false)
        private set
    var downloadingInvoiceId by mutableStateOf<Int?>(null)
        private set
    var downloadedFile by mutableStateOf<File?>(null)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            when (val result = repository.me()) {
                is ApiResult.Success -> user = result.value
                is ApiResult.Failure -> Unit
            }
            when (val result = repository.invoices()) {
                is ApiResult.Success -> invoices = result.value
                is ApiResult.Failure -> Unit
            }
            isLoading = false
        }
    }

    fun downloadInvoice(invoice: Invoice) {
        downloadingInvoiceId = invoice.id
        errorMessage = null
        viewModelScope.launch {
            when (val result = repository.downloadInvoice(invoice)) {
                is ApiResult.Success -> downloadedFile = result.value
                is ApiResult.Failure -> errorMessage = result.message
            }
            downloadingInvoiceId = null
        }
    }

    fun consumeDownloadedFile() {
        downloadedFile = null
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            loggedOut = true
        }
    }
}
