package es.udc.psi.tt.ConfortTravel.repositories;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorRepository {

   private final DatabaseReference firebaseDbRef;

   public SensorRepository() {
       FirebaseDatabase database = FirebaseDatabase.getInstance("https://comforttravel-f1738-default-rtdb.europe-west1.firebasedatabase.app/");
       firebaseDbRef = database.getReference("sensor_data");
   }

   public Task<Void> saveSensorBatch(List<float[]> dataList) {
       String sessionId = "session_" + System.currentTimeMillis();
       DatabaseReference sessionRef = firebaseDbRef.child(sessionId);

       for (int i = 0; i < dataList.size(); i++) {
        float[] data = dataList.get(i);
        
        Map<String, Object> readingMap = new HashMap<>();
        readingMap.put("date", new Date());
        readingMap.put("x", data[0]);
        readingMap.put("y", data[1]);
        readingMap.put("z", data[2]);
        
        sessionRef.child("reading_" + i).setValue(readingMap);
    }

       return sessionRef.child("info").child("total_readings").setValue(dataList.size());
   }
}