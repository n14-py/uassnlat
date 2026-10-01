package lat.noticias.tienda.api;

import lat.noticias.tienda.modelo.LineaVenta;
import lat.noticias.tienda.modelo.Venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaJson(
        String id,
        String numero,
        LocalDateTime fecha,
        String clienteNombre,
        String clienteEmail,
        String clienteTelefono,
        String documento,
        String direccion,
        String ciudad,
        List<LineaJson> lineas,
        BigDecimal total,
        String metodoPago,
        String estado,
        String origen,
        String notas
) {
    public static VentaJson de(Venta venta) {
        return new VentaJson(
                venta.getId(),
                venta.getNumero(),
                venta.getFecha(),
                venta.getClienteNombre(),
                venta.getClienteEmail(),
                venta.getClienteTelefono(),
                venta.getDocumento(),
                venta.getDireccion(),
                venta.getCiudad(),
                venta.getLineas().stream().map(LineaJson::de).toList(),
                venta.getTotal(),
                venta.getMetodoPago().name(),
                venta.getEstado().name(),
                venta.getOrigen().name(),
                venta.getNotas()
        );
    }

    public record LineaJson(
            String productoId,
            String sku,
            String nombre,
            BigDecimal precioUnitario,
            int cantidad,
            BigDecimal subtotal
    ) {
        static LineaJson de(LineaVenta linea) {
            return new LineaJson(
                    linea.getProductoId(),
                    linea.getSku(),
                    linea.getNombre(),
                    linea.getPrecioUnitario(),
                    linea.getCantidad(),
                    linea.getSubtotal()
            );
        }
    }
}
