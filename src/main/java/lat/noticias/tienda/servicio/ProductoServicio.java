package lat.noticias.tienda.servicio;

import lat.noticias.tienda.modelo.Categoria;
import lat.noticias.tienda.modelo.Portadas;
import lat.noticias.tienda.modelo.Producto;
import lat.noticias.tienda.repositorio.ProductoRepositorio;
import lat.noticias.tienda.web.formulario.ProductoFormulario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class ProductoServicio {

    private final ProductoRepositorio productos;
    private final MongoTemplate mongoTemplate;

    public ProductoServicio(ProductoRepositorio productos, MongoTemplate mongoTemplate) {
        this.productos = productos;
        this.mongoTemplate = mongoTemplate;
    }

    public Page<Producto> buscar(String texto, Categoria categoria, String orden, int pagina, int tamano) {
        List<Criteria> partes = new ArrayList<>();
        partes.add(Criteria.where("activo").is(true));
        if (categoria != null) {
            partes.add(Criteria.where("categoria").is(categoria));
        }
        if (texto != null && !texto.isBlank()) {
            String regex = Pattern.quote(texto.trim());
            partes.add(new Criteria().orOperator(
                    Criteria.where("nombre").regex(regex, "i"),
                    Criteria.where("resumen").regex(regex, "i"),
                    Criteria.where("sku").regex(regex, "i")
            ));
        }
        Criteria criterio = new Criteria().andOperator(partes.toArray(Criteria[]::new));
        Sort ordenacion = ordenar(orden);
        long total = mongoTemplate.count(new Query(criterio), Producto.class);
        int paginas = tamano == 0 ? 0 : (int) Math.ceil((double) total / tamano);
        int paginaSegura = Math.max(pagina, 0);
        if (paginas > 0 && paginaSegura >= paginas) {
            paginaSegura = paginas - 1;
        }
        Pageable pageable = PageRequest.of(paginaSegura, tamano, ordenacion);
        Query consulta = new Query(criterio).with(pageable);
        List<Producto> lista = mongoTemplate.find(consulta, Producto.class);
        return new PageImpl<>(lista, pageable, total);
    }

    public List<Producto> destacados(int limite) {
        Query consulta = new Query(Criteria.where("activo").is(true).and("destacado").is(true))
                .with(Sort.by(Sort.Direction.ASC, "nombre"))
                .limit(limite);
        List<Producto> lista = mongoTemplate.find(consulta, Producto.class);
        if (!lista.isEmpty()) {
            return lista;
        }
        return recientes(limite);
    }

    public List<Producto> recientes(int limite) {
        Query consulta = new Query(Criteria.where("activo").is(true))
                .with(Sort.by(Sort.Direction.DESC, "creadoEn"))
                .limit(limite);
        return mongoTemplate.find(consulta, Producto.class);
    }

    public List<Producto> relacionados(Producto producto, int limite) {
        Query consulta = new Query(Criteria.where("activo").is(true)
                .and("categoria").is(producto.getCategoria())
                .and("_id").ne(producto.getId()))
                .limit(limite);
        return mongoTemplate.find(consulta, Producto.class);
    }

    public Optional<Producto> porSlug(String slug) {
        return productos.findBySlug(slug);
    }

    public Optional<Producto> porId(String id) {
        return productos.findById(id);
    }

    public Optional<Producto> porSku(String sku) {
        return productos.findBySkuIgnoreCase(sku);
    }

    public List<Producto> conStock() {
        return productos.findByActivoTrueAndStockGreaterThanOrderByNombreAsc(0);
    }

    public List<Producto> listarGestion(String texto, String filtro) {
        List<Criteria> partes = new ArrayList<>();
        if ("activos".equals(filtro)) {
            partes.add(Criteria.where("activo").is(true));
        } else if ("archivados".equals(filtro)) {
            partes.add(Criteria.where("activo").is(false));
        }
        if (texto != null && !texto.isBlank()) {
            String regex = Pattern.quote(texto.trim());
            partes.add(new Criteria().orOperator(
                    Criteria.where("nombre").regex(regex, "i"),
                    Criteria.where("sku").regex(regex, "i")
            ));
        }
        Query consulta = new Query();
        if (!partes.isEmpty()) {
            consulta.addCriteria(new Criteria().andOperator(partes.toArray(Criteria[]::new)));
        }
        consulta.with(Sort.by(Sort.Direction.ASC, "nombre"));
        return mongoTemplate.find(consulta, Producto.class);
    }

    public Map<Categoria, Long> contarPorCategoria() {
        Map<Categoria, Long> conteo = new EnumMap<>(Categoria.class);
        for (Categoria categoria : Categoria.values()) {
            conteo.put(categoria, productos.countByCategoriaAndActivoTrue(categoria));
        }
        return conteo;
    }

    public long contarActivos() {
        return productos.countByActivoTrue();
    }

    public List<Producto> stockBajo(int umbral) {
        return productos.findByActivoTrueAndStockLessThanEqualOrderByStockAsc(umbral);
    }

    public Producto guardar(ProductoFormulario formulario) {
        if (!Portadas.existe(formulario.getImagen())) {
            throw new ReglaNegocioException("Elegí una portada de la galería.");
        }
        if (formulario.getPrecioLista() != null
                && formulario.getPrecioLista().compareTo(formulario.getPrecio()) <= 0) {
            throw new ReglaNegocioException("El precio de lista tiene que ser mayor que el precio de venta, o quedar vacío.");
        }

        String sku = formulario.getSku().trim().toUpperCase(Locale.ROOT);
        Producto producto;
        if (formulario.getId() == null || formulario.getId().isBlank()) {
            producto = new Producto();
            producto.setCreadoEn(LocalDateTime.now());
        } else {
            producto = productos.findById(formulario.getId())
                    .orElseThrow(() -> new ReglaNegocioException("No encontramos el producto para editar."));
        }

        productos.findBySkuIgnoreCase(sku).ifPresent(existente -> {
            if (producto.getId() == null || !existente.getId().equals(producto.getId())) {
                throw new ReglaNegocioException("Ya existe un producto con el SKU " + sku + ".");
            }
        });

        producto.setSku(sku);
        producto.setNombre(formulario.getNombre().trim());
        producto.setResumen(formulario.getResumen().trim());
        producto.setDescripcion(formulario.getDescripcion().trim());
        producto.setPrecio(CalculadoraVenta.dinero(formulario.getPrecio()));
        producto.setPrecioLista(formulario.getPrecioLista() == null ? null : CalculadoraVenta.dinero(formulario.getPrecioLista()));
        producto.setStock(formulario.getStock());
        producto.setCategoria(formulario.getCategoria());
        producto.setImagen(formulario.getImagen());
        producto.setDestacado(formulario.isDestacado());
        producto.setActivo(formulario.isActivo());
        producto.setSlug(slugUnico(producto.getNombre(), producto.getId()));
        producto.setActualizadoEn(LocalDateTime.now());
        return productos.save(producto);
    }

    public void cambiarActivo(String id, boolean activo) {
        Producto producto = productos.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("No encontramos el producto."));
        producto.setActivo(activo);
        producto.setActualizadoEn(LocalDateTime.now());
        productos.save(producto);
    }

    private String slugUnico(String nombre, String idActual) {
        String base = Textos.slug(nombre);
        if (base.isBlank()) {
            base = "producto";
        }
        String candidato = base;
        int sufijo = 2;
        while (true) {
            Optional<Producto> existente = productos.findBySlug(candidato);
            if (existente.isEmpty() || existente.get().getId().equals(idActual)) {
                return candidato;
            }
            candidato = base + "-" + sufijo;
            sufijo++;
        }
    }

    private Sort ordenar(String orden) {
        return switch (orden == null ? "" : orden) {
            case "precio_asc" -> Sort.by(Sort.Direction.ASC, "precio");
            case "precio_desc" -> Sort.by(Sort.Direction.DESC, "precio");
            case "nombre" -> Sort.by(Sort.Direction.ASC, "nombre");
            case "recientes" -> Sort.by(Sort.Direction.DESC, "creadoEn");
            default -> Sort.by(Sort.Order.desc("destacado"), Sort.Order.asc("nombre"));
        };
    }
}
