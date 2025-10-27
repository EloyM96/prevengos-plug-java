# PRL Hub API · Borrador versionado

Este paquete mantiene el OpenAPI versionado de la API REST del Hub PRL. Cada release se publica
como un fichero `openapi.vX.Y.Z.yaml` siguiendo versionado semántico. La versión 0.1.0 se genera
a partir del *canvas* de arquitectura inicial y se tomará como base para las integraciones con
los conectores móviles, Prevengos y servicios de terceros.

## Versionado

- **MAJOR**: cambios incompatibles (nuevos endpoints obligatorios, cambios de contratos breaking).
- **MINOR**: incorporación de operaciones opcionales o campos adicionales retrocompatibles.
- **PATCH**: aclaraciones descriptivas, correcciones menores o ejemplos.

Se documenta el historial en la clave `x-changelog` dentro del propio documento OpenAPI. Además,
cada nueva release debe incluir una entrada en `docs/CHANGELOG.md` (pendiente de crear) y anunciarse
en el canal de integraciones.

## Artefactos

| Versión | Archivo                           | Notas clave |
|---------|-----------------------------------|-------------|
| 0.1.0   | `openapi.v0.1.0.yaml`             | Borrador inicial: endpoints de pacientes, cuestionarios, reconocimientos, sincronización offline y jobs RRHH. |

## Próximos pasos

1. Publicar un mock de la API con Prism o Stoplight para validar integraciones tempranas.
2. Generar SDKs tipados (TypeScript/Java/Kotlin) a partir del OpenAPI 0.1.0.
3. Completar esquemas de seguridad OIDC una vez esté disponible el Identity Provider corporativo.
4. Añadir escenarios de errores estandarizados y ejemplos de payloads.
