# Pruebas manuales en entorno local

Esta guía resume cómo levantar el hub local de Prevengos Plug y ejecutar un recorrido
manual end-to-end sobre SQL Server. Toma como punto de partida la [Guía rápida](../quickstart.md)
y complementa el checklist de sincronización manual en [`docs/quality/manual-sync-checklist.md`](../quality/manual-sync-checklist.md).

## 1. Requisitos previos

Asegúrate de tener instalados los componentes indicados en la guía rápida:

- **JDK 21** para construir y ejecutar los módulos Java.
- **Docker Engine 24+** y **Docker Compose V2** para levantar SQL Server y el backend Spring Boot.
- **Node.js 20+** si piensas ejecutar las pruebas end-to-end alojadas en `tests/e2e`.

Clona el repositorio y copia el entorno de ejemplo:

```bash
git clone git@github.com:prevengos/prevengos-plug-java.git
cd prevengos-plug-java
cp infra/local-hub/.env.example infra/local-hub/.env
```

## 2. Arranque del hub con SQL Server

Desde la raíz del repositorio ejecuta:

```bash
docker compose --env-file infra/local-hub/.env -f infra/local-hub/docker-compose.yml up --build
```

El `docker-compose.yml` inicia dos contenedores:

1. **prevengos-local-sqlserver**: instancia de SQL Server con la base y credenciales del hub.
2. **hub-backend**: aplicación Spring Boot que expone `/sincronizacion/*` y `/actuator/*`.

Cuando los contenedores estén disponibles, comprueba el estado de la API:

```bash
curl http://localhost:8080/actuator/health
```

## 3. Inyección de datos para pruebas manuales

1. Genera `pacientes.json` y `cuestionarios.json` empleando las plantillas del directorio `contracts/json`
   o exportando desde la app Android/desktop.
2. Sigue los pasos del [checklist de sincronización manual](../quality/manual-sync-checklist.md)
   para subir los payloads mediante los scripts de `scripts/manual-sync`, validar las tablas con `sqlcmd`
   y ejecutar pulls subsecuentes.
3. Registra los resultados y métricas observadas (por ejemplo, `hub.sync.events.registered`) con los
   endpoints de `/actuator` mencionados en la guía rápida.

## 4. Variantes y soporte

- Si necesitas un motor alternativo para pipelines o entornos sin SQL Server, utiliza la composición
  de [`infra/postgresql`](../../infra/postgresql/README.md), recordando que su alcance es exclusivamente
  de pruebas.
- Los scripts reutilizables para generar lotes, limpiar tablas y consultar diagnósticos están en
  [`scripts/manual-sync`](../../scripts/manual-sync/README.md).

Con estos pasos el repositorio queda listo para recorridos manuales en local, desde la generación de
payloads hasta la verificación de la base de datos y la observabilidad del hub.
