# Prevengos Plug – Infrastructure Requirements

## Kubernetes cluster

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| Kubernetes version | 1.25 | 1.29 (matching CI pipeline tooling) |
| Worker node size | 2 vCPU / 4 GiB RAM | 4 vCPU / 8 GiB RAM |
| Node count | 2 | 3 (for HA control plane workloads) |
| Persistent storage | 20 GiB CSI-backed volume support | 100 GiB total provisioned capacity |

A dedicated `staging` namespace is required. The CI/CD workflow creates the namespace automatically when it does not exist.

## Application pods

* **CPU:** Requests 250 millicores, limits 500 millicores per replica.
* **Memory:** Requests 512 MiB, limits 1 GiB per replica.
* **Storage:** No persistent volumes by default. Optional mounts can be configured through the Helm chart (`persistence` values).
* **Networking:** One HTTP service exposed on port 80 (mapped to container port 8080) with optional ingress.

## External dependencies

* **SQL Server** – version 2019 o superior en modalidad Developer o Standard. La base de datos `prevengos_plug` debe existir y el hub necesita un usuario dedicado con permisos `db_datareader`, `db_datawriter` y `db_ddladmin` (o un rol equivalente que permita ejecutar migraciones Flyway). Las credenciales se inyectan mediante el secreto Kubernetes `plug-database` (claves `username`, `password`, `url`).
* **Redis** – version 6 o newer operating in standalone mode. Connection URI stored in the `plug-redis` Secret (key `url`).

PostgreSQL permanece disponible únicamente para pipelines y pruebas de compatibilidad (`infra/postgresql`). No se utiliza en despliegues oficiales.

Ensure both services are reachable from the staging namespace and enforce TLS where available. The Flyway migration job expects the database to be accessible over the JDBC URL defined in the `DATABASE_URL` secret used by GitHub Actions.

## Registry access

Container images are published to the registry defined by the `REGISTRY_URL`, `REGISTRY_USERNAME`, and `REGISTRY_PASSWORD` secrets. Grant the CI user permissions to push images and to pull from the staging cluster.

## CI/CD secrets summary

| Secret | Location | Purpose |
|--------|----------|---------|
| `REGISTRY_URL` | GitHub Actions | Container registry host (e.g., `ghcr.io`). |
| `REGISTRY_USERNAME` / `REGISTRY_PASSWORD` | GitHub Actions | Credentials for image publication. |
| `DATABASE_URL` | GitHub Actions | JDBC URL used by Flyway migrations. |
| `DATABASE_USER` / `DATABASE_PASSWORD` | GitHub Actions & Kubernetes Secret | Database credentials for migrations and runtime. |
| `REDIS_URL` | Kubernetes Secret | Redis connection string. |
| `KUBE_CONFIG` | GitHub Actions | Base64-encoded kubeconfig with access to the staging namespace. |

Document and rotate these secrets following your organisation's security policies.
