# Estrategia de Calidad

## Métricas objetivo
- **Cobertura de código**: ≥ 85% global, ≥ 70% por módulo crítico (`modules/api-rest`, `modules/gateway`).
- **Contratos integrados**: 100% de endpoints documentados en OpenAPI con Pact verificado.
- **Vulnerabilidades**: 0 vulnerabilidades críticas, máximo 2 altas abiertas con plan de mitigación < 7 días.
- **Tiempo medio de corrección (MTTR)**: < 24 horas para incidencias de severidad alta.

## Políticas de aprobación
- Pull Requests requieren al menos **2 aprobaciones** (líder técnico + QA) antes de mergear.
- Bloqueo automático de PR si fallan unidades, contratos, E2E o análisis SAST/DAST.
- Cambios en contratos (`contracts/**`) requieren revisión de los equipos consumidores suscritos.
- Releases a producción necesitan evidencia de checklist Go-Live completada y reporte de riesgos actualizado.

## Gobernanza de pruebas
- Unitarias y de integración ejecutadas en cada commit (`./gradlew clean build`).
- Contratos Pact verificados en ramas y publicados en broker al hacer merge.
- Suite E2E Playwright nightly con reporte HTML y retención de trazas 14 días.
- Escaneos de dependencias (OWASP Dependency-Check) en cada PR; fallos ≥7 CVSS bloquean merge.

## Gestión de vulnerabilidades
- SonarQube actualizado con Quality Gate custom: cobertura mínima 85%, debt ratio < 5%.
- OWASP ZAP baseline en staging tras cada despliegue; findings críticos bloquean promoción.
- Revisión trimestral de dependencias y contenedores con soporte extendido.

## Mejora continua
- Retro trimestral de calidad con seguimiento de acciones y métricas.
- Automatización de métricas en tablero compartido (Datadog/Grafana) con acceso a stakeholders.
- Formación anual obligatoria en seguridad segura y respuesta a incidentes para todo el equipo técnico.
