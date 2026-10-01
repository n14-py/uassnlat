package lat.noticias.tienda.modelo;

public enum OrigenVenta {
    WEB("Tienda"),
    ADMIN("Mostrador");

    private final String etiqueta;

    OrigenVenta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
