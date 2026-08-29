package gr.lhrental.b2b.ui.screens.dates

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import gr.lhrental.b2b.data.repo.EventDatesStore
import java.time.LocalDate

class DatesViewModel(private val store: EventDatesStore) : ViewModel() {

    var startDate by mutableStateOf<LocalDate?>(store.dates.value?.start)
        private set
    var endDate by mutableStateOf<LocalDate?>(store.dates.value?.end)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onStartSelected(date: LocalDate) {
        startDate = date
        // Keep the range sane instead of silently accepting a return before pickup.
        if (endDate != null && endDate!!.isBefore(date)) {
            endDate = date
        }
        errorMessage = null
    }

    fun onEndSelected(date: LocalDate) {
        endDate = date
        errorMessage = null
    }

    /** Returns true when the dates were valid and saved. */
    fun confirm(): Boolean {
        val start = startDate
        val end = endDate
        if (start == null || end == null) {
            errorMessage = "Επιλέξτε ημερομηνία παραλαβής και επιστροφής."
            return false
        }
        if (end.isBefore(start)) {
            errorMessage = "Η ημερομηνία επιστροφής δεν μπορεί να είναι πριν την παραλαβή."
            return false
        }
        store.set(start, end)
        return true
    }
}
