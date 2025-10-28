# Puesta en marcha de la app Android Prevengos Plug

Este procedimiento guía a equipos no técnicos en la configuración inicial de la aplicación Android, desde la comprobación de requisitos hasta la sincronización con el hub PRL. Sigue los pasos en orden y marca cada hito para garantizar una instalación homogénea.

## 1. Requisitos previos

| Elemento | Detalle |
| --- | --- |
| Dispositivo | Terminal Android con **Android 7.0 (API 24)** o superior y al menos 2 GB de memoria libre. |
| Conectividad | Acceso a la red corporativa donde resida el hub PRL (Wi-Fi o datos). |
| Servidor hub | Endpoint HTTPS expuesto por el hub PRL (`https://servidor.local:8443/api`, por ejemplo). |
| Puesto de trabajo | Equipo Windows/macOS/Linux con **Java 17** y permisos para ejecutar scripts. |
| Herramientas | Gradle Wrapper incluido en el repositorio (`./gradlew`); el archivo `gradle-wrapper.jar` debe generarse localmente ejecutando `gradle wrapper` con Gradle 8.14.3. El Android SDK se descarga automáticamente al compilar. |

> 💡 **Consejo**: si no dispones de Java 17 instalado, solicita al área de sistemas que valide la instalación antes de continuar.

## 2. Preparar el entorno de compilación

1. Clona o descarga el repositorio `prevengos-plug-java` en el puesto de trabajo operativo.
2. Abre una consola en la carpeta raíz del proyecto.
3. Si es la primera vez que se usa el repositorio en esa máquina, ejecuta `gradle wrapper --gradle-version 8.14.3` para recrear `gradle/wrapper/gradle-wrapper.jar`. (Se requiere tener Gradle instalado de forma local únicamente para este paso.)
4. Ejecuta el script de soporte para registrar la URL del hub (ver apartado siguiente). El script creará o actualizará el archivo `gradle.properties` sin necesidad de editar código manualmente.【F:docs/scripts/android/setup_android_app.sh†L1-L102】
5. (Opcional) Si es la primera vez que se compila, deja que Gradle descargue las dependencias; puede tardar varios minutos.

## 3. Configurar el endpoint del hub

La app obtiene la dirección del hub desde un campo de configuración generado en tiempo de compilación. Por defecto apunta a `https://api.prevengos.test/`, pero puede cambiarse sin editar código usando el script `docs/scripts/android/setup_android_app.sh`:

```bash
./docs/scripts/android/setup_android_app.sh --base-url https://hub.prevengos.corp:8443/api --run-build --build-type assembleRelease
```

El script valida la URL, añade la barra final si falta y registra el valor en `gradle.properties`. Posteriormente, al compilar, Gradle inyecta ese dato en la constante `BuildConfig.SYNC_BASE_URL`, que la app utiliza para crear el cliente Retrofit responsable de las llamadas `sincronizacion/push` y `sincronizacion/pull`.【F:android-app/build.gradle†L7-L35】【F:android-app/src/main/java/com/prevengos/plug/android/di/AppContainer.java†L7-L65】【F:android-app/src/main/java/com/prevengos/plug/android/data/remote/api/PrevengosSyncApi.java†L1-L26】

### Verificación rápida

1. Comprueba que el archivo `gradle.properties` contiene una línea `prevengosApiBaseUrl=https://hub.prevengos.corp:8443/api/`.
2. Ejecuta `./gradlew assembleRelease` (o `assembleDebug`) para generar el APK.
3. Localiza el paquete generado en `android-app/build/outputs/apk/` y distribúyelo mediante el canal interno establecido (MDM, instalación manual, etc.).

## 4. Entender la sincronización con el hub PRL

* **Sincronización en segundo plano**: al abrir la app por primera vez, se programa un trabajo periódico (`prevengos-sync`) que envía y recupera cambios cada 6 horas mediante `WorkManager`. No requiere intervención manual.【F:android-app/src/main/java/com/prevengos/plug/android/PrevengosApplication.java†L4-L44】
* **Sincronización bajo demanda**: cada alta o actualización de pacientes/cuestionarios ejecuta una sincronización inmediata para no esperar al ciclo de fondo.【F:android-app/src/main/java/com/prevengos/plug/android/ui/MainViewModel.java†L1-L162】
* **Qué se intercambia**:
  - Cambios locales pendientes (`dirty`) se envían como un lote JSON único a `sincronizacion/push` junto con `source` y `correlationId`.
  - El hub responde con el último `syncToken` aplicado, que se almacena en la tabla local `sync_metadata` para evitar duplicados.
  - Las descargas posteriores recuperan hasta 200 elementos por llamada usando `sincronizacion/pull`.【F:android-app/src/main/java/com/prevengos/plug/android/data/repository/SyncRepository.java†L1-L183】

> ✅ **Consejo operativo**: si el equipo necesita forzar una sincronización (por ejemplo, tras restaurar un backup del hub), basta con crear un paciente de prueba y eliminarlo. Esto provocará el disparo inmediato de `WorkManager` y permitirá verificar la conectividad sin tocar ajustes avanzados.

## 5. Checklist final antes de entregar el dispositivo

- [ ] APK instalado en el dispositivo correcto.
- [ ] Inicio de sesión o primer arranque realizado, confirmando que los listados aparecen vacíos pero operativos.
- [ ] Conectividad verificada: realizar un alta de paciente de prueba y confirmar que desaparece tras sincronizar con el hub.
- [ ] Documentar la URL del hub y el dispositivo entregado en el registro de activos.

Una vez completados estos pasos, el terminal queda listo para su uso en campo sin necesidad de soporte técnico adicional.
