# Entorno local de sincronización

Este paquete define el entorno mínimo para validar la sincronización entre el Hub PRL y PostgreSQL en una máquina local. Utiliza Docker Compose y variables de entorno compartidas para ambos servicios.

## Requisitos

* Docker Engine 24+
* Docker Compose V2 (`docker compose`)

## Configuración

1. Copiar el archivo `.env.example` y ajustar credenciales según sea necesario:
   ```bash
   cp infra/local-hub/.env.example infra/local-hub/.env
   ```
2. (Opcional) Cambiar los puertos publicados (`POSTGRES_PORT`, `HUB_PORT`) si ya están en uso.

## Uso

```bash
docker compose --env-file infra/local-hub/.env -f infra/local-hub/docker-compose.yml up --build
```

Esto levantará los contenedores `postgres` y `hub-backend` utilizando la misma configuración de credenciales.

* La API del Hub quedará accesible en `http://localhost:${HUB_PORT:-8080}`.
* PostgreSQL escuchará en `localhost:${POSTGRES_PORT:-5432}`.

Para detener el entorno:

```bash
docker compose --env-file infra/local-hub/.env -f infra/local-hub/docker-compose.yml down
```

## Datos persistentes

El volumen `postgres_data` conserva los datos entre reinicios. Para reiniciar con una base limpia ejecute:

```bash
docker compose --env-file infra/local-hub/.env -f infra/local-hub/docker-compose.yml down -v
```

## Variables de entorno compartidas

| Variable           | Uso                     | Descripción                                           |
|--------------------|-------------------------|-------------------------------------------------------|
| `POSTGRES_USER`     | PostgreSQL & Hub        | Usuario principal de la base de datos.                |
| `POSTGRES_PASSWORD` | PostgreSQL & Hub        | Contraseña del usuario principal.                     |
| `POSTGRES_DB`       | PostgreSQL & Hub        | Base de datos empleada por la aplicación.             |
| `POSTGRES_PORT`     | PostgreSQL (expuesto)   | Puerto local publicado del contenedor de PostgreSQL.  |
| `HUB_PORT`          | Hub (expuesto)          | Puerto local publicado del servicio Hub.              |

El `SPRING_DATASOURCE_URL` del Hub se genera automáticamente usando las variables anteriores.
