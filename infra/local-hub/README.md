# Entorno local de sincronización (SQL Server)

Este paquete define el entorno mínimo para validar la sincronización entre el
Hub PRL y una instancia de SQL Server en una máquina local. Utiliza Docker
Compose, crea automáticamente la base de datos de trabajo y provisiona un
usuario de aplicación con permisos `db_owner`.

> **Nota:** La base de datos oficial del proyecto es SQL Server. PostgreSQL solo
> se usa en pruebas automatizadas y escenarios de compatibilidad específicos
> documentados en [`infra/postgresql`](../postgresql/README.md).

## Requisitos

* Docker Engine 24+
* Docker Compose V2 (`docker compose`)

## Configuración

1. Copiar el archivo `.env.example` y ajustar credenciales según sea necesario
   (recuerda cumplir las políticas de contraseñas de SQL Server):
   ```bash
   cp infra/local-hub/.env.example infra/local-hub/.env
   ```
2. (Opcional) Cambiar los puertos publicados (`SQLSERVER_PORT`, `HUB_PORT`) si
   ya están en uso.

## Uso

```bash
docker compose --env-file infra/local-hub/.env -f infra/local-hub/docker-compose.yml up --build
```

Esto levantará los contenedores `sqlserver` y `hub-backend` utilizando la misma
configuración de credenciales.

* La API del Hub quedará accesible en `http://localhost:${HUB_PORT:-8080}`.
* SQL Server escuchará en `localhost:${SQLSERVER_PORT:-1433}`.

Para detener el entorno:

```bash
docker compose --env-file infra/local-hub/.env -f infra/local-hub/docker-compose.yml down
```

## Datos persistentes

El volumen `sqlserver_data` conserva los datos entre reinicios. Para reiniciar
con una base limpia ejecute:

```bash
docker compose --env-file infra/local-hub/.env -f infra/local-hub/docker-compose.yml down -v
```

## Variables de entorno compartidas

| Variable                | Uso                        | Descripción                                                        |
|-------------------------|----------------------------|--------------------------------------------------------------------|
| `MSSQL_SA_PASSWORD`     | SQL Server                 | Contraseña del usuario `sa` (obligatoria y con complejidad válida). |
| `MSSQL_DB`              | SQL Server & Hub           | Base de datos empleada por la aplicación.                           |
| `MSSQL_APP_USER`        | SQL Server & Hub           | Usuario de aplicación creado automáticamente.                       |
| `MSSQL_APP_PASSWORD`    | SQL Server & Hub           | Contraseña del usuario de aplicación.                               |
| `SQLSERVER_PORT`        | SQL Server (expuesto)      | Puerto local publicado del contenedor de SQL Server.                |
| `HUB_PORT`              | Hub (expuesto)             | Puerto local publicado del servicio Hub.                            |

El `SPRING_DATASOURCE_URL` del Hub se genera automáticamente usando las
variables anteriores y el contenedor inicializa la base con el usuario de
aplicación indicado.
