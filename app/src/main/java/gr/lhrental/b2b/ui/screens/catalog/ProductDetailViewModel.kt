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
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val repository: B2bRepository,
    private val cartStore: CartStore,
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

    init {
        viewModelScope.launch {
            when (val result = repository.product(productId)) {
                is ApiResult.Success -> {
                    product = result.value
                    isLoading = false
                }
                is ApiResult.Failure -> {
                    errorMessage = result.message
                    isLoading = false
                }
            }
        }
    }

    fun increment() { quantity++ }
    fun decrement() { if (quantity > 1) quantity-- }

    fun addToCart() {
        product?.let {
            cartStore.add(it, quantity)
            addedToCart = true
        }
    }
}
