package io.github.dhianapereira.inventario.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dhianapereira.inventario.data.backup.BackupException
import io.github.dhianapereira.inventario.data.backup.BackupRepository
import io.github.dhianapereira.inventario.data.backup.BackupSummary
import io.github.dhianapereira.inventario.data.backup.JsonBackupRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class BackupOperation { EXPORT, RESTORE }
data class BackupUiState(val operation: BackupOperation? = null)

sealed interface BackupEvent {
    data class Completed(val operation: BackupOperation, val summary: BackupSummary) : BackupEvent
    data class Failed(val reason: BackupFailureReason) : BackupEvent
    data object Cancelled : BackupEvent
}

enum class BackupFailureReason { INVALID_FILE, UNSUPPORTED_VERSION, TOO_LARGE, EMPTY, FILE_ACCESS, UNKNOWN }

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupRepository: JsonBackupRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<BackupEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()
    private var job: Job? = null

    fun export(uri: String) = run(BackupOperation.EXPORT) { backupRepository.exportTo(uri) }
    fun restore(uri: String) = run(BackupOperation.RESTORE) { backupRepository.restoreFrom(uri) }
    fun cancel() { job?.cancel() }

    private fun run(operation: BackupOperation, block: suspend () -> BackupSummary) {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            _uiState.value = BackupUiState(operation)
            try {
                _events.emit(BackupEvent.Completed(operation, block()))
            } catch (_: CancellationException) {
                _events.emit(BackupEvent.Cancelled)
            } catch (exception: BackupException) {
                _events.emit(BackupEvent.Failed(exception.failureReason()))
            } catch (_: Exception) {
                _events.emit(BackupEvent.Failed(BackupFailureReason.UNKNOWN))
            } finally {
                _uiState.value = BackupUiState()
            }
        }
    }

    private fun BackupException.failureReason() = when (this) {
        is BackupException.InvalidFile -> BackupFailureReason.INVALID_FILE
        is BackupException.UnsupportedVersion -> BackupFailureReason.UNSUPPORTED_VERSION
        is BackupException.TooManyRecords -> BackupFailureReason.TOO_LARGE
        is BackupException.EmptyBackup -> BackupFailureReason.EMPTY
        is BackupException.CannotOpenFile -> BackupFailureReason.FILE_ACCESS
    }
}
