# Registro de cambios · Prevengos Plug

Este documento sigue la convención de [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/)
y emplea versionado semántico.

## [Unreleased]

### Añadido
- Carpeta `infra/mocks/prl-hub` con `docker-compose.yml` y guía para ejecutar un mock HTTP
  basado en Prism a partir del OpenAPI versionado.
- Archivo `.env.sample` compartido en la raíz y plantilla `infra/local-hub/.env.example`
  para alinear las variables entre equipos y repositorios.
- Propiedades `prl.notifier.*` y un enriquecedor de metadatos para eventos de sincronización,
  preparando la exportación hacia `prl-notifier` cuando se habilite.
- Entrada en la guía de preparación para `prl-notifier` confirmando la disponibilidad de
  variables compartidas.

### Documentación
- Actualización de `docs/apis/prl-hub/README.md` para referenciar el mock y el changelog.

## [0.1.0] - 2024-07-15

### Añadido
- Publicación inicial del OpenAPI `openapi.v0.1.0.yaml` con endpoints de pacientes,
  cuestionarios, sincronización offline y jobs de RRHH.
