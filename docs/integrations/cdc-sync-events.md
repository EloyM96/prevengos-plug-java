# CDC aplicativo: sync_events ↔ contratos JSON

La tabla `sync_events` orquesta la propagación de cambios desde el backend del hub PRL hacia integraciones externas. Cada fila
contiene la representación JSON canónica definida en `contracts/json/v1` para los recursos `paciente` y `cuestionario`.

## Esquema de sincronización

| Campo `sync_events` | Tipo | Descripción |
| --- | --- | --- |
| `sync_event_id` | `uuid` | Identificador del evento (primaria, generado con `uuid_generate_v4()`). |
| `entity_type` | `text` | Nombre lógico de la entidad (`pacientes`, `cuestionarios`). |
| `entity_id` | `uuid` | UUID de la entidad afectada. |
| `operation` | `text` | Operación DML (`INSERT`, `UPDATE`, `DELETE`). |
| `payload` | `jsonb` | Documento conforme al contrato JSON correspondiente. |
| `occurred_at` | `timestamptz` | Marca temporal de inserción en cola CDC. |
| `published_at` | `timestamptz` | Marca opcional cuando la integración externa confirma recepción. |
| `metadata` | `jsonb` | Espacio para flags de transporte (por defecto `{}`). |

Indices auxiliares permiten paginación por `occurred_at` y búsquedas por entidad.

## Mapeo de entidades

### Pacientes

Los triggers `trg_pacientes_touch_updated` (que delega en la función `touch_pacientes_updated_at()`) y `trg_pacientes_sync_events`
garantizan marcas temporales consistentes y generan un `payload` con la siguiente correspondencia:

| Columna `pacientes` | Propiedad JSON | Notas |
| --- | --- | --- |
| `paciente_id` | `paciente_id` | UUID obligatorio (`format: uuid`). |
| `nif` | `nif` | Validación `^[0-9A-Za-z]{5,16}$` según contrato. |
| `nombre` | `nombre` | Texto libre. |
| `apellidos` | `apellidos` | Texto libre. |
| `fecha_nacimiento` | `fecha_nacimiento` | Serializado como ISO 8601 (`date`). |
| `sexo` | `sexo` | Enum `M`, `F`, `X`. |
| `telefono` | `telefono` | Campo opcional. |
| `email` | `email` | Validación `format: email`. |
| `empresa_id` | `empresa_id` | UUID opcional. |
| `centro_id` | `centro_id` | UUID opcional. |
| `externo_ref` | `externo_ref` | Referencia externa opcional. |
| `created_at` | `created_at` | Fecha de alta (`timestamptz`). |
| `updated_at` | `updated_at` | Gestionado automáticamente por `touch_pacientes_updated_at()`. |

El resultado cumple con `contracts/json/v1/paciente.schema.json` y se utiliza tanto para altas como para modificaciones y bajas
(los deletes conservan los últimos valores persistidos).

### Cuestionarios

Para cuestionarios se agregan las respuestas normalizadas antes de generar el evento. El trigger `trg_cuestionarios_sync_events`
utiliza la tabla `cuestionario_respuestas` para producir un arreglo ordenado por inserción con la siguiente proyección:

| Fuente | Propiedad JSON | Notas |
| --- | --- | --- |
| `cuestionarios.cuestionario_id` | `cuestionario_id` | UUID obligatorio. |
| `cuestionarios.paciente_id` | `paciente_id` | UUID del paciente asociado. |
| `cuestionarios.plantilla_codigo` | `plantilla_codigo` | Código de plantilla Prevengos. |
| `cuestionarios.estado` | `estado` | Enum `borrador`, `completado`, `validado`. |
| `cuestionarios.created_at` | `created_at` | Fecha de creación. |
| `cuestionarios.updated_at` | `updated_at` | Actualizado automáticamente. |
| `cuestionario_respuestas` | `respuestas[]` | Cada item incluye `pregunta_codigo`, `valor`, `unidad`, `metadata`, `created_at` (ordenados por `created_at`). |

Los campos opcionales `firmas` y `adjuntos` quedan vacíos (no presentes) hasta contar con soporte en tablas dedicadas.

## Ejemplo de evento

```json
{
  "sync_event_id": "7d7284a7-53ac-4e28-b987-45c5105bb7b9",
  "entity_type": "pacientes",
  "entity_id": "41c9c7da-b4d7-4d61-a1fd-9e7b2816aa7f",
  "operation": "UPDATE",
  "occurred_at": "2024-05-13T10:22:48.312Z",
  "payload": {
    "paciente_id": "41c9c7da-b4d7-4d61-a1fd-9e7b2816aa7f",
    "nif": "12345678A",
    "nombre": "Elena",
    "apellidos": "Martín López",
    "fecha_nacimiento": "1984-01-12",
    "sexo": "F",
    "telefono": "+34 600 123 456",
    "email": "elena.martin@example.org",
    "empresa_id": "e6b5fb41-3b4d-4f14-8c9c-6bf8924d6e42",
    "centro_id": null,
    "externo_ref": "prevengos:98765",
    "created_at": "2023-11-02T08:31:17.902Z",
    "updated_at": "2024-05-13T10:22:48.300Z"
  },
  "metadata": {}
}
```

El consumidor debe validar `payload` con el esquema JSON correspondiente antes de procesarlo. Los eventos se sirven en orden de
`occurred_at` y pueden marcarse como entregados poblado `published_at`.
