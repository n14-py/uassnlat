package lat.noticias.tienda.api;

import jakarta.validation.Valid;
import lat.noticias.tienda.servicio.VentaServicio;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ventas")
public class ApiVentasControlador {

    private final VentaServicio ventaServicio;

    public ApiVentasControlador(VentaServicio ventaServicio) {
        this.ventaServicio = ventaServicio;
    }

    @GetMapping
    public List<VentaJson> listar() {
        return ventaServicio.listar(null).stream().map(VentaJson::de).toList();
    }

    @GetMapping("/{id}")
    public VentaJson una(@PathVariable String id) {
        return ventaServicio.porId(id)
                .map(VentaJson::de)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada"));
    }

    @PostMapping
    public VentaJson crear(@Valid @RequestBody VentaAlta alta) {
        return VentaJson.de(ventaServicio.registrar(alta.aSolicitud()));
    }
}
