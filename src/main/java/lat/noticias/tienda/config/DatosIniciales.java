package lat.noticias.tienda.config;

import lat.noticias.tienda.modelo.Categoria;
import lat.noticias.tienda.modelo.EstadoVenta;
import lat.noticias.tienda.modelo.MetodoPago;
import lat.noticias.tienda.modelo.OrigenVenta;
import lat.noticias.tienda.modelo.Producto;
import lat.noticias.tienda.modelo.Usuario;
import lat.noticias.tienda.modelo.Venta;
import lat.noticias.tienda.repositorio.ProductoRepositorio;
import lat.noticias.tienda.repositorio.UsuarioRepositorio;
import lat.noticias.tienda.repositorio.VentaRepositorio;
import lat.noticias.tienda.servicio.SolicitudVenta;
import lat.noticias.tienda.servicio.Textos;
import lat.noticias.tienda.servicio.VentaServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final ProductoRepositorio productos;
    private final VentaRepositorio ventas;
    private final UsuarioRepositorio usuarios;
    private final VentaServicio ventaServicio;
    private final PasswordEncoder passwordEncoder;

    @Value("${tienda.admin.usuario}")
    private String usuarioAdmin;

    @Value("${tienda.admin.clave}")
    private String claveAdmin;

    @Value("${tienda.admin.nombre}")
    private String nombreAdmin;

    public DatosIniciales(ProductoRepositorio productos,
                          VentaRepositorio ventas,
                          UsuarioRepositorio usuarios,
                          VentaServicio ventaServicio,
                          PasswordEncoder passwordEncoder) {
        this.productos = productos;
        this.ventas = ventas;
        this.usuarios = usuarios;
        this.ventaServicio = ventaServicio;
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
        if (productos.count() == 0) {
            cargarCatalogo();
            cargarVentasDeMuestra();
        }
        log.info("Tienda lista en http://localhost:8080 — ingreso de redacción: {}", usuarioAdmin);
    }

    private void cargarCatalogo() {
        crear("NT-SUB-DIG", "Suscripción digital anual",
                "Un año de cobertura latinoamericana, sin publicidad y con archivo completo.",
                "Doce meses de acceso a la edición digital de Noticias.lat: portada diaria, archivo de crónicas y los envíos de la redacción. Pensada para leer con calma, sin anuncios que corten el texto.",
                "49.00", "72.00", 500, Categoria.SUSCRIPCION, "portada-01", true);
        crear("NT-SUB-PAP", "Suscripción papel + digital",
                "La revista impresa del mes y el archivo digital abierto todo el año.",
                "La edición en papel llega una vez por mes y la digital queda disponible los doce meses. Es el formato para quien quiere el objeto del domingo y el archivo en el teléfono.",
                "89.00", "110.00", 200, Categoria.SUSCRIPCION, "portada-07", false);
        crear("NT-EDI-MES", "Edición impresa del mes",
                "La revista de octubre, en papel obra, con el cuadernillo central de crónicas.",
                "Una tirada corta de la edición del mes. Incluye la tapa, el sumario y el cuadernillo que no se publica completo en la web. Se envía mientras dure el stock.",
                "7.50", null, 80, Categoria.EDICION, "portada-07", false);
        crear("NT-ANU-2026", "Anuario 2026",
                "Las piezas que marcaron el año, editadas para guardar.",
                "Una selección de crónicas, cronologías y mapas de América Latina. No es un volcado de la home: cada texto fue revisado para la página impresa y encuadernado como libro de año.",
                "24.00", "29.00", 60, Categoria.EDICION, "portada-08", true);
        crear("NT-LIB-CRO", "Crónicas de un continente",
                "Ocho crónicas de viaje y ciudad, reescritas para el libro.",
                "Un volumen breve. Cada texto salió de una cobertura y después se volvió a escribir para la página, con el ritmo de un libro y no el de una nota del día.",
                "18.90", null, 40, Categoria.LIBRO, "portada-01", true);
        crear("NT-LIB-VER", "El oficio de verificar",
                "Una guía práctica para contrastar datos, imágenes y declaraciones.",
                "Sirve en la redacción y en el aula. Recorre fuentes, imágenes, cifras y el momento en que una pieza todavía no está lista para publicarse.",
                "22.00", null, 35, Categoria.LIBRO, "portada-03", false);
        crear("NT-MER-TAZ", "Taza de la redacción",
                "Cerámica esmaltada en blanco hueso, con el sello de la tienda.",
                "Apta para lavavajillas. Capacidad de 300 ml. El sello va en rojo, del lado que mira a quien toma el café de cierre.",
                "14.00", null, 50, Categoria.MERCHANDISING, "portada-06", false);
        crear("NT-MER-REM", "Remera Primera plana",
                "Algodón peinado y una estampa tipográfica en una sola tinta.",
                "Corte recto. La frase es la que abre cada edición de la casa: contar con precisión. Se lava al revés, con agua fría.",
                "28.00", "34.00", 30, Categoria.MERCHANDISING, "portada-05", true);
        crear("NT-MER-BOL", "Bolso de corresponsal",
                "Lona de algodón, base reforzada y bolsillo para el cuaderno.",
                "Entra una laptop de 14 pulgadas y un libro de tapa blanda. La correa es larga para cruzarla cuando se cubre una calle.",
                "36.00", null, 25, Categoria.MERCHANDISING, "portada-04", false);
        crear("NT-MER-CUA", "Cuaderno de campo",
                "96 páginas rayadas, tapa flexible color tinta y elástico de cierre.",
                "El formato entra en el bolso y en el abrigo. La primera página queda en blanco para el nombre y la cobertura.",
                "12.50", null, 70, Categoria.MERCHANDISING, "portada-04", false);
        crear("NT-EXP-TAL", "Taller de crónica",
                "Tres horas con un editor de la casa. Cupo limitado.",
                "El registro reserva el lugar. Después de la venta, la redacción coordina día y horario por correo. No es una clase grabada: es una mesa chica.",
                "45.00", null, 18, Categoria.EXPERIENCIA, "portada-05", false);
        crear("NT-EXP-VIS", "Visita a la redacción",
                "Una hora por el archivo, la mesa de cierre y la imprenta asociada.",
                "Grupo reducido. La fecha se coordina después del registro. El recorrido muestra cómo se arma una edición, no una escenografía.",
                "20.00", null, 12, Categoria.EXPERIENCIA, "portada-02", false);
    }

    private void crear(String sku, String nombre, String resumen, String descripcion, String precio,
                       String precioLista, int stock, Categoria categoria, String imagen, boolean destacado) {
        Producto producto = new Producto();
        producto.setSku(sku);
        producto.setNombre(nombre);
        producto.setSlug(Textos.slug(nombre));
        producto.setResumen(resumen);
        producto.setDescripcion(descripcion);
        producto.setPrecio(new BigDecimal(precio));
        if (precioLista != null) {
            producto.setPrecioLista(new BigDecimal(precioLista));
        }
        producto.setStock(stock);
        producto.setCategoria(categoria);
        producto.setImagen(imagen);
        producto.setDestacado(destacado);
        producto.setActivo(true);
        LocalDateTime ahora = LocalDateTime.now();
        producto.setCreadoEn(ahora);
        producto.setActualizadoEn(ahora);
        productos.save(producto);
    }

    private void cargarVentasDeMuestra() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime ayer = ahora.minusDays(1);
        if (ayer.getMonth() != ahora.getMonth()) {
            ayer = ahora.minusHours(5);
        }
        vender("Ana López", "ana.lopez@correo.test", "11 5555 0101", "CABA",
                List.of(item("NT-SUB-DIG", 1), item("NT-MER-CUA", 1)),
                MetodoPago.TRANSFERENCIA, OrigenVenta.WEB, EstadoVenta.EN_PREPARACION, ahora.minusHours(2),
                "Dejar en portería si no hay nadie.");
        vender("Mostrador de la redacción", null, null, "Redacción",
                List.of(item("NT-MER-REM", 2)),
                MetodoPago.EFECTIVO, OrigenVenta.ADMIN, EstadoVenta.REGISTRADA, ahora.minusHours(1),
                "Retiro en el momento.");
        vender("Lucía Ferreira", "lucia.ferreira@correo.test", "099 555 202", "Montevideo",
                List.of(item("NT-LIB-CRO", 1), item("NT-MER-TAZ", 1)),
                MetodoPago.TARJETA, OrigenVenta.WEB, EstadoVenta.ENTREGADA, ayer,
                null);
    }

    private SolicitudVenta.Item item(String sku, int cantidad) {
        Producto producto = productos.findBySkuIgnoreCase(sku)
                .orElseThrow(() -> new IllegalStateException("Falta el producto " + sku));
        return new SolicitudVenta.Item(producto.getId(), cantidad);
    }

    private void vender(String nombre, String email, String telefono, String ciudad,
                        List<SolicitudVenta.Item> items, MetodoPago metodo, OrigenVenta origen,
                        EstadoVenta estado, LocalDateTime fecha, String notas) {
        Venta venta = ventaServicio.registrar(new SolicitudVenta(
                nombre, email, telefono, null, null, ciudad, metodo, origen, notas, items));
        venta.setEstado(estado);
        venta.setFecha(fecha);
        ventas.save(venta);
    }
}
