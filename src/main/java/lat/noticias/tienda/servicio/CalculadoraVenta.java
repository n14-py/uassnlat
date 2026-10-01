package lat.noticias.tienda.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class CalculadoraVenta {

    private CalculadoraVenta() {
    }

    public static BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal linea(BigDecimal precioUnitario, int cantidad) {
        return dinero(precioUnitario.multiply(BigDecimal.valueOf(cantidad)));
    }

    public static BigDecimal total(List<BigDecimal> subtotales) {
        return dinero(subtotales.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
