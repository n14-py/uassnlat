package lat.noticias.tienda.api;

import jakarta.validation.Valid;
import lat.noticias.tienda.servicio.ProductoServicio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/productos", "/api/admin/productos"})
public class ProductoControlador {

    private final ProductoServicio servicio;

    public ProductoControlador(ProductoServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<ProductoJson> crear(@Valid @RequestBody ProductoAlta alta) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductoJson.de(servicio.guardar(alta)));
    }

    @GetMapping
    public ResponseEntity<List<ProductoJson>> listar() {
        return ResponseEntity.ok(servicio.listarTodos().stream().map(ProductoJson::de).toList());
    }

    @GetMapping("/{id}")
    public ProductoJson obtener(@PathVariable Long id) {
        return servicio.buscarPorId(id)
                .map(ProductoJson::de)
                .orElseThrow(ProductoNoEncontrado::new);
    }

    @PutMapping("/{id}")
    public ProductoJson actualizar(@PathVariable Long id, @Valid @RequestBody ProductoAlta alta) {
        return servicio.actualizar(id, alta)
                .map(ProductoJson::de)
                .orElseThrow(ProductoNoEncontrado::new);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        if (!servicio.eliminar(id)) {
            throw new ProductoNoEncontrado();
        }
    }
}
