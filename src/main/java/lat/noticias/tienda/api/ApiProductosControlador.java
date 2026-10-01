package lat.noticias.tienda.api;

import jakarta.validation.Valid;
import lat.noticias.tienda.modelo.Categoria;
import lat.noticias.tienda.modelo.Producto;
import lat.noticias.tienda.servicio.ProductoServicio;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiProductosControlador {

    private final ProductoServicio productoServicio;

    public ApiProductosControlador(ProductoServicio productoServicio) {
        this.productoServicio = productoServicio;
    }

    @GetMapping("/categorias")
    public List<Map<String, String>> categorias() {
        return Arrays.stream(Categoria.values())
                .map(categoria -> Map.of(
                        "id", categoria.name(),
                        "nombre", categoria.getNombre(),
                        "descripcion", categoria.getDescripcion()))
                .toList();
    }

    @GetMapping("/productos")
    public List<ProductoJson> listar() {
        return productoServicio.buscar(null, null, "nombre", 0, 200)
                .getContent()
                .stream()
                .map(ProductoJson::de)
                .toList();
    }

    @GetMapping("/productos/{id}")
    public ProductoJson uno(@PathVariable String id) {
        return ProductoJson.de(buscar(id));
    }

    @GetMapping("/admin/productos")
    public List<ProductoJson> listarAdmin() {
        return productoServicio.listarGestion(null, "todos").stream().map(ProductoJson::de).toList();
    }

    @PostMapping("/admin/productos")
    public ProductoJson crear(@Valid @RequestBody ProductoAlta alta) {
        return ProductoJson.de(productoServicio.guardar(alta.aFormulario(null)));
    }

    @PutMapping("/admin/productos/{id}")
    public ProductoJson actualizar(@PathVariable String id, @Valid @RequestBody ProductoAlta alta) {
        buscar(id);
        return ProductoJson.de(productoServicio.guardar(alta.aFormulario(id)));
    }

    private Producto buscar(String id) {
        return productoServicio.porId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }
}
