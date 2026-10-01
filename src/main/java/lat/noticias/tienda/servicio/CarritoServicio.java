package lat.noticias.tienda.servicio;

import lat.noticias.tienda.modelo.Carrito;
import lat.noticias.tienda.modelo.LineaCarrito;
import lat.noticias.tienda.modelo.Producto;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class CarritoServicio {

    static final String CLAVE = "CARRITO_NOTICIAS";
    private static final int MAXIMO = 20;

    private final ProductoServicio productos;

    public CarritoServicio(ProductoServicio productos) {
        this.productos = productos;
    }

    public Carrito obtener(HttpSession session) {
        Object guardado = session.getAttribute(CLAVE);
        if (guardado instanceof Carrito carrito) {
            return carrito;
        }
        Carrito carrito = new Carrito();
        session.setAttribute(CLAVE, carrito);
        return carrito;
    }

    public int cantidad(HttpSession session) {
        return obtener(session).getCantidadTotal();
    }

    public void agregar(HttpSession session, String productoId, int cantidad) {
        if (cantidad < 1) {
            throw new ReglaNegocioException("La cantidad tiene que ser al menos 1.");
        }
        Producto producto = productoVigente(productoId);
        Carrito carrito = obtener(session);
        if (carrito.getLineas().size() >= 30 && carrito.getLineas().stream().noneMatch(l -> l.getProductoId().equals(producto.getId()))) {
            throw new ReglaNegocioException("El carrito llegó al máximo de 30 productos distintos.");
        }
        LineaCarrito linea = buscar(carrito, producto.getId()).orElseGet(() -> {
            LineaCarrito nueva = new LineaCarrito();
            nueva.setProductoId(producto.getId());
            nueva.setCantidad(0);
            carrito.getLineas().add(nueva);
            return nueva;
        });
        int total = linea.getCantidad() + cantidad;
        if (total > MAXIMO) {
            throw new ReglaNegocioException("El máximo por producto es 20 unidades.");
        }
        if (total > producto.getStock()) {
            throw new ReglaNegocioException("No hay stock suficiente de " + producto.getNombre() + ".");
        }
        copiar(linea, producto, total);
        session.setAttribute(CLAVE, carrito);
    }

    public void actualizar(HttpSession session, String productoId, int cantidad) {
        Carrito carrito = obtener(session);
        if (cantidad < 1) {
            quitar(session, productoId);
            return;
        }
        Producto producto = productoVigente(productoId);
        LineaCarrito linea = buscar(carrito, productoId)
                .orElseThrow(() -> new ReglaNegocioException("Ese producto no está en el carrito."));
        if (cantidad > MAXIMO) {
            throw new ReglaNegocioException("El máximo por producto es 20 unidades.");
        }
        if (cantidad > producto.getStock()) {
            throw new ReglaNegocioException("Solo quedan " + producto.getStock() + " unidades de " + producto.getNombre() + ".");
        }
        copiar(linea, producto, cantidad);
        session.setAttribute(CLAVE, carrito);
    }

    public void quitar(HttpSession session, String productoId) {
        Carrito carrito = obtener(session);
        carrito.getLineas().removeIf(linea -> linea.getProductoId().equals(productoId));
        session.setAttribute(CLAVE, carrito);
    }

    public void vaciar(HttpSession session) {
        session.setAttribute(CLAVE, new Carrito());
    }

    public List<String> sincronizar(HttpSession session) {
        Carrito carrito = obtener(session);
        List<String> avisos = new ArrayList<>();
        Iterator<LineaCarrito> iterador = carrito.getLineas().iterator();
        while (iterador.hasNext()) {
            LineaCarrito linea = iterador.next();
            Optional<Producto> encontrado = productos.porId(linea.getProductoId());
            if (encontrado.isEmpty() || !encontrado.get().isActivo()) {
                iterador.remove();
                avisos.add(linea.getNombre() + " ya no está disponible.");
                continue;
            }
            Producto producto = encontrado.get();
            if (producto.getStock() <= 0) {
                iterador.remove();
                avisos.add(producto.getNombre() + " se agotó.");
                continue;
            }
            int cantidad = linea.getCantidad();
            if (producto.getStock() < cantidad) {
                cantidad = producto.getStock();
                avisos.add("Ajustamos la cantidad de " + producto.getNombre() + " al stock disponible.");
            }
            if (producto.getPrecio().compareTo(linea.getPrecioUnitario()) != 0) {
                avisos.add("Actualizamos el precio de " + producto.getNombre() + ".");
            }
            copiar(linea, producto, cantidad);
        }
        session.setAttribute(CLAVE, carrito);
        return avisos;
    }

    private Producto productoVigente(String productoId) {
        Producto producto = productos.porId(productoId)
                .orElseThrow(() -> new ReglaNegocioException("No encontramos el producto."));
        if (!producto.isActivo()) {
            throw new ReglaNegocioException("Ese producto ya no está a la venta.");
        }
        if (producto.getStock() <= 0) {
            throw new ReglaNegocioException(producto.getNombre() + " está agotado.");
        }
        return producto;
    }

    private Optional<LineaCarrito> buscar(Carrito carrito, String productoId) {
        return carrito.getLineas().stream()
                .filter(linea -> linea.getProductoId().equals(productoId))
                .findFirst();
    }

    private void copiar(LineaCarrito linea, Producto producto, int cantidad) {
        linea.setProductoId(producto.getId());
        linea.setNombre(producto.getNombre());
        linea.setSlug(producto.getSlug());
        linea.setImagen(producto.getImagen());
        linea.setPrecioUnitario(producto.getPrecio());
        linea.setCantidad(cantidad);
    }
}
