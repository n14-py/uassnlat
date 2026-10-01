package lat.noticias.tienda.servicio;

import lat.noticias.tienda.modelo.Contador;
import lat.noticias.tienda.modelo.EstadoVenta;
import lat.noticias.tienda.modelo.LineaVenta;
import lat.noticias.tienda.modelo.Producto;
import lat.noticias.tienda.modelo.Venta;
import lat.noticias.tienda.repositorio.ProductoRepositorio;
import lat.noticias.tienda.repositorio.VentaRepositorio;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VentaServicio {

    private final VentaRepositorio ventas;
    private final ProductoRepositorio productos;
    private final MongoTemplate mongoTemplate;

    public VentaServicio(VentaRepositorio ventas, ProductoRepositorio productos, MongoTemplate mongoTemplate) {
        this.ventas = ventas;
        this.productos = productos;
        this.mongoTemplate = mongoTemplate;
    }

    public synchronized Venta registrar(SolicitudVenta solicitud) {
        if (solicitud.items() == null || solicitud.items().isEmpty()) {
            throw new ReglaNegocioException("La venta no tiene productos.");
        }
        if (solicitud.clienteNombre() == null || solicitud.clienteNombre().isBlank()) {
            throw new ReglaNegocioException("El nombre del cliente es obligatorio.");
        }
        if (solicitud.metodoPago() == null) {
            throw new ReglaNegocioException("Elegí un medio de pago.");
        }

        List<LineaPreparada> preparadas = new ArrayList<>();
        for (SolicitudVenta.Item item : solicitud.items()) {
            if (item.productoId() == null || item.productoId().isBlank() || item.cantidad() <= 0) {
                continue;
            }
            if (item.cantidad() > 20) {
                throw new ReglaNegocioException("El máximo por producto es 20 unidades.");
            }
            Producto producto = productos.findById(item.productoId())
                    .orElseThrow(() -> new ReglaNegocioException("Hay un producto que ya no existe."));
            if (!producto.isActivo()) {
                throw new ReglaNegocioException(producto.getNombre() + " está archivado y no se puede vender.");
            }
            if (producto.getStock() < item.cantidad()) {
                throw new ReglaNegocioException("No hay stock suficiente de " + producto.getNombre() + ".");
            }
            preparadas.add(new LineaPreparada(producto, item.cantidad()));
        }
        if (preparadas.isEmpty()) {
            throw new ReglaNegocioException("Agregá al menos un producto con cantidad mayor a cero.");
        }

        List<Producto> tocados = new ArrayList<>();
        List<Integer> stocksAnteriores = new ArrayList<>();
        try {
            List<LineaVenta> lineas = new ArrayList<>();
            List<BigDecimal> subtotales = new ArrayList<>();
            for (LineaPreparada preparada : preparadas) {
                Producto producto = preparada.producto();
                stocksAnteriores.add(producto.getStock());
                producto.setStock(producto.getStock() - preparada.cantidad());
                producto.setActualizadoEn(LocalDateTime.now());
                productos.save(producto);
                tocados.add(producto);

                BigDecimal subtotal = CalculadoraVenta.linea(producto.getPrecio(), preparada.cantidad());
                LineaVenta linea = new LineaVenta();
                linea.setProductoId(producto.getId());
                linea.setSku(producto.getSku());
                linea.setNombre(producto.getNombre());
                linea.setImagen(producto.getImagen());
                linea.setPrecioUnitario(producto.getPrecio());
                linea.setCantidad(preparada.cantidad());
                linea.setSubtotal(subtotal);
                lineas.add(linea);
                subtotales.add(subtotal);
            }

            Venta venta = new Venta();
            venta.setNumero(siguienteNumero());
            venta.setFecha(LocalDateTime.now());
            venta.setClienteNombre(solicitud.clienteNombre().trim());
            venta.setClienteEmail(blanco(solicitud.clienteEmail()));
            venta.setClienteTelefono(blanco(solicitud.clienteTelefono()));
            venta.setDocumento(blanco(solicitud.documento()));
            venta.setDireccion(blanco(solicitud.direccion()));
            venta.setCiudad(blanco(solicitud.ciudad()));
            venta.setLineas(lineas);
            venta.setTotal(CalculadoraVenta.total(subtotales));
            venta.setMetodoPago(solicitud.metodoPago());
            venta.setEstado(EstadoVenta.REGISTRADA);
            venta.setOrigen(solicitud.origen());
            venta.setNotas(blanco(solicitud.notas()));
            venta.setStockReintegrado(false);
            return ventas.save(venta);
        } catch (RuntimeException ex) {
            for (int i = 0; i < tocados.size(); i++) {
                Producto producto = tocados.get(i);
                producto.setStock(stocksAnteriores.get(i));
                productos.save(producto);
            }
            if (ex instanceof ReglaNegocioException regla) {
                throw regla;
            }
            throw new ReglaNegocioException("No se pudo registrar la venta. El stock no fue descontado.");
        }
    }

    public synchronized Venta cambiarEstado(String id, EstadoVenta nuevo) {
        Venta venta = ventas.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("No encontramos la venta."));
        if (venta.getEstado() == nuevo) {
            return venta;
        }
        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new ReglaNegocioException("Una venta cancelada no se puede reabrir.");
        }
        if (venta.getEstado() == EstadoVenta.ENTREGADA) {
            throw new ReglaNegocioException("Una venta entregada queda cerrada.");
        }
        if (nuevo == EstadoVenta.CANCELADA) {
            reintegrarStock(venta);
            venta.setEstado(EstadoVenta.CANCELADA);
            return ventas.save(venta);
        }
        if (venta.getEstado() == EstadoVenta.REGISTRADA && nuevo == EstadoVenta.EN_PREPARACION) {
            venta.setEstado(nuevo);
            return ventas.save(venta);
        }
        if (venta.getEstado() == EstadoVenta.EN_PREPARACION && nuevo == EstadoVenta.ENTREGADA) {
            venta.setEstado(nuevo);
            return ventas.save(venta);
        }
        throw new ReglaNegocioException("Ese cambio de estado no está permitido.");
    }

    public List<Venta> listar(EstadoVenta estado) {
        Sort orden = Sort.by(Sort.Direction.DESC, "fecha");
        if (estado == null) {
            return ventas.findAll(orden);
        }
        return ventas.findByEstado(estado, orden);
    }

    public Optional<Venta> porId(String id) {
        return ventas.findById(id);
    }

    public Optional<Venta> porNumero(String numero) {
        return ventas.findByNumero(numero);
    }

    public ResumenTablero resumen() {
        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
        LocalDateTime finHoy = inicioHoy.plusDays(1);
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<Venta> hoy = vigentesEntre(inicioHoy, finHoy);
        List<Venta> mes = vigentesEntre(inicioMes, finHoy);
        BigDecimal montoHoy = sumar(hoy);
        BigDecimal montoMes = sumar(mes);
        BigDecimal ticket = mes.isEmpty()
                ? BigDecimal.ZERO.setScale(2)
                : montoMes.divide(BigDecimal.valueOf(mes.size()), 2, java.math.RoundingMode.HALF_UP);
        List<Venta> recientes = ventas.findAll(PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "fecha"))).getContent();
        return new ResumenTablero(
                hoy.size(),
                montoHoy,
                mes.size(),
                montoMes,
                ticket,
                productos.countByActivoTrue(),
                productos.findByActivoTrueAndStockLessThanEqualOrderByStockAsc(5),
                recientes
        );
    }

    private void reintegrarStock(Venta venta) {
        if (venta.isStockReintegrado()) {
            return;
        }
        for (LineaVenta linea : venta.getLineas()) {
            productos.findById(linea.getProductoId()).ifPresent(producto -> {
                producto.setStock(producto.getStock() + linea.getCantidad());
                producto.setActualizadoEn(LocalDateTime.now());
                productos.save(producto);
            });
        }
        venta.setStockReintegrado(true);
    }

    private List<Venta> vigentesEntre(LocalDateTime desde, LocalDateTime hasta) {
        Query consulta = new Query(Criteria.where("fecha").gte(desde).lt(hasta)
                .and("estado").ne(EstadoVenta.CANCELADA));
        return mongoTemplate.find(consulta, Venta.class);
    }

    private BigDecimal sumar(List<Venta> lista) {
        return CalculadoraVenta.total(lista.stream().map(Venta::getTotal).toList());
    }

    private String siguienteNumero() {
        Query consulta = new Query(Criteria.where("_id").is("venta"));
        Update actualizacion = new Update().inc("valor", 1);
        FindAndModifyOptions opciones = FindAndModifyOptions.options().upsert(true).returnNew(true);
        Contador contador = mongoTemplate.findAndModify(consulta, actualizacion, opciones, Contador.class);
        if (contador == null) {
            throw new ReglaNegocioException("No se pudo generar el número de venta.");
        }
        return "NT-" + LocalDate.now().getYear() + "-" + String.format("%05d", contador.getValor());
    }

    private String blanco(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    private record LineaPreparada(Producto producto, int cantidad) {
    }
}
