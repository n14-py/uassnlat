package lat.noticias.tienda.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoApiTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void altaListadoConsultaModificacionYBaja() throws Exception {
        String cuerpo = """
                {
                  "nombre": "Poster de cierre",
                  "sku": "NT-POS-TP01",
                  "resumen": "Una tinta sobre papel obra.",
                  "descripcion": "Impreso para la mesa de cierre.",
                  "precio": 15.00,
                  "stock": 8,
                  "categoria": "MERCHANDISING",
                  "imagen": "portada-06",
                  "destacado": false,
                  "activo": true
                }
                """;

        String respuesta = mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.sku").value("NT-POS-TP01"))
                .andReturn().getResponse().getContentAsString();

        String id = respuesta.replaceAll("(?s).*\"id\"\\s*:\\s*(\\d+).*", "$1");

        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.sku == 'NT-POS-TP01')].nombre").value("Poster de cierre"));

        mvc.perform(get("/api/productos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(15.00));

        mvc.perform(get("/api/productos/99999"))
                .andExpect(status().isNotFound());

        String cambio = cuerpo.replace("15.00", "18.00");
        mvc.perform(put("/api/productos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cambio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(18.00));

        mvc.perform(put("/api/productos/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/productos/" + id))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/productos/" + id))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/productos/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void altaVaciaResponde400() throws Exception {
        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos invalidos"))
                .andExpect(jsonPath("$.campos.nombre").exists());
    }

    @Test
    void skuRepetidoResponde400() throws Exception {
        String cuerpo = """
                {
                  "nombre": "Anuario",
                  "sku": "NT-ANU-2026",
                  "resumen": "Las piezas del ano.",
                  "descripcion": "Seleccion de cronicas.",
                  "precio": 24.00,
                  "stock": 10,
                  "categoria": "EDICION",
                  "imagen": "portada-08"
                }
                """;
        mvc.perform(post("/api/admin/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/admin/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
