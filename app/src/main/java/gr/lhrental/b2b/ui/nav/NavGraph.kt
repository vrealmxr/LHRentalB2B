package gr.lhrental.b2b.ui.nav

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import gr.lhrental.b2b.R
import gr.lhrental.b2b.data.repo.B2bRepository
import gr.lhrental.b2b.data.repo.CartStore
import gr.lhrental.b2b.data.repo.EventDatesStore
import gr.lhrental.b2b.ui.screens.account.AccountScreen
import gr.lhrental.b2b.ui.screens.auth.LoginScreen
import gr.lhrental.b2b.ui.screens.dates.DatesScreen

private data class BottomTab(val destination: Destination, val label: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomTabs = listOf(
    BottomTab(Destination.Catalog, R.string.nav_catalog, Icons.Default.Home),
    BottomTab(Destination.Cart, R.string.nav_cart, Icons.Default.ShoppingCart),
    BottomTab(Destination.Orders, R.string.nav_orders, Icons.Default.Receipt),
    BottomTab(Destination.Account, R.string.nav_account, Icons.Default.Person),
)

@Composable
fun LhNavGraph(
    repository: B2bRepository,
    cartStore: CartStore,
    eventDatesStore: EventDatesStore,
    startLoggedIn: Boolean,
) {
    val navController = rememberNavController()
    val startDestination = when {
        !startLoggedIn -> Destination.Login.route
        eventDatesStore.dates.collectAsState().value == null -> Destination.Dates.route
        else -> Destination.Catalog.route
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination
    val showBottomBar = bottomTabs.any { currentRoute?.hierarchy?.any { d -> d.route == it.destination.route } == true }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val cartCount by cartStore.lines.collectAsState()
                    bottomTabs.forEach { tab ->
                        val selected = currentRoute?.hierarchy?.any { it.route == tab.destination.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (tab.destination == Destination.Cart && cartCount.isNotEmpty()) {
                                    BadgedBox(badge = { Badge { Text(cartCount.sumOf { it.quantity }.toString()) } }) {
                                        Icon(tab.icon, contentDescription = null)
                                    }
                                } else {
                                    Icon(tab.icon, contentDescription = null)
                                }
                            },
                            label = {
                                Text(
                                    stringResource(tab.label),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                )
                            },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                enterTransition = { slideInHorizontally(initialOffsetX = { it / 3 }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut() },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it / 3 }) + fadeOut() },
            ) {
                composable(Destination.Login.route) {
                    LoginScreen(repository = repository, onLoggedIn = {
                        navController.navigate(Destination.Dates.route) {
                            popUpTo(Destination.Login.route) { inclusive = true }
                        }
                    })
                }
                composable(Destination.Dates.route) {
                    DatesScreen(eventDatesStore = eventDatesStore, onConfirmed = {
                        navController.navigate(Destination.Catalog.route) {
                            popUpTo(Destination.Dates.route) { inclusive = true }
                        }
                    })
                }
                composable(Destination.Catalog.route) {
                    gr.lhrental.b2b.ui.screens.catalog.CatalogScreen(
                        repository = repository,
                        eventDatesStore = eventDatesStore,
                        onEditDates = { navController.navigate(Destination.Dates.route) },
                        onProductClick = { product -> navController.navigate(Destination.ProductDetail.of(product.id)) },
                    )
                }
                composable(
                    Destination.ProductDetail.route,
                    arguments = listOf(navArgument("productId") { type = NavType.IntType }),
                ) { entry ->
                    val productId = entry.arguments?.getInt("productId") ?: return@composable
                    gr.lhrental.b2b.ui.screens.catalog.ProductDetailScreen(
                        repository = repository,
                        cartStore = cartStore,
                        eventDatesStore = eventDatesStore,
                        productId = productId,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Destination.Cart.route) {
                    gr.lhrental.b2b.ui.screens.cart.CartScreen(
                        cartStore = cartStore,
                        eventDatesStore = eventDatesStore,
                        onEditDates = { navController.navigate(Destination.Dates.route) },
                        onCheckout = { navController.navigate(Destination.Checkout.route) },
                    )
                }
                composable(Destination.Checkout.route) {
                    gr.lhrental.b2b.ui.screens.cart.CheckoutScreen(
                        repository = repository,
                        cartStore = cartStore,
                        eventDatesStore = eventDatesStore,
                        onSubmitted = { orderId ->
                            navController.navigate(Destination.OrderDetail.of(orderId)) {
                                popUpTo(Destination.Catalog.route)
                            }
                        },
                    )
                }
                composable(Destination.Orders.route) {
                    gr.lhrental.b2b.ui.screens.orders.OrdersScreen(repository = repository) { order ->
                        navController.navigate(Destination.OrderDetail.of(order.id))
                    }
                }
                composable(
                    Destination.OrderDetail.route,
                    arguments = listOf(navArgument("orderId") { type = NavType.IntType }),
                ) { entry ->
                    val orderId = entry.arguments?.getInt("orderId") ?: return@composable
                    gr.lhrental.b2b.ui.screens.orders.OrderDetailScreen(
                        repository = repository,
                        orderId = orderId,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Destination.Account.route) {
                    AccountScreen(repository = repository, onLoggedOut = {
                        cartStore.clear()
                        eventDatesStore.clear()
                        navController.navigate(Destination.Login.route) {
                            popUpTo(0)
                        }
                    })
                }
            }
        }
    }
}
