package dev.consumerfinance.ogwallet.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import androidx.compose.ui.graphics.Color
import dev.consumerfinance.ogwallet.models.CreditCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CreditCardRepository(private val dbManager: DatabaseManager) {

    suspend fun addCard(card: CreditCard) = withContext(Dispatchers.Default) {
        dbManager.queries?.insertCard(
            id = card.id,
            name = card.name,
            card_number = card.cardNumber,
            last4 = card.last4,
            cvv = card.cvv,
            expiry = card.expiry,
            balance = card.balance,
            credit_limit = card.limit.toDouble(),
            available_credit = card.availableCredit,
            network = card.network,
            next_payment = card.nextPayment,
            min_payment = card.minPayment
        )
    }

    fun getAllCards(): Flow<List<CreditCard>> {
        return try {
            dbManager.queries?.getAllCards()?.asFlow()?.mapToList(Dispatchers.Default)?.map { list ->
                list.map { row ->
                    CreditCard(
                        id = row.id,
                        name = row.name,
                        cardNumber = row.card_number ?: "",
                        last4 = row.last4,
                        cvv = row.cvv ?: "",
                        expiry = row.expiry ?: "",
                        balance = row.balance ?: 0.0,
                        limit = row.credit_limit?.toInt() ?: 0,
                        availableCredit = row.available_credit ?: 0.0,
                        gradient = listOf(Color.Gray, Color.DarkGray),
                        network = row.network ?: "Card",
                        nextPayment = row.next_payment,
                        minPayment = row.min_payment ?: 0.0
                    )
                }
            } ?: kotlinx.coroutines.flow.flowOf(emptyList())
        } catch (e: Exception) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }

    fun getCardById(id: String): Flow<CreditCard?> {
        return dbManager.queries?.getCardById(id)?.asFlow()?.mapToOneOrNull(Dispatchers.Default)?.map { row ->
            row?.let {
                CreditCard(
                    id = it.id,
                    name = it.name,
                    cardNumber = it.card_number ?: "",
                    last4 = it.last4,
                    cvv = it.cvv ?: "",
                    expiry = it.expiry ?: "",
                    balance = it.balance ?: 0.0,
                    limit = it.credit_limit?.toInt() ?: 0,
                    availableCredit = it.available_credit ?: 0.0,
                    gradient = listOf(Color.Gray, Color.DarkGray),
                    network = it.network ?: "Card",
                    nextPayment = it.next_payment,
                    minPayment = it.min_payment ?: 0.0
                )
            }
        } ?: kotlinx.coroutines.flow.flowOf(null)
    }

    suspend fun updateCardBalance(cardId: String, balance: Double) = withContext(Dispatchers.Default) {
        dbManager.queries?.updateCardBalance(balance, balance, cardId)
    }

    suspend fun deleteCard(cardId: String) = withContext(Dispatchers.Default) {
        dbManager.queries?.deleteCard(cardId)
    }
}