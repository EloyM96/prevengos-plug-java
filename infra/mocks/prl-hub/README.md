# Mock de la PRL Hub API

Este paquete levanta un mock HTTP de la API del Hub PRL usando
[Stoplight Prism](https://github.com/stoplightio/prism). El objetivo es
validar integraciones tempranas (móviles, Prevengos o servicios externos)
sin depender del backend Java.

## Requisitos

- Docker Engine 24+
- Docker Compose V2 (`docker compose`)

## Uso

1. Opcionalmente, copia `.env.sample` a `.env` y ajusta
   `PRL_HUB_MOCK_PORT` si necesitas exponer el mock en otro puerto.
2. Levanta el mock con Docker Compose:

   ```bash
   docker compose --env-file ../../local-hub/.env.example -f docker-compose.yml up
   ```

   También puedes suministrar tus propias variables mediante
   `--env-file ../../../.env` si compartes la configuración con otros
   componentes del monorepo.

Una vez iniciado, la API mock queda disponible en
`http://localhost:${PRL_HUB_MOCK_PORT:-4010}` y responde en base al
archivo OpenAPI versionado en `docs/apis/prl-hub/openapi.v0.1.0.yaml`.

## Actualización del mock

Cada vez que se publique una nueva versión de la especificación OpenAPI:

1. Actualiza el volumen de `docker-compose.yml` para que apunte al nuevo
   archivo versionado.
2. Registra la nueva release en `docs/CHANGELOG.md`.
3. Anuncia la disponibilidad del mock en el canal de integraciones para
   que los clientes sin backend puedan validar los cambios.
