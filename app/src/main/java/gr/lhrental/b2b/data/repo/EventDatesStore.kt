package gr.lhrental.b2b.data.repo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

data class EventDates(val start: LocalDate, val end: LocalDate) {
    val startIso: String get() = start.toString() // LocalDate.toString() is ISO-8601 (yyyy-MM-dd)
    val endIso: String get() = end.toString()
}

/**
 * The event window the whole session is shopping for — set once up front
 * (see ui/screens/dates/DatesScreen.kt) so every product the customer looks
 * at can show real availability instead of a raw stock count, and so
 * checkout never re-asks for dates the app already has.
 *
 * Session-scoped, same trade-off as CartStore: cleared on process death.
 */
class EventDatesStore {
    private val _dates = MutableStateFlow<EventDates?>(null)
    val dates: StateFlow<EventDates?> = _dates

    fun set(start: LocalDate, end: LocalDate) {
        _dates.value = EventDates(start, end)
    }

    fun clear() {
        _dates.value = null
    }
}
