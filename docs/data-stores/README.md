# Modelado de datos y stores base

Este paquete documenta los artefactos fundacionales que soportan la sincronización entre el hub PRL y los sistemas Prevengos. La versión inicial establece:

- **PostgreSQL** como almacén operativo centralizado para eventos y agregados normalizados.
- **SQLite** como store embebido para caches offline en la app de escritorio.
- **SQL Server** (solo lectura) como superficie de vistas federadas autorizadas por Prevengos.
- **Blob storage** para adjuntos binarios (firmas, documentos, plantillas).

Cada tecnología posee un esquema versionado de forma independiente. Las migraciones iniciales (`V1__*.sql`) materializan las estructuras mínimas para operar en el MVP y pueden evolucionar de forma desacoplada siguiendo versionado semántico.

| Store        | Versión base | Artefacto principal                                    |
|--------------|--------------|--------------------------------------------------------|
| PostgreSQL   | `1.0.0`      | [`schema_v1.sql`](postgresql/schema_v1.sql)             |
| SQLite       | `1.0.0`      | [`schema_v1.sql`](sqlite/schema_v1.sql)                 |
| SQL Server   | `1.0.0`      | [`views_v1.sql`](sqlserver/views_v1.sql)                |
| Blob storage | `1.0.0`      | [`layout_v1.md`](blob-storage/layout_v1.md)             |

Las migraciones asociadas se encuentran en la carpeta raíz [`migrations/`](../../migrations). Cada motor mantiene su propio historial para permitir despliegues controlados por plataforma.

## Convenciones de versionado

- **Versionado semántico** (`MAJOR.MINOR.PATCH`) para cada store y contrato JSON.
- Incrementos **MAJOR** cuando se introduzcan cambios incompatibles con versiones anteriores (por ejemplo, eliminación de columnas o renombre de contenedores).
- Incrementos **MINOR** para adiciones compatibles (nuevas columnas opcionales, nuevas vistas, contenedores adicionales).
- Incrementos **PATCH** para correcciones menores (ajustes de índices, documentación, comentarios).
- Toda migración debe indicar explícitamente su dependencia mínima del esquema anterior.

## Gobernanza de migraciones

1. Toda propuesta se presenta como SQL autodescriptivo más comentarios de trazabilidad.
2. Las migraciones se ejecutan mediante pipelines diferenciados por motor.
3. Se incluyen pruebas de humo (p.ej. `SELECT 1 FROM ...`) donde sea posible.
4. Cualquier dato sensible se mueve fuera de repositorio; este repositorio solo contiene estructuras.
