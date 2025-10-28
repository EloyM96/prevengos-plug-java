package com.prevengos.plug.android.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.prevengos.plug.android.data.local.entity.CuestionarioEntity;
import com.prevengos.plug.android.databinding.ItemCuestionarioBinding;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class CuestionarioAdapter extends ListAdapter<CuestionarioEntity, CuestionarioAdapter.ViewHolder> {
    public interface Callbacks {
        void onCuestionarioEdit(CuestionarioEntity cuestionario);
    }

    private final Callbacks callbacks;
    private final DateFormat dateFormat = DateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT,
            Locale.getDefault());

    public CuestionarioAdapter(Callbacks callbacks) {
        super(DIFF_CALLBACK);
        this.callbacks = callbacks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCuestionarioBinding binding = ItemCuestionarioBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCuestionarioBinding binding;

        ViewHolder(ItemCuestionarioBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CuestionarioEntity cuestionario) {
            binding.plantilla.setText(cuestionario.getPlantillaCodigo());
            binding.estado.setText(cuestionario.getEstado());
            binding.fecha.setText(dateFormat.format(new Date(cuestionario.getLastModified())));
            binding.editButton.setOnClickListener(v -> callbacks.onCuestionarioEdit(cuestionario));
        }
    }

    private static final DiffUtil.ItemCallback<CuestionarioEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CuestionarioEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull CuestionarioEntity oldItem, @NonNull CuestionarioEntity newItem) {
                    return oldItem.getCuestionarioId().equals(newItem.getCuestionarioId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull CuestionarioEntity oldItem, @NonNull CuestionarioEntity newItem) {
                    return oldItem.getEstado().equals(newItem.getEstado())
                            && oldItem.getLastModified() == newItem.getLastModified();
                }
            };
}
