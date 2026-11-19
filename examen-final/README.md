# API Tienda Noticias.lat

Examen final de Lenguaje de Programación III.  
Estudiante: Luis Fernando Arce Fretes.

API REST de productos para la tienda de la redacción. Java 17, Spring Boot y PostgreSQL.

## Qué tiene que estar instalado

- JDK 17
- PostgreSQL en el puerto 5432
- Postman, o la extensión Postman de Visual Studio Code

## Base de datos

El usuario y la clave de este proyecto son `postgres` / `postgres`. Si en la computadora son otros, cambiar estas líneas de `src/main/resources/application.properties`:

```
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Crear la base. La tabla `productos` la crea la aplicación al arrancar.

En PowerShell, desde la carpeta del proyecto:

```
psql -U postgres -f sql/crear-base.sql
```

Si `psql` no está en el PATH, abrirlo desde pgAdmin y ejecutar:

```
CREATE DATABASE tienda_noticias;
```

## Ejecutar

En la carpeta donde está `pom.xml`:

```
mvnw.cmd spring-boot:run
```

En Linux o macOS:

```
./mvnw spring-boot:run
```

Cuando la consola dice `Tomcat started on port 8080`, la API ya está lista. Se detiene con Ctrl + C.

La primera vez hace falta internet para que el wrapper baje Maven.

## Probar en Postman

La URL base es `http://localhost:8080`. No pide usuario ni contraseña.

En Body elegir **raw** y el tipo **JSON**.

| Método | URL | Si sale bien | Si falla |
| --- | --- | --- | --- |
| POST | http://localhost:8080/api/productos | 201 | 400 |
| GET | http://localhost:8080/api/productos | 200 | |
| GET | http://localhost:8080/api/productos/1 | 200 | 404 |
| PUT | http://localhost:8080/api/productos/1 | 200 | 404 |
| DELETE | http://localhost:8080/api/productos/1 | 204 | 404 |

Las mismas rutas funcionan con `/api/admin/productos`.

JSON de alta:

```json
{
  "nombre": "Póster de cierre",
  "sku": "NT-POS-TP01",
  "resumen": "Una tinta sobre papel obra.",
  "descripcion": "Impreso para la mesa de cierre. Se envía enrollado.",
  "precio": 15.00,
  "stock": 8,
  "categoria": "MERCHANDISING",
  "imagen": "portada-06",
  "destacado": false,
  "activo": true
}
```

El `id` lo asigna PostgreSQL. En el PUT y en el DELETE se usa ese número.

## Archivos

- `Producto.java`: la entidad y la tabla `productos`.
- `ProductoRepositorio.java`: el acceso a la base.
- `ProductoServicio.java`: las validaciones de negocio.
- `ProductoControlador.java`: POST, GET, PUT y DELETE.
- `application.properties`: la conexión a PostgreSQL.
- `sql/crear-base.sql`: la creación de la base.
