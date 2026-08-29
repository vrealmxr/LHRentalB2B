package gr.lhrental.b2b.ui.screens.catalog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.lhrental.b2b.data.model.Category
import gr.lhrental.b2b.data.model.Product
import gr.lhrental.b2b.data.repo.ApiResult
import gr.lhrental.b2b.data.repo.B2bRepository
import kotlinx.coroutines.launch

class CatalogViewModel(private val repository: B2bRepository) : ViewModel() {

    var categories by mutableStateOf<List<Category>>(emptyList())
        private set

    /**
     * Flattened, one level deep: products are tagged with a leaf category id
     * (e.g. "CHAIRS", not the "SEATS" group above it), so the filter row
     * needs to offer leaves — a parent with children is not itself a valid
     * product_categorie value.
     */
    val filterableCategories: List<Category>
        get() = categories.flatMap { if (it.children.isNotEmpty()) it.children else listOf(it) }
    var products by mutableStateOf<List<Product>>(emptyList())
        private set
    var selectedCategoryId by mutableStateOf<Int?>(null)
        private set
    var searchQuery by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var page = 1
    private var totalPages = 1

    init {
        loadCategories()
        loadProducts(reset = true)
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = repository.categories()) {
                is ApiResult.Success -> categories = result.value
                is ApiResult.Failure -> Unit // categories are a nice-to-have filter; don't block the catalogue on it
            }
        }
    }

    fun selectCategory(categoryId: Int?) {
        if (selectedCategoryId == categoryId) return
        selectedCategoryId = categoryId
        loadProducts(reset = true)
    }

    fun onSearchChange(value: String) {
        searchQuery = value
    }

    fun submitSearch() {
        loadProducts(reset = true)
    }

    fun loadMoreIfNeeded(lastVisibleIndex: Int) {
        if (!isLoading && lastVisibleIndex >= products.size - 6 && page < totalPages) {
            loadProducts(reset = false)
        }
    }

    fun retry() = loadProducts(reset = true)

    private fun loadProducts(reset: Boolean) {
        if (reset) {
            page = 1
            products = emptyList()
        }
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val query = searchQuery.trim().ifBlank { null }
            when (val result = repository.products(selectedCategoryId, query, page)) {
                is ApiResult.Success -> {
                    val (items, pagination) = result.value
                    products = if (reset) items else products + items
                    totalPages = pagination.totalPages
                    page = pagination.page + 1
                    isLoading = false
                }
                is ApiResult.Failure -> {
                    isLoading = false
                    errorMessage = result.message
                }
            }
        }
    }
}
