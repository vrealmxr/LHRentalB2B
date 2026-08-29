package gr.lhrental.b2b.ui.screens.cart

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import gr.lhrental.b2b.R
import gr.lhrental.b2b.data.repo.B2bRepository
import gr.lhrental.b2b.data.repo.CartStore
import gr.lhrental.b2b.ui.util.viewModelFactoryOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    repository: B2bRepository,
    cartStore: CartStore,
    onSubmitted: (orderId: Int) -> Unit,
) {
    val viewModel: CheckoutViewModel = viewModel(factory = viewModelFactoryOf { CheckoutViewModel(repository, cartStore) })

    LaunchedEffect(viewModel.submittedOrderId) {
        viewModel.submittedOrderId?.let(onSubmitted)
    }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.checkout_title)) }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            LabeledField(stringResource(R.string.checkout_project_title), viewModel.title, viewModel::onTitleChange)
            LabeledField(stringResource(R.string.checkout_event_type), viewModel.eventType, viewModel::onEventTypeChange)
            LabeledField(stringResource(R.string.checkout_date_start) + " (YYYY-MM-DD)", viewModel.dateStart, viewModel::onDateStartChange)
            LabeledField(stringResource(R.string.checkout_date_return) + " (YYYY-MM-DD)", viewModel.dateReturn, viewModel::onDateReturnChange)
            LabeledField(stringResource(R.string.checkout_location), viewModel.location, viewModel::onLocationChange)
            LabeledField(stringResource(R.string.checkout_address), viewModel.address, viewModel::onAddressChange)
            LabeledField(stringResource(R.string.checkout_town), viewModel.town, viewModel::onTownChange)
            LabeledField(stringResource(R.string.checkout_postcode), viewModel.postcode, viewModel::onPostcodeChange)
            LabeledField(stringResource(R.string.checkout_comments), viewModel.comments, viewModel::onCommentsChange)

            viewModel.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            Button(
                onClick = viewModel::submit,
                enabled = !viewModel.isSubmitting,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 32.dp),
            ) {
                if (viewModel.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.padding(2.dp))
                } else {
                    Text(stringResource(R.string.checkout_submit))
                }
            }
        }
    }
}

@Composable
private fun LabeledField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
    )
}
