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
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val repository: B2bRepository,
    private val cartStore: CartStore,
) : ViewModel() {

    var title by mutableStateOf("")
        private set
    var eventType by mutableStateOf("")
        private set
    var dateStart by mutableStateOf("")
        private set
    var dateReturn by mutableStateOf("")
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
    fun onDateStartChange(v: String) { dateStart = v }
    fun onDateReturnChange(v: String) { dateReturn = v }
    fun onLocationChange(v: String) { location = v }
    fun onAddressChange(v: String) { address = v }
    fun onTownChange(v: String) { town = v }
    fun onPostcodeChange(v: String) { postcode = v }
    fun onCommentsChange(v: String) { comments = v }

    fun submit() {
        val lines = cartStore.lines.value
        if (title.isBlank() || dateStart.isBlank() || dateReturn.isBlank() || lines.isEmpty()) {
            errorMessage = "Συμπληρώστε τίτλο, ημερομηνίες και προσθέστε τουλάχιστον ένα προϊόν."
            return
        }

        isSubmitting = true
        errorMessage = null

        val request = CreateOrderRequest(
            title = title.trim(),
            eventType = eventType.trim(),
            dateStart = dateStart.trim(),
            dateReturn = dateReturn.trim(),
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
