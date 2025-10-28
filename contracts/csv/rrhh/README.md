# Plantillas CSV RRHH Prevengos

Los ficheros intercambiados entre el hub PRL y Prevengos siguen estas reglas
comunes:

- Codificación UTF-8 sin BOM.
- Separador `;` y comillas dobles para escapar valores.
- Cada CSV dispone de un checksum SHA-256 en un fichero adyacente con sufijo
  `.sha256`.
- Estructura de carpetas oficial: `YYYYMMDD/<proceso>/<origen>/`.

## `pacientes.csv`

| Columna           | Tipo / formato                 | Notas |
|-------------------|--------------------------------|-------|
| `paciente_id`     | UUID                           | Identificador canónico. |
| `nif`             | Texto (5-16)                   | Normalizado en mayúsculas. |
| `nombre`          | Texto                          | |
| `apellidos`       | Texto                          | |
| `fecha_nacimiento`| `YYYY-MM-DD`                   | |
| `sexo`            | `M` \| `F` \| `X`             | |
| `telefono`        | Texto                          | Opcional. |
| `email`           | Correo válido                  | Opcional. |
| `empresa_id`      | UUID                           | Opcional. |
| `centro_id`       | UUID                           | Opcional. |
| `externo_ref`     | Texto                          | Referencia externa Prevengos. |
| `created_at`      | `YYYY-MM-DDTHH:MM:SSZ`         | Opcional, UTC. |
| `updated_at`      | `YYYY-MM-DDTHH:MM:SSZ`         | Opcional, UTC. |

## `cuestionarios.csv`

| Columna            | Tipo / formato                 | Notas |
|--------------------|--------------------------------|-------|
| `cuestionario_id`  | UUID                           | |
| `paciente_id`      | UUID                           | Debe existir en `pacientes`. |
| `plantilla_codigo` | Texto                          | Código contrato Prevengos. |
| `estado`           | `borrador` \| `completado` \| `validado` | |
| `respuestas`       | JSON array                     | Serializado como texto. |
| `firmas`           | JSON array                     | Opcional. |
| `adjuntos`         | JSON array                     | Opcional. |
| `created_at`       | `YYYY-MM-DDTHH:MM:SSZ`         | Opcional, UTC. |
| `updated_at`       | `YYYY-MM-DDTHH:MM:SSZ`         | Opcional, UTC. |

## Ejemplos

Se incluyen ficheros de ejemplo (`pacientes.example.csv` y
`cuestionarios.example.csv`) con un registro ficticio para probar el parser y
las automatizaciones.
