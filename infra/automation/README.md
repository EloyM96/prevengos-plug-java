# Automatización intercambios RRHH

El script `rrhh_csv_exchange.sh` ejecuta en secuencia los endpoints internos del
hub para generar los CSV de salida y consumir los ficheros entrantes.

## Variables de entorno

| Variable         | Descripción                                                    | Valor por defecto |
|------------------|----------------------------------------------------------------|-------------------|
| `HUB_BASE_URL`   | URL base del hub (incluye protocolo y puerto).                 | `http://localhost:8080` |
| `HUB_API_TOKEN`  | Token Bearer opcional para proteger los endpoints.             | *(vacío)* |
| `LOGGER_TAG`     | Etiqueta utilizada por el comando `logger`.                    | `prevengos-rrhh` |
| `CURL_TIMEOUT`   | Tiempo máximo (segundos) para cada invocación HTTP.            | `30` |

## Ejecución manual

```bash
HUB_BASE_URL="https://hub.prevengos.local" \
  HUB_API_TOKEN="token-super-secreto" \
  ./rrhh_csv_exchange.sh
```

El script escribe un resumen en STDOUT y en syslog (si está disponible). Cualquier
error retorna código distinto de cero para facilitar la monitorización.

## Programación recomendada

Para cron:

```
0 3 * * * HUB_BASE_URL=https://hub.prevengos.local /opt/prevengos/rrhh_csv_exchange.sh
```

Para `systemd`, crear una unidad `prevengos-rrhh.service` que ejecute el script y
una unidad `prevengos-rrhh.timer` configurada a las 03:00 CET.
