package dev.consumerfinance.ogwallet.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReminderScreen(manager: ReminderManager) {

    val list = manager.upcoming()

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = {
            manager.add(
                Reminder(
                    id = "bill-${System.currentTimeMillis()}",
                    title = "Credit Card",
                    amount = 75.50,
                    due = kotlinx.datetime.Clock.System.now()
                        .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                        .plus(DateTimePeriod(day = 1))
                )
            )
        }) {
            Text("+")
        }
    }) { padding ->

        LazyColumn(Modifier.padding(padding).padding(16.dp)) {
            items(list) { r ->
                Card(Modifier.fillMaxWidth().padding(8.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(r.title)
                        Text("Due: ${r.due}")
                        Text("Amount: ${r.amount}")
                    }
                }
            }
        }
    }
}
