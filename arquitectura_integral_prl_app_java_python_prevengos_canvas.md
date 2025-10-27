# Arquitectura integral para app PRL (Java + Python) con integración Prevengos, móvil/escritorio, LLM y conectores externos

> **Objetivo**: Diseñar una solución modular, segura y extensible que permita a una empresa de PRL recabar datos médicos en tablet/móvil (offline‑first), sincronizarlos con un hub backend, y realizar **integración bidireccional** con **Prevengos** (RR. HH., médico, portal web), además de conectarse a **Moodle**, **canales de notificación** (WhatsApp/SMS/email), **Google**, y habilitar un **motor Python** para analítica/LLM.

---

## 1) Visión general (arquitectura lógica)

```
[Android (Java) App]          [Escritorio (JavaFX/Web)]
       |                             |
   SQLite (offline)              Cache local
       |                             |
       v                             v
        ---->  [API Hub (Spring Boot, Hexagonal)]  <----
                      |        ^                     \
                      |        |                      \
               [Message Bus]   |                       \
              (RabbitMQ/Redis) |                        \
                      v        |                         v
     [Adaptadores]-------------+-----------+   [Python Engine]
      | Prevengos RRHH (MDB/ACCDB drop)    |    (FastAPI+Celery)
      | Prevengos Analíticas (fichero)     |--> LLM, Analytics, ETL
      | Prevengos SOAP (.asmx)             |    Moodle WS, Notifier
      | Prevengos SQL (read-only)          |
      | Moodle (REST WS)                   |
      | Notificaciones (WhatsApp/SMS/Email)|
      | Google (Drive/Sheets/Gmail)        |

                          [DB Operativa: PostgreSQL]
                          [Blob Storage: S3/MinIO]
                          [Logs/Events: ELK/Tempo]
                          [Metrics/Tracing: Prom/Grafana/Otel]
```

**Principios clave**
- **Hexagonal (puertos/adaptadores)**: el dominio no conoce detalles de Prevengos ni de canales.
- **Offline‑first**: la app móvil funciona sin red; sincroniza por lotes con reintentos e idempotencia.
- **Staging propio** (PostgreSQL) para orquestar integraciones sin tocar directamente la BD núcleo de Prevengos.
- **Contratos JSON estables** (versionados) entre apps ⇄ hub ⇄ Python.
- **Seguridad y RGPD** by‑design: minimización, cifrado, auditoría, consentimientos.
- **Observabilidad** end‑to‑end y **CI/CD** reproducible.

---

## 2) Módulos del sistema

### 2.1 Apps cliente
- **Android (Java, Room/SQLite, WorkManager)**: formularios de datos médicos, firma, adjuntos, cuestionarios.
- **Escritorio**: JavaFX nativo o front web (React/Electron) para personal sanitario/administrativo.

### 2.2 Hub backend (Spring Boot)
- **Capas**: dominio (entidades/servicios), aplicación (casos de uso), infraestructura (repositorios, adaptadores, seguridad, web).
- **Puertos** (interfaces): `PacientesPort`, `CuestionariosPort`, `CitasPort`, `ResultadosPort`, `RRHHPort`, `NotificacionPort`, `MoodlePort`, `PrevengosPort`.
- **Adaptadores**: concretan cada integración (Prevengos, Moodle, Notifier, Google, etc.).
- **Mensajería**: publicación de eventos de dominio (e.g., `PacienteRegistrado`, `CuestionarioCompletado`).

### 2.3 Data stores
- **PostgreSQL**: verdad operacional de tu app, staging para integraciones, control de versiones de entidades y colas outbox.
- **SQLite (móvil)**: almacenamiento offline con tablas espejo y marcas de sincronización.
- **SQL Server (Prevengos)**: **solo lectura** mediante vistas/replica autorizada.
- **Blob**: MinIO/S3 para ficheros (consentimientos, PDFs, adjuntos).

