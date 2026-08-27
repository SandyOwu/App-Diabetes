package com.example.smaeandrstud3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AlimentoAdapter extends RecyclerView.Adapter<AlimentoAdapter.ViewHolder> {

    private List<Alimento> alimentos;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Alimento alimento);
    }

    public AlimentoAdapter(List<Alimento> alimentos, OnItemClickListener listener) {
        this.alimentos = alimentos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alimento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Alimento alimento = alimentos.get(position);
        holder.textViewAlimento.setText(alimento.getNombre());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(alimento);
            }
        });
    }

    @Override
    public int getItemCount() {
        return alimentos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewAlimento;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAlimento = itemView.findViewById(R.id.textViewAlimento);
        }
    }
}
