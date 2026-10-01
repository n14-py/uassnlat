package lat.noticias.tienda.servicio;

import lat.noticias.tienda.modelo.Producto;
import lat.noticias.tienda.modelo.Venta;

import java.math.BigDecimal;
import java.util.List;

public record ResumenTablero(
        long ventasHoy,
        BigDecimal montoHoy,
        long ventasMes,
        BigDecimal montoMes,
        BigDecimal ticketPromedio,
        long productosActivos,
        List<Producto> stockBajo,
        List<Venta> recientes
) {
}
