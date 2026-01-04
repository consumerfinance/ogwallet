package dev.consumerfinance.ogwallet.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.consumerfinance.ogwallet.models.travel.CostItem
import dev.consumerfinance.ogwallet.models.travel.ChecklistItem
import dev.consumerfinance.ogwallet.models.travel.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TripRepository(private val dbManager: DatabaseManager) {

    suspend fun addTrip(trip: Trip) = withContext(Dispatchers.Default) {
        dbManager.queries?.insertTrip(
            id = trip.id,
            destination = trip.destination,
            dates = trip.dates,
            status = trip.status,
            points_used = trip.pointsUsed.toLong(),
            savings = trip.savings.toLong(),
            emoji = trip.emoji,
            bookings = trip.bookings.toLong(),
            created_at = System.currentTimeMillis(),
            updated_at = System.currentTimeMillis()
        )
    }

    fun getAllTrips(): Flow<List<Trip>> {
        return try {
            dbManager.queries?.getAllTrips()?.asFlow()?.mapToList(Dispatchers.Default)?.map { list ->
                list.map { row ->
                    Trip(
                        id = row.id,
                        destination = row.destination,
                        dates = row.dates,
                        status = row.status,
                        pointsUsed = row.points_used?.toInt() ?: 0,
                        savings = row.savings?.toInt() ?: 0,
                        emoji = row.emoji ?: "✈️",
                        bookings = row.bookings?.toInt() ?: 0
                    )
                }
            } ?: kotlinx.coroutines.flow.flowOf(emptyList())
        } catch (e: Exception) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }

    fun getTripById(id: String): Flow<Trip?> {
        return dbManager.queries?.getTripById(id)?.asFlow()?.mapToOneOrNull(Dispatchers.Default)?.map { row ->
            row?.let {
                Trip(
                    id = it.id,
                    destination = it.destination,
                    dates = it.dates,
                    status = it.status,
                    pointsUsed = it.points_used?.toInt() ?: 0,
                    savings = it.savings?.toInt() ?: 0,
                    emoji = it.emoji ?: "✈️",
                    bookings = it.bookings?.toInt() ?: 0
                )
            }
        } ?: kotlinx.coroutines.flow.flowOf(null)
    }

    suspend fun updateTrip(trip: Trip) = withContext(Dispatchers.Default) {
        dbManager.queries?.updateTrip(
            destination = trip.destination,
            dates = trip.dates,
            status = trip.status,
            points_used = trip.pointsUsed.toLong(),
            savings = trip.savings.toLong(),
            emoji = trip.emoji,
            bookings = trip.bookings.toLong(),
            updated_at = System.currentTimeMillis(),
            id = trip.id
        )
    }

    suspend fun deleteTrip(tripId: String) = withContext(Dispatchers.Default) {
        dbManager.queries?.deleteTrip(tripId)
    }

    // Cost Item methods
    suspend fun addTripCostItem(tripId: String, costItem: CostItem) = withContext(Dispatchers.Default) {
        dbManager.queries?.insertTripCostItem(
            id = costItem.id,
            trip_id = tripId,
            category = costItem.category.name,
            description = costItem.description,
            amount = costItem.amount
        )
    }

    fun getTripCostItems(tripId: String): Flow<List<CostItem>> {
        return try {
            dbManager.queries?.getTripCostItems(tripId)?.asFlow()?.mapToList(Dispatchers.Default)?.map { list ->
                list.map { row ->
                    CostItem(
                        id = row.id,
                        category = dev.consumerfinance.ogwallet.models.travel.CostCategory.valueOf(row.category),
                        description = row.description,
                        amount = row.amount
                    )
                }
            } ?: kotlinx.coroutines.flow.flowOf(emptyList())
        } catch (e: Exception) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }

    suspend fun updateTripCostItem(tripId: String, costItem: CostItem) = withContext(Dispatchers.Default) {
        dbManager.queries?.updateTripCostItem(
            category = costItem.category.name,
            description = costItem.description,
            amount = costItem.amount,
            id = costItem.id,
            trip_id = tripId
        )
    }

    suspend fun deleteTripCostItem(tripId: String, costItemId: String) = withContext(Dispatchers.Default) {
        dbManager.queries?.deleteTripCostItem(costItemId, tripId)
    }

    // Checklist Item methods
    suspend fun addTripChecklistItem(tripId: String, checklistItem: ChecklistItem) = withContext(Dispatchers.Default) {
        dbManager.queries?.insertTripChecklistItem(
            id = checklistItem.id,
            trip_id = tripId,
            text = checklistItem.text,
            completed = checklistItem.completed,
            category = checklistItem.category
        )
    }

    fun getTripChecklistItems(tripId: String): Flow<List<ChecklistItem>> {
        return try {
            dbManager.queries?.getTripChecklistItems(tripId)?.asFlow()?.mapToList(Dispatchers.Default)?.map { list ->
                list.map { row ->
                    ChecklistItem(
                        id = row.id,
                        text = row.text,
                        completed = row.completed ?: false,
                        category = row.category
                    )
                }
            } ?: kotlinx.coroutines.flow.flowOf(emptyList())
        } catch (e: Exception) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }

    suspend fun updateTripChecklistItem(tripId: String, checklistItem: ChecklistItem) = withContext(Dispatchers.Default) {
        dbManager.queries?.updateTripChecklistItem(
            text = checklistItem.text,
            completed = checklistItem.completed,
            category = checklistItem.category,
            id = checklistItem.id,
            trip_id = tripId
        )
    }

    suspend fun deleteTripChecklistItem(tripId: String, checklistItemId: String) = withContext(Dispatchers.Default) {
        dbManager.queries?.deleteTripChecklistItem(checklistItemId, tripId)
    }
}