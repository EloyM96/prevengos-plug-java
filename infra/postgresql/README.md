# Postgres local para Prevengos Plug

Este entorno docker-compose está optimizado para validar las migraciones Flyway y preparar escenarios de replicación lógica.

## Pasos

1. Copiar `.env.example` a `.env` y ajustar credenciales (edita el archivo para definir contraseñas robustas):
   ```bash
   cp infra/postgresql/.env.example infra/postgresql/.env
   ```
2. Levantar el contenedor:
   ```bash
   docker compose --env-file infra/postgresql/.env -f infra/postgresql/docker-compose.yml up -d
   ```
3. Ejecutar Flyway (desde Gradle) apuntando a `jdbc:postgresql://localhost:${POSTGRES_PORT}/prevengos_plug` con el usuario
   `plug_rw`.
4. Marcar eventos publicados actualizando `sync_events.published_at` según el consumidor CDC.

## Configuración destacada

- `wal_level=logical`, `max_wal_senders=10` y `max_replication_slots=10` dejan listo el clúster para crear réplicas en caliente o
  para streaming de eventos.
- El script `init/01-init-db.sh` crea la base `prevengos_plug` y un rol de aplicación con permisos limitados.
- Los datos persistentes viven en el volumen `postgres_data`.

> **Seguridad**: Las credenciales del ejemplo son sólo para entornos locales. Genera contraseñas robustas y almacénalas en un
> gestor seguro o en secretos de CI/CD.
