-- Ejecutar conectado a la base postgres, antes de levantar la aplicacion.
-- En Windows, con PostgreSQL instalado:
--   psql -U postgres -f crear-base.sql
-- Si la base ya existe, el CREATE DATABASE falla y no pasa nada: la tabla
-- la crea la aplicacion al arrancar.

CREATE DATABASE tienda_noticias;

-- Referencia de la tabla. Con spring.jpa.hibernate.ddl-auto=update
-- Hibernate la crea sola. Este script queda para la entrega.

-- CREATE TABLE productos (
--     id              BIGSERIAL PRIMARY KEY,
--     sku             VARCHAR(20)  NOT NULL UNIQUE,
--     nombre          VARCHAR(80)  NOT NULL,
--     resumen         VARCHAR(180) NOT NULL,
--     descripcion     VARCHAR(2000) NOT NULL,
--     precio          NUMERIC(10, 2) NOT NULL,
--     precio_lista    NUMERIC(10, 2),
--     stock           INTEGER NOT NULL,
--     categoria       VARCHAR(40) NOT NULL,
--     imagen          VARCHAR(40) NOT NULL,
--     destacado       BOOLEAN NOT NULL,
--     activo          BOOLEAN NOT NULL
-- );
