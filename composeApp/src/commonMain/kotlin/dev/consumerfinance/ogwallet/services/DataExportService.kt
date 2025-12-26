package dev.consumerfinance.ogwallet.services

import dev.consumerfinance.ogwallet.db.TransactionRepository
import dev.consumerfinance.ogwallet.db.BillRepository // Assuming this exists
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dev.consumerfinance.ogwallet.util.EncryptionUtil

// Data class to hold all exportable data
@kotlinx.serialization.Serializable
data class ExportedData(
    val transactions: List<dev.consumerfinance.ogwallet.models.TransactionEntry>,
    // Add other data types here as needed, e.g.,
    // val bills: List<dev.consumerfinance.ogwallet.models.Bill>,
    // val vaultConfig: dev.consumerfinance.ogwallet.models.VaultConfig
)

class DataExportService(
    private val transactionRepository: TransactionRepository,
    private val billRepository: BillRepository // Assuming this exists
) : KoinComponent {

    // Inject Encryption utility
    private val encryptionUtil: EncryptionUtil by inject()

    suspend fun exportData(password: String): String {
        return withContext(Dispatchers.IO) {
            // 1. Retrieve all data
            val allTransactions = transactionRepository.getAllTransactions().first()
            // val allBills = billRepository.getAllBills().first() // TODO: Implement getAllBills

            val dataToExport = ExportedData(
                transactions = allTransactions,
                // bills = allBills
            )

            // 2. Serialize to JSON
            val jsonString = Json.encodeToString(dataToExport)

            // 3. Encrypt JSON with password
            encryptionUtil.encrypt(jsonString, password)
        }
    }
}

