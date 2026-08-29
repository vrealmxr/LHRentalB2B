package gr.lhrental.b2b.ui.nav

sealed class Destination(val route: String) {
    data object Login : Destination("login")
    data object Dates : Destination("dates")
    data object Catalog : Destination("catalog")
    data object ProductDetail : Destination("product/{productId}") {
        fun of(productId: Int) = "product/$productId"
    }
    data object Cart : Destination("cart")
    data object Checkout : Destination("checkout")
    data object Orders : Destination("orders")
    data object OrderDetail : Destination("order/{orderId}") {
        fun of(orderId: Int) = "order/$orderId"
    }
    data object Account : Destination("account")
}
