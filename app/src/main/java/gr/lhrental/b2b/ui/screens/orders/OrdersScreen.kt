package gr.lhrental.b2b.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import gr.lhrental.b2b.R
import gr.lhrental.b2b.data.model.OrderSummary
import gr.lhrental.b2b.data.repo.B2bRepository
import gr.lhrental.b2b.ui.util.viewModelFactoryOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    repository: B2bRepository,
    onOrderClick: (OrderSummary) -> Unit,
) {
    val viewModel: OrdersViewModel = viewModel(factory = viewModelFactoryOf { OrdersViewModel(repository) })

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_orders)) }) }) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                viewModel.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                viewModel.orders.isEmpty() -> Text(
                    text = viewModel.errorMessage ?: stringResource(R.string.orders_empty),
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
                else -> LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(viewModel.orders, key = { it.id }) { order ->
                        OrderCard(order = order, onClick = { onOrderClick(order) })
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: OrderSummary, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    order.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                )
                StageBadge(order.stage)
            }
            Text("${order.dateStart ?: "—"} → ${order.dateReturn ?: "—"}", style = MaterialTheme.typography.bodyMedium)
            order.location?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

/**
 * No spare hue to signal "stage" with (monochrome palette, and the label text
 * itself already says which stage it is) — so completed orders get a solid
 * ink-filled badge and the two in-progress stages get an outlined one. Two
 * clearly different weights, still pure black/white, nothing grey.
 */
@Composable
fun StageBadge(stage: Int) {
    val shape = RoundedCornerShape(6.dp)
    if (stage >= 2) {
        Surface(color = MaterialTheme.colorScheme.inverseSurface, shape = shape) {
            Text(
                text = orderStageLabel(stage),
                color = MaterialTheme.colorScheme.inverseOnSurface,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    } else {
        Surface(
            color = androidx.compose.ui.graphics.Color.Transparent,
            shape = shape,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
        ) {
            Text(
                text = orderStageLabel(stage),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}
