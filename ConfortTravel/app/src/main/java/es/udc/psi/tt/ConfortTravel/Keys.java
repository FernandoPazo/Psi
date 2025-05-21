package es.udc.psi.tt.ConfortTravel;

public class Keys {
    public static String SENSOR_FOREGROUND_NOTIFICATION_CHANNEL_ID = "es.udc.psi.tt.notifications.id.FN1";

    /**
     * CLAVES NUMÉRICAS.
     * Convenio para la creación de claves numéricas: AÑO_DE_IMPLEMENTACIÓN + MES_DE_IMPLEMENTACIÓN + NUMERO_RANGO_0001-9999
     */
    public static int FOREGROUND_NOTIFICATION_ID = 2025040001;
    public static int REQUEST_CODE_PERMISSION = 2025040002;

    /**
     * SENSORES.
     */
    /* ACELERÓMETRO */
    public static String ACCELEROMETER_AXIS_X = "es.udc.psi.tt.accelerometer.axis.x";
    public static String ACCELEROMETER_AXIS_Y = "es.udc.psi.tt.accelerometer.axis.y";
    public static String ACCELEROMETER_AXIS_Z = "es.udc.psi.tt.accelerometer.axis.z";

    /* GIROSCOPIO */
    public static String GYROSCOPE_AXIS_X = "es.udc.psi.tt.gyroscope.axis.x";
    public static String GYROSCOPE_AXIS_Y = "es.udc.psi.tt.gyroscope.axis.y";
    public static String GYROSCOPE_AXIS_Z = "es.udc.psi.tt.gyroscope.axis.z";


    /**
     * INTENTS
     */
    public static String INTENT_SENSOR_DATA_TO_MAIN_ACTION = "es.udc.psi.tt.intents.action.SENSOR_DATA_TO_MAIN";
    public static String INTENT_GYROSCOPE_DATA_TO_MAIN_ACTION = "es.udc.psi.tt.intents.action.GYROSCOPE_DATA_TO_MAIN";
    public static final String INTENT_SENSOR_STATE_CHANGED = "es.udc.psi.tt.ConfortTravel.SENSOR_STATE_CHANGED";
    public static final String IS_MEASURING = "isMeasuring";
    public static final String ACTION_TOGGLE_MEASUREMENT = "es.udc.psi.tt.ConfortTravel.ACTION_TOGGLE_MEASUREMENT";
}
