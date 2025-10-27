# Checklist de Go-Live

## Monitoreo
- [ ] Dashboard en Grafana con métricas de latencia P95/P99 para API REST.
- [ ] Alertas de error rate (>2% en 5 min) con integración en Slack/Teams.
- [ ] Trazas distribuidas habilitadas (OpenTelemetry) para flujos críticos de onboarding.
- [ ] Validación de retención de logs (mínimo 30 días) en Loki/ELK.

## Alertas
- [ ] Alertas de disponibilidad (uptime < 99.5%) conectadas a canal 24/7.
- [ ] Alertas de capacidad (CPU > 80%, memoria > 75%) con runbook asociado.
- [ ] Alertas funcionales sobre colas atrasadas (>5 min) y reintentos fallidos.

## Seguridad
- [ ] Revisiones SAST/DAST aprobadas sin vulnerabilidades críticas abiertas.
- [ ] Certificados TLS actualizados y automatizados (Let's Encrypt/ACM) con renovación monitorizada.
- [ ] Gestión de secretos con rotación semestral y auditoría habilitada.
- [ ] Hardening de contenedores (non-root user, mínimo de capabilities, escaneo de imágenes).

## Cumplimiento
- [ ] DPIA/PIA documentado y firmado por Seguridad y Legal.
- [ ] Evidencias de consentimiento explícito guardadas y accesibles.
- [ ] Trazabilidad de cambios (RFCs) almacenada y aprobada.
- [ ] Revisiones de accesibilidad WCAG 2.1 nivel AA completadas.

## Reversibilidad
- [ ] Plan de rollback probado en entorno staging con tiempo objetivo < 15 min.
- [ ] Backups consistentes validados (restore dry-run semanal).
- [ ] Feature flags disponibles para desactivar funcionalidades críticas sin redeploy.
- [ ] Procedimiento de comunicación de incidentes listo (contactos, plantillas, SLA).
