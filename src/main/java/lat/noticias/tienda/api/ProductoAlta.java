package lat.noticias.tienda.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lat.noticias.tienda.modelo.Categoria;
import lat.noticias.tienda.web.formulario.ProductoFormulario;

import java.math.BigDecimal;

public record ProductoAlta(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre admite hasta 80 caracteres")
        String nombre,
        @NotBlank(message = "El SKU es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9-]{3,20}$", message = "El SKU solo puede tener letras, números y guiones")
        String sku,
        @NotBlank(message = "El resumen es obligatorio")
        @Size(max = 180) String resumen,
        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 2000) String descripcion,
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio tiene que ser mayor a cero")
        @Digits(integer = 8, fraction = 2) BigDecimal precio,
        @DecimalMin(value = "0.00")
        @Digits(integer = 8, fraction = 2) BigDecimal precioLista,
        @NotNull(message = "El stock es obligatorio") @Min(0) @Max(100000) Integer stock,
        @NotNull(message = "La sección es obligatoria") Categoria categoria,
        @NotBlank(message = "La portada es obligatoria") String imagen,
        boolean destacado,
        Boolean activo
) {
    public ProductoFormulario aFormulario(String id) {
        ProductoFormulario formulario = new ProductoFormulario();
        formulario.setId(id);
        formulario.setNombre(nombre);
        formulario.setSku(sku);
        formulario.setResumen(resumen);
        formulario.setDescripcion(descripcion);
        formulario.setPrecio(precio);
        formulario.setPrecioLista(precioLista);
        formulario.setStock(stock);
        formulario.setCategoria(categoria);
        formulario.setImagen(imagen);
        formulario.setDestacado(destacado);
        formulario.setActivo(activo == null || activo);
        return formulario;
    }
}
