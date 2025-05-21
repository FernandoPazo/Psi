package es.udc.psi.tt.ConfortTravel.repositories;
import android.os.Build;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class SensorRepository {

    private final DatabaseReference firebaseDbRef;

    public SensorRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://comforttravel-f1738-default-rtdb.europe-west1.firebasedatabase.app/");
        firebaseDbRef = database.getReference("sensor_data");
    }
    private String generateSessionId() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        //Enero empieza en 0
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        long timestamp = System.currentTimeMillis();

        return String.format("session_%04d%02d%02d_%d", year, month, day, timestamp);
    }

    private String formattedDate (Date date){
        TimeZone spainTimeZone = TimeZone.getTimeZone("Europe/Madrid");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        dateFormat.setTimeZone(spainTimeZone);
        return dateFormat.format(date);
    }

    public Task<Void> saveSensorData(List<Float> accelData, List<float[]> gyroData) {
        String sessionId = generateSessionId();
        DatabaseReference sessionRef = firebaseDbRef.child(sessionId);
        String dateNow = formattedDate(new Date());

        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("date", dateNow);
        sessionInfo.put("device_model", Build.MODEL);
        sessionRef.child("info").setValue(sessionInfo);

        //Colección dedicada a los datos del acelerómetro
        if (!accelData.isEmpty()) {
            DatabaseReference accelRef = sessionRef.child("accelerometer_data");
            Map<String, Object> readingMap = new HashMap<>();
            readingMap.put("Measurement", accelData);
            float sum = 0f;
            for (Float m : accelData) {
                sum += m;
            }
            readingMap.put("Mean", sum / accelData.size());
            accelRef.child("reading").setValue(readingMap);

            sessionRef.child("info").child("accelerometer_readings").
                    setValue(1);
        }

        //Colección dedicada a los datos del giroscopio
        if (!gyroData.isEmpty()) {
            DatabaseReference gyroRef = sessionRef.child("gyroscope_data");

            for (int i = 0; i < gyroData.size(); i++) {
                float[] data = gyroData.get(i);

                Map<String, Object> readingMap = new HashMap<>();
                readingMap.put("timestamp", new Date().getTime());
                readingMap.put("x", data[0]);
                readingMap.put("y", data[1]);
                readingMap.put("z", data[2]);

                gyroRef.child("reading_" + i).setValue(readingMap);
            }

            sessionRef.child("info").child("gyroscope_readings").setValue(gyroData.size());
        }

        return sessionRef.child("info").child("completed").setValue(true);
    }
}