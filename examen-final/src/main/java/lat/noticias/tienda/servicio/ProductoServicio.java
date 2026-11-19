package lat.noticias.tienda.servicio;

import lat.noticias.tienda.api.ProductoAlta;
import lat.noticias.tienda.modelo.Producto;
import lat.noticias.tienda.repositorio.ProductoRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoServicio {

    private final ProductoRepositorio repositorio;

    public ProductoServicio(ProductoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Producto guardar(ProductoAlta datos) {
        exigirSkuLibre(datos.getSku(), null);
        Producto producto = new Producto();
        datos.copiarEn(producto);
        return repositorio.save(producto);
    }

    public List<Producto> listarTodos() {
        return repositorio.findAll();
    }

    public Optional<Producto> buscarPorId(Long id) {
        return repositorio.findById(id);
    }

    public Optional<Producto> actualizar(Long id, ProductoAlta datos) {
        return repositorio.findById(id).map(existente -> {
            exigirSkuLibre(datos.getSku(), existente.getId());
            datos.copiarEn(existente);
            return repositorio.save(existente);
        });
    }

    public boolean eliminar(Long id) {
        if (!repositorio.existsById(id)) {
            return false;
        }
        repositorio.deleteById(id);
        return true;
    }

    private void exigirSkuLibre(String sku, Long idActual) {
        repositorio.findBySkuIgnoreCase(sku.trim()).ifPresent(existente -> {
            if (idActual == null || !existente.getId().equals(idActual)) {
                throw new ReglaNegocioException("Ya existe un producto con el SKU " + sku.trim().toUpperCase() + ".");
            }
        });
    }
}
