package com.prevengos.plug.android.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.prevengos.plug.android.PrevengosApplication;
import com.prevengos.plug.android.di.AppContainer;

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
            AppContainer container = application.getContainer();
            return (T) new CaptureViewModel(
                    container.getPacienteRepository(),
                    container.getCuestionarioRepository(),
                    application,
                    container.getIoExecutor());
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
