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

    public ValoracionAdapter(List<Valoracion> valoraciones, Context context) {
        this.valoraciones = valoraciones;
        this.context = context;
    }

    @NonNull
    @Override
    public ValoracionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_valoracion, parent, false);
        return new ValoracionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ValoracionViewHolder holder, int position) {
        Valoracion val = valoraciones.get(position);
        holder.usernameTextView.setText(val.getUsername());
        holder.valoracionTextView.setText(val.getValoracion());

        // Mostrar fecha al mantener pulsado
        holder.itemView.setOnLongClickListener(v -> {
            Toast.makeText(context, context.getString(R.string.str_date) + val.getDate(), Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return valoraciones.size();
    }

    static class ValoracionViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, valoracionTextView;

        public ValoracionViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            valoracionTextView = itemView.findViewById(R.id.valoracionTextView);
        }
    }
}
