package es.udc.psi.tt.ConfortTravel;

public class Valoracion {
    private String username;
    private String valoracion;
    private String date;

    public Valoracion() {} // Requerido por Firebase

    public Valoracion(String username, String valoracion, String date) {
        this.username = username;
        this.valoracion = valoracion;
        this.date = date;
    }

    public String getUsername() { return username; }
    public String getValoracion() { return valoracion; }
    public String getDate() { return date; }
}
