# Guía de compilación y distribución de la app Android

Esta guía describe cómo generar artefactos reproducibles de la app Android de Prevengos Plug, ejecutar la batería de tests unitarios y preparar entregables firmados para distribución interna o en Play Store.

## 1. Requisitos previos

* Android Studio Giraffe o superior con JDK 17.
* Herramientas de línea de comandos de Android (`sdkmanager`, `adb`).
* Node.js 20+ si vas a reutilizar los mocks de sincronización en local.
* Configura el endpoint del hub ejecutando el script documentado en la guía de onboarding [`docs/operations/android-app-onboarding.md`](android-app-onboarding.md); este script actualiza `gradle.properties` para inyectar `BuildConfig.SYNC_BASE_URL`.【F:android-app/build.gradle†L7-L35】

## 2. Compilaciones locales

* **Debug APK** — útil para validaciones rápidas o para instalar en dispositivos de QA:
  ```bash
  ./gradlew :android-app:assembleDebug
  adb install -r android-app/build/outputs/apk/debug/android-app-debug.apk
  ```
* **Release AAB/APK sin firmar** — genera artefactos optimizados sobre los que aplicarás tu keystore:
  ```bash
  ./gradlew :android-app:assembleRelease
  ./gradlew :android-app:bundleRelease
  ```
  Los resultados se almacenan en `android-app/build/outputs/` diferenciados por tipo (`apk/` y `bundle/`).

## 3. Tests automatizados

Los tests unitarios JVM aseguran que el repositorio de sincronización marca correctamente los lotes, interpreta los pulls y limpia entidades en conflicto.

```bash
./gradlew :android-app:test
```

La suite utiliza mocks de Retrofit y Room para cubrir los escenarios descritos en `SyncRepository`.【F:android-app/src/main/java/com/prevengos/plug/android/data/repository/SyncRepository.java†L1-L199】【F:android-app/src/test/java/com/prevengos/plug/android/data/repository/SyncRepositoryTest.java†L1-L186】 Ejecuta `./gradlew :android-app:connectedAndroidTest` si necesitas instrumentar dispositivos físicos.

## 4. Firma y configuración de release

1. Crea o reutiliza un keystore (`.jks`) siguiendo las políticas de TI.
2. Añade el bloque `signingConfigs` en `android-app/build.gradle` o usa la configuración asistida de Android Studio (`Build > Generate Signed Bundle / APK`).
3. Exporta las credenciales sensibles a un `gradle.properties` local (no las subas al repositorio) y referencia las propiedades en el bloque `release`.
4. Vuelve a ejecutar `assembleRelease`/`bundleRelease` para generar artefactos firmados.

## 5. Distribución y checklist final

* Verifica que `BuildConfig.SYNC_BASE_URL` apunta al hub correcto (p. ej. mediante `adb shell getprop` o revisando `BuildConfig`).【F:android-app/build.gradle†L7-L19】
* Adjunta el changelog correspondiente (`docs/CHANGELOG.md`) y documentación operativa de sincronización (`docs/quality/manual-sync-checklist.md`).
* Para despliegues internos, comparte el APK firmado y la URL del hub local; para Play Store sube el `.aab` y completa la ficha según las políticas corporativas.