### 2.4 Python Engine (servicios auxiliares)
- **FastAPI** para exponer APIs internas/ops.
- **Celery** para tareas (ETL Moodle, inferencias LLM, generación de documentos, notificaciones masivas).
- **Pydantic** alineado con contratos JSON del Hub.

### 2.5 Observabilidad y seguridad
- **OpenTelemetry** (trazas), **Prometheus/Grafana** (métricas), **ELK/Tempo** (logs/eventos).
- **Keycloak** o IdP equivalente (OAuth2/OIDC), **RBAC** por rol sanitario/administrativo.
- **WAF** delante de portal IIS si se usan servicios .asmx.
- **Vault/Keystore** para secretos.

---

## 3) Rutas de integración con Prevengos (hoy)

> Se emplearán **mecanismos soportados** por el fabricante y prácticas seguras. Cuando recibamos la especificación exacta, sustituiremos los *placeholders* por los nombres de tablas/columnas/formatos definitivos.

**A) RR. HH. (altas/actualizaciones)**
- Generación de **.mdb/.accdb** con **esquema Prevengos** (empresas/centros/trabajadores) desde el Hub.
- Entrega por **carpeta compartida/SMB** o **FTPS** a la ruta que monitoriza Prevengos; importación automática o programada.
- Uso de **UCanAccess** (Java) para crear/escribir ficheros Access sin componentes nativos.

**B) Médico / Analíticas**
- Emisión de ficheros de **resultados de pruebas** (formato Prevengos o de laboratorio soportado) para asociar cuestionarios a reconocimientos.

**C) Portal web / Servicios .asmx (SOAP)**
- Consumo **JAX‑WS/CXF** de métodos autorizados (crear cita, consultar aptitud, cerrar reconocimiento, etc.).
- Seguridad: TLS, credenciales de servicio, **allowlist de IP**, WAF y auditoría.

**D) SQL Server (lectura controlada)**
- Conexión de solo lectura a **vistas**/replica autorizada para consulta de estados/aptitudes e informes.

**E) Export/Drop de ficheros**
- Exportaciones a Excel/CSV cuando corresponda; jobs de escucha y reconciliación.

---

## 4) Contratos JSON (v1) – Canon del dominio

> **Versión**: `1.0.0`  ·  **Convención**: *snake_case* en propiedades, *kebab-case* en topics/eventos  ·  **Fechas**: ISO‑8601 con zona

### 4.1 Envoltorio de evento (`EventEnvelope`)
```json
{
  "event_id": "uuid",
  "event_type": "cuestionario-completado",
  "version": 1,
  "occurred_at": "2025-10-27T10:12:00+02:00",
  "source": "android-app",
  "correlation_id": "uuid",
  "payload": { }
}
```

### 4.2 Esquemas principales (extracto)

**Paciente**
```json
{
  "$schema": "https://example.org/schemas/paciente.json",
  "type": "object",
  "required": ["paciente_id","nif","nombre","apellidos","fecha_nacimiento","sexo"],
  "properties": {
    "paciente_id": {"type":"string","format":"uuid"},
    "nif": {"type":"string"},
    "nombre": {"type":"string"},
    "apellidos": {"type":"string"},
    "fecha_nacimiento": {"type":"string","format":"date"},
    "sexo": {"type":"string","enum":["M","F","X"]},
    "telefono": {"type":"string"},
    "email": {"type":"string","format":"email"},
    "empresa_id": {"type":"string"},
    "centro_id": {"type":"string"},
    "externo_ref": {"type":"string","description":"ID externo en Prevengos si existe"},
    "created_at": {"type":"string","format":"date-time"},
    "updated_at": {"type":"string","format":"date-time"}
  }
}
```

