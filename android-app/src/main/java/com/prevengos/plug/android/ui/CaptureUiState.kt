package com.prevengos.plug.android.ui

data class CaptureUiState(
    val isSaving: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
