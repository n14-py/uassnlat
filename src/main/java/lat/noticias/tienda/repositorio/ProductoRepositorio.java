package lat.noticias.tienda.repositorio;

import lat.noticias.tienda.modelo.Categoria;
import lat.noticias.tienda.modelo.Producto;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepositorio extends MongoRepository<Producto, String> {

    Optional<Producto> findBySlug(String slug);

    Optional<Producto> findBySkuIgnoreCase(String sku);

    long countByActivoTrue();

    long countByCategoriaAndActivoTrue(Categoria categoria);

    List<Producto> findByActivoTrueAndStockLessThanEqualOrderByStockAsc(int stock);

    List<Producto> findByActivoTrueAndStockGreaterThanOrderByNombreAsc(int stock);
}