**Cuestionario** (cabecera + respuestas normalizadas)
```json
{
  "$schema": "https://example.org/schemas/cuestionario.json",
  "type":"object",
  "required":["cuestionario_id","paciente_id","plantilla_codigo","respuestas"],
  "properties":{
    "cuestionario_id":{"type":"string","format":"uuid"},
    "paciente_id":{"type":"string","format":"uuid"},
    "plantilla_codigo":{"type":"string"},
    "estado":{"type":"string","enum":["borrador","completado","validado"]},
    "respuestas":{"type":"array","items":{
      "type":"object",
      "required":["pregunta_codigo","valor"],
      "properties":{
        "pregunta_codigo":{"type":"string"},
        "valor":{"type":["string","number","boolean","null"]},
        "unidad":{"type":"string"},
        "metadata":{"type":"object"}
      }
    }},
    "firmas":{"type":"array","items":{"type":"string","contentEncoding":"base64"}},
    "adjuntos":{"type":"array","items":{"type":"string","contentEncoding":"base64"}},
    "created_at":{"type":"string","format":"date-time"},
    "updated_at":{"type":"string","format":"date-time"}
  }
}
```

**Cita/Reconocimiento** (resumen)
```json
{
  "cita_id":"uuid",
  "paciente_id":"uuid",
  "fecha":"2025-11-05T09:30:00+02:00",
  "tipo":"inicial|periodico|extraordinario",
  "estado":"planificada|en_curso|finalizada",
  "aptitud":"apto|apto_con_limitaciones|no_apto|pendiente",
  "externo_ref":"string"
}
```

> Mantener **versionado semántico** de esquemas; compatibilidad retroactiva mediante campos opcionales.

---

## 5) API del Hub (OpenAPI – extracto)

```yaml
openapi: 3.0.3
info:
  title: PRL Hub API
  version: 1.0.0
servers:
  - url: https://hub.prl.local/api
paths:
  /pacientes:
    post:
      summary: Alta/actualización de paciente
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/Paciente' }
      responses:
        '202': { description: Encolado para procesamiento }
  /cuestionarios:
    post:
      summary: Registrar cuestionario completado
      responses:
        '202': { description: Encolado }
  /sincronizacion/pull:
    get:
      summary: Pull incremental para móviles
      parameters:
        - in: query
          name: since
          schema: { type: string, format: date-time }
      responses:
        '200': { description: Cambios desde la marca temporal }
  /prevengos/jobs/rrhh:
    post:
      summary: Disparar generación de fichero Access RRHH y drop
      responses:
        '202': { description: Job aceptado }
components:
  schemas:
    Paciente: { }
```

**Webhooks de salida** (eventos):
- `paciente-registrado`, `cuestionario-completado`, `reconocimiento-actualizado`, `aptitud-cambiada`.

---

## 6) Sincronización offline‑first (móvil ⇄ hub)

**Estrategia**
- **CDC aplicativo**: cada entidad tiene `updated_at` y `change_version` (entero autoincremental por tabla).
- **Pull**: el cliente pide `/sincronizacion/pull?since=…` y recibe cambios por entidad ordenados.
- **Push**: el cliente envía lotes firmados; el Hub aplica **idempotencia** por `event_id`.
- **Resolución de conflictos**: *optimistic locking* (If‑Match con `change_version`), política por entidad (p.ej., última escritura válida por usuario con rol médico prevalece).
- **Compresión**: JSONL en `gzip` si el lote es grande.

**Pseudocódigo cliente**
```text
loop:
  if queue_outbound not empty: POST /sync/push (batch)
  GET /sync/pull?since=last_sync
  apply_changes(); last_sync = server_timestamp
  sleep(backoff)
```

---

## 7) Mapeo de datos hacia Prevengos (borrador)

> Se completará con las especificaciones del fabricante.

**RRHH (Access)**
| Nuestro campo                   | Tabla/columna Prevengos (Access) | Transformación |
|---|---|---|
| empresa_id                      | EMPRESA.ID_EXT                   | directo        |
| empresa_nombre                  | EMPRESA.NOMBRE                   | trim           |
| centro_id                       | CENTRO.ID_EXT                    | directo        |
| centro_nombre                   | CENTRO.NOMBRE                    | trim           |
| trabajador_id (paciente_id)     | TRAB.ID_EXT                      | uuid→texto     |
| nif                             | TRAB.NIF                         | normalizar     |
| nombre, apellidos               | TRAB.NOMBRE, TRAB.APELLIDOS      | mayúsculas     |
| fecha_nacimiento                | TRAB.FECHA_NAC                   | ISO→date       |

