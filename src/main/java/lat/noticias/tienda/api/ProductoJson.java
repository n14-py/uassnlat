package lat.noticias.tienda.api;

import lat.noticias.tienda.modelo.Producto;

import java.math.BigDecimal;

public record ProductoJson(
        String id,
        String sku,
        String nombre,
        String slug,
        String resumen,
        String descripcion,
        BigDecimal precio,
        BigDecimal precioLista,
        Integer descuento,
        int stock,
        String categoria,
        String imagen,
        boolean destacado,
        boolean activo
) {
    public static ProductoJson de(Producto producto) {
        return new ProductoJson(
                producto.getId(),
                producto.getSku(),
                producto.getNombre(),
                producto.getSlug(),
                producto.getResumen(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getPrecioLista(),
                producto.getDescuento(),
                producto.getStock(),
                producto.getCategoria().name(),
                producto.getImagen(),
                producto.isDestacado(),
                producto.isActivo()
        );
    }
}
