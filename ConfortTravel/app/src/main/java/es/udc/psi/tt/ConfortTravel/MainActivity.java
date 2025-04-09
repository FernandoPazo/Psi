package es.udc.psi.tt.ConfortTravel;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.List;

import es.udc.psi.tt.ConfortTravel.databinding.ActivityMainBinding;
import es.udc.psi.tt.ConfortTravel.repositories.SensorRepository;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener sensorListener;
    private SensorRepository sensorRepository;
    private List<float[]> accelData = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        setUI();
    }

    public void setUI(){
        sensorRepository = new SensorRepository();
        Button testButton  = binding.saveDataButton;

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                String valores = "Acelerómetro:\nX = " + x + "\nY = " + y + "\nZ = " + z;
                binding.textView.setText(valores);
                accelData.add(new float[]{x, y, z});

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        binding.swInerciales.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
            } else {
                sensorManager.unregisterListener(sensorListener);
                binding.textView.setText("Lectura detenida");
            }
        });
        testButton.setOnClickListener(v -> {
            saveData();
        });
    }

    private void saveData(){
        if(accelData.isEmpty()){
            Toast.makeText(this, getString(R.string.no_data_error),
                           Toast.LENGTH_SHORT).show();
        }
        else{
            sensorRepository.saveSensorBatch(accelData)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, getString(R.string.save_data_Firebase),
                               Toast.LENGTH_SHORT).show();
            })
        .addOnFailureListener(e -> {
            Toast.makeText(this, getString(R.string.save_data_error) + e.getMessage(),
                           Toast.LENGTH_SHORT).show();
        });    
        }
    }
}