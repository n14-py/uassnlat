package lat.noticias.tienda.repositorio;

import lat.noticias.tienda.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductoRepositorio extends JpaRepository<Producto, Long> {

    Optional<Producto> findBySkuIgnoreCase(String sku);
}
