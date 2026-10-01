# Tienda Noticias.lat

Tienda de la redacción de [Noticias.lat](https://www.noticias.lat) para un proyecto universitario. Corre en local. El catálogo, el carrito y el panel de redacción están hechos en **Java**. La base es **MongoDB**. No hay frontend en JavaScript: las páginas las arma el servidor con Thymeleaf.

El pedido se registra y descuenta stock. No hay pasarela de pago y no se cobra nada.

## Qué incluye

- Tienda pública: inicio, catálogo con búsqueda y secciones, ficha, carrito y registro del pedido.
- Panel de redacción: tablero, alta y edición de productos, archivo, ventas de mostrador y cambio de estado.
- API REST en JSON para mostrar el backend aparte de las pantallas.
- Catálogo de demostración y tres ventas de ejemplo, solo la primera vez que la base está vacía.

## Requisitos

- JDK 21
- MongoDB 7 u 8 en `localhost:27017`

Docker, si lo preferís:

```bash
docker compose up -d
```

Sin Docker, instalá MongoDB Community y dejá el servicio escuchando en el puerto 27017.

## Cómo correrla

```bash
./mvnw spring-boot:run
```

En Windows: `mvnw.cmd spring-boot:run`

Abrí [http://localhost:8080](http://localhost:8080).

También podés usar `scripts/iniciar.sh`, que avisa si MongoDB no está levantado.

La base se llama `tienda_noticias`. Para apuntar a otra:

```bash
MONGODB_URI="mongodb://localhost:27017/tienda_noticias" ./mvnw spring-boot:run
```

## Ingreso de redacción

| Campo | Valor |
| --- | --- |
| Usuario | `admin` |
| Clave | `admin123` |

Están en `src/main/resources/application.yml` (`tienda.admin`). Cambialos si la vas a mostrar fuera de tu máquina. La clave se guarda con BCrypt.

## Recorrido para la defensa

1. Entrá a la tienda, filtrá una sección y abrí una ficha.
2. Agregá piezas al carrito y registrá un pedido. El comprobante queda en la sesión.
3. Entrá por **Ingreso redacción**.
4. En el tablero vas a ver las ventas del día, el ticket y el stock bajo.
5. Creá un producto, elegí una portada y guardalo. Tiene que aparecer en el catálogo.
6. Registrá una venta de mostrador. El stock baja. Si la cancelás antes de entregarla, el stock vuelve.

## API

La tienda y el panel usan los servicios Java directo, sin JavaScript. La API queda para probar el backend con curl o Postman. Las rutas `/api/admin/**` piden autenticación básica con el mismo usuario.

```bash
curl http://localhost:8080/api/productos
curl http://localhost:8080/api/categorias
curl -u admin:admin123 http://localhost:8080/api/admin/ventas
```

Alta de producto:

```bash
curl -u admin:admin123 -H "Content-Type: application/json" \
  -d '{"nombre":"Póster de cierre","sku":"NT-MER-POS","resumen":"Una tinta sobre papel obra.","descripcion":"Impreso en la imprenta asociada.","precio":15.00,"stock":10,"categoria":"MERCHANDISING","imagen":"portada-06","destacado":false,"activo":true}' \
  http://localhost:8080/api/admin/productos
```

## Arquitectura

```
Navegador  --HTML-->  Controladores web (Thymeleaf)
curl       --JSON-->  Controladores REST
                         |
                     Servicios
                         |
                   Repositorios Spring Data
                         |
                      MongoDB
```

Paquetes:

- `modelo`: productos, ventas, carrito, usuario
- `repositorio`: acceso a MongoDB
- `servicio`: stock, totales, carrito y reglas
- `web`: pantallas y formularios
- `api`: JSON
- `config`: seguridad, datos iniciales y datos comunes de la vista

El carrito vive en la sesión HTTP. La venta guarda el precio y el nombre de ese momento, así un cambio posterior de la ficha no reescribe el comprobante. El número `NT-2026-00001` sale de un contador en MongoDB.

## Pruebas

```bash
./mvnw test
```

Cubren el total de una venta y la generación del slug. No levantan MongoDB.
