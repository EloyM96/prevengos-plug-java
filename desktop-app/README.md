# Prevengos Desktop App

Aplicación de escritorio construida con **JavaFX** para gestionar pacientes y cuestionarios offline y sincronizarlos con el Hub PRL descrito en `docs/data-stores/sync-flows.md`.

## Características principales

- Persistencia local en SQLite con inicialización automática de tablas para pacientes, cuestionarios, eventos de sincronización y metadatos.
- Sincronización incremental con los endpoints `/sincronizacion/lotes` y `/sincronizacion/pull`, incluyendo gestión de tokens y reintentos.
- Paneles visuales para alta, edición y borrado de pacientes y cuestionarios.
- Controles para exportar/importar los datos en JSON y disparar sincronizaciones manuales.
- Pruebas funcionales con TestFX y `fatJar` listo para distribución.

## Ejecución y pruebas

```bash
# Ejecutar la interfaz (requiere Java 17+)
gradle :desktop-app:run

# Ejecutar las pruebas
gradle :desktop-app:test

# Generar un JAR ejecutable con dependencias
gradle :desktop-app:fatJar
```

Para entornos sin conexión configure `database.path`, `api.baseUrl` y otros parámetros mediante variables del sistema (por ejemplo `-Ddatabase.path=/ruta/db.db`).

Los datos exportados/importados se serializan en JSON utilizando el mismo esquema que los eventos de sincronización, por lo que son compatibles con los flujos definidos en la documentación.
