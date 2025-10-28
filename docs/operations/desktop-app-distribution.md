# Distribución de la app de escritorio Prevengos Plug

> ⚠️ **Estado del documento**: la aplicación de escritorio aún no se ha implementado. Esta guía captura el diseño previsto para compilación y distribución y se activará cuando exista un prototipo funcional.

El contenido sirve como referencia de planificación; los pasos concretos se confirmarán durante el desarrollo.

## 1. Requisitos _(diseño preliminar)_

* JDK 17 con soporte para JavaFX (el proyecto declara la toolchain en Gradle).【F:desktop-app/build.gradle†L1-L38】
* Dependencias resueltas mediante `./gradlew` (no es necesario instalar Maven).
* Acceso a los ficheros de configuración descritos en [`desktop-app/README.md`](../../desktop-app/README.md) para parametrizar la sincronización.

## 2. Ejecución local y pruebas _(pendiente de implementación)_

* Arranca la aplicación en modo desarrollo:
  ```bash
  ./gradlew :desktop-app:run
  ```
* Ejecuta la suite de tests (incluye configuración headless con TestFX):
  ```bash
  ./gradlew :desktop-app:test
  ```
  El `build.gradle` ya fuerza los flags Monocle y Prism para entornos CI, por lo que no necesitas variables adicionales.【F:desktop-app/build.gradle†L30-L47】

## 3. Empaquetado _(se definirá junto a los artefactos reales)_

* **JAR auto-contenido** — genera un ejecutable con dependencias incluidas:
  ```bash
  ./gradlew :desktop-app:fatJar
  ```
  El artefacto aparece en `desktop-app/build/libs/` con sufijo `-all.jar`.【F:desktop-app/build.gradle†L39-L58】
* **Instaladores nativos** — si necesitas `.msi` o `.pkg`, importa el módulo en herramientas como jpackage apuntando al `fatJar` y añade los recursos descritos en el README.

## 4. Checklist previo a distribución _(quedará habilitado tras el primer release)_

1. Revisa el fichero de configuración (`config/application.yaml`) siguiendo la sección “Sincronización con el Hub PRL” del README de escritorio.【F:desktop-app/README.md†L9-L37】
2. Valida que los endpoints `/sincronizacion` del hub estén operativos (puedes reutilizar la suite Playwright o `curl`).
3. Incluye junto al paquete la documentación de sincronización (`docs/quality/manual-sync-checklist.md`) y notas de versión (`docs/CHANGELOG.md`).
