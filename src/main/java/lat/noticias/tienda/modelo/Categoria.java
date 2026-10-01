package lat.noticias.tienda.modelo;

public enum Categoria {
    SUSCRIPCION("Suscripciones", "Digital y papel, por un año"),
    EDICION("Ediciones", "Revistas y anuarios"),
    LIBRO("Libros", "Crónica y oficio"),
    MERCHANDISING("Objetos", "Piezas de la redacción"),
    EXPERIENCIA("Experiencias", "Talleres y visitas");

    private final String nombre;
    private final String descripcion;

    Categoria(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
