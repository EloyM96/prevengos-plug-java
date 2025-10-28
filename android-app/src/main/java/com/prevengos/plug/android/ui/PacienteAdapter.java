package com.prevengos.plug.android.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.prevengos.plug.android.data.local.entity.PacienteEntity;
import com.prevengos.plug.android.databinding.ItemPacienteBinding;

public class PacienteAdapter extends ListAdapter<PacienteEntity, PacienteAdapter.PacienteViewHolder> {
    public interface Callbacks {
        void onPacienteSelected(PacienteEntity paciente);

        void onPacienteEdit(PacienteEntity paciente);
    }

    private final Callbacks callbacks;
    private String selectedPacienteId;

    public PacienteAdapter(Callbacks callbacks) {
        super(DIFF_CALLBACK);
        this.callbacks = callbacks;
    }

    public void setSelectedPacienteId(String pacienteId) {
        selectedPacienteId = pacienteId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PacienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemPacienteBinding binding = ItemPacienteBinding.inflate(inflater, parent, false);
        return new PacienteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PacienteViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class PacienteViewHolder extends RecyclerView.ViewHolder {
        private final ItemPacienteBinding binding;

        PacienteViewHolder(ItemPacienteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PacienteEntity paciente) {
            binding.nombre.setText(paciente.getNombre() + " " + paciente.getApellidos());
            binding.nif.setText(paciente.getNif());
            boolean selected = selectedPacienteId != null && selectedPacienteId.equals(paciente.getPacienteId());
            binding.pacienteCard.setChecked(selected);
            binding.pacienteCard.setOnClickListener(v -> callbacks.onPacienteSelected(paciente));
            binding.editButton.setOnClickListener(v -> callbacks.onPacienteEdit(paciente));
        }
    }

    private static final DiffUtil.ItemCallback<PacienteEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<PacienteEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull PacienteEntity oldItem, @NonNull PacienteEntity newItem) {
                    return oldItem.getPacienteId().equals(newItem.getPacienteId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull PacienteEntity oldItem, @NonNull PacienteEntity newItem) {
                    return oldItem.getNif().equals(newItem.getNif())
                            && oldItem.getNombre().equals(newItem.getNombre())
                            && oldItem.getApellidos().equals(newItem.getApellidos())
                            && equalsNullable(oldItem.getTelefono(), newItem.getTelefono())
                            && equalsNullable(oldItem.getEmail(), newItem.getEmail());
                }
            };

    private static boolean equalsNullable(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }
}
