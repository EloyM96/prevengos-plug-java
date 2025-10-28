# Preparación para integraciones con `prl-notifier`

Este documento consolida el estado actual del hub Java y las acciones mínimas
requeridas para que, en cuanto se definan funcionalidades compartidas con el
monolito FastAPI/Next.js (`prl-notifier`), la interoperabilidad sea directa,
predecible y escalable.

## Alcance actual garantizado

- **Sincronización local**: las apps Android y de escritorio consumen los
  contratos serializados en `modules/shared` y se comunican con el backend del
  hub (`modules/hub-backend`) mediante los endpoints documentados en OpenAPI
  ([`docs/apis/prl-hub/openapi.v0.1.0.yaml`](../apis/prl-hub/openapi.v0.1.0.yaml)). El backend opera exclusivamente contra la
  base de datos SQL Server y los intercambios CSV de Prevengos.
- **Intercambio CSV operativo**: los procedimientos descritos en
  [`docs/integrations/drops.md`](./drops.md) y el runbook de RRHH cubren la
  generación, validación y archivado de drops. Con ello se garantiza el canal de
  datos que `prl-notifier` puede consumir en modo «read-only».
- **Instrumentación y auditoría**: `SyncEventService` y los jobs expuestos en el
  backend almacenan eventos serializados con metadatos incrementales. Esto
  habilita futuros extractores o replicadores sin alterar los clientes actuales.

## Puntos de integración listos

| Dominio | Preparación actual | Próximos pasos cuando se concreten requisitos |
| --- | --- | --- |
| **Contratos JSON** | DTOs Java alineados con el *event envelope* en `modules/shared/contracts`. | Incorporar los modelos equivalentes a los contratos FastAPI (non-compliance, notifications, uploads) en el mismo módulo. |
| **Datos operacionales** | Exportaciones CSV y vistas SQL documentadas para pacientes y cuestionarios. | Añadir vistas y drops para cursos, matrículas y reglas cuando los YAML del repositorio Python se confirmen. |
| **Eventos de sincronización** | Registro persistente de jobs y eventos en tablas propias del hub. | Publicar un canal de replicación (JDBC read-only o CDC) para que `prl-notifier` consuma los eventos con latencia mínima. |
| **Configuración** | Variables para rutas de drops y SQL Server + `.env.sample` raíz y plantilla `infra/local-hub/.env.example` compartidas. | Coordinar documentación conjunta con las variables Postgres/Redis/Moodle del monolito FastAPI. |
| **Automatizaciones** | Scripts en `infra/automation` listos para *cron* y checklists de operación. | Extender los scripts para disparar webhooks o colas cuando se habiliten adaptadores de notificaciones externos. |

## Checklist de preparación continua

1. **Alinear dependencias**: mantener actualizados `build.gradle` y las
   versiones compartidas con `prl-notifier` (p. ej. librerías de testing o
   serialización) documentando cualquier divergencia.
2. **Versionar contratos compartidos**: cuando se definan nuevas respuestas JSON
   o YAML de reglas/playbooks, replicar su definición en `modules/shared` y
   publicarla en `contracts/` para tests cruzados.
3. **Documentar entornos**: ampliar `docs/operations` con guías de despliegue en
   escenarios híbridos (hub local + `prl-notifier` en la nube) y describir cómo
   compartir variables sensibles mediante gestores de secretos.
4. **Pruebas end-to-end**: añadir suites de integración que validen ingestiones
   CSV y consultas REST simulando el consumo desde `prl-notifier`. Las pruebas
   deben residir en `tests/e2e` con *fixtures* compartidos.
5. **Observabilidad**: estandarizar el uso de `job_id`, `status`, `channel` y
   otros campos sugeridos por `prl-notifier` dentro de los logs y eventos del
   hub para garantizar la correlación.

## Consideraciones de escalabilidad

- **Separación de responsabilidades**: mantener la lógica de ingestión y reglas
  de negocio en artefactos compartidos (YAML, DTOs) para que futuros repos (Java
  o Python) consuman la misma fuente de verdad.
- **Extensibilidad por módulos**: nuevas integraciones deben exponerse como
  módulos adicionales dentro de `modules/`, evitando acoplarlas a clientes
  específicos y facilitando su reutilización.
- **Resiliencia operativa**: cualquier ampliación de drops debe conservar la
  política de archivado y validaciones checksum existente para simplificar la
  observabilidad entre equipos.
- **Compatibilidad temporal**: documentar cambios de esquema con versionado
  semántico y migraciones SQL atómicas (`migrations/`) que permitan mantener
  integraciones antiguas mientras se adopta la nueva versión.

## Próximas decisiones a documentar

- Identificadores oficiales de playbooks/reglas compartidos con `prl-notifier`.
- Estrategia de distribución de YAML (submódulo Git, artefacto Maven, paquete
  Python) para garantizar consistencia en ambos stacks.
- Lineamientos de seguridad para exponer los endpoints del hub a servicios
  externos sin comprometer el entorno local.

Mantener este documento actualizado a medida que se concreten las
funcionalidades compartidas garantizará que el repositorio Java siga siendo la
pieza de sincronización básica pero preparada para integrarse con el ecosistema
`prl-notifier` sin sorpresas.
