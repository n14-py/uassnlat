package lat.noticias.tienda.servicio;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraVentaTest {

    @Test
    void calculaElSubtotalDeUnaLinea() {
        assertEquals(new BigDecimal("56.00"), CalculadoraVenta.linea(new BigDecimal("28"), 2));
    }

    @Test
    void sumaElTotalConDosDecimales() {
        BigDecimal total = CalculadoraVenta.total(List.of(new BigDecimal("49.00"), new BigDecimal("12.50")));
        assertEquals(new BigDecimal("61.50"), total);
    }

    @Test
    void armaUnSlugSinAcentos() {
        assertEquals("cronicas-de-un-continente", Textos.slug("Crónicas de un continente"));
    }
}
