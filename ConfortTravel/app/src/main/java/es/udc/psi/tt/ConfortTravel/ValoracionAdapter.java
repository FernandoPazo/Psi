package es.udc.psi.tt.ConfortTravel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ValoracionAdapter extends RecyclerView.Adapter<ValoracionAdapter.ValoracionViewHolder> {

    private final List<Valoracion> valoraciones;
    private final Context context;
    private final OnRatingClickListener listener;

    public ValoracionAdapter(List<Valoracion> valoraciones, Context context, OnRatingClickListener listener) {
        this.valoraciones = valoraciones;
        this.context = context;
        this.listener = listener;
    }

    public interface OnRatingClickListener {
        void onRatingClick(Valoracion valoracion);
    }

    @NonNull
    @Override
    public ValoracionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_valoracion, parent, false);
        return new ValoracionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ValoracionViewHolder holder, int position) {
        Valoracion valoracion = valoraciones.get(position);
        holder.usernameText.setText(valoracion.getUsername());
        holder.dateText.setText(valoracion.getDate());
        holder.ratingText.setText(valoracion.getValoracion());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRatingClick(valoracion);
            }
        });

        // Mostrar fecha al mantener pulsado
        // holder.itemView.setOnLongClickListener(v -> {
        //     Toast.makeText(context, context.getString(R.string.str_date) + valoracion.getDate(), Toast.LENGTH_SHORT).show();
        //     return true;
        // });
    }

    @Override
    public int getItemCount() {
        return valoraciones.size();
    }

    static class ValoracionViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, dateText, ratingText;

        public ValoracionViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.usernameText);
            dateText = itemView.findViewById(R.id.dateText);
            ratingText = itemView.findViewById(R.id.ratingText);
        }
    }
}
