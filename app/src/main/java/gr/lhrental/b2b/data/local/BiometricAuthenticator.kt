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

    fun isAvailable(): Boolean = availabilityReason() == null

    /**
     * Null when available; otherwise a user-facing reason it isn't, so the
     * account-screen toggle can explain itself instead of just doing
     * nothing (which is exactly what was reported: "it's like it doesn't
     * exist" — canAuthenticate() was quietly failing with no visible signal
     * anywhere in the app).
     */
    fun availabilityReason(): String? {
        return when (BiometricManager.from(activity).canAuthenticate(ALLOWED_AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> null
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                "Η συσκευή δεν έχει αισθητήρα δακτυλικού/προσώπου."
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                "Ο αισθητήρας δεν είναι διαθέσιμος αυτή τη στιγμή."
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                "Δεν έχετε ρυθμίσει κλείδωμα οθόνης (δακτυλικό, πρόσωπο ή PIN) στη συσκευή σας. Ρυθμίστε το πρώτα από τις Ρυθμίσεις της συσκευής."
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                "Χρειάζεται ενημέρωση ασφαλείας στη συσκευή για να λειτουργήσει."
            else -> "Μη διαθέσιμο σε αυτή τη συσκευή."
        }
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
