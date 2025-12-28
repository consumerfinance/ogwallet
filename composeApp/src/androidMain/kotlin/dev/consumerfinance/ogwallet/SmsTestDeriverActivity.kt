package dev.consumerfinance.ogwallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.consumerfinance.ogwallet.util.SmsParser
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.compose.koinInject

class SmsTestDeriverActivity : ComponentActivity() {
    private val smsReader: SmsReader by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                SmsDeriverScreen(smsReader) {
                    finish()
                }
            }
        }
    }
}

@Composable
fun SmsDeriverScreen(smsReader: SmsReader, onFinish: () -> Unit) {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Ready to derive test SMS") }
    var isLoading by remember { mutableStateOf(false) }
    var smsByCategory by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    val smsParser = koinInject<SmsParser>()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "SMS Test Deriver",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        try {
                            smsByCategory = deriveTestSms(smsReader, smsParser) { progress ->
                                status = progress
                            }
                            status = "✅ Successfully derived test SMS for ${smsByCategory.size} categories!"
                        } catch (e: Exception) {
                            status = "❌ Error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                }
            ) {
                Text("Derive Test SMS")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onFinish) {
                Text("Close")
            }
        }

        if (smsByCategory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(smsByCategory.entries.toList()) { (category, smsList) ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "$category (${smsList.size} SMS)",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            smsList.take(3).forEach { sms ->
                                Text(
                                    text = sms.take(100) + if (sms.length > 100) "..." else "",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // Generate test code snippet
                                val testCode = """
                                    @Test
                                    fun test${category.replace(" ", "")}FromDevice_${smsList.indexOf(sms) + 1}() {
                                        val sms = \"\"\"${sms.replace("\"", "\\\"")}\"\"\"
                                        val result = smsParser.parse(sms)

                                        assertNotNull(result, "Should parse SMS")
                                        assertEquals("$category", result.category)
                                    }
                                """.trimIndent()
                                Text(
                                    text = "Test code:",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                )
                                Text(
                                    text = testCode,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            if (smsList.size > 3) {
                                Text(
                                    text = "... and ${smsList.size - 3} more (showing test code for first 3)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

suspend fun deriveTestSms(smsReader: SmsReader, smsParser: SmsParser, onProgress: (String) -> Unit): Map<String, List<String>> {
    val smsMessages = smsReader.readSmsMessages()
    val smsByCategory = mutableMapOf<String, MutableList<String>>()

    onProgress("Processing ${smsMessages.size} SMS messages...")

    smsMessages.forEachIndexed { index: Int, sms: String ->
        if (index % 100 == 0) {
            onProgress("Processed $index/${smsMessages.size} SMS...")
        }

        val parsed = smsParser.parse(sms)
        if (parsed != null) {
            val category = parsed.category
            smsByCategory.getOrPut(category) { mutableListOf() }.add(sms)
        }
    }

    // Sort categories and limit to 5 examples per category for testing
    val result = smsByCategory.mapValues { (_, smsList) ->
        smsList.take(5)
    }.toSortedMap()

    onProgress("Derived SMS for ${result.size} categories")

    return result
}