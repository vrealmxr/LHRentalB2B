package gr.lhrental.b2b.ui.screens.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import gr.lhrental.b2b.R
import gr.lhrental.b2b.data.repo.B2bRepository
import gr.lhrental.b2b.data.repo.CartStore
import gr.lhrental.b2b.data.repo.EventDatesStore
import gr.lhrental.b2b.ui.util.viewModelFactoryOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    repository: B2bRepository,
    cartStore: CartStore,
    eventDatesStore: EventDatesStore,
    productId: Int,
    onBack: () -> Unit,
) {
    val viewModel: ProductDetailViewModel = viewModel(
        factory = viewModelFactoryOf { ProductDetailViewModel(repository, cartStore, eventDatesStore, productId) },
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.product?.sku ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                viewModel.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                viewModel.errorMessage != null -> Text(
                    text = viewModel.errorMessage.orEmpty(),
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
                viewModel.product != null -> {
                    val product = viewModel.product!!
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = product.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                        )
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${product.effectivePrice} €",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = 4.dp),
                            )

                            viewModel.remainingAvailable?.let { available ->
                                Text(
                                    text = if (available > 0) "$available διαθέσιμα για τις επιλεγμένες ημερομηνίες" else "Μη διαθέσιμο για τις επιλεγμένες ημερομηνίες",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (available > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            }

                            product.dimensions?.let {
                                Text("Διαστάσεις: $it", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 12.dp))
                            }
                            product.description?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 12.dp))
                            }

                            if (!viewModel.isSoldOut) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.padding(top = 24.dp),
                                ) {
                                    OutlinedButton(onClick = viewModel::decrement) { Text("−") }
                                    Text(viewModel.quantity.toString(), style = MaterialTheme.typography.titleMedium)
                                    OutlinedButton(onClick = viewModel::increment) { Text("+") }
                                }
                            }

                            Button(
                                onClick = viewModel::addToCart,
                                enabled = !viewModel.isSoldOut,
                                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                            ) {
                                Text(
                                    when {
                                        viewModel.isSoldOut -> "Μη διαθέσιμο"
                                        viewModel.addedToCart -> "Προστέθηκε ✓"
                                        else -> stringResource(R.string.product_add_to_cart)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
