package es.udc.psi.tt.ConfortTravel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
    private Sensor gyroscope;



    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && Keys.ACTION_TOGGLE_MEASUREMENT.equals(intent.getAction())) {
            boolean isMeasuring = getMeasuringState();
            boolean newState = !isMeasuring;

            setMeasuringState(newState);

            // Registrar o desregistrar sensores según el nuevo estado
            if (newState) {
                if (accelerometer != null) {
                    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
                }
                if (gyroscope != null) {
                    sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI);
                }
            } else {
                sensorManager.unregisterListener(this);
            }

            // Crear y actualizar notificación con el estado correcto
            Notification notification = createNotification(newState);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.notify(Keys.FOREGROUND_NOTIFICATION_ID, notification);

            return START_STICKY;
        }

        // Primer inicio
        setMeasuringState(true);
        Notification notification = createNotification(true);
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
            //Log.d("SensorService", "Aceleración: x=" + x + " y=" + y + " z=" + z);
        }
        else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            float gx = event.values[0];
            float gy = event.values[1];
            float gz = event.values[2];

            Intent i = new Intent(Keys.INTENT_GYROSCOPE_DATA_TO_MAIN_ACTION);
            i.setPackage(getPackageName());
            i.putExtra(Keys.GYROSCOPE_AXIS_X, gx);
            i.putExtra(Keys.GYROSCOPE_AXIS_Y, gy);
            i.putExtra(Keys.GYROSCOPE_AXIS_Z, gz);
            sendBroadcast(i);

            //Log.d("SensorService", "Giroscopio: gx=" + gx + " gy=" + gy + " gz=" + gz);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No hace falta.
    }

    private Notification createNotification(boolean isMeasuring) {
        String channelId = Keys.SENSOR_FOREGROUND_NOTIFICATION_CHANNEL_ID;

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);


        NotificationChannel channel = new NotificationChannel(channelId, getString(R.string.notification_channel_foreground_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(getString(R.string.notification_channel_foreground_desc));
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Keys.SENSOR_FOREGROUND_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(isMeasuring ? getString(R.string.measuring) : getString(R.string.stop_reading))
                .setContentText(isMeasuring ? getString(R.string.data_gathering) : getString(R.string.data_paused))
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(pendingIntent)
                .setOngoing(isMeasuring)
                .setAutoCancel(!isMeasuring)
                .addAction(isMeasuring ? createPauseOnlyAction() : createResumeOnlyAction());

        return builder.build();
    }
    private void setMeasuringState(boolean measuring) {
        getSharedPreferences("SensorPrefs", MODE_PRIVATE)
                .edit()
                .putBoolean("isMeasuring", measuring)
                .apply();
        Intent intent = new Intent(Keys.INTENT_SENSOR_STATE_CHANGED);
        intent.putExtra(Keys.IS_MEASURING, measuring);
        sendBroadcast(intent);
    }

    private boolean getMeasuringState() {
        return getSharedPreferences("SensorPrefs", MODE_PRIVATE)
                .getBoolean("isMeasuring", false);
    }















    private NotificationCompat.Action createPauseOnlyAction() {
        Intent toggleIntent = new Intent(this, SensorService.class);
        toggleIntent.setAction(Keys.ACTION_TOGGLE_MEASUREMENT);
        PendingIntent togglePendingIntent = PendingIntent.getService(this, 1, toggleIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_pause,
                getString(R.string.str_pause),
                togglePendingIntent
        ).build();
    }

    private NotificationCompat.Action createResumeOnlyAction() {
        Intent toggleIntent = new Intent(this, SensorService.class);
        toggleIntent.setAction(Keys.ACTION_TOGGLE_MEASUREMENT);
        PendingIntent togglePendingIntent = PendingIntent.getService(this, 1, toggleIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_play,
                getString(R.string.str_resume),
                togglePendingIntent
        ).build();
    }


}