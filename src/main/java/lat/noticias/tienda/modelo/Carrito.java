package lat.noticias.tienda.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Carrito implements Serializable {

    private final List<LineaCarrito> lineas = new ArrayList<>();

    public List<LineaCarrito> getLineas() {
        return lineas;
    }

    public boolean estaVacio() {
        return lineas.isEmpty();
    }

    public int getCantidadTotal() {
        return lineas.stream().mapToInt(LineaCarrito::getCantidad).sum();
    }

    public BigDecimal getTotal() {
        return lineas.stream()
                .map(LineaCarrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
