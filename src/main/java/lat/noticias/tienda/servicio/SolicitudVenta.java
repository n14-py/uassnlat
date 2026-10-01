package lat.noticias.tienda.servicio;

import lat.noticias.tienda.modelo.MetodoPago;
import lat.noticias.tienda.modelo.OrigenVenta;

import java.util.List;

public record SolicitudVenta(
        String clienteNombre,
        String clienteEmail,
        String clienteTelefono,
        String documento,
        String direccion,
        String ciudad,
        MetodoPago metodoPago,
        OrigenVenta origen,
        String notas,
        List<Item> items
) {
    public record Item(String productoId, int cantidad) {
    }
}
