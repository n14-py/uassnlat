package lat.noticias.tienda.web.formulario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lat.noticias.tienda.modelo.MetodoPago;

public class CheckoutFormulario {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre admite hasta 80 caracteres")
    private String clienteNombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 120, message = "El correo es demasiado largo")
    private String clienteEmail;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 30, message = "El teléfono admite hasta 30 caracteres")
    private String clienteTelefono;

    @Size(max = 20, message = "El documento admite hasta 20 caracteres")
    private String documento;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 160, message = "La dirección admite hasta 160 caracteres")
    private String direccion;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 80, message = "La ciudad admite hasta 80 caracteres")
    private String ciudad;

    @NotNull(message = "El medio de pago es obligatorio")
    private MetodoPago metodoPago;

    @Size(max = 500, message = "Las notas admiten hasta 500 caracteres")
    private String notas;

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
