package com.algebnaly.neonfiles.ui

import java.nio.file.Path

sealed interface FileBrowserAction {
    data class Open(val path: Path) : FileBrowserAction
    data class Select(val path: Path) : FileBrowserAction
    data class ToggleSelection(val path: Path) : FileBrowserAction
    data object StartCopy : FileBrowserAction
    data object Paste : FileBrowserAction
    data object CancelSelection : FileBrowserAction
    data object CancelPendingCopy : FileBrowserAction
    data object CancelLoading : FileBrowserAction
    data object Refresh : FileBrowserAction
    data object Back : FileBrowserAction
}