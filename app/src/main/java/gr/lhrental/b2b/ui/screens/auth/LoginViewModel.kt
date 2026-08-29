package gr.lhrental.b2b.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.lhrental.b2b.data.repo.ApiResult
import gr.lhrental.b2b.data.repo.B2bRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: B2bRepository) : ViewModel() {

    var username by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onUsernameChange(value: String) { username = value }
    fun onPasswordChange(value: String) { password = value }

    fun submit(onSuccess: () -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "Συμπληρώστε όνομα χρήστη και κωδικό."
            return
        }
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            when (val result = repository.login(username.trim(), password)) {
                is ApiResult.Success -> {
                    isLoading = false
                    onSuccess()
                }
                is ApiResult.Failure -> {
                    isLoading = false
                    errorMessage = result.message
                }
            }
        }
    }
}
