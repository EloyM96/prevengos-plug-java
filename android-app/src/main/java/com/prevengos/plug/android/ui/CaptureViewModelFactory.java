package com.prevengos.plug.android.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.prevengos.plug.android.PrevengosApplication;

public class CaptureViewModelFactory implements ViewModelProvider.Factory {
    private final PrevengosApplication application;

    public CaptureViewModelFactory(PrevengosApplication application) {
        this.application = application;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CaptureViewModel.class)) {
            return (T) new CaptureViewModel(
                    application.getContainer().getPacienteRepository(),
                    application.getContainer().getCuestionarioRepository(),
                    application
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
