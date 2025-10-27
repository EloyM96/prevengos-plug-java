# Contratos JSON · versión 1.x

Los contratos definen la forma canónica para mensajes y entidades compartidas entre el hub PRL y los sistemas Prevengos. Se publican como JSON Schema Draft 2020-12 y siguen versionado semántico independiente.

- **Ubicación**: `contracts/json/<major>.<minor>/<schema>.schema.json`.
- **Convenciones**: propiedades en *snake_case*, topics/eventos en *kebab-case*, fechas en ISO 8601 con zona horaria.
- **Compatibilidad**: las minor/patch deben mantenerse retrocompatibles agregando campos opcionales o extensiones `oneOf`.

Reglas de versionado:

1. Cambios incompatibles rompen compatibilidad → incrementar **MAJOR** y documentar estrategia de migración.
2. Nuevos campos opcionales, nuevos enums compatibles → incrementar **MINOR**.
3. Correcciones menores (descripciones, ejemplos) → incrementar **PATCH**.
4. Los productores deben incluir la propiedad `version` en `EventEnvelope` para negociar compatibilidad.

La versión 1.0.0 incluye los esquemas:

- `event-envelope`
- `paciente`
- `cuestionario`
- `cita`
