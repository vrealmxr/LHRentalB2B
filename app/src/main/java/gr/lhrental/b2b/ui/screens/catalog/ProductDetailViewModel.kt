package gr.lhrental.b2b.ui.screens.catalog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.lhrental.b2b.data.model.Product
import gr.lhrental.b2b.data.repo.ApiResult
import gr.lhrental.b2b.data.repo.B2bRepository
import gr.lhrental.b2b.data.repo.CartStore
import gr.lhrental.b2b.data.repo.EventDatesStore
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val repository: B2bRepository,
    private val cartStore: CartStore,
    private val eventDatesStore: EventDatesStore,
    private val productId: Int,
) : ViewModel() {

    var product by mutableStateOf<Product?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var quantity by mutableStateOf(1)
        private set
    var addedToCart by mutableStateOf(false)
        private set

    /** What's already in the cart counts against availability too. */
    private val alreadyInCart: Int
        get() = cartStore.lines.value.find { it.product.id == productId }?.quantity ?: 0

    /** Null = availability unknown (shouldn't happen — dates are mandatory before this screen is reachable). */
    val remainingAvailable: Int?
        get() = product?.availableQuantity?.let { (it - alreadyInCart).coerceAtLeast(0) }

    val isSoldOut: Boolean
        get() = remainingAvailable == 0

    init {
        viewModelScope.launch {
            when (val result = repository.product(productId, eventDatesStore.dates.value)) {
                is ApiResult.Success -> {
                    product = result.value
                    quantity = if ((result.value.availableQuantity ?: 1) <= 0) 0 else 1
                    isLoading = false
                }
                is ApiResult.Failure -> {
                    errorMessage = result.message
                    isLoading = false
                }
            }
        }
    }

    fun increment() {
        val cap = remainingAvailable ?: Int.MAX_VALUE
        if (quantity < cap) quantity++
    }

    fun decrement() {
        if (quantity > 1) quantity--
    }

    fun addToCart() {
        val cap = remainingAvailable ?: return
        if (cap <= 0) return
        product?.let {
            cartStore.add(it, quantity.coerceAtMost(cap))
            addedToCart = true
        }
    }
}
