package com.example.petdetective.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class ReportType { LOST, FOUND }

enum class PetSpecies(val label: String) {
    DOG("Perro"),
    CAT("Gato"),
    BIRD("Ave"),
    OTHER("Otro")
}

@Entity(tableName = "pet_reports")
data class PetReport(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: ReportType = ReportType.LOST,
    val species: PetSpecies = PetSpecies.DOG,
    val name: String = "",
    val description: String = "",
    val location: String = "",
    val contactPhone: String = "",
    val photoUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val userId: String? = null,
    val isResolved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "type" to type.name,
            "species" to species.name,
            "name" to name,
            "description" to description,
            "location" to location,
            "contactPhone" to contactPhone,
            "photoUri" to photoUri,
            "latitude" to latitude,
            "longitude" to longitude,
            "userId" to userId,
            "isResolved" to isResolved,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromDocument(id: String, map: Map<String, Any>?): PetReport {
            if (map == null) return PetReport(id = id)
            return PetReport(
                id = id,
                type = try { ReportType.valueOf(map["type"] as? String ?: "LOST") } catch (e: Exception) { ReportType.LOST },
                species = try { PetSpecies.valueOf(map["species"] as? String ?: "DOG") } catch (e: Exception) { PetSpecies.DOG },
                name = map["name"] as? String ?: "",
                description = map["description"] as? String ?: "",
                location = map["location"] as? String ?: "",
                contactPhone = map["contactPhone"] as? String ?: "",
                photoUri = map["photoUri"] as? String,
                latitude = map["latitude"] as? Double,
                longitude = map["longitude"] as? Double,
                userId = map["userId"] as? String,
                isResolved = map["isResolved"] as? Boolean ?: false,
                timestamp = map["timestamp"] as? Long ?: System.currentTimeMillis()
            )
        }
    }
}