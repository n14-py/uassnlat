package lat.noticias.tienda.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lat.noticias.tienda.modelo.Categoria;
import lat.noticias.tienda.modelo.EstadoVenta;
import lat.noticias.tienda.modelo.MetodoPago;
import lat.noticias.tienda.modelo.OrigenVenta;
import lat.noticias.tienda.modelo.Portadas;
import lat.noticias.tienda.modelo.Producto;
import lat.noticias.tienda.modelo.Venta;
import lat.noticias.tienda.servicio.ProductoServicio;
import lat.noticias.tienda.servicio.ReglaNegocioException;
import lat.noticias.tienda.servicio.SolicitudVenta;
import lat.noticias.tienda.servicio.VentaServicio;
import lat.noticias.tienda.web.formulario.LineaFormulario;
import lat.noticias.tienda.web.formulario.ProductoFormulario;
import lat.noticias.tienda.web.formulario.VentaFormulario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminControlador {

    private static final int LINEAS = 6;

    private final ProductoServicio productoServicio;
    private final VentaServicio ventaServicio;

    @ModelAttribute("ruta")
    public String ruta(HttpServletRequest request) {
        return request.getRequestURI();
    }

    public AdminControlador(ProductoServicio productoServicio, VentaServicio ventaServicio) {
        this.productoServicio = productoServicio;
        this.ventaServicio = ventaServicio;
    }

    @GetMapping({"", "/"})
    public String tablero(Model model) {
        model.addAttribute("resumen", ventaServicio.resumen());
        return "admin/tablero";
    }

    @GetMapping("/productos")
    public String productos(@RequestParam(required = false) String q,
                            @RequestParam(defaultValue = "todos") String filtro,
                            Model model) {
        model.addAttribute("productos", productoServicio.listarGestion(q, filtro));
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("filtro", filtro);
        return "admin/productos";
    }

    @GetMapping("/productos/nuevo")
    public String nuevoProducto(Model model) {
        if (!model.containsAttribute("formulario")) {
            ProductoFormulario formulario = new ProductoFormulario();
            formulario.setActivo(true);
            formulario.setImagen("portada-01");
            formulario.setStock(1);
            model.addAttribute("formulario", formulario);
        }
        cargarProducto(model);
        return "admin/producto-form";
    }

    @GetMapping("/productos/{id}/editar")
    public String editarProducto(@PathVariable String id, Model model) {
        Producto producto = productoServicio.porId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!model.containsAttribute("formulario")) {
            model.addAttribute("formulario", ProductoFormulario.desde(producto));
        }
        cargarProducto(model);
        return "admin/producto-form";
    }

    @PostMapping("/productos")
    public String guardarProducto(@Valid @ModelAttribute("formulario") ProductoFormulario formulario,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirect) {
        if (result.hasErrors()) {
            cargarProducto(model);
            return "admin/producto-form";
        }
        try {
            Producto guardado = productoServicio.guardar(formulario);
            redirect.addFlashAttribute("mensaje", "Producto guardado: " + guardado.getNombre() + ".");
            return "redirect:/admin/productos";
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            cargarProducto(model);
            return "admin/producto-form";
        }
    }

    @PostMapping("/productos/{id}/archivar")
    public String archivar(@PathVariable String id, RedirectAttributes redirect) {
        productoServicio.cambiarActivo(id, false);
        redirect.addFlashAttribute("mensaje", "El producto quedó archivado y ya no se muestra en la tienda.");
        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/{id}/reactivar")
    public String reactivar(@PathVariable String id, RedirectAttributes redirect) {
        productoServicio.cambiarActivo(id, true);
        redirect.addFlashAttribute("mensaje", "El producto volvió a la tienda.");
        return "redirect:/admin/productos";
    }

    @GetMapping("/ventas")
    public String ventas(@RequestParam(required = false) EstadoVenta estado, Model model) {
        model.addAttribute("ventas", ventaServicio.listar(estado));
        model.addAttribute("estado", estado);
        model.addAttribute("estados", EstadoVenta.values());
        return "admin/ventas";
    }

    @GetMapping("/ventas/nueva")
    public String nuevaVenta(Model model) {
        if (!model.containsAttribute("formulario")) {
            VentaFormulario formulario = new VentaFormulario();
            formulario.setMetodoPago(MetodoPago.EFECTIVO);
            formulario.setLineas(lineasVacias());
            model.addAttribute("formulario", formulario);
        }
        cargarVenta(model);
        return "admin/venta-form";
    }

    @PostMapping("/ventas")
    public String registrarVenta(@Valid @ModelAttribute("formulario") VentaFormulario formulario,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirect) {
        completarLineas(formulario);
        if (result.hasErrors()) {
            cargarVenta(model);
            return "admin/venta-form";
        }
        try {
            Venta venta = ventaServicio.registrar(aSolicitud(formulario));
            redirect.addFlashAttribute("mensaje", "Venta registrada: " + venta.getNumero() + ".");
            return "redirect:/admin/ventas/" + venta.getId();
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            cargarVenta(model);
            return "admin/venta-form";
        }
    }

    @GetMapping("/ventas/{id}")
    public String detalleVenta(@PathVariable String id, Model model) {
        Venta venta = ventaServicio.porId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("venta", venta);
        return "admin/venta-detalle";
    }

    @PostMapping("/ventas/{id}/estado")
    public String estado(@PathVariable String id,
                         @RequestParam EstadoVenta estado,
                         RedirectAttributes redirect) {
        try {
            ventaServicio.cambiarEstado(id, estado);
            redirect.addFlashAttribute("mensaje", "El estado pasó a " + estado.getEtiqueta().toLowerCase() + ".");
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/ventas/" + id;
    }

    private void cargarProducto(Model model) {
        model.addAttribute("secciones", Categoria.values());
        model.addAttribute("portadas", Portadas.TODAS);
    }

    private void cargarVenta(Model model) {
        model.addAttribute("productos", productoServicio.conStock());
        model.addAttribute("metodos", MetodoPago.values());
    }

    private List<LineaFormulario> lineasVacias() {
        List<LineaFormulario> lineas = new ArrayList<>();
        for (int i = 0; i < LINEAS; i++) {
            LineaFormulario linea = new LineaFormulario();
            linea.setCantidad(i == 0 ? 1 : 0);
            lineas.add(linea);
        }
        return lineas;
    }

    private void completarLineas(VentaFormulario formulario) {
        if (formulario.getLineas() == null) {
            formulario.setLineas(new ArrayList<>());
        }
        while (formulario.getLineas().size() < LINEAS) {
            LineaFormulario linea = new LineaFormulario();
            linea.setCantidad(0);
            formulario.getLineas().add(linea);
        }
    }

    private SolicitudVenta aSolicitud(VentaFormulario formulario) {
        List<SolicitudVenta.Item> items = formulario.getLineas().stream()
                .map(linea -> new SolicitudVenta.Item(
                        linea.getProductoId(),
                        linea.getCantidad() == null ? 0 : linea.getCantidad()))
                .toList();
        return new SolicitudVenta(
                formulario.getClienteNombre(),
                formulario.getClienteEmail(),
                formulario.getClienteTelefono(),
                formulario.getDocumento(),
                formulario.getDireccion(),
                formulario.getCiudad(),
                formulario.getMetodoPago(),
                OrigenVenta.ADMIN,
                formulario.getNotas(),
                items
        );
    }
}
