# Formatos CSV oficiales Prevengos Plug

Esta guía complementa las plantillas versionadas en `contracts/csv` y resume qué columnas, validaciones y archivos intervienen en el intercambio hub ⇄ Prevengos.

## Convenciones comunes

- **Codificación:** UTF-8 sin BOM.
- **Separador:** `;` con comillas dobles para valores que contienen caracteres especiales.
- **Checksum:** cada archivo dispone de un `.sha256` adyacente generado con `sha256sum <archivo> > <archivo>.sha256`.
- **Carpetas:** estructura `YYYYMMDD/<proceso>/<origen>/` donde `<origen>` puede ser `hub` (export) o `prevengos` (import).

## Exportaciones del hub

### `pacientes.csv`

| Columna | Obligatoria | Formato | Descripción |
| --- | --- | --- | --- |
| `paciente_id` | Sí | UUID | Identificador canónico compartido con Prevengos. |
| `nif` | Sí | Texto 5-16 | Documento normalizado en mayúsculas. |
| `nombre` | Sí | Texto | Nombre legal. |
| `apellidos` | Sí | Texto | Apellidos concatenados. |
| `fecha_nacimiento` | Sí | `YYYY-MM-DD` | Fecha ISO. |
| `sexo` | Sí | `M`\|`F`\|`X` | Sexo registrado. |
| `telefono` | No | Texto | Formato E.164 recomendado. |
| `email` | No | Email válido | Solo correos corporativos. |
| `empresa_id` | No | UUID | Relación con tabla `empresas`. |
| `centro_id` | No | UUID | Relación con centros de trabajo. |
| `externo_ref` | Sí | Texto | Identificador dado por Prevengos. |
| `created_at` | No | `YYYY-MM-DDTHH:MM:SSZ` | Fecha de creación UTC. |
| `updated_at` | No | `YYYY-MM-DDTHH:MM:SSZ` | Última actualización UTC. |

**Ejemplo:** [`contracts/csv/rrhh/pacientes.example.csv`](../../contracts/csv/rrhh/pacientes.example.csv)

### `cuestionarios.csv`

| Columna | Obligatoria | Formato | Descripción |
| --- | --- | --- | --- |
| `cuestionario_id` | Sí | UUID | Identificador de cuestionario. |
| `paciente_id` | Sí | UUID | Debe existir previamente en `pacientes.csv`. |
| `plantilla_codigo` | Sí | Texto | Código de plantilla Prevengos. |
| `estado` | Sí | `borrador`\|`completado`\|`validado` | Estado de flujo. |
| `respuestas` | Sí | JSON string | Matriz serializada con respuestas. |
| `firmas` | No | JSON string | Firmas digitalizadas, cuando existan. |
| `adjuntos` | No | JSON string | URLs o paths de adjuntos. |
| `created_at` | No | `YYYY-MM-DDTHH:MM:SSZ` | Fecha de creación UTC. |
| `updated_at` | No | `YYYY-MM-DDTHH:MM:SSZ` | Última actualización UTC. |

**Ejemplo:** [`contracts/csv/rrhh/cuestionarios.example.csv`](../../contracts/csv/rrhh/cuestionarios.example.csv)

## Importaciones desde Prevengos

Los CSV de retorno comparten las mismas columnas obligatorias de las exportaciones y añaden metadatos operativos:

| Columna | Formato | Descripción |
| --- | --- | --- |
| `estado_prevengos` | Texto | Estado final asignado por Prevengos (`pendiente`, `apto`, `no_apto`, etc.). |
| `procesado_en` | `YYYY-MM-DDTHH:MM:SSZ` | Momento en que Prevengos generó el CSV. |
| `observaciones` | Texto | Comentarios libres para el operador. |

Los importadores del hub validan:

- Integridad referencial (`paciente_id` y `cuestionario_id` existentes).
- Coherencia de estado (no se retrocede a estados anteriores).
- Presencia de checksum `.sha256` coincidente con el contenido.

## Automatización de drops

Consulta [`docs/operations/csv-automation.md`](../operations/csv-automation.md) para implementar los jobs que copian/recogen estos archivos de manera desatendida.

## Validaciones automatizadas

Las pruebas Playwright (`tests/e2e/tests/csv-exchange.spec.ts`) leen las plantillas oficiales y verifican:

1. Que los encabezados coinciden con lo documentado.
2. Que los valores de ejemplo respetan los formatos requeridos.
3. Que los CSV importados contienen los metadatos esperados.

Ejecuta `npm test` dentro de `tests/e2e` para confirmar la cobertura antes de liberar paquetes o habilitar validaciones manuales por equipos no técnicos.
