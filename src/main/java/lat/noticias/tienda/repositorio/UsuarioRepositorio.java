package lat.noticias.tienda.repositorio;

import lat.noticias.tienda.modelo.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UsuarioRepositorio extends MongoRepository<Usuario, String> {

    Optional<Usuario> findByUsername(String username);
}
