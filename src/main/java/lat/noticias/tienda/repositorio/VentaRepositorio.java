package lat.noticias.tienda.repositorio;

import lat.noticias.tienda.modelo.EstadoVenta;
import lat.noticias.tienda.modelo.Venta;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface VentaRepositorio extends MongoRepository<Venta, String> {

    Optional<Venta> findByNumero(String numero);

    List<Venta> findByEstado(EstadoVenta estado, Sort sort);
}
