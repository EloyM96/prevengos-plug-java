# Python Engine

Motor auxiliar en Python que expone APIs internas con **FastAPI** y encola trabajos asíncronos con **Celery**. La estructura inicial queda preparada para conectar con el Hub y los clientes JVM.

```
python-engine/
├── app/
│   ├── __init__.py
│   ├── main.py           # Punto de entrada FastAPI
│   ├── config.py         # Configuración compartida (broker/result backend)
│   ├── celery_app.py     # Instancia de Celery reutilizable
│   ├── routes/
│   │   └── notifications.py
│   └── tasks/
│       └── notifications.py
└── pyproject.toml
```

Ejecutar la API de desarrollo:

```bash
uvicorn app.main:app --reload
```

Lanzar el *worker* de Celery:

```bash
celery -A app.celery_app.celery worker --loglevel=info
```
