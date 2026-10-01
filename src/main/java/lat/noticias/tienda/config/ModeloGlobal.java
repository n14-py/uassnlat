package lat.noticias.tienda.config;

import jakarta.servlet.http.HttpSession;
import lat.noticias.tienda.modelo.Categoria;
import lat.noticias.tienda.servicio.CarritoServicio;
import lat.noticias.tienda.web.AccesoControlador;
import lat.noticias.tienda.web.AdminControlador;
import lat.noticias.tienda.web.TiendaControlador;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@ControllerAdvice(assignableTypes = {TiendaControlador.class, AdminControlador.class, AccesoControlador.class})
public class ModeloGlobal {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern(
            "EEEE d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-AR"));

    private final CarritoServicio carritoServicio;

    public ModeloGlobal(CarritoServicio carritoServicio) {
        this.carritoServicio = carritoServicio;
    }

    @ModelAttribute("cantidadCarrito")
    public int cantidadCarrito(HttpSession session) {
        return carritoServicio.cantidad(session);
    }

    @ModelAttribute("categorias")
    public Categoria[] categorias() {
        return Categoria.values();
    }

    @ModelAttribute("fechaHoy")
    public String fechaHoy() {
        String texto = LocalDate.now().format(FECHA);
        if (texto.isEmpty()) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase(Locale.forLanguageTag("es-AR")) + texto.substring(1);
    }

    @InitBinder
    public void blancosANulo(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
}
