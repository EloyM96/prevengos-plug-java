package com.prevengos.plug.android.ui;

import androidx.annotation.Nullable;

public class CaptureUiState {
    private final boolean saving;
    @Nullable
    private final String message;
    @Nullable
    private final String error;

    public CaptureUiState() {
        this(false, null, null);
    }

    public CaptureUiState(boolean saving, @Nullable String message, @Nullable String error) {
        this.saving = saving;
        this.message = message;
        this.error = error;
    }

    public boolean isSaving() {
        return saving;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    @Nullable
    public String getError() {
        return error;
    }

    public static CaptureUiState saving() {
        return new CaptureUiState(true, null, null);
    }

    public static CaptureUiState success(String message) {
        return new CaptureUiState(false, message, null);
    }

    public static CaptureUiState failure(String error) {
        return new CaptureUiState(false, null, error);
    }
}
