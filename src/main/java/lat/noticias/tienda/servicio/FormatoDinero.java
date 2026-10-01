package lat.noticias.tienda.servicio;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Component("dinero")
public class FormatoDinero {

    public String format(BigDecimal valor) {
        if (valor == null) {
            return "—";
        }
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.forLanguageTag("es-AR"));
        DecimalFormat formato = new DecimalFormat("#,##0.00", simbolos);
        return "US$ " + formato.format(valor);
    }
}
