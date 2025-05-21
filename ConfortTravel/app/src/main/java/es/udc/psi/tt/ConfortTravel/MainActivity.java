package es.udc.psi.tt.ConfortTravel;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.List;

import es.udc.psi.tt.ConfortTravel.databinding.ActivityMainBinding;
import es.udc.psi.tt.ConfortTravel.repositories.SensorRepository;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SensorRepository sensorRepository;
    private List<Float> accelData = new ArrayList<>();
    private List<float[]> gyroData = new ArrayList<>();

    LineChart lineChart;
    LineDataSet dataSet;
    LineData lineData;

    private final BroadcastReceiver sensorDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Keys.INTENT_SENSOR_DATA_TO_MAIN_ACTION.equals(intent.getAction())) {
                float measurement = intent.getFloatExtra(Keys.SEND_COMPLETE_MEASUREMENT, 0);

                // Añadir el nuevo punto al gráfico
                Entry entry = new Entry(dataSet.getEntryCount(), measurement);
                dataSet.addEntry(entry);
                dataSet.notifyDataSetChanged();
                lineData.notifyDataChanged();
                lineChart.notifyDataSetChanged();
                lineChart.invalidate(); // actualiza el gráfico

                String value = getString(R.string.accelerometer) + ":\nLast measurement = " + measurement + "\n";
                binding.textView.setText(value);
                accelData.add(measurement);
            }
        }
    };

    private final BroadcastReceiver gyroscopeDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Keys.INTENT_GYROSCOPE_DATA_TO_MAIN_ACTION.equals(intent.getAction())) {
                float gx = intent.getFloatExtra(Keys.GYROSCOPE_AXIS_X, 0);
                float gy = intent.getFloatExtra(Keys.GYROSCOPE_AXIS_Y, 0);
                float gz = intent.getFloatExtra(Keys.GYROSCOPE_AXIS_Z, 0);

                String valores = getString(R.string.gyroscope) + ":\nX = " + gx + "\nY = " + gy + "\nZ = " + gz;
                binding.gyroscopeTextView.setText(valores);
                gyroData.add(new float[]{gx, gy, gz});
            }
        }
    };
    private final BroadcastReceiver sensorStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Keys.INTENT_SENSOR_STATE_CHANGED.equals(intent.getAction())) {
                boolean isMeasuring = intent.getBooleanExtra(Keys.IS_MEASURING, false);
                binding.swInerciales.setChecked(isMeasuring); // Actualiza el Switch
            }
        }
    };



    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyThemeFromPreferences();
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);

        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        createNotificationChannel();

        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, Keys.REQUEST_CODE_PERMISSION);
        }

        IntentFilter filter = new IntentFilter(Keys.INTENT_SENSOR_DATA_TO_MAIN_ACTION);
        registerReceiver(sensorDataReceiver, filter, RECEIVER_NOT_EXPORTED);

        IntentFilter gyroFilter = new IntentFilter(Keys.INTENT_GYROSCOPE_DATA_TO_MAIN_ACTION);
        registerReceiver(gyroscopeDataReceiver, gyroFilter, RECEIVER_NOT_EXPORTED);

        SharedPreferences prefs = getSharedPreferences("SensorPrefs", MODE_PRIVATE);
        if (!prefs.contains("isMeasuring")) {
            prefs.edit().putBoolean("isMeasuring", false).apply();
        }

        setUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(sensorDataReceiver);
        unregisterReceiver(gyroscopeDataReceiver);
        unregisterReceiver(sensorStateReceiver);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Keys.INTENT_SENSOR_STATE_CHANGED);
        registerReceiver(sensorStateReceiver, filter, RECEIVER_NOT_EXPORTED);

        boolean isMeasuring = getSharedPreferences("SensorPrefs", MODE_PRIVATE)
                .getBoolean("isMeasuring", false);
        binding.swInerciales.setChecked(isMeasuring);
    }


    public void setUI(){
        sensorRepository = new SensorRepository();
        Button testButton  = binding.saveDataButton;

        binding.swInerciales.setOnCheckedChangeListener(null); // Limpia cualquier listener anterior

        // Sincroniza el estado inicial
        boolean isMeasuring = getSharedPreferences("SensorPrefs", MODE_PRIVATE)
                .getBoolean("isMeasuring", false);
        binding.swInerciales.setChecked(isMeasuring);

        binding.swInerciales.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs = getSharedPreferences("SensorPrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("isMeasuring", isChecked).apply();

            Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vib.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));

            if (isChecked) {
                if (dataSet != null) {
                    dataSet.clear();
                    lineData.notifyDataChanged();
                    lineChart.notifyDataSetChanged();
                    lineChart.invalidate();
                }
                accelData.clear();
                ContextCompat.startForegroundService(this, new Intent(this, SensorService.class));
            } else {
                stopService(new Intent(this, SensorService.class));
                binding.textView.setText(getString(R.string.stop_reading));
                binding.gyroscopeTextView.setText(getString(R.string.stop_reading));
            }
        });

        initializeLineChart(Color.BLUE, Color.BLUE, LineDataSet.Mode.CUBIC_BEZIER, 2f);

        testButton.setOnClickListener(v -> {
            saveData();
        });
    }

    private void initializeLineChart(int graphColor, int textColor, LineDataSet.Mode graphType, float lineWidth) {
        lineChart = findViewById(R.id.lineChart);

        // Inicializar el dataSet
        dataSet = new LineDataSet(new ArrayList<>(), getString(R.string.measurements));
        dataSet.setColor(graphColor);
        dataSet.setValueTextColor(textColor);
        dataSet.setLineWidth(lineWidth);
        dataSet.setDrawCircles(false);
        dataSet.setMode(graphType); // opcional: suavizado

        // Inicializa el lineData
        lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        lineChart.getDescription().setText(getString(R.string.measure_each_6s));
        lineChart.getXAxis().setDrawLabels(false); // por ejemplo
        lineChart.invalidate();
    }

    private void createNotificationChannel() {

        CharSequence name = getString(R.string.notification_channel_foreground_name);
        String description = getString(R.string.notification_channel_foreground_desc);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(Keys.SENSOR_FOREGROUND_NOTIFICATION_CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager notifManager = getSystemService(NotificationManager.class);
        notifManager.createNotificationChannel(channel);
    }

    private void saveData(){
        if(accelData.isEmpty() && gyroData.isEmpty()){
            Toast.makeText(this, getString(R.string.no_data_error),
                    Toast.LENGTH_SHORT).show();
        }
        else{
           sensorRepository.saveSensorData(accelData, gyroData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, getString(R.string.save_data_Firebase),
                                    Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, getString(R.string.save_data_error) + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
           //Limpio las listas para que una vez le des al botón se guarden dichos datos una vez
            // pero si le vuelves a dar no se guarden datos repetidos si no únicamente los nuevos
                accelData.clear();
                gyroData.clear();
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);  // Paso 4
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void applyThemeFromPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString("theme", "system");

        switch (theme) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

}