package lat.noticias.tienda.config;

import lat.noticias.tienda.modelo.Usuario;
import lat.noticias.tienda.repositorio.UsuarioRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final UsuarioRepositorio usuarios;
    private final PasswordEncoder passwordEncoder;

    @Value("${tienda.admin.usuario}")
    private String usuarioAdmin;

    @Value("${tienda.admin.clave}")
    private String claveAdmin;

    @Value("${tienda.admin.nombre}")
    private String nombreAdmin;

    public DatosIniciales(UsuarioRepositorio usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarios.findByUsername(usuarioAdmin).isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setUsername(usuarioAdmin);
            usuario.setPassword(passwordEncoder.encode(claveAdmin));
            usuario.setNombre(nombreAdmin);
            usuario.setRol("ADMIN");
            usuarios.save(usuario);
        }
        log.info("Tienda lista en http://localhost:8080 — ingreso de redacción: {}", usuarioAdmin);
    }
}
