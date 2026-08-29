package gr.lhrental.b2b.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import gr.lhrental.b2b.data.model.OrderLineItem
import gr.lhrental.b2b.data.repo.B2bRepository
import gr.lhrental.b2b.ui.util.viewModelFactoryOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    repository: B2bRepository,
    orderId: Int,
    onBack: () -> Unit,
) {
    val viewModel: OrderDetailViewModel = viewModel(factory = viewModelFactoryOf { OrderDetailViewModel(repository, orderId) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Παραγγελία #$orderId") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                viewModel.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                viewModel.order == null -> Text(
                    text = viewModel.errorMessage ?: "Δεν βρέθηκε.",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
                else -> {
                    val order = viewModel.order!!
                    Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(order.title, style = MaterialTheme.typography.headlineSmall)
                            StageBadge(order.stage)
                        }
                        Text("${order.dateStart ?: "—"} → ${order.dateReturn ?: "—"}", modifier = Modifier.padding(top = 6.dp))
                        order.location?.takeIf { it.isNotBlank() }?.let {
                            Text("Τοποθεσία: $it", modifier = Modifier.padding(top = 4.dp))
                        }
                        order.deliveryAddress?.takeIf { it.isNotBlank() }?.let {
                            Text("Παράδοση: $it, ${order.deliveryTown.orEmpty()} ${order.deliveryPostcode.orEmpty()}", modifier = Modifier.padding(top = 4.dp))
                        }
                        order.comments?.takeIf { it.isNotBlank() }?.let {
                            Text("Σχόλια: $it", modifier = Modifier.padding(top = 4.dp))
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        Text("Προϊόντα", style = MaterialTheme.typography.titleMedium)
                        order.items.forEach { item -> OrderItemRow(item) }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        val itemsTotal = order.items.sumOf { it.lineTotal }
                        SummaryRow("Προϊόντα", itemsTotal)
                        if (order.transportationCost > 0) SummaryRow("Μεταφορικά", order.transportationCost)
                        if (order.extraCost > 0) SummaryRow("Επιπλέον κόστος", order.extraCost)
                        if (order.delayCost > 0) SummaryRow("Καθυστέρηση", order.delayCost)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: OrderLineItem) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(item.name, fontWeight = FontWeight.Medium)
            Text("${item.quantity} × ${item.unitPrice} €", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("${item.lineTotal} €")
    }
}

@Composable
private fun SummaryRow(label: String, amount: Double) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("$amount €")
    }
}