**Analíticas (fichero médico)**
| Origen (cuestionario) | Destino Prevengos | Observaciones |
|---|---|---|
| presión_arterial       | PRUEBA:PA         | mmHg         |
| imc                    | PRUEBA:IMC        | número       |
| audición_izq           | PRUEBA:AUD_L      | dB           |

---

## 8) Adaptador Prevengos – detalles técnicos

**RRHH (Access .mdb/.accdb)**
- Plantilla base en `/opt/prevengos/rrhh/template.accdb`.
- Proceso:
  1. Crear copia temporal por job.
  2. Insertar/actualizar tablas con UCanAccess.
  3. Validaciones (unicidad NIF, claves externas, encoding CP1252 si aplica).
  4. Firmar (hash SHA‑256) y mover a **drop**: `\\prevengos-srv\rrhh\inbox` o `ftps://prevengos.local/rrhh/inbox`.
  5. Registrar resultado (OK/errores) y correlación con entidades del Hub.

**Analíticas**
- Generar fichero plano/CSV/XML según espec. del proveedor; misma operativa de drop.

**SOAP (.asmx)**
- Generar cliente a partir de WSDL; configurar timeouts y *retry* exponencial solo en métodos idempotentes.
- Autenticación de servicio + mTLS si el portal lo soporta.
- Registrar request/response redactando datos sensibles para logs.

**SQL (read‑only)**
- DSN dedicado de solo lectura, vistas materializadas si se requiere rendimiento.
- Sin joins a tablas núcleo sin autorización; respetar ventana de mantenimiento.

---

## 9) Motor Python (FastAPI + Celery)

**Servicios**
- `/llm/anotar-cuestionario` (entrada: cuestionario; salida: resumen/alertas clínicas no diagnósticas).
- `/moodle/sync` (tareas programadas: alta de usuarios, progreso, certificaciones vía WS token).
- `/notify/whatsapp|sms|email` (plantillas, adjuntos).

**Pipelines**
- ETL de respuestas → normalización → indicadores (pandas) → export a Sheets/BigQuery (opcional).

**Seguridad**
- API interna detrás de red privada; autenticación con OAuth2 Client Credentials emitido por el IdP del Hub.

---

## 10) Notificaciones y canales

- **WhatsApp Business** (Cloud API o BSP) con plantillas aprobadas.
- **SMS** (Twilio/MessageBird) con trazabilidad.
- **Email** (SMTP transaccional) con DMARC/DKIM/SPF.
- **Política de consentimiento** por canal y propósito.

---

## 11) Seguridad y RGPD

- **DPIA** previa: categorías de datos de salud → medidas reforzadas.
- **Cifrado**: TLS 1.2+ en tránsito; FDE en móviles; cifrado por campo sensible en reposo.
- **Gestión de llaves/secretos**: Vault/Keystore; rotación periódica.
- **Autorización**: RBAC por rol; *break‑glass* auditado.
- **WAF/Hardening**: delante del portal Prevengos; segmentación de red; VPN site‑to‑site para drops.
- **Registro de actividad**: auditoría médica completa (quién/qué/cuándo/dónde).
- **Retención y borrado**: políticas por normativa, anonimización para analítica.

---

## 12) Observabilidad

- **Dashboards**: tasas de sync, tiempos de jobs Prevengos, colas, errores SOAP, tamaño de drops.
- **Alerting**: umbrales en reintentos, latencias, ocupación de colas, fallos de import.
- **Trazas**: correlación por `correlation_id` desde móvil hasta Prevengos y vuelta.

---

## 13) CI/CD y entornos

