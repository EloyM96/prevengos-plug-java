# Blob storage v1.0.0

## Contenedores

| Contenedor                         | TTL/Redundancia | Contenido                                           |
|------------------------------------|-----------------|-----------------------------------------------------|
| `prl-cuestionarios`                | 180 días / LRS  | Adjuntos y firmas asociados a cuestionarios.        |
| `prl-citas`                        | 2 años / LRS    | Informes PDF, documentación médica de la cita.      |
| `prl-pacientes`                    | 2 años / GRS    | Documentación legal del paciente (consentimientos). |
| `prl-event-envelopes-dead-letter`  | 30 días / LRS   | Mensajes rechazados en el bus de eventos.           |

## Convenciones de path

```
{container}/{yyyy}/{MM}/{entity_type}/{entity_id}/{filename}
```

- `entity_type` ∈ `paciente`, `cuestionario`, `cita`.
- `filename` debe incluir un prefijo ISO 8601 extendido (`2025-02-15T103000Z_...`).
- Los adjuntos se almacenan cifrados en reposo mediante claves gestionadas por el proveedor.
- Los metadatos requeridos: `content-type`, `checksum-sha256`, `schema-version`.

## Versionado

- Incrementar la **MAJOR** al renombrar contenedores o modificar la convención de path.
- Incrementar la **MINOR** al añadir contenedores o metadatos opcionales.
- Incrementar la **PATCH** para ajustes menores (p. ej. aclaraciones de TTL).
