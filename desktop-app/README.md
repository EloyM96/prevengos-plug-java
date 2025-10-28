# Prevengos Desktop App

Aplicación de escritorio construida con **JavaFX** para gestionar pacientes y cuestionarios offline y sincronizarlos con el Hub PRL descrito en `docs/data-stores/sync-flows.md`.

## Características principales

- **Persistencia local en SQLite**: al iniciarse la aplicación crea automáticamente las tablas `pacientes`, `cuestionarios`, `sync_events` y `metadata`. Los repositorios Java encapsulan las operaciones CRUD y el estado de sincronización (`dirty`, `sync_token`, `last_modified`).
- **Interfaz JavaFX**: la escena principal muestra el listado de pacientes, los detalles del registro activo y los cuestionarios asociados. Desde ahí es posible crear, editar o borrar entidades mediante diálogos modales.
- **Sincronización con el Hub PRL**: un servicio dedicado empuja los cambios locales a `/sincronizacion/push` y consume novedades desde `/sincronizacion/pull`, manteniendo los tokens y registrando los eventos recibidos.
- **Importación/exportación offline**: los datos se serializan en JSON reutilizando el modelo de sincronización. Las acciones están accesibles desde el menú *Archivo* y permiten mover información entre estaciones desconectadas.
- **Seguimiento de metadatos**: la barra inferior expone el último token confirmado así como las marcas temporales de los `push` y `pull` exitosos.
- **Pruebas automatizadas**: la capa de persistencia se cubre con pruebas JUnit para garantizar la creación, actualización y limpieza de entidades.

## Configuración

Las propiedades se leen desde variables del sistema (por ejemplo `-Ddatabase.path=/ruta/bd.db`) y cuentan con valores por defecto para un arranque rápido.

| Propiedad | Descripción | Valor por defecto |
|-----------|-------------|-------------------|
| `database.path` | Ruta del archivo SQLite donde se almacenan pacientes, cuestionarios y metadatos. | `prevengos-desktop.db` |
| `api.baseUrl` | URL base del Hub PRL (se concatenan los endpoints de sincronización). | `http://localhost:8080` |
| `api.sourceSystem` | Identificador enviado en la cabecera `X-Source-System` y en los eventos locales. | `desktop-app` |
| `sync.pageSize` | Límite de elementos al solicitar `/sincronizacion/pull`. | `200` |
| `api.timeoutSeconds` | Timeout de las peticiones HTTP. | `30` |

Los valores se normalizan y validan al arrancar; cualquier configuración inválida detiene la aplicación con un mensaje claro.

## Escenas y flujos

1. **Vista principal (`ui/main-view.fxml`)**: combina un `TableView` de pacientes, el detalle del registro seleccionado y otro `TableView` con los cuestionarios asociados. La barra de menús permite importar/exportar datos y lanzar sincronizaciones manuales.
2. **Diálogo de pacientes (`ui/paciente-dialog.fxml`)**: formulario para alta y edición. Se valida que NIF, nombre y apellidos estén presentes antes de aceptar.
3. **Diálogo de cuestionarios (`ui/cuestionario-dialog.fxml`)**: permite registrar estados, respuestas y adjuntos en formato JSON para el paciente activo.

Cada acción actualiza automáticamente los listados y el indicador de estado, simplificando el uso en entornos sin conectividad.

## Ejecución y pruebas

```bash
# Ejecutar la interfaz (requiere Java 17+)
gradle :desktop-app:run

# Ejecutar las pruebas
gradle :desktop-app:test

# Generar un JAR ejecutable con dependencias
gradle :desktop-app:fatJar
```

Las pruebas crean bases de datos temporales y validan que los repositorios gestionan correctamente los flags de sincronización y los tokens.

Para entornos sin conexión configure `database.path`, `api.baseUrl` y otros parámetros mediante variables del sistema (por ejemplo `-Ddatabase.path=/ruta/db.db`).

Los datos exportados/importados se serializan en JSON utilizando el mismo esquema que los eventos de sincronización, por lo que son compatibles con los flujos definidos en la documentación.
