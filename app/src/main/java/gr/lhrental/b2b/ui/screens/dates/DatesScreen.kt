package gr.lhrental.b2b.ui.screens.dates

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import gr.lhrental.b2b.R
import gr.lhrental.b2b.data.repo.EventDatesStore
import gr.lhrental.b2b.ui.theme.LhInk
import gr.lhrental.b2b.ui.util.viewModelFactoryOf
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val displayFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("el", "GR"))

@Composable
fun DatesScreen(
    eventDatesStore: EventDatesStore,
    onConfirmed: () -> Unit,
) {
    val viewModel: DatesViewModel = viewModel(factory = viewModelFactoryOf { DatesViewModel(eventDatesStore) })
    var pickerTarget by remember { mutableStateOf<DateTarget?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f)
                .background(LhInk),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_lh_logo),
                contentDescription = "LH Rental",
                modifier = Modifier.height(56.dp),
            )
        }

        Column(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxWidth()
                .padding(28.dp),
        ) {
            Text("Ημερομηνίες εκδήλωσης", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "Επιλέξτε πότε χρειάζεστε τον εξοπλισμό — έτσι θα βλέπετε πραγματική διαθεσιμότητα σε κάθε προϊόν, όχι απλά το συνολικό απόθεμα.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 28.dp),
            )

            DateCard(
                label = "Παραλαβή",
                date = viewModel.startDate,
                onClick = { pickerTarget = DateTarget.START },
            )
            Box(modifier = Modifier.padding(vertical = 10.dp)) {
                Text("έως", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DateCard(
                label = "Επιστροφή",
                date = viewModel.endDate,
                onClick = { pickerTarget = DateTarget.END },
            )

            viewModel.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
            }

            Button(
                onClick = { if (viewModel.confirm()) onConfirmed() },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .padding(top = 24.dp),
            ) {
                Text("Συνέχεια στον κατάλογο")
            }
        }
    }

    pickerTarget?.let { target ->
        val initial = when (target) {
            DateTarget.START -> viewModel.startDate
            DateTarget.END -> viewModel.endDate ?: viewModel.startDate
        }
        DatePickerModal(
            initialDate = initial,
            onDismiss = { pickerTarget = null },
            onConfirm = { date ->
                when (target) {
                    DateTarget.START -> viewModel.onStartSelected(date)
                    DateTarget.END -> viewModel.onEndSelected(date)
                }
                pickerTarget = null
            },
        )
    }
}

private enum class DateTarget { START, END }

@Composable
private fun DateCard(label: String, date: LocalDate?, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = date?.format(displayFormatter) ?: "Επιλέξτε ημερομηνία",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
            Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    initialDate: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val initialMillis = (initialDate ?: LocalDate.now())
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
    val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { millis ->
                    onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                }
            }) { Text("Επιλογή") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Άκυρο") } },
    ) {
        DatePicker(state = state)
    }
}