- **Repos**: monorepo con *workspaces* o multi‑repo por módulo.
- **Pipelines**:
  1. Lint/format + unit tests.
  2. *Contract tests* (Pact) entre apps/hub/python.
  3. Seguridad (SAST/DAST/dep scan).
  4. Build imágenes Docker (Hub, Python, Jobs).
  5. Migraciones DB (Flyway/Liquibase).
  6. Deploy a **DEV → UAT → PROD** con *blue/green* o *canary*.

- **Infra**: Kubernetes (K3s/K8s), ingress con TLS, secreto de FTPS/SMB como *Secret*.

---

## 14) Estructura de proyecto (Gradle – ejemplo)

```
root
├─ buildSrc/                # convenciones
├─ modules/
│  ├─ domain/               # entidades, servicios puros
│  ├─ app/                  # casos de uso
│  ├─ api-rest/             # controladores, DTOs, OpenAPI
│  ├─ adapter-prevengos/    # rrhh, analiticas, soap, sqlro
│  ├─ adapter-moodle/
│  ├─ adapter-notify/
│  ├─ infra-persistence/    # postgres, outbox
│  ├─ infra-messaging/      # rabbit/redis
│  └─ infra-security/       # oauth2, rbac
├─ python-engine/           # FastAPI + Celery
├─ android-app/             # app móvil
└─ desktop-app/             # JavaFX o web-electron
```

**build.gradle.kts (raíz – extracto)**
```kotlin
subprojects {
  repositories { mavenCentral() }
}
```

---

## 15) Ejemplos técnicos (extractos)

**15.1 Java – escribir Access (RRHH) con UCanAccess**
```java
Connection c = DriverManager.getConnection("jdbc:ucanaccess:///tmp/rrhh.accdb");
try (PreparedStatement st = c.prepareStatement(
  "INSERT INTO TRAB (ID_EXT,NIF,NOMBRE,APELLIDOS,FECHA_NAC) VALUES (?,?,?,?,?)")) {
  st.setString(1, trabajadorId);
  st.setString(2, nif);
  st.setString(3, nombre);
  st.setString(4, apellidos);
  st.setDate(5, Date.valueOf(fechaNac));
  st.executeUpdate();
}
```

**15.2 Java – cliente SOAP (.asmx) con JAX‑WS**
```java
URL wsdl = new URL("https://portal.prevengos.local/servicios/Servicio.asmx?wsdl");
QName qname = new QName("http://tempuri.org/", "Servicio");
Service svc = Service.create(wsdl, qname);
Servicio port = svc.getPort(Servicio.class);
// port.setEndpointAddress(...), timeouts, handlers de seguridad
Resultado r = port.crearCita(token, pacienteExtId, fechaISO);
```

**15.3 Spring – endpoint de sync (pull)**
```java
@GetMapping("/sincronizacion/pull")
public Changes pull(@RequestParam Instant since) {
  return syncService.fetchChangesSince(since);
}
```

**15.4 Python FastAPI – notificación**
```python
from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()

class Msg(BaseModel):
    canal: str
    destino: str
    plantilla: str
    datos: dict

@app.post('/notify/send')
async def send(m: Msg):
    # encolar a Celery según canal
    return {"status": "queued"}
```

**15.5 Android (Room) – entidad y DAO**
```java
@Entity(tableName = "cuestionarios")
public class CuestionarioEntity {
  @PrimaryKey @NonNull public String cuestionarioId;
  public String pacienteId;
  public String plantillaCodigo;
  public String estado; // borrador|completado|validado
  public String respuestasJson;
  public long updatedAt;
}

@Dao
interface CuestionarioDao {
  @Query("SELECT * FROM cuestionarios WHERE updatedAt > :since")
  List<CuestionarioEntity> changedSince(long since);
}
```

**15.6 SQL – vista read‑only para aptitudes**
```sql
CREATE VIEW v_aptitudes AS
SELECT a.TrabajadorIdExt AS paciente_ext,
       a.ReconocimientoId AS reconocimiento,
       a.AptitudCodigo AS aptitud,
       a.Fecha AS fecha
FROM   PrevengosDB.dbo.Aptitudes a;
```

