package gr.lhrental.b2b.ui.screens.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import gr.lhrental.b2b.R
import gr.lhrental.b2b.data.repo.CartLine
import gr.lhrental.b2b.data.repo.CartStore
import gr.lhrental.b2b.data.repo.EventDates
import gr.lhrental.b2b.data.repo.EventDatesStore
import java.time.format.DateTimeFormatter
import java.util.Locale

private val bannerFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("el", "GR"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartStore: CartStore,
    eventDatesStore: EventDatesStore,
    onEditDates: () -> Unit,
    onCheckout: () -> Unit,
) {
    val lines by cartStore.lines.collectAsState()
    val dates by eventDatesStore.dates.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.cart_title)) }) },
        bottomBar = {
            if (lines.isNotEmpty()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Σύνολο", style = MaterialTheme.typography.titleMedium)
                        Text("${cartStore.total} €", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp).padding(top = 12.dp)) {
                        Text(stringResource(R.string.cart_checkout))
                    }
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            dates?.let { EventDatesRow(dates = it, onEdit = onEditDates) }

            Box(modifier = Modifier.fillMaxSize()) {
                if (lines.isEmpty()) {
                    Text(
                        text = stringResource(R.string.cart_empty),
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                } else {
                    LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
                        items(lines, key = { it.product.id }) { line ->
                            CartLineCard(
                                line = line,
                                onIncrement = {
                                    val cap = line.product.availableQuantity ?: Int.MAX_VALUE
                                    if (line.quantity < cap) cartStore.setQuantity(line.product.id, line.quantity + 1)
                                },
                                onDecrement = { cartStore.setQuantity(line.product.id, line.quantity - 1) },
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventDatesRow(dates: EventDates, onEdit: () -> Unit) {
    // Same solid ink/paper treatment as the catalog's dates banner — not grey.
    Surface(color = MaterialTheme.colorScheme.inverseSurface) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.height(18.dp), tint = MaterialTheme.colorScheme.inverseOnSurface)
                Text(
                    "${dates.start.format(bannerFormatter)} – ${dates.end.format(bannerFormatter)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            TextButton(
                onClick = onEdit,
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.inverseOnSurface),
            ) { Text("Αλλαγή") }
        }
    }
}

@Composable
private fun CartLineCard(line: CartLine, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    Card {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = line.product.imageUrl,
                contentDescription = line.product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(4.dp)),
            )
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(line.product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("${line.product.effectivePrice} €", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            OutlinedButton(onClick = onDecrement) { Text("−") }
            Text(line.quantity.toString(), modifier = Modifier.padding(horizontal = 10.dp))
            OutlinedButton(onClick = onIncrement) { Text("+") }
        }
    }
}
