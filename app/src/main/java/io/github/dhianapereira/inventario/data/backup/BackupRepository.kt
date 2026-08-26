package io.github.dhianapereira.inventario.data.backup

interface BackupRepository {
    suspend fun exportTo(uri: String): BackupSummary
    suspend fun restoreFrom(uri: String): BackupSummary
}

data class BackupSummary(
    val categories: Int,
    val items: Int,
    val updates: Int,
    val closures: Int,
)

sealed class BackupException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class InvalidFile(cause: Throwable? = null) : BackupException("Invalid backup file", cause)
    class UnsupportedVersion : BackupException("Unsupported backup version")
    class TooManyRecords : BackupException("Backup contains too many records")
    class CannotOpenFile(cause: Throwable? = null) : BackupException("Cannot open backup file", cause)
    class EmptyBackup : BackupException("Empty backup cannot replace current data")
}
