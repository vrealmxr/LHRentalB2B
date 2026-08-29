package gr.lhrental.b2b.ui.screens.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.lhrental.b2b.data.model.Invoice
import gr.lhrental.b2b.data.model.UpdateProfileRequest
import gr.lhrental.b2b.data.model.User
import gr.lhrental.b2b.data.repo.ApiResult
import gr.lhrental.b2b.data.repo.B2bRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

/** Plain text-field state for the edit form — separate from `user` so cancel just drops these. */
class ProfileFormState {
    var companyName by mutableStateOf("")
    var email by mutableStateOf("")
    var vatNumber by mutableStateOf("")
    var address by mutableStateOf("")
    var city by mutableStateOf("")
    var postcode by mutableStateOf("")
    var country by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var profession by mutableStateOf("")

    fun loadFrom(user: User) {
        companyName = user.companyName.orEmpty()
        email = user.email.orEmpty()
        vatNumber = user.vatNumber?.takeIf { it != 0 }?.toString().orEmpty()
        address = user.address.orEmpty()
        city = user.city.orEmpty()
        postcode = user.postcode?.takeIf { it != 0 }?.toString().orEmpty()
        country = user.country.orEmpty()
        phoneNumber = user.phoneNumber.orEmpty()
        profession = user.profession.orEmpty()
    }

    fun toRequest() = UpdateProfileRequest(
        companyName = companyName.trim(),
        email = email.trim(),
        vatNumber = vatNumber.trim().toIntOrNull() ?: 0,
        address = address.trim(),
        city = city.trim(),
        postcode = postcode.trim().toIntOrNull() ?: 0,
        country = country.trim(),
        phoneNumber = phoneNumber.trim(),
        profession = profession.trim(),
    )
}

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

    // ---- profile editing ----
    val form = ProfileFormState()
    var isEditing by mutableStateOf(false)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var saveError by mutableStateOf<String?>(null)
        private set

    // ---- change password ----
    var showChangePassword by mutableStateOf(false)
        private set
    var currentPassword by mutableStateOf("")
        private set
    var newPassword by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set
    var isChangingPassword by mutableStateOf(false)
        private set
    var passwordError by mutableStateOf<String?>(null)
        private set
    var passwordChanged by mutableStateOf(false)
        private set

    // ---- biometric unlock preference ----
    var biometricEnabled by mutableStateOf(false)
        private set

    // Named differently from the `biometricEnabled` property on purpose — Kotlin's
    // synthetic JVM accessor for a Boolean var property is also called
    // setBiometricEnabled(Z), and the two would clash if this kept that name.
    fun updateBiometricPreference(enabled: Boolean) {
        biometricEnabled = enabled // optimistic — this is a local DataStore write, not a network call
        viewModelScope.launch { repository.setBiometricEnabled(enabled) }
    }

    init {
        viewModelScope.launch {
            biometricEnabled = repository.biometricEnabledFlow.first()
        }
        viewModelScope.launch {
            when (val result = repository.me()) {
                is ApiResult.Success -> user = result.value
                is ApiResult.Failure -> errorMessage = result.message
            }
            when (val result = repository.invoices()) {
                is ApiResult.Success -> invoices = result.value
                is ApiResult.Failure -> Unit
            }
            isLoading = false
        }
    }

    fun startEditing() {
        user?.let(form::loadFrom)
        saveError = null
        isEditing = true
    }

    fun cancelEditing() {
        isEditing = false
        saveError = null
    }

    fun saveProfile() {
        isSaving = true
        saveError = null
        viewModelScope.launch {
            when (val result = repository.updateProfile(form.toRequest())) {
                is ApiResult.Success -> {
                    user = result.value
                    isSaving = false
                    isEditing = false
                }
                is ApiResult.Failure -> {
                    isSaving = false
                    saveError = result.message
                }
            }
        }
    }

    fun openChangePassword() {
        currentPassword = ""; newPassword = ""; confirmPassword = ""
        passwordError = null
        passwordChanged = false
        showChangePassword = true
    }

    fun dismissChangePassword() {
        showChangePassword = false
    }

    fun onCurrentPasswordChange(v: String) { currentPassword = v }
    fun onNewPasswordChange(v: String) { newPassword = v }
    fun onConfirmPasswordChange(v: String) { confirmPassword = v }

    fun submitPasswordChange() {
        if (newPassword.length < 6) {
            passwordError = "Ο νέος κωδικός πρέπει να έχει τουλάχιστον 6 χαρακτήρες."
            return
        }
        if (newPassword != confirmPassword) {
            passwordError = "Οι νέοι κωδικοί δεν ταιριάζουν."
            return
        }
        isChangingPassword = true
        passwordError = null
        viewModelScope.launch {
            when (val result = repository.changePassword(currentPassword, newPassword)) {
                is ApiResult.Success -> {
                    isChangingPassword = false
                    passwordChanged = true
                }
                is ApiResult.Failure -> {
                    isChangingPassword = false
                    passwordError = result.message
                }
            }
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
