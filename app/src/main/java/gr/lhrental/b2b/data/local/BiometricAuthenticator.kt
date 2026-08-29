package gr.lhrental.b2b.data.local

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

sealed interface BiometricOutcome {
    data object Success : BiometricOutcome
    /** cancelled = the user backed out (not an error worth showing). */
    data class Failed(val message: String, val cancelled: Boolean) : BiometricOutcome
}

private const val ALLOWED_AUTHENTICATORS = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

/**
 * Gates access to the already-stored session token behind the device's own
 * fingerprint/face/PIN — this isn't re-authenticating with the server (we
 * never store the password), it's a local "prove it's still you" check
 * before handing over what's already unlocked in DataStore.
 */
class BiometricAuthenticator(private val activity: FragmentActivity) {

    fun isAvailable(): Boolean {
        return BiometricManager.from(activity).canAuthenticate(ALLOWED_AUTHENTICATORS) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    suspend fun authenticate(title: String, subtitle: String): BiometricOutcome = suspendCancellableCoroutine { cont ->
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                if (cont.isActive) cont.resume(BiometricOutcome.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (!cont.isActive) return
                val cancelled = errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_CANCELED
                cont.resume(BiometricOutcome.Failed(errString.toString(), cancelled))
            }

            // onAuthenticationFailed = one bad attempt (wrong finger); the prompt
            // stays open on its own, nothing to resolve yet.
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .build()

        prompt.authenticate(promptInfo)
    }
}
