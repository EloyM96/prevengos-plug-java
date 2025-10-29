# Aplicación Android Prevengos Plug

Este módulo contiene la aplicación Android usada por equipos de campo para registrar pacientes y cuestionarios sin conexión y sincronizarlos con el hub PRL corporativo. El `BuildConfig.SYNC_BASE_URL` se inyecta en tiempo de compilación desde `gradle.properties`, lo que permite apuntar la app a diferentes hubs sin modificar el código fuente.【F:android-app/build.gradle†L6-L35】

## Arquitectura y componentes clave

La app sigue una arquitectura ligera basada en capas:

- **Arranque y sincronización**: `PrevengosApplication` inicializa el contenedor de dependencias y agenda un trabajo periódico de sincronización (`WorkManager`) cada 6 horas, además de exponer un método para sincronizaciones inmediatas tras cada alta o edición.【F:android-app/src/main/java/com/prevengos/plug/android/PrevengosApplication.java†L15-L41】
- **Contenedor de dependencias**: `AppContainer` crea la base de datos Room, configura Retrofit/OkHttp con el endpoint definido, y expone repositorios para pacientes, cuestionarios y sincronización.【F:android-app/src/main/java/com/prevengos/plug/android/di/AppContainer.java†L1-L88】
- **Capa de datos**: `PrevengosDatabase` encapsula Room y expone los DAO que gestionan entidades locales y metadatos de sincronización.【F:android-app/src/main/java/com/prevengos/plug/android/data/local/PrevengosDatabase.java†L1-L47】 Los repositorios (`PacienteRepository`, `CuestionarioRepository`, `SyncRepository`) coordinan la persistencia local y las llamadas al API remoto.【F:android-app/src/main/java/com/prevengos/plug/android/data/repository/SyncRepository.java†L1-L118】
- **Interfaz de usuario**: `MainActivity` presenta listados maestro-detalle de pacientes y cuestionarios, gestionando formularios modales para altas/ediciones y delegando la lógica en `MainViewModel` y sus adaptadores de RecyclerView.【F:android-app/src/main/java/com/prevengos/plug/android/MainActivity.java†L1-L123】 `MainViewModel` orquesta operaciones con los repositorios, valida formularios y dispara sincronizaciones a través de la aplicación.【F:android-app/src/main/java/com/prevengos/plug/android/ui/MainViewModel.java†L1-L120】

## Estructura de carpetas

```
android-app/
├── build.gradle           # Configuración del módulo, dependencias y BuildConfig.SYNC_BASE_URL
├── src/main
│   ├── AndroidManifest.xml
│   ├── java/com/prevengos/plug/android
│   │   ├── PrevengosApplication.java
│   │   ├── MainActivity.java
│   │   ├── data/…          # Entidades Room, DAO y repositorios
│   │   ├── di/…            # AppContainer y wiring de dependencias
│   │   ├── sync/…          # Worker y utilidades de sincronización
│   │   └── ui/…            # ViewModel, adapters y estado de UI
│   └── res/                # Layouts con ViewBinding y recursos estáticos
├── src/test                # Tests unitarios JVM
└── src/androidTest         # Tests instrumentados (opcional)
```

## Puesta en marcha rápida

1. Configura el endpoint del hub ejecutando el script `docs/scripts/android/setup_android_app.sh`, que valida la URL y actualiza `gradle.properties` con `prevengosApiBaseUrl` antes de compilar.【F:docs/operations/android-app-onboarding.md†L21-L40】
2. Genera el wrapper si es la primera vez en la máquina: `gradle wrapper --gradle-version 8.14.3`.
3. Compila un APK debug para validaciones locales:
   ```bash
   ./gradlew :android-app:assembleDebug
   adb install -r android-app/build/outputs/apk/debug/android-app-debug.apk
   ```
4. Para builds de release, ejecuta `assembleRelease` o `bundleRelease` y aplica tu keystore siguiendo la guía de distribución.【F:docs/operations/android-build-and-distribution.md†L17-L47】

## Tests

Ejecuta la batería de tests unitarios JVM con:
```bash
./gradlew :android-app:test
```
La suite valida el flujo de sincronización mockeando los DAO y el API remoto para comprobar que se limpian marcadores `dirty`, se actualiza el token y se persistente la descarga.【F:docs/operations/android-build-and-distribution.md†L33-L41】【F:android-app/src/test/java/com/prevengos/plug/android/data/repository/SyncRepositoryTest.java†L1-L104】 Usa `:android-app:connectedAndroidTest` para pruebas instrumentadas si dispones de dispositivos.

## Documentación relacionada

- [Puesta en marcha de la app Android](../docs/operations/android-app-onboarding.md)
- [Guía de compilación y distribución](../docs/operations/android-build-and-distribution.md)
- [Quickstart general del monorepo](../docs/quickstart.md)

Estas guías complementan la configuración específica del módulo y los procesos operativos de sincronización.
