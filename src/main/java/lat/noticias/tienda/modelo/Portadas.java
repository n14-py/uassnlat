package lat.noticias.tienda.modelo;

import java.util.List;

public final class Portadas {

    public static final List<Portada> TODAS = List.of(
            new Portada("portada-01", "Crónica"),
            new Portada("portada-02", "Archivo"),
            new Portada("portada-03", "Oficio"),
            new Portada("portada-04", "Campo"),
            new Portada("portada-05", "Taller"),
            new Portada("portada-06", "Objeto"),
            new Portada("portada-07", "Papel"),
            new Portada("portada-08", "Anuario")
    );

    private Portadas() {
    }

    public static boolean existe(String id) {
        return id != null && TODAS.stream().anyMatch(portada -> portada.id().equals(id));
    }
}
