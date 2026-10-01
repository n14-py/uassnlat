package lat.noticias.tienda.modelo;

public enum EstadoVenta {
    REGISTRADA("Registrada"),
    EN_PREPARACION("En preparación"),
    ENTREGADA("Entregada"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoVenta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
