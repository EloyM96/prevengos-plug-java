# Arquitectura integral para app PRL (Java) con sincronización local Prevengos

> **Objetivo**: Definir una solución 100 % Java que funcione como puente local entre las aplicaciones de captura (Android y escritorio) y la instalación on‑premise de Prevengos. El sistema debe escribir en la base de datos SQL Server local y producir/consumir ficheros CSV compatibles con Prevengos para mantener alineada la información de pacientes, cuestionarios y reconocimientos sin replicar funcionalidades de mensajería u orquestación externa.

---

## 1) Visión general (arquitectura lógica)

```
[Android App (Java)]      [Escritorio (Java)]
        |                         |
        v                         v
   Cache offline (Room)      Cache local (H2/Files)
        |                         |
        +-----------[Módulos compartidos Java]-----------+
                                    |
                                    v
                        [Hub Spring Boot local]
                                    |
                      +-------------+-------------+
                      |                           |
        [SQL Server Prevengos (escritura controlada)]   [Intercambio CSV Prevengos]
```

**Principios clave**
- **Sincronización bidireccional controlada**: las apps leen/escriben en la base de datos SQL Server local y el hub genera/consume CSV que se depositan en las rutas oficiales de Prevengos.
- **Stack homogéneo**: todo el código del repositorio es Java (Android, JavaFX, Spring Boot, utilidades). No existe motor Python ni módulos Kotlin.
- **Responsabilidades acotadas**: mensajería, notificaciones multicanal, analítica o automatización viven en repos externos (p. ej. [`prl-notifier`](https://github.com/prevengos/prl-notifier)). Este proyecto solo expone los datos necesarios mediante SQL y CSV para que dichos sistemas trabajen sin solaparse.
- **Operación local**: el despliegue objetivo es un servidor local donde conviven el hub Java y SQL Server; los clientes se conectan a través de la red corporativa.

---

## 2) Módulos del sistema

### 2.1 Aplicaciones cliente
- **Android (Java, Room/WorkManager)**: captura de datos médicos en movilidad con soporte offline. Persiste formularios y respuestas en Room y delega en el hub la sincronización con SQL Server y la generación de CSV.
- **Escritorio (JavaFX)**: interfaz para personal sanitario/administrativo que consulta y edita la misma información almacenada en SQL Server. Comparte contratos y validaciones con la app móvil mediante `modules/shared`.

### 2.2 Hub local (Spring Boot)
- Expone APIs REST para las apps y ejecuta jobs de sincronización (import/export CSV, reconciliaciones básicas).
- Se conecta directamente a SQL Server mediante JDBC y aplica las reglas de negocio comunes.
- Mantiene módulos `modules:gateway` y `modules:hub-backend` como puertos/adaptadores para encapsular acceso a datos y transformación CSV.

### 2.3 Módulos compartidos (`modules/shared`, `modules/gateway`)
- Definen DTOs, contratos JSON y utilidades de transformación que comparten Android, escritorio y el hub.
- Implementan lógica de validación, mapeo a CSV y helpers de fechas para garantizar consistencia en todos los clientes.

### 2.4 Integración con Prevengos
- **CSV oficiales**: el hub genera ficheros con los campos esperados por Prevengos (altas de trabajadores, cuestionarios, reconocimientos) y consume los CSV de retorno para reflejar estados o aptitudes.
- **SQL Server**: las operaciones de lectura/escritura se realizan sobre tablas autorizadas del servidor local. El hub controla las escrituras y aplica auditoría básica (timestamps, usuario, origen).
- **Flujos manuales/automatizados**: la entrega y recogida de CSV puede automatizarse con scripts Windows o tareas del servidor; este repositorio solo ofrece los productores/consumidores.

### 2.5 Integraciones externas (fuera de este repositorio)
- Cualquier integración con Moodle, canales de notificación, correo, WhatsApp, analítica o LLM debe resolverse mediante herramientas dedicadas como `prl-notifier` y consumir los datos expuestos por este hub (CSV o vistas SQL).
- El hub puede exponer endpoints de solo lectura para que otros sistemas consulten información, pero no implementa la entrega multicanal ni motores de reglas externos.

---

## 3) Flujos de datos principales

1. **Captura en Android/escritorio** → se guarda en la base de datos local (Room o SQL Server) y se marca como pendiente de sincronizar.
2. **Sincronización con el hub** → jobs locales envían los registros pendientes al hub Spring Boot, que los consolida en SQL Server.
3. **Generación de CSV** → el hub empaqueta las novedades en CSV con el formato Prevengos y los deposita en la ruta compartida para su importación oficial.
4. **Importación de CSV desde Prevengos** → el hub monitoriza la carpeta de retorno o ejecuta un job manual para leer los CSV generados por Prevengos y actualizar SQL Server, lo que a su vez se replica a las apps.
5. **Consumo por sistemas externos** → repositorios como `prl-notifier` consultan SQL Server o los CSV publicados para disparar notificaciones, reportes o analítica avanzada.

---

## 4) Consideraciones de operación y seguridad

- **Control de acceso**: usar cuentas de servicio con permisos mínimos en SQL Server y rutas de ficheros restringidas. El hub debe registrar auditoría básica (quién creó/modificó, desde qué cliente).
- **RGPD**: cifrado en reposo en SQL Server, anonimización en CSV cuando se comparten fuera del entorno sanitario y purga periódica de datos innecesarios.
- **Resiliencia offline**: la app Android conserva colas de sincronización y reintentos; el escritorio puede trabajar en modo desconectado utilizando caches locales y sincronizar cuando el hub esté disponible.
- **Monitorización ligera**: logs y métricas locales (Micrometer + JMX/Prometheus) para diagnosticar jobs de CSV y conexiones con SQL Server. No se incluyen stacks de observabilidad complejos.

---

## 5) Relación con otros proyectos

- `prl-notifier` y otras iniciativas reciben datos desde este hub pero implementan sus propios pipelines (FastAPI, Redis, notificaciones). Es fundamental evitar duplicidades: este repositorio solo mantiene el circuito de captura ↔ Prevengos via SQL/CSV.
- Los contratos JSON y CSV deben mantenerse sincronizados entre ambos repositorios. Cualquier cambio en esquemas debe versionarse y comunicarse explícitamente.

---

## 6) Próximos pasos

1. Completar la documentación técnica de los formatos CSV aceptados y producidos por Prevengos, incluyendo ejemplos y validaciones en `contracts/json` y `docs/`.
2. Implementar pruebas de integración en Java que verifiquen la escritura/lectura en SQL Server y la generación correcta de CSV.
3. Definir procedimientos operativos (cron jobs, scripts) para automatizar el intercambio de CSV y la reconciliación de registros con Prevengos.
