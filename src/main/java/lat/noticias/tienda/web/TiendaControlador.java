package lat.noticias.tienda.web;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lat.noticias.tienda.modelo.Carrito;
import lat.noticias.tienda.modelo.Categoria;
import lat.noticias.tienda.modelo.OrigenVenta;
import lat.noticias.tienda.modelo.Producto;
import lat.noticias.tienda.modelo.Venta;
import lat.noticias.tienda.servicio.CarritoServicio;
import lat.noticias.tienda.servicio.ProductoServicio;
import lat.noticias.tienda.servicio.ReglaNegocioException;
import lat.noticias.tienda.servicio.SolicitudVenta;
import lat.noticias.tienda.servicio.VentaServicio;
import lat.noticias.tienda.web.formulario.CheckoutFormulario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
public class TiendaControlador {

    static final String PEDIDO_RECIENTE = "PEDIDO_RECIENTE";

    private final ProductoServicio productoServicio;
    private final CarritoServicio carritoServicio;
    private final VentaServicio ventaServicio;

    public TiendaControlador(ProductoServicio productoServicio,
                             CarritoServicio carritoServicio,
                             VentaServicio ventaServicio) {
        this.productoServicio = productoServicio;
        this.carritoServicio = carritoServicio;
        this.ventaServicio = ventaServicio;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("hero", productoServicio.porSku("NT-ANU-2026").filter(Producto::isActivo).orElse(null));
        model.addAttribute("destacados", productoServicio.destacados(4));
        model.addAttribute("conteoCategorias", productoServicio.contarPorCategoria());
        return "inicio";
    }

    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(required = false) String q,
                           @RequestParam(required = false) String categoria,
                           @RequestParam(defaultValue = "destacados") String orden,
                           @RequestParam(defaultValue = "0") int pagina,
                           Model model) {
        Categoria seccion = parsearCategoria(categoria);
        model.addAttribute("pagina", productoServicio.buscar(q, seccion, orden, pagina, 9));
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("categoria", seccion == null ? "" : seccion.name());
        model.addAttribute("categoriaActual", seccion);
        model.addAttribute("orden", orden);
        return "catalogo";
    }

    @GetMapping("/producto/{slug}")
    public String producto(@PathVariable String slug, Model model) {
        Producto producto = productoServicio.porSlug(slug)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("producto", producto);
        model.addAttribute("relacionados", productoServicio.relacionados(producto, 3));
        return "producto";
    }

    @GetMapping("/carrito")
    public String carrito(HttpSession session, Model model) {
        List<String> avisos = carritoServicio.sincronizar(session);
        if (!avisos.isEmpty()) {
            model.addAttribute("avisos", avisos);
        }
        model.addAttribute("carrito", carritoServicio.obtener(session));
        return "carrito";
    }

    @PostMapping("/carrito/agregar")
    public String agregar(@RequestParam String productoId,
                          @RequestParam(defaultValue = "1") int cantidad,
                          @RequestParam(required = false) String volver,
                          HttpSession session,
                          RedirectAttributes redirect) {
        try {
            carritoServicio.agregar(session, productoId, cantidad);
            redirect.addFlashAttribute("mensaje", "El producto se agregó al carrito.");
            return "redirect:/carrito";
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return volverSeguro(volver);
        }
    }

    @PostMapping("/carrito/actualizar")
    public String actualizar(@RequestParam String productoId,
                             @RequestParam int cantidad,
                             HttpSession session,
                             RedirectAttributes redirect) {
        try {
            carritoServicio.actualizar(session, productoId, cantidad);
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/quitar")
    public String quitar(@RequestParam String productoId, HttpSession session) {
        carritoServicio.quitar(session, productoId);
        return "redirect:/carrito";
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model, RedirectAttributes redirect) {
        if (redirigirSiCambia(session, redirect)) {
            return "redirect:/carrito";
        }
        if (carritoServicio.obtener(session).estaVacio()) {
            redirect.addFlashAttribute("error", "El carrito está vacío.");
            return "redirect:/catalogo";
        }
        if (!model.containsAttribute("formulario")) {
            CheckoutFormulario formulario = new CheckoutFormulario();
            model.addAttribute("formulario", formulario);
        }
        model.addAttribute("carrito", carritoServicio.obtener(session));
        model.addAttribute("metodos", lat.noticias.tienda.modelo.MetodoPago.values());
        return "checkout";
    }

    @PostMapping("/checkout")
    public String confirmar(@Valid @ModelAttribute("formulario") CheckoutFormulario formulario,
                            BindingResult result,
                            HttpSession session,
                            Model model,
                            RedirectAttributes redirect) {
        if (redirigirSiCambia(session, redirect)) {
            return "redirect:/carrito";
        }
        Carrito carrito = carritoServicio.obtener(session);
        if (carrito.estaVacio()) {
            redirect.addFlashAttribute("error", "El carrito está vacío.");
            return "redirect:/catalogo";
        }
        if (result.hasErrors()) {
            model.addAttribute("carrito", carrito);
            model.addAttribute("metodos", lat.noticias.tienda.modelo.MetodoPago.values());
            return "checkout";
        }
        try {
            List<SolicitudVenta.Item> items = carrito.getLineas().stream()
                    .map(linea -> new SolicitudVenta.Item(linea.getProductoId(), linea.getCantidad()))
                    .toList();
            Venta venta = ventaServicio.registrar(new SolicitudVenta(
                    formulario.getClienteNombre(),
                    formulario.getClienteEmail(),
                    formulario.getClienteTelefono(),
                    formulario.getDocumento(),
                    formulario.getDireccion(),
                    formulario.getCiudad(),
                    formulario.getMetodoPago(),
                    OrigenVenta.WEB,
                    formulario.getNotas(),
                    items
            ));
            carritoServicio.vaciar(session);
            session.setAttribute(PEDIDO_RECIENTE, venta.getNumero());
            return "redirect:/pedido/" + venta.getNumero();
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("carrito", carritoServicio.obtener(session));
            model.addAttribute("metodos", lat.noticias.tienda.modelo.MetodoPago.values());
            return "checkout";
        }
    }

    @GetMapping("/pedido/{numero}")
    public String pedido(@PathVariable String numero, HttpSession session, Model model) {
        Object reciente = session.getAttribute(PEDIDO_RECIENTE);
        if (reciente == null || !reciente.equals(numero)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Venta venta = ventaServicio.porNumero(numero)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("venta", venta);
        return "pedido";
    }

    private boolean redirigirSiCambia(HttpSession session, RedirectAttributes redirect) {
        List<String> avisos = carritoServicio.sincronizar(session);
        if (!avisos.isEmpty()) {
            redirect.addFlashAttribute("avisos", avisos);
            return true;
        }
        return false;
    }

    private String volverSeguro(String volver) {
        if (volver != null && volver.matches("/producto/[a-z0-9-]+|/catalogo")) {
            return "redirect:" + volver;
        }
        return "redirect:/catalogo";
    }

    private Categoria parsearCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return null;
        }
        return Arrays.stream(Categoria.values())
                .filter(valor -> valor.name().equalsIgnoreCase(categoria))
                .findFirst()
                .orElse(null);
    }
}
