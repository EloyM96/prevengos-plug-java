# Payloads de ejemplo

Este directorio contiene ejemplos autocontenidos para los recorridos de sincronización manual.

## `sync-request.json`

Lote de ejemplo listo para enviarse a `POST /sincronizacion`.

* `source`: identifica el dispositivo o aplicación que envía el lote.
* `correlationId`: UUID utilizado para rastrear el envío manual.
* `pacientes` y `cuestionarios`: colecciones serializadas utilizando los contratos de `modules/shared/sync/dto`.

Los campos `respuestas`, `firmas` y `adjuntos` de los cuestionarios se serializan como strings JSON para simplificar el ejemplo.
Ajusta estos valores con los datos reales exportados desde las aplicaciones Android o desktop antes de ejecutar pruebas manuales.
