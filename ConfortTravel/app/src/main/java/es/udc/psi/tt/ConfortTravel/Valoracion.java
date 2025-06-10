package es.udc.psi.tt.ConfortTravel;

public class Valoracion {
    private String username;
    private String valoracion;
    private String date;
    private String sessionId;

    public Valoracion() {} // Requerido por Firebase

    public Valoracion(String username, String valoracion, String date, String sessionId) {
        this.username = username;
        this.valoracion = valoracion;
        this.date = date;
        this.sessionId = sessionId;
    }

    public String getUsername() { return username; }
    public String getValoracion() { return valoracion; }
    public String getDate() { return date; }
    public String getSessionId() { return sessionId; }
}
