package gr.lhrental.b2b.data.repo

import gr.lhrental.b2b.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class CartLine(val product: Product, val quantity: Int)

/**
 * In-memory cart for the current app session (cleared on process death).
 *
 * The catalogue here is a quote/booking request, not a live checkout with
 * payment — losing the cart on a rare process kill is an acceptable v1
 * trade-off. Worth moving to DataStore/Room if that turns out wrong.
 */
class CartStore {
    private val _lines = MutableStateFlow<List<CartLine>>(emptyList())
    val lines: StateFlow<List<CartLine>> = _lines

    val total: Double get() = _lines.value.sumOf { it.product.effectivePrice * it.quantity }
    val itemCount: Int get() = _lines.value.sumOf { it.quantity }

    fun add(product: Product, quantity: Int = 1) {
        _lines.update { current ->
            val existing = current.find { it.product.id == product.id }
            if (existing != null) {
                current.map {
                    if (it.product.id == product.id) it.copy(quantity = it.quantity + quantity) else it
                }
            } else {
                current + CartLine(product, quantity)
            }
        }
    }

    fun setQuantity(productId: Int, quantity: Int) {
        _lines.update { current ->
            if (quantity <= 0) {
                current.filterNot { it.product.id == productId }
            } else {
                current.map { if (it.product.id == productId) it.copy(quantity = quantity) else it }
            }
        }
    }

    fun remove(productId: Int) {
        _lines.update { current -> current.filterNot { it.product.id == productId } }
    }

    fun clear() {
        _lines.value = emptyList()
    }
}
