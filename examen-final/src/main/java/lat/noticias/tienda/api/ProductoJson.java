package lat.noticias.tienda.api;

import lat.noticias.tienda.modelo.Producto;

import java.math.BigDecimal;

public class ProductoJson {

    private Long id;
    private String sku;
    private String nombre;
    private String resumen;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal precioLista;
    private Integer stock;
    private String categoria;
    private String imagen;
    private boolean destacado;
    private boolean activo;

    public static ProductoJson de(Producto producto) {
        ProductoJson json = new ProductoJson();
        json.id = producto.getId();
        json.sku = producto.getSku();
        json.nombre = producto.getNombre();
        json.resumen = producto.getResumen();
        json.descripcion = producto.getDescripcion();
        json.precio = producto.getPrecio();
        json.precioLista = producto.getPrecioLista();
        json.stock = producto.getStock();
        json.categoria = producto.getCategoria();
        json.imagen = producto.getImagen();
        json.destacado = producto.isDestacado();
        json.activo = producto.isActivo();
        return json;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getNombre() {
        return nombre;
    }

    public String getResumen() {
        return resumen;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public BigDecimal getPrecioLista() {
        return precioLista;
    }

    public Integer getStock() {
        return stock;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getImagen() {
        return imagen;
    }

    public boolean isDestacado() {
        return destacado;
    }

    public boolean isActivo() {
        return activo;
    }
}
