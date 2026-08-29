package gr.lhrental.b2b.ui.screens.cart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.lhrental.b2b.data.model.CreateOrderItem
import gr.lhrental.b2b.data.model.CreateOrderRequest
import gr.lhrental.b2b.data.repo.ApiResult
import gr.lhrental.b2b.data.repo.B2bRepository
import gr.lhrental.b2b.data.repo.CartStore
import gr.lhrental.b2b.data.repo.EventDatesStore
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val repository: B2bRepository,
    private val cartStore: CartStore,
    private val eventDatesStore: EventDatesStore,
) : ViewModel() {

    val dates get() = eventDatesStore.dates.value

    var title by mutableStateOf("")
        private set
    var eventType by mutableStateOf("")
        private set
    var location by mutableStateOf("")
        private set
    var address by mutableStateOf("")
        private set
    var town by mutableStateOf("")
        private set
    var postcode by mutableStateOf("")
        private set
    var comments by mutableStateOf("")
        private set

    var isSubmitting by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var submittedOrderId by mutableStateOf<Int?>(null)
        private set

    fun onTitleChange(v: String) { title = v }
    fun onEventTypeChange(v: String) { eventType = v }
    fun onLocationChange(v: String) { location = v }
    fun onAddressChange(v: String) { address = v }
    fun onTownChange(v: String) { town = v }
    fun onPostcodeChange(v: String) { postcode = v }
    fun onCommentsChange(v: String) { comments = v }

    fun submit() {
        val lines = cartStore.lines.value
        val eventDates = dates
        if (title.isBlank() || eventDates == null || lines.isEmpty()) {
            errorMessage = "Συμπληρώστε τίτλο εκδήλωσης και προσθέστε τουλάχιστον ένα προϊόν."
            return
        }

        isSubmitting = true
        errorMessage = null

        // Re-check availability right before submitting — the cart could be
        // stale (someone else booked the same item since it was added).
        // orders/create.php validates this too and would reject the whole
        // request, but catching it here lets us say *which* item and send
        // the customer back to the cart instead of a flat failure.
        viewModelScope.launch {
            val productIds = lines.map { it.product.id }
            when (val availabilityResult = repository.checkAvailability(productIds, eventDates)) {
                is ApiResult.Success -> {
                    val shortages = availabilityResult.value.filter { avail ->
                        val requested = lines.find { it.product.id == avail.productId }?.quantity ?: 0
                        !avail.exists || avail.availableQuantity < requested
                    }
                    if (shortages.isNotEmpty()) {
                        isSubmitting = false
                        val names = shortages.mapNotNull { s -> lines.find { it.product.id == s.productId }?.product?.name }
                        errorMessage = "Δεν είναι πια διαθέσιμα σε αυτή την ποσότητα: ${names.joinToString(", ")}. Επιστρέψτε στο καλάθι για να προσαρμόσετε."
                        return@launch
                    }
                    submitOrder(lines, eventDates)
                }
                is ApiResult.Failure -> {
                    // Availability check itself failed (network, etc.) — don't block
                    // the submit on that; orders/create.php re-validates server-side anyway.
                    submitOrder(lines, eventDates)
                }
            }
        }
    }

    private fun submitOrder(lines: List<gr.lhrental.b2b.data.repo.CartLine>, eventDates: gr.lhrental.b2b.data.repo.EventDates) {
        val request = CreateOrderRequest(
            title = title.trim(),
            eventType = eventType.trim(),
            dateStart = eventDates.startIso,
            dateReturn = eventDates.endIso,
            location = location.trim(),
            address = address.trim(),
            postcode = postcode.trim(),
            town = town.trim(),
            comments = comments.trim(),
            items = lines.map { CreateOrderItem(it.product.id, it.quantity) },
        )

        viewModelScope.launch {
            when (val result = repository.createOrder(request)) {
                is ApiResult.Success -> {
                    isSubmitting = false
                    submittedOrderId = result.value.id
                    cartStore.clear()
                }
                is ApiResult.Failure -> {
                    isSubmitting = false
                    errorMessage = result.message
                }
            }
        }
    }
}
