package lat.noticias.tienda.web.formulario;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class LineaFormulario {

    private String productoId;

    @Min(value = 0, message = "La cantidad no puede ser negativa")
    @Max(value = 20, message = "El máximo por producto es 20")
    private Integer cantidad;

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
