package gr.lhrental.b2b.ui.screens.catalog

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import gr.lhrental.b2b.R
import gr.lhrental.b2b.data.model.Category
import gr.lhrental.b2b.data.model.Product
import gr.lhrental.b2b.data.repo.B2bRepository
import gr.lhrental.b2b.data.repo.EventDates
import gr.lhrental.b2b.data.repo.EventDatesStore
import gr.lhrental.b2b.data.repo.ProductFilters
import gr.lhrental.b2b.data.repo.SortOption
import gr.lhrental.b2b.ui.theme.LhInk
import gr.lhrental.b2b.ui.theme.lhTextFieldColors
import gr.lhrental.b2b.ui.util.viewModelFactoryOf
import java.time.format.DateTimeFormatter
import java.util.Locale

private val bannerFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("el", "GR"))

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CatalogScreen(
    repository: B2bRepository,
    eventDatesStore: EventDatesStore,
    onEditDates: () -> Unit,
    onProductClick: (Product) -> Unit,
) {
    val viewModel: CatalogViewModel = viewModel(factory = viewModelFactoryOf { CatalogViewModel(repository, eventDatesStore) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(R.drawable.ic_lh_logo),
                        contentDescription = "LH Rental",
                        modifier = Modifier.height(28.dp),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LhInk,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            viewModel.eventDates?.let { dates ->
                EventDatesBanner(dates = dates, onEdit = onEditDates)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = viewModel::onSearchChange,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text(stringResource(R.string.catalog_search_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { viewModel.submitSearch() }),
                    colors = lhTextFieldColors(),
                    modifier = Modifier.weight(1f),
                )
                Box {
                    IconButton(onClick = viewModel::openFilterSheet, modifier = Modifier.padding(start = 4.dp)) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Φίλτρα",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    if (viewModel.filters.isActive) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .clip(CircleShape),
                        ) {
                            Surface(color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxSize()) {}
                        }
                    }
                }
            }

            if (viewModel.filterableCategories.isNotEmpty()) {
                CategoryChipsRow(
                    categories = viewModel.filterableCategories,
                    selectedId = viewModel.selectedCategoryId,
                    onSelect = viewModel::selectCategory,
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (viewModel.products.isEmpty() && !viewModel.isLoading) {
                    Text(
                        text = viewModel.errorMessage ?: stringResource(R.string.catalog_empty),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(viewModel.products, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                onClick = { onProductClick(product) },
                                modifier = Modifier.animateItemPlacement(),
                            )
                        }
                    }
                }

                if (viewModel.isLoading && viewModel.products.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }

    if (viewModel.showFilterSheet) {
        FilterSheet(
            current = viewModel.filters,
            onDismiss = viewModel::dismissFilterSheet,
            onApply = viewModel::applyFilters,
            onClear = viewModel::clearFilters,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    current: ProductFilters,
    onDismiss: () -> Unit,
    onApply: (Double?, Double?, SortOption) -> Unit,
    onClear: () -> Unit,
) {
    var minPrice by remember { mutableStateOf(current.minPrice?.toInt()?.toString().orEmpty()) }
    var maxPrice by remember { mutableStateOf(current.maxPrice?.toInt()?.toString().orEmpty()) }
    var sort by remember { mutableStateOf(current.sort) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            Text("Φίλτρα & ταξινόμηση", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

            Text("Εύρος τιμής (€)", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = minPrice,
                    onValueChange = { minPrice = it.filter(Char::isDigit) },
                    label = { Text("Από") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = lhTextFieldColors(),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = maxPrice,
                    onValueChange = { maxPrice = it.filter(Char::isDigit) },
                    label = { Text("Έως") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = lhTextFieldColors(),
                    modifier = Modifier.weight(1f),
                )
            }

            Text("Ταξινόμηση", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
            Column {
                SortOption.entries.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .padding(vertical = 4.dp),
                    ) {
                        RadioButton(selected = sort == option, onClick = { sort = option })
                        Text(option.label, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onClear, modifier = Modifier.weight(1f)) { Text("Καθαρισμός") }
                Button(
                    onClick = { onApply(minPrice.toDoubleOrNull(), maxPrice.toDoubleOrNull(), sort) },
                    modifier = Modifier.weight(1f),
                ) { Text("Εφαρμογή") }
            }
        }
    }
}

@Composable
private fun EventDatesBanner(dates: EventDates, onEdit: () -> Unit) {
    // Solid ink/paper banner (inverse of the page) instead of a grey fill —
    // keeps the "active info" bar high-contrast black/white, not muted grey.
    Surface(color = MaterialTheme.colorScheme.inverseSurface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.height(18.dp),
                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                )
                Text(
                    text = "${dates.start.format(bannerFormatter)} – ${dates.end.format(bannerFormatter)}",
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
private fun CategoryChipsRow(
    categories: List<Category>,
    selectedId: Int?,
    onSelect: (Int?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(selected = selectedId == null, onClick = { onSelect(null) }, label = { Text("Όλα") })
        }
        items(categories, key = { it.id }) { category ->
            FilterChip(
                selected = selectedId == category.id,
                onClick = { onSelect(category.id) },
                label = { Text(category.label) },
            )
        }
    }
}

@Composable
private fun ProductCard(product: Product, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val soldOut = product.availableQuantity == 0
    val context = LocalContext.current
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (soldOut) 0.55f else 1f),
    ) {
        Column {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(product.imageUrl)
                        .crossfade(250)
                        .build(),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                )
                if (soldOut) {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                    ) {
                        Text(
                            "ΕΞΑΝΤΛΗΘΗΚΕ",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row {
                        Text(
                            text = "${product.effectivePrice} €",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (product.onOutlet) {
                            Text(
                                text = "  OUTLET",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    product.availableQuantity?.takeIf { !soldOut }?.let { available ->
                        Text(
                            text = "$available διαθ.",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}