---

## 16) Plan de implantación (12 semanas – ejemplo)

1. **Descubrimiento y contratos (S1‑S2)**: catálogo de campos Prevengos (RRHH/analíticas/SOAP), DPIA, diagramas.
2. **MVP Offline (S3‑S4)**: app Android básica, API Hub, sync mínimo, PostgreSQL.
3. **Adapter RRHH (S5)**: generación Access y drop; pruebas de import.
4. **Adapter Analíticas (S6)**: fichero de resultados + reconciliación.
5. **Lectura SQL (S7)**: vistas read‑only, sincronización de aptitudes/estados al móvil.
6. **Notificaciones y Python (S8‑S9)**: FastAPI+Celery, WhatsApp/SMS, plantillas.
7. **SOAP opcional (S10)**: métodos autorizados, WAF, *allowlist*.
8. **End‑to‑end, hardening, observabilidad (S11)**.
9. **UAT y Go‑Live (S12)**: formación, manuales, *runbooks*.

---

## 17) Requisitos de infraestructura

- **Hub/Python**: 2‑3 nodos (4 vCPU, 8‑16 GB RAM) + PostgreSQL HA.
- **Mensajería**: RabbitMQ cluster pequeño (o Redis HA).
- **Almacenamiento**: S3/MinIO 1‑2 TB inicial.
- **Conectividad**: VPN site‑to‑site a red Prevengos; FTPS/SMB seguro para drops.
- **WAF**: delante de portal IIS si se usa SOAP; reglas anti‑SQLi/XSS.
- **Backups**: 3‑2‑1; RPO ≤ 1h, RTO ≤ 4h; pruebas de restauración.

---

## 18) Testing y calidad

- **Contract tests** (Pact) entre móvil/hub y hub/python.
- **Pruebas de importación** (RRHH/analíticas) con datasets sintéticos.
- **E2E**: escenarios de clínica (cita → cuestionario → reconocimiento → aptitud).
- **Carga**: 95p latencia < 300 ms en sync; cola vaciada < 2 min.
- **Seguridad**: SAST/DAST, pentest, escáner de dependencias.

---

## 19) Operación y soporte

- **Runbooks**: cómo reintentar jobs fallidos, rotación de claves, ampliar colas.
- **KPIs**: tasa de éxito de import, tiempo de procesamiento, errores por tipo.
- **Feature flags** para activar integraciones gradualmente.

---

## 20) Roadmap futuro

- **LLM**: resúmenes de cuestionarios, extracción de señales, asistentes internos.
- **Google**: exportaciones a Sheets/Drive, notificaciones Gmail.
- **Analytics**: paneles de indicadores de salud ocupacional, cohortes.
- **Firma biométrica** y verificación documentaria.

---

## 21) Checklist previo a producción

- [ ] Portal Prevengos actualizado y protegido (WAF, listas blancas, TLS).
- [ ] Contratos JSON congelados (v1) y *contract tests* en verde.
- [ ] Jobs RRHH/Analíticas con *retry* e idempotencia verificada.
- [ ] Auditoría end‑to‑end y DPIA firmada.
- [ ] Copias de seguridad probadas y cifrado en reposo activado.
- [ ] Procedimiento de revocación de acceso a dispositivos móviles.

---

## 22) Glosario
- **RRHH**: Altas/actualizaciones de empresas, centros y trabajadores.
- **Drop**: Carpeta/FTPS donde se depositan ficheros para importación automática.
- **SOAP .asmx**: servicios web del portal Prevengos.
- **Offline‑first**: diseño que prioriza operación sin conectividad.
- **Idempotencia**: aplicar la misma operación múltiples veces con el mismo resultado.

---

> **Siguientes pasos**: cuando nos compartas los **formatos exactos** (esquema Access RRHH, layout de analíticas y WSDL de servicios), rellenamos los mapeos definitivos, generamos clientes y añadimos pruebas de contrato y datasets de ejemplo en este mismo canvas.

