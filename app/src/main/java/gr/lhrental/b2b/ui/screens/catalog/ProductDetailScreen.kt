package gr.lhrental.b2b.ui.screens.catalog

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AirlineSeatReclineNormal
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import gr.lhrental.b2b.R
import gr.lhrental.b2b.data.model.Product
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
                                    color = if (available > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            }

                            AttributeChips(product, modifier = Modifier.padding(top = 14.dp))
                            ArButton(product, modifier = Modifier.padding(top = 10.dp))

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

/** Waterproof / sun-resistant / transferable / seating capacity — all present on Product but never shown before. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun AttributeChips(product: Product, modifier: Modifier = Modifier) {
    val chips = buildList {
        if (product.hasPeopleCapacity && product.peopleCapacity > 0) {
            add(Icons.Default.AirlineSeatReclineNormal to "${product.peopleCapacity} θέσεις")
        }
        if (product.isWaterproof) add(Icons.Default.Water to "Αδιάβροχο")
        if (product.isSunproof) add(Icons.Default.WbSunny to "Ανθεκτικό στον ήλιο")
        if (product.isTransferable) add(Icons.Default.LocalShipping to "Μεταφέρεται")
    }
    if (chips.isEmpty()) return

    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        chips.forEach { (icon, label) ->
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(label) },
                leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.padding(0.dp)) },
                colors = AssistChipDefaults.assistChipColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

/**
 * Hands off to Google's Scene Viewer (no in-app AR renderer here — that's a
 * much bigger build than a button). Falls back to the Play Store listing
 * for Google Play Services for AR if it isn't installed, and to a plain
 * disabled label when the product has no 3D scan at all.
 */
@Composable
private fun ArButton(product: Product, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val modelUrl = product.model3dUrl

    if (modelUrl == null) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
            Icon(Icons.Default.ViewInAr, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "AR μη διαθέσιμο ακόμα",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        return
    }

    OutlinedButton(
        onClick = {
            val sceneViewerUri = Uri.parse("https://arvr.google.com/scene-viewer/1.0?file=$modelUrl&mode=ar_preferred")
            val intent = Intent(Intent.ACTION_VIEW, sceneViewerUri).apply { setPackage("com.google.ar.core") }
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.ar.core")))
                } catch (e2: ActivityNotFoundException) {
                    Toast.makeText(context, "Χρειάζεται η εφαρμογή Google Play Services for AR.", Toast.LENGTH_LONG).show()
                }
            }
        },
        modifier = modifier.fillMaxWidth(),
    ) {
        Icon(Icons.Default.ViewInAr, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
        Text("Προβολή σε AR")
    }
}
