# Contratos HTTP · versión 1.x

Las APIs HTTP del hub PRL se documentan con OpenAPI 3.1. Cada versión mayor se publica en un
subdirectorio `contracts/http/v<major>` y expone un único documento `openapi.yaml` por servicio.

- **Estructura**: los esquemas de entidades reutilizan los contratos JSON (`../../json/v1/*.schema.json`) mediante referencias externas.
- **Versionado**: los cambios incompatibles requieren incrementar la versión mayor (`v2`). Nuevos campos opcionales o parámetros tolerantes
  se publican como una nueva versión menor del documento e incluyen anotaciones de compatibilidad.
- **Entrega**: los contratos se sincronizan con los equipos consumidores a través de Pact y se publican como artefactos en el repositorio de contratos.
- **Validación**: la compilación ejecuta pruebas contractuales (Pact + validadores de esquema) para asegurar que los endpoints expuestos siguen los contratos.

## Servicios publicados

- `plug-api` · API REST pública para orquestar pacientes, cuestionarios y citas.
