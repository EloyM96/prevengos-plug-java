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

function registerEvent(eventType, payload, source, metadataExtras = {}) {
  const syncToken = state.nextSyncToken++;
  const occurredAt = new Date().toISOString();
  const event = {
    syncToken,
    eventId: randomUUID(),
    eventType,
    version: 1,
    occurredAt,
    source: source || 'hub-backend',
    correlationId: null,
    causationId: null,
    payload,
    metadata: {
      channel: 'prevengos.notifications',
      webhook_secret_set: false,
      ...metadataExtras,
    },
  };
  state.events.push(event);
  return event;
}

function handlePacientes(req, res) {
  readBody(req)
    .then((body) => {
      const pacientes = Array.isArray(body) ? body : [];
      const identifiers = [];
      for (const paciente of pacientes) {
        if (!paciente || !paciente.paciente_id) {
          continue;
        }
        const stored = state.pacientes.get(paciente.paciente_id);
        const incomingUpdatedAt = parseDate(paciente.updated_at);
        if (stored && incomingUpdatedAt < stored.updatedAt) {
          jsonResponse(res, 409, {
            message: 'Conflicto de sincronización',
            conflict: {
              entity: 'pacientes',
              paciente_id: paciente.paciente_id,
              incoming_updated_at: paciente.updated_at,
              stored_updated_at: new Date(stored.updatedAt).toISOString(),
            },
          });
          return;
        }
        state.pacientes.set(paciente.paciente_id, {
          record: paciente,
          updatedAt: incomingUpdatedAt,
        });
        identifiers.push(paciente.paciente_id);
        registerEvent('paciente-upserted', paciente, req.headers['x-source-system']);
      }
      jsonResponse(res, 200, { processed: identifiers.length, identifiers });
    })
    .catch((error) => {
      console.error('Error procesando pacientes', error);
      jsonResponse(res, 400, { message: 'Payload inválido' });
    });
}

function handleCuestionarios(req, res) {
  readBody(req)
    .then((body) => {
      const cuestionarios = Array.isArray(body) ? body : [];
      const identifiers = [];
      for (const cuestionario of cuestionarios) {
        if (!cuestionario || !cuestionario.cuestionario_id) {
          continue;
        }
        state.cuestionarios.set(cuestionario.cuestionario_id, cuestionario);
        identifiers.push(cuestionario.cuestionario_id);
        registerEvent('cuestionario-upserted', cuestionario, req.headers['x-source-system']);
      }
      jsonResponse(res, 200, { processed: identifiers.length, identifiers });
    })
    .catch((error) => {
      console.error('Error procesando cuestionarios', error);
      jsonResponse(res, 400, { message: 'Payload inválido' });
    });
}

function handlePull(req, res, query) {
  const limit = Math.max(1, Math.min(500, Number(query.limit) || 100));
  const syncToken = query.syncToken ? Number(query.syncToken) : null;
  const since = query.since ? Date.parse(query.since) : null;

  const filtered = state.events.filter((event) => {
    const tokenMatch = syncToken == null || event.syncToken > syncToken;
    const sinceMatch = since == null || Date.parse(event.occurredAt) >= since;
    return tokenMatch && sinceMatch;
  });
  const events = filtered.slice(0, limit);
  const nextSyncToken = events.length > 0 ? events[events.length - 1].syncToken : syncToken;
  const warnings = filtered.length > limit ? ['Resultados truncados por limit'] : [];

  jsonResponse(res, 200, {
    events,
    nextSyncToken,
    warnings,
  });
}

function route(req, res) {
  const parsedUrl = url.parse(req.url, true);
  if (req.method === 'GET' && parsedUrl.pathname === '/actuator/health') {
    jsonResponse(res, 200, { status: 'UP' });
    return;
  }
  if (req.method === 'POST' && parsedUrl.pathname === '/sincronizacion/pacientes') {
    handlePacientes(req, res);
    return;
  }
  if (req.method === 'POST' && parsedUrl.pathname === '/sincronizacion/cuestionarios') {
    handleCuestionarios(req, res);
    return;
  }
  if (req.method === 'GET' && parsedUrl.pathname === '/sincronizacion/pull') {
    handlePull(req, res, parsedUrl.query);
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
