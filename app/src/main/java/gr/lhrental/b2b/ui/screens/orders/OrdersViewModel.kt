package gr.lhrental.b2b.ui.screens.orders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.lhrental.b2b.data.model.OrderDetail
import gr.lhrental.b2b.data.model.OrderSummary
import gr.lhrental.b2b.data.repo.ApiResult
import gr.lhrental.b2b.data.repo.B2bRepository
import kotlinx.coroutines.launch

class OrdersViewModel(private val repository: B2bRepository) : ViewModel() {
    var orders by mutableStateOf<List<OrderSummary>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init { load() }

    fun retry() = load()

    private fun load() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            when (val result = repository.orders()) {
                is ApiResult.Success -> { orders = result.value; isLoading = false }
                is ApiResult.Failure -> { errorMessage = result.message; isLoading = false }
            }
        }
    }
}

class OrderDetailViewModel(private val repository: B2bRepository, private val orderId: Int) : ViewModel() {
    var order by mutableStateOf<OrderDetail?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            when (val result = repository.order(orderId)) {
                is ApiResult.Success -> { order = result.value; isLoading = false }
                is ApiResult.Failure -> { errorMessage = result.message; isLoading = false }
            }
        }
    }
}

/**
 * Human labels for orders.order_stage — confirmed against the admin panel's own
 * badges (admin-legacy/orders.php, admin-legacy/order.php): only 0/1/2 are used.
 */
fun orderStageLabel(stage: Int): String = when (stage) {
    0 -> "Μη επιβεβαιωμένη"
    1 -> "Επιβεβαιωμένη"
    2 -> "Παραδόθηκε"
    else -> "Άγνωστη κατάσταση ($stage)"
}
