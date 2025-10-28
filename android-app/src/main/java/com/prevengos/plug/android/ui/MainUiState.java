package com.prevengos.plug.android.ui;

import androidx.annotation.Nullable;

public class MainUiState {
    private final boolean saving;
    @Nullable
    private final String message;
    @Nullable
    private final String error;

    private MainUiState(boolean saving, @Nullable String message, @Nullable String error) {
        this.saving = saving;
        this.message = message;
        this.error = error;
    }

    public static MainUiState idle() {
        return new MainUiState(false, null, null);
    }

    public static MainUiState saving() {
        return new MainUiState(true, null, null);
    }

    public static MainUiState success(String message) {
        return new MainUiState(false, message, null);
    }

    public static MainUiState failure(String error) {
        return new MainUiState(false, null, error);
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
}
