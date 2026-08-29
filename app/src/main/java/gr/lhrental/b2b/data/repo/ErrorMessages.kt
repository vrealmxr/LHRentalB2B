package gr.lhrental.b2b.data.repo

/**
 * Turns a backend error code into something a user can actually act on,
 * instead of a raw HTTP status or the API's (English, developer-facing)
 * message text. See docs/API.md for the full code list.
 */
fun friendlyErrorMessage(code: String?, serverMessage: String?, httpCode: Int): String {
    return when (code) {
        "user_not_found" -> "Δεν βρέθηκε λογαριασμός με αυτό το όνομα χρήστη. Ελέγξτε ότι το γράψατε σωστά."
        "wrong_password" -> "Λάθος κωδικός πρόσβασης."
        "missing_fields" -> "Συμπληρώστε όλα τα απαιτούμενα πεδία."
        "invalid_date" -> "Ελέγξτε τις ημερομηνίες — πρέπει να είναι στη μορφή ΕΕΕΕ-ΜΜ-ΗΗ."
        "invalid_item" -> serverMessage ?: "Ένα από τα προϊόντα του καλαθιού δεν είναι πλέον διαθέσιμο."
        "insufficient_availability" -> "Κάποια προϊόντα του καλαθιού δεν είναι πια διαθέσιμα σε αυτή την ποσότητα για τις ημερομηνίες σας — κάποιος άλλος πρόλαβε. Επιστρέψτε στο καλάθι και προσαρμόστε τις ποσότητες."
        "not_found" -> "Δεν βρέθηκε."
        "unauthenticated" -> "Η σύνδεσή σας έληξε. Παρακαλώ συνδεθείτε ξανά."
        "method_not_allowed", "db_error" -> "Προσωρινό πρόβλημα διακομιστή. Δοκιμάστε ξανά σε λίγο."
        else -> serverMessage?.takeIf { it.isNotBlank() } ?: "Κάτι πήγε στραβά (κωδικός $httpCode)."
    }
}
