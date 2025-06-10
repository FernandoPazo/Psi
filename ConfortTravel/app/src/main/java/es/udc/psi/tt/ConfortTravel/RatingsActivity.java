package es.udc.psi.tt.ConfortTravel;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RatingsActivity extends AppCompatActivity implements ValoracionAdapter.OnRatingClickListener {
    private RecyclerView recyclerViewRatings;
    private ValoracionAdapter adapter;
    private List<Valoracion> ratingsList;
        
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings);

        //Configurar Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Inicializar RecyclerView
        recyclerViewRatings = findViewById(R.id.recyclerViewRatings);
        recyclerViewRatings.setLayoutManager(new LinearLayoutManager(this));
        ratingsList = new ArrayList<>();
        adapter = new ValoracionAdapter(ratingsList, this, this);
        recyclerViewRatings.setAdapter(adapter);

        //Cargar datos
        loadRatings();
    }
    private void loadRatings() {
        FirebaseDatabase.getInstance().getReference("sensor_data")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        ratingsList.clear();
                        for (DataSnapshot session : snapshot.getChildren()) {
                            DataSnapshot infoSnapshot = session.child("info");

                            String username = infoSnapshot.child("username").getValue(String.class);
                            Object valoracionObj = infoSnapshot.child("valoracion").getValue();
                            String valoracion = valoracionObj != null ? valoracionObj.toString() : null;
                            Object dateObj = infoSnapshot.child("date").getValue();
                            String date = dateObj != null ? dateObj.toString() : null;

                            if (username != null && valoracion != null && date != null && session.getKey() != null) {
                                ratingsList.add(new Valoracion(username, valoracion, date, session.getKey()));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(RatingsActivity.this, "Error al cargar las valoraciones", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @Override
    public void onRatingClick(Valoracion valoracion) {
        GraphDialog dialog = GraphDialog.newInstance(
            valoracion.getSessionId(),
            valoracion.getUsername(),
            valoracion.getDate()
        );
        dialog.show(getSupportFragmentManager(), "GraphDialog");
    }
}