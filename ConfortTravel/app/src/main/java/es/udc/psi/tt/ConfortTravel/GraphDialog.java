package es.udc.psi.tt.ConfortTravel;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GraphDialog extends DialogFragment {
    private static final String ARG_SESSION_ID = "session_id";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_DATE = "date";

    public static GraphDialog newInstance(String sessionId, String username, String date) {
        GraphDialog fragment = new GraphDialog();
        Bundle args = new Bundle();
        args.putString(ARG_SESSION_ID, sessionId);
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_graph, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView titleText = view.findViewById(R.id.titleText);
        TextView usernameText = view.findViewById(R.id.usernameText);
        TextView dateText = view.findViewById(R.id.dateText);
        LineChart lineChart = view.findViewById(R.id.lineChart);
        ImageButton closeButton = view.findViewById(R.id.closeButton);

        // Configurar título
        String username = getArguments().getString(ARG_USERNAME);
        String date = getArguments().getString(ARG_DATE);
        titleText.setText(R.string.str_graph_title);
        usernameText.setText(String.format(getString(R.string.str_graph_user), username));
        dateText.setText(String.format(getString(R.string.str_graph_date), date));

        closeButton.setOnClickListener(v -> dismiss());

        // Inicializar gráfica
        initializeLineChart(lineChart);

        // Cargar datos
        String sessionId = getArguments().getString(ARG_SESSION_ID);
        if (sessionId != null) {
            loadGraphData(sessionId, lineChart);
        }
    }

    private void initializeLineChart(LineChart lineChart) {
        LineDataSet dataSet = new LineDataSet(new ArrayList<>(), getString(R.string.measurements));
        dataSet.setColor(getResources().getColor(android.R.color.holo_blue_dark));
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setDrawLabels(false);
        lineChart.invalidate();
    }

    private void loadGraphData(String sessionId, LineChart lineChart) {
        FirebaseDatabase.getInstance().getReference(getString(R.string.firebase_sensor_data))
                .child(sessionId)
                .child(getString(R.string.firebase_accelerometer_data))
                .child(getString(R.string.firebase_reading))
                .child(getString(R.string.firebase_measurement))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Entry> entries = new ArrayList<>();
                        int index = 0;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Float value = dataSnapshot.getValue(Float.class);
                            if (value != null) {
                                entries.add(new Entry(index++, value));
                            }
                        }

                        LineDataSet dataSet = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
                        dataSet.setValues(entries);
                        lineChart.getData().notifyDataChanged();
                        lineChart.notifyDataSetChanged();
                        lineChart.invalidate();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(requireContext(), getString(R.string.str_graph_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}