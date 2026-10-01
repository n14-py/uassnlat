package lat.noticias.tienda.web.formulario;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lat.noticias.tienda.modelo.Categoria;
import lat.noticias.tienda.modelo.Producto;

import java.math.BigDecimal;

public class ProductoFormulario {

    private String id;

    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 20, message = "El SKU admite hasta 20 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9-]{3,20}$", message = "El SKU solo puede tener letras, números y guiones")
    private String sku;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre admite hasta 80 caracteres")
    private String nombre;

    @NotBlank(message = "El resumen es obligatorio")
    @Size(max = 180, message = "El resumen admite hasta 180 caracteres")
    private String resumen;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 2000, message = "La descripción admite hasta 2000 caracteres")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio tiene que ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 2 decimales")
    private BigDecimal precio;

    @DecimalMin(value = "0.00", message = "El precio de lista no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio de lista admite hasta 2 decimales")
    private BigDecimal precioLista;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Max(value = 100000, message = "El stock supera el máximo permitido")
    private Integer stock;

    @NotNull(message = "La sección es obligatoria")
    private Categoria categoria;

    @NotBlank(message = "Elegí una portada")
    private String imagen;

    private boolean destacado;
    private boolean activo = true;

    public static ProductoFormulario desde(Producto producto) {
        ProductoFormulario formulario = new ProductoFormulario();
        formulario.setId(producto.getId());
        formulario.setSku(producto.getSku());
        formulario.setNombre(producto.getNombre());
        formulario.setResumen(producto.getResumen());
        formulario.setDescripcion(producto.getDescripcion());
        formulario.setPrecio(producto.getPrecio());
        formulario.setPrecioLista(producto.getPrecioLista());
        formulario.setStock(producto.getStock());
        formulario.setCategoria(producto.getCategoria());
        formulario.setImagen(producto.getImagen());
        formulario.setDestacado(producto.isDestacado());
        formulario.setActivo(producto.isActivo());
        return formulario;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getResumen() {
        return resumen;
    }

    public void setResumen(String resumen) {
        this.resumen = resumen;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getPrecioLista() {
        return precioLista;
    }

    public void setPrecioLista(BigDecimal precioLista) {
        this.precioLista = precioLista;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public boolean isDestacado() {
        return destacado;
    }

    public void setDestacado(boolean destacado) {
        this.destacado = destacado;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
