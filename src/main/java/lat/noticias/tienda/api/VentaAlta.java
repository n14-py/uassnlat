package lat.noticias.tienda.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lat.noticias.tienda.modelo.MetodoPago;
import lat.noticias.tienda.modelo.OrigenVenta;
import lat.noticias.tienda.servicio.SolicitudVenta;

import java.util.List;

public record VentaAlta(
        @NotBlank(message = "El nombre del cliente es obligatorio")
        @Size(max = 80) String clienteNombre,
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 120) String clienteEmail,
        @Size(max = 30) String clienteTelefono,
        @Size(max = 20) String documento,
        @Size(max = 160) String direccion,
        @Size(max = 80) String ciudad,
        @NotNull(message = "El medio de pago es obligatorio") MetodoPago metodoPago,
        @Size(max = 500) String notas,
        @NotEmpty(message = "La venta necesita productos")
        @Valid List<ItemAlta> items
) {
    public SolicitudVenta aSolicitud() {
        return new SolicitudVenta(
                clienteNombre,
                clienteEmail,
                clienteTelefono,
                documento,
                direccion,
                ciudad,
                metodoPago,
                OrigenVenta.ADMIN,
                notas,
                items.stream().map(item -> new SolicitudVenta.Item(item.productoId(), item.cantidad())).toList()
        );
    }

    public record ItemAlta(
            @NotBlank(message = "El producto es obligatorio") String productoId,
            @NotNull @Min(1) @Max(20) Integer cantidad
    ) {
    }
}
