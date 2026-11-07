package com.example.petdetective.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.petdetective.data.model.PetReport
import com.example.petdetective.data.repository.PetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PetViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PetRepository()

    val reports: StateFlow<List<PetReport>> = repository.allReports.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addReport(report: PetReport) {
        viewModelScope.launch {
            try {
                repository.insert(report)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getReportById(id: String): PetReport? {
        return reports.value.firstOrNull { it.id == id }
    }

    fun deleteReport(id: String) {
        viewModelScope.launch {
            val report = reports.value.firstOrNull { it.id == id }
            if (report != null) {
                try {
                    repository.delete(report)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}