# Prevengos Plug

## Visión general
Prevengos Plug es un programa 100 % Java pensado para ejecutarse en entornos controlados de Prevengos. El objetivo es ofrecer una herramienta local que permita capturar datos de pacientes desde la app Android y la aplicación de escritorio, almacenarlos en la base de datos SQL Server del servidor de Prevengos y sincronizarlos con la plataforma oficial a través de intercambios CSV. Toda la mensajería, analítica avanzada o automatización multicanal se delega en repositorios externos —como [`prl-notifier`](https://github.com/prevengos/prl-notifier)— para evitar duplicar responsabilidades.

El monorepo agrupa las aplicaciones cliente y los módulos Java compartidos que encapsulan contratos, lógica de sincronización y acceso a datos. No existe motor Python, ni código Kotlin, ni adaptadores de terceros incluidos: el alcance se limita a la sincronización local con Prevengos y al mantenimiento de consistencia entre los clientes Java y la base de datos corporativa.

## Principios clave
- **Sincronización local controlada**: todas las operaciones de lectura y escritura se realizan contra SQL Server local y los CSV intercambiados con Prevengos.
- **Tecnología homogénea**: solo se utiliza Java tanto en los clientes como en los módulos compartidos para simplificar despliegues y soporte.
- **Responsabilidades claras**: la mensajería, notificaciones y automatizaciones externas viven fuera de este repositorio y se integran únicamente a través de CSV o procesos coordinados.
- **Reutilización entre clientes**: los módulos compartidos definen contratos y utilidades comunes para Android y escritorio, evitando divergencias en el modelo de datos.

## Diagrama lógico
```mermaid
flowchart LR
    subgraph "Servidor local Prevengos"
        DB[(SQL Server local)]
        CSV[Intercambio CSV con Prevengos]
    end

    subgraph "Clientes Java"
        ANDROID[Aplicación Android]
        DESKTOP[Aplicación de escritorio]
    end

    subgraph "Módulos compartidos"
        SH[modules/shared]
        GW[modules/gateway]
        HUB[modules/hub-backend]
    end

    ANDROID --> SH
    DESKTOP --> SH
    SH --> GW
    GW --> HUB
    HUB --> DB
    HUB --> CSV
```

El diagrama refleja que las apps Java se apoyan en módulos compartidos para persistir información en SQL Server y generar o consumir los CSV requeridos por Prevengos. Cualquier otro canal (mensajería, email, analítica o integraciones externas) debe implementarse en sistemas anexos y coordinarse mediante los mismos CSV o la base de datos local.

## Guía de implementación

La [Guía de implementación](docs/guia-de-implementacion.md) recopila los módulos principales, flujos de datos y controles de seguridad que deben seguir los equipos de Android, escritorio y backend al trabajar con Prevengos Plug. Consulta ese documento antes de acometer nuevos desarrollos o despliegues para mantener la coherencia con la arquitectura acordada.

## Próximos pasos sugeridos
1. Documentar el procedimiento operativo para exportar e importar CSV con Prevengos desde este programa local.
2. Completar los adaptadores JDBC hacia SQL Server en `modules/gateway` y `modules/hub-backend` para cubrir todos los casos de uso requeridos por la app y el escritorio.
3. Sincronizar dependencias y contratos con el repositorio `prl-notifier`, asegurando que no se solapan funcionalidades y que ambos proyectos comparten la misma fuente de verdad.
4. Revisar periódicamente la guía de [preparación para integraciones con `prl-notifier`](docs/integrations/prl-notifier-readiness.md) para garantizar que la documentación y los artefactos técnicos permanecen alineados ante nuevas funcionalidades.
