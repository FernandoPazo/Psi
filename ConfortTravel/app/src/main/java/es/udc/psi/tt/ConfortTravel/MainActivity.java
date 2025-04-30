package es.udc.psi.tt.ConfortTravel;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
    private SensorRepository sensorRepository;
    private List<float[]> accelData = new ArrayList<>();

    private final BroadcastReceiver sensorDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Keys.INTENT_SENSOR_DATA_TO_MAIN_ACTION.equals(intent.getAction())) {
                float x = intent.getFloatExtra(Keys.ACCELEROMETER_AXIS_X, 0);
                float y = intent.getFloatExtra(Keys.ACCELEROMETER_AXIS_Y, 0);
                float z = intent.getFloatExtra(Keys.ACCELEROMETER_AXIS_Z, 0);

                String valores = getString(R.string.accelerometer) + ":\nX = " + x + "\nY = " + y + "\nZ = " + z;
                binding.textView.setText(valores);
                accelData.add(new float[]{x, y, z});
            }
        }
    };



    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
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
        createNotificationChannel();

        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, Keys.REQUEST_CODE_PERMISSION);
        }

        IntentFilter filter = new IntentFilter(Keys.INTENT_SENSOR_DATA_TO_MAIN_ACTION);
        registerReceiver(sensorDataReceiver, filter, RECEIVER_NOT_EXPORTED);

        setUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(sensorDataReceiver);
    }

    public void setUI(){
        sensorRepository = new SensorRepository();
        Button testButton  = binding.saveDataButton;

        binding.swInerciales.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ContextCompat.startForegroundService(this, new Intent(this, SensorService.class));
            } else {
                stopService(new Intent(this, SensorService.class));
                binding.textView.setText(getString(R.string.stop_reading));
            }
        });
        testButton.setOnClickListener(v -> {
            saveData();
        });
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