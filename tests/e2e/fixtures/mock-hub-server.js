const http = require('http');
const { randomUUID } = require('crypto');
const url = require('url');

const HOST = process.env.MOCK_HUB_HOST || '127.0.0.1';
const PORT = Number(process.env.MOCK_HUB_PORT || '3100');

const state = {
  pacientes: new Map(),
  cuestionarios: new Map(),
  events: [],
  nextSyncToken: 1,
  lastExport: null,
};

function jsonResponse(res, statusCode, body) {
  const payload = JSON.stringify(body);
  res.statusCode = statusCode;
  res.setHeader('Content-Type', 'application/json');
  res.setHeader('Content-Length', Buffer.byteLength(payload));
  res.end(payload);
}

function readBody(req) {
  return new Promise((resolve, reject) => {
    let data = '';
    req.on('data', (chunk) => {
      data += chunk;
    });
    req.on('end', () => {
      if (!data) {
        resolve(null);
        return;
      }
      try {
        resolve(JSON.parse(data));
      } catch (error) {
        reject(error);
      }
    });
    req.on('error', reject);
  });
}

function parseDate(value) {
  if (!value) {
    return Date.now();
  }
  const timestamp = Date.parse(value);
  return Number.isNaN(timestamp) ? Date.now() : timestamp;
}

function registerEvent(eventType, payload, source, correlationId, metadataExtras = {}) {
  const syncToken = state.nextSyncToken++;
  const occurredAt = new Date().toISOString();
  const event = {
    syncToken,
    eventId: randomUUID(),
    eventType,
    version: 1,
    occurredAt,
    source: source || 'hub-backend',
    correlationId: correlationId || null,
    causationId: null,
    payload: JSON.stringify(payload),
    metadata: JSON.stringify({
      channel: 'prevengos.notifications',
      webhook_secret_set: false,
      ...metadataExtras,
    }),
  };
  state.events.push(event);
  return event;
}

function buildDropPath(prefix) {
  const datePrefix = new Date().toISOString().slice(0, 10).replace(/-/g, '');
  return `${datePrefix}/rrhh/${prefix}`;
}

function handleRrhhExport(req, res) {
  const traceId = randomUUID();
  const pacientesCount = state.pacientes.size;
  const cuestionariosCount = state.cuestionarios.size;
  const remotePath = buildDropPath('hub');
  const stagingDir = `/var/lib/prevengos/staging/${traceId}`;
  const archiveDir = `/var/lib/prevengos/archive/${traceId}`;

  state.lastExport = {
    traceId,
    remotePath,
    stagingDir,
    archiveDir,
    pacientesCount,
    cuestionariosCount,
  };

  jsonResponse(res, 202, {
    traceId,
    remotePath,
    stagingDir,
    archiveDir,
    pacientes: pacientesCount,
    cuestionarios: cuestionariosCount,
  });
}

function handleRrhhImport(req, res) {
  if (!state.lastExport) {
    jsonResponse(res, 202, {
      processedDrops: 0,
      pacientesImported: 0,
      cuestionariosImported: 0,
      archivedDrops: [],
      failedDrops: [],
    });
    return;
  }

  const archivedDrop = buildDropPath('prevengos');

  jsonResponse(res, 202, {
    processedDrops: 1,
    pacientesImported: state.lastExport.pacientesCount,
    cuestionariosImported: state.lastExport.cuestionariosCount,
    archivedDrops: [archivedDrop],
    failedDrops: [],
  });
}

function normaliseTimestamp(value) {
  if (!value) {
    return new Date().toISOString();
  }
  const parsed = Date.parse(value);
  if (Number.isNaN(parsed)) {
    return new Date().toISOString();
  }
  return new Date(parsed).toISOString();
}

function handlePush(req, res) {
  readBody(req)
    .then((body) => {
      if (!body || typeof body !== 'object') {
        jsonResponse(res, 400, { message: 'Payload inválido' });
        return;
      }

      const pacientes = Array.isArray(body.pacientes) ? body.pacientes : [];
      const cuestionarios = Array.isArray(body.cuestionarios) ? body.cuestionarios : [];
      const source = body.source || req.headers['x-source-system'] || 'mock-client';
      const correlationId = body.correlationId || null;

      let processedPacientes = 0;
      let processedCuestionarios = 0;
      let lastSyncToken = 0;
      const identifiers = [];

      for (const paciente of pacientes) {
        if (!paciente || !paciente.pacienteId) {
          continue;
        }
        const updatedAtIso = normaliseTimestamp(paciente.updatedAt || paciente.lastModified);
        const stored = state.pacientes.get(paciente.pacienteId);
        const incomingUpdatedAt = Date.parse(updatedAtIso);
        if (stored && incomingUpdatedAt < stored.updatedAt) {
          jsonResponse(res, 409, {
            message: 'Conflicto de sincronización',
            conflict: {
              entity: 'pacientes',
              pacienteId: paciente.pacienteId,
              incomingUpdatedAt: updatedAtIso,
              storedUpdatedAt: new Date(stored.updatedAt).toISOString(),
            },
          });
          return;
        }
        const event = registerEvent('paciente-upserted', paciente, source, correlationId);
        const record = {
          ...paciente,
          updatedAt: updatedAtIso,
          lastModified: normaliseTimestamp(paciente.lastModified || updatedAtIso),
          syncToken: event.syncToken,
        };
        state.pacientes.set(paciente.pacienteId, {
          record,
          updatedAt: incomingUpdatedAt,
          syncToken: event.syncToken,
        });
        processedPacientes += 1;
        identifiers.push(paciente.pacienteId);
        lastSyncToken = Math.max(lastSyncToken, event.syncToken);
      }

      for (const cuestionario of cuestionarios) {
        if (!cuestionario || !cuestionario.cuestionarioId) {
          continue;
        }
        const updatedAtIso = normaliseTimestamp(cuestionario.updatedAt || cuestionario.lastModified);
        const stored = state.cuestionarios.get(cuestionario.cuestionarioId);
        const incomingUpdatedAt = Date.parse(updatedAtIso);
        if (stored && incomingUpdatedAt < stored.updatedAt) {
          jsonResponse(res, 409, {
            message: 'Conflicto de sincronización',
            conflict: {
              entity: 'cuestionarios',
              cuestionarioId: cuestionario.cuestionarioId,
              incomingUpdatedAt: updatedAtIso,
              storedUpdatedAt: new Date(stored.updatedAt).toISOString(),
            },
          });
          return;
        }
        const event = registerEvent('cuestionario-upserted', cuestionario, source, correlationId);
        const record = {
          ...cuestionario,
          updatedAt: updatedAtIso,
          lastModified: normaliseTimestamp(cuestionario.lastModified || updatedAtIso),
          syncToken: event.syncToken,
        };
        state.cuestionarios.set(cuestionario.cuestionarioId, {
          record,
          updatedAt: incomingUpdatedAt,
          syncToken: event.syncToken,
        });
        processedCuestionarios += 1;
        identifiers.push(cuestionario.cuestionarioId);
        lastSyncToken = Math.max(lastSyncToken, event.syncToken);
      }

      jsonResponse(res, 200, {
        processedPacientes,
        processedCuestionarios,
        lastSyncToken,
        createdOrUpdatedIds: identifiers,
      });
    })
    .catch((error) => {
      console.error('Error procesando lote de sincronización', error);
      jsonResponse(res, 400, { message: 'Payload inválido' });
    });
}

function handlePull(req, res, query) {
  const limit = Math.max(1, Math.min(500, Number(query.limit) || 100));
  const syncToken = query.syncToken ? Number(query.syncToken) : 0;

  const pacientes = Array.from(state.pacientes.values())
    .filter((entry) => entry.syncToken > syncToken)
    .sort((a, b) => a.syncToken - b.syncToken)
    .slice(0, limit)
    .map((entry) => entry.record);

  const cuestionarios = Array.from(state.cuestionarios.values())
    .filter((entry) => entry.syncToken > syncToken)
    .sort((a, b) => a.syncToken - b.syncToken)
    .slice(0, limit)
    .map((entry) => entry.record);

  const events = state.events
    .filter((event) => event.syncToken > syncToken)
    .sort((a, b) => a.syncToken - b.syncToken)
    .slice(0, limit);

  const lastPacienteToken = pacientes.length > 0 ? pacientes[pacientes.length - 1].syncToken : 0;
  const lastCuestionarioToken = cuestionarios.length > 0 ? cuestionarios[cuestionarios.length - 1].syncToken : 0;
  const lastEventToken = events.length > 0 ? events[events.length - 1].syncToken : 0;
  const nextSyncToken = Math.max(syncToken, lastPacienteToken, lastCuestionarioToken, lastEventToken);

  jsonResponse(res, 200, {
    pacientes,
    cuestionarios,
    events,
    nextSyncToken,
  });
}

function route(req, res) {
  const parsedUrl = url.parse(req.url, true);
  if (req.method === 'GET' && parsedUrl.pathname === '/actuator/health') {
    jsonResponse(res, 200, { status: 'UP' });
    return;
  }
  if (req.method === 'POST' && parsedUrl.pathname === '/sincronizacion/push') {
    handlePush(req, res);
    return;
  }
  if (req.method === 'GET' && parsedUrl.pathname === '/sincronizacion/pull') {
    handlePull(req, res, parsedUrl.query);
    return;
  }
  if (req.method === 'POST' && parsedUrl.pathname === '/rrhh/export') {
    handleRrhhExport(req, res);
    return;
  }
  if (req.method === 'POST' && parsedUrl.pathname === '/rrhh/import') {
    handleRrhhImport(req, res);
    return;
  }

  jsonResponse(res, 404, { message: 'Not Found' });
}

const server = http.createServer(route);

server.listen(PORT, HOST, () => {
  console.log(`Mock hub server running at http://${HOST}:${PORT}`);
});

function shutdown() {
  server.close(() => {
    process.exit(0);
  });
}

process.on('SIGINT', shutdown);
process.on('SIGTERM', shutdown);
