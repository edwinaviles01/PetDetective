package com.example.petdetective.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.petdetective.data.model.PetReport
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: PetReport)

    @Delete
    suspend fun delete(report: PetReport)

    @Query("SELECT * FROM pet_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<PetReport>>
}