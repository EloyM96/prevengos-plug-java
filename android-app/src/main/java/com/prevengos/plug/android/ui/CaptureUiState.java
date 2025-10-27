package com.prevengos.plug.android.ui;

public class CaptureUiState {
    private final boolean saving;
    private final String message;
    private final String error;

    public CaptureUiState() {
        this(false, null, null);
    }

    public CaptureUiState(boolean saving, String message, String error) {
        this.saving = saving;
        this.message = message;
        this.error = error;
    }

    public boolean isSaving() {
        return saving;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }
}
