package com.prevengos.plug.android.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.prevengos.plug.android.PrevengosApplication;

public class MainViewModelFactory implements ViewModelProvider.Factory {
    private final PrevengosApplication application;

    public MainViewModelFactory(PrevengosApplication application) {
        this.application = application;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainViewModel.class)) {
            return (T) new MainViewModel(
                    application.getContainer().getPacienteRepository(),
                    application.getContainer().getCuestionarioRepository(),
                    application,
                    application.getContainer().getIoExecutor());
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
