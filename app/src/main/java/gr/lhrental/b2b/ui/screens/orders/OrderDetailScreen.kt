package gr.lhrental.b2b.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(order.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)

                        OutlinedCard(shape = RoundedCornerShape(4.dp)) {
                            OrderStageTracker(stage = order.stage, modifier = Modifier.padding(20.dp))
                        }

                        OutlinedCard(shape = RoundedCornerShape(4.dp)) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                InfoLine("Ημερομηνίες", "${order.dateStart ?: "—"} → ${order.dateReturn ?: "—"}")
                                order.location?.takeIf { it.isNotBlank() }?.let { InfoLine("Τοποθεσία", it) }
                                order.deliveryAddress?.takeIf { it.isNotBlank() }?.let {
                                    InfoLine("Παράδοση", "$it, ${order.deliveryTown.orEmpty()} ${order.deliveryPostcode.orEmpty()}".trim())
                                }
                                order.comments?.takeIf { it.isNotBlank() }?.let { InfoLine("Σχόλια", it) }
                            }
                        }

                        OutlinedCard(shape = RoundedCornerShape(4.dp)) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Προϊόντα", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                order.items.forEach { item ->
                                    OrderItemRow(item)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }

                        OutlinedCard(shape = RoundedCornerShape(4.dp)) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                val itemsTotal = order.items.sumOf { it.lineTotal }
                                SummaryRow("Προϊόντα", itemsTotal)
                                if (order.transportationCost > 0) SummaryRow("Μεταφορικά", order.transportationCost)
                                if (order.extraCost > 0) SummaryRow("Επιπλέον κόστος", order.extraCost)
                                if (order.delayCost > 0) SummaryRow("Καθυστέρηση", order.delayCost)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                val grandTotal = itemsTotal + order.transportationCost + order.extraCost + order.delayCost
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Σύνολο", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Text("$grandTotal €", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * order_stage is 0/1/2 (not verified / verified / delivered — see
 * admin-legacy/orders.php's own badges). Rendered as a 3-step tracker with
 * a bar that fills up to the current stage, per request.
 */
@Composable
fun OrderStageTracker(stage: Int, modifier: Modifier = Modifier) {
    val steps = listOf("Υποβλήθηκε", "Επιβεβαιώθηκε", "Παραδόθηκε")
    val activeIndex = stage.coerceIn(0, steps.lastIndex)
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            steps.indices.forEach { index ->
                StageDot(filled = index <= activeIndex, color = activeColor, inactiveColor = inactiveColor)
                if (index != steps.lastIndex) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .background(if (index < activeIndex) activeColor else inactiveColor),
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            steps.forEachIndexed { index, label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (index == activeIndex) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (index <= activeIndex) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = when (index) {
                        0 -> TextAlign.Start
                        steps.lastIndex -> TextAlign.End
                        else -> TextAlign.Center
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StageDot(filled: Boolean, color: Color, inactiveColor: Color) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(if (filled) color else inactiveColor),
    )
}

@Composable
private fun InfoLine(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun OrderItemRow(item: OrderLineItem) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(item.name, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("${item.quantity} × ${item.unitPrice} €", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        Text("${item.lineTotal} €")
    }
}

@Composable
private fun SummaryRow(label: String, amount: Double) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurface)
        Text("$amount €")
    }
}
