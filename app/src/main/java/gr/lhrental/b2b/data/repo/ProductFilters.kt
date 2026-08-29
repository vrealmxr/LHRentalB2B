package gr.lhrental.b2b.data.repo

enum class SortOption(val apiValue: String, val label: String) {
    NEWEST("newest", "Νεότερα"),
    PRICE_ASC("price_asc", "Τιμή: Χαμηλή προς Υψηλή"),
    PRICE_DESC("price_desc", "Τιμή: Υψηλή προς Χαμηλή"),
}

data class ProductFilters(
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val sort: SortOption = SortOption.NEWEST,
) {
    val isActive: Boolean get() = minPrice != null || maxPrice != null || sort != SortOption.NEWEST
}
