package es.udc.psi.tt.ConfortTravel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;


public class SensorService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = createNotification();
        startForeground(Keys.FOREGROUND_NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            Intent i = new Intent(Keys.INTENT_SENSOR_DATA_TO_MAIN_ACTION);
            //Solucion para hcer el broadcast explícito
            //se indica que debe entregar el broadcast a los receptores dentro de nuestra aplicación
            i.setPackage(getPackageName());
            i.putExtra(Keys.ACCELEROMETER_AXIS_X, x);
            i.putExtra(Keys.ACCELEROMETER_AXIS_Y, y);
            i.putExtra(Keys.ACCELEROMETER_AXIS_Z, z);
            sendBroadcast(i);

            // Aquí puedes guardar los valores o hacer cálculos
            Log.d("SensorService", "Aceleración: x=" + x + " y=" + y + " z=" + z);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No hace falta.
    }

    private Notification createNotification() {
        String channelId = Keys.SENSOR_FOREGROUND_NOTIFICATION_CHANNEL_ID;

        NotificationChannel channel = new NotificationChannel(channelId, getString(R.string.notification_channel_foreground_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(getString(R.string.notification_channel_foreground_desc));
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, Keys.SENSOR_FOREGROUND_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.measuring))
                .setContentText(getString(R.string.data_gathering))
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .build();
    }
}