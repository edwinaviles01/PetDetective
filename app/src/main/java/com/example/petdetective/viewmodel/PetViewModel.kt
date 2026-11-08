package com.example.petdetective.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.petdetective.data.model.PetReport
import com.example.petdetective.data.repository.PetRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PetViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PetRepository()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    val reports: StateFlow<List<PetReport>> = repository.allReports.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun clearError() {
        _errorMessage.value = null
    }

    fun addReport(report: PetReport, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                var finalReport = report
                if (report.photoUri != null && !report.photoUri.startsWith("http")) {
                    val downloadUrl = repository.uploadImage(Uri.parse(report.photoUri))
                    if (downloadUrl != null) {
                        finalReport = report.copy(photoUri = downloadUrl)
                    }
                }
                repository.insert(finalReport)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar el reporte. Verifica tu conexión."
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun markAsResolved(report: PetReport) {
        viewModelScope.launch {
            try {
                val updatedReport = report.copy(isResolved = true)
                repository.update(updatedReport)
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar el reporte."
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
                    _errorMessage.value = "Error al eliminar el reporte."
                }
            }
        }
    }
}