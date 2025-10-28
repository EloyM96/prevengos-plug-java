import { test, expect } from '@playwright/test';
import { readFileSync } from 'fs';
import * as path from 'path';

const REPO_ROOT = path.resolve(__dirname, '../../..');
const CSV_DIR = path.resolve(REPO_ROOT, 'contracts', 'csv', 'rrhh');

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const ISO_DATE_REGEX = /^\d{4}-\d{2}-\d{2}$/;
const ISO_INSTANT_REGEX = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z$/;

function splitCsvLine(line: string): string[] {
  const values: string[] = [];
  let current = '';
  let inQuotes = false;

  for (let index = 0; index < line.length; index += 1) {
    const char = line[index];

    if (char === '"') {
      const isEscaped = index > 0 && line[index - 1] === '\\';
      if (!isEscaped) {
        inQuotes = !inQuotes;
        continue;
      }
    }

    if (char === ';' && !inQuotes) {
      values.push(current);
      current = '';
    } else {
      current += char;
    }
  }

  values.push(current);

  return values.map((value) => value.replace(/\\"/g, '"'));
}

function loadCsv(fileName: string) {
  const filePath = path.resolve(CSV_DIR, fileName);
  const content = readFileSync(filePath, 'utf-8');
  const lines = content
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter((line) => line.length > 0);
  const rows = lines.map(splitCsvLine);
  const [header, ...dataRows] = rows;
  const records = dataRows.map((row) =>
    header.reduce<Record<string, string>>((acc, column, index) => {
      acc[column] = row[index] ?? '';
      return acc;
    }, {}),
  );
  return { header, records };
}

const PACIENTE_MOCK = {
  paciente_id: '3f9f55bc-3b92-4bf2-8c3c-61116941a9bd',
  nif: '87654321B',
  nombre: 'Lucía',
  apellidos: 'Prevengos',
  fecha_nacimiento: '1992-07-21',
  sexo: 'F',
  telefono: '+34911111222',
  email: 'lucia.prevengos@example.com',
  empresa_id: '1c6431e6-0a60-4c3f-a1e5-91dc66712b43',
  centro_id: '18a9d2af-6530-42cf-a93a-5b625492159a',
  externo_ref: 'EXT-200',
  created_at: '2024-03-04T09:30:00Z',
  updated_at: '2024-03-04T11:15:00Z',
};

const CUESTIONARIO_MOCK = {
  cuestionario_id: '94dfb93e-27a2-4c69-86f9-9b7031af8720',
  paciente_id: PACIENTE_MOCK.paciente_id,
  plantilla_codigo: 'AUTO-CS-01',
  estado: 'validado',
  respuestas: [
    {
      pregunta_codigo: 'peso',
      valor: '65',
      unidad: 'kg',
      metadata: { instrumento: 'balanza-medica' },
    },
  ],
  firmas: ['dr.prevengos'],
  adjuntos: ['informe-prevengos.pdf'],
  created_at: '2024-03-04T11:30:00Z',
  updated_at: '2024-03-04T11:30:00Z',
};

function expectJson(value: string) {
  expect(() => JSON.parse(value)).not.toThrow();
}

test.describe('Intercambio CSV RRHH', () => {
  test('las plantillas oficiales cumplen las validaciones documentadas', () => {
    const pacientesCsv = loadCsv('pacientes.example.csv');
    expect(pacientesCsv.header).toEqual([
      'paciente_id',
      'nif',
      'nombre',
      'apellidos',
      'fecha_nacimiento',
      'sexo',
      'telefono',
      'email',
      'empresa_id',
      'centro_id',
      'externo_ref',
      'created_at',
      'updated_at',
    ]);
    expect(pacientesCsv.records).toHaveLength(1);
    const paciente = pacientesCsv.records[0];
    expect(paciente.paciente_id).toMatch(UUID_REGEX);
    expect(paciente.nif).toMatch(/^[A-Z0-9]{9}$/);
    expect(paciente.nombre).not.toEqual('');
    expect(paciente.apellidos).not.toEqual('');
    expect(paciente.fecha_nacimiento).toMatch(ISO_DATE_REGEX);
    expect(paciente.sexo).toMatch(/^(M|F|X)$/);
    expect(paciente.telefono).toMatch(/^\+?\d{9,15}$/);
    expect(paciente.email).toMatch(/^[^@]+@[^@]+\.[^@]+$/);
    expect(paciente.empresa_id).toMatch(UUID_REGEX);
    expect(paciente.centro_id).toMatch(UUID_REGEX);
    expect(paciente.externo_ref).not.toEqual('');
    expect(paciente.created_at).toMatch(ISO_INSTANT_REGEX);
    expect(paciente.updated_at).toMatch(ISO_INSTANT_REGEX);

    const cuestionariosCsv = loadCsv('cuestionarios.example.csv');
    expect(cuestionariosCsv.header).toEqual([
      'cuestionario_id',
      'paciente_id',
      'plantilla_codigo',
      'estado',
      'respuestas',
      'firmas',
      'adjuntos',
      'created_at',
      'updated_at',
    ]);
    expect(cuestionariosCsv.records).toHaveLength(1);
    const cuestionario = cuestionariosCsv.records[0];
    expect(cuestionario.cuestionario_id).toMatch(UUID_REGEX);
    expect(cuestionario.paciente_id).toMatch(UUID_REGEX);
    expect(cuestionario.estado).toMatch(/^(borrador|completado|validado)$/);
    expectJson(cuestionario.respuestas);
    expectJson(cuestionario.firmas || '[]');
    expectJson(cuestionario.adjuntos || '[]');
    expect(cuestionario.created_at).toMatch(ISO_INSTANT_REGEX);
    expect(cuestionario.updated_at).toMatch(ISO_INSTANT_REGEX);
  });

  test('la exportación e importación exponen metadatos coherentes', async ({ request }) => {
    const health = await request.get('/actuator/health');
    await expect(health).toBeOK();

    const pacienteResponse = await request.post('/sincronizacion/pacientes', {
      headers: { 'X-Source-System': 'android-app' },
      data: [PACIENTE_MOCK],
    });
    await expect(pacienteResponse).toBeOK();

    const cuestionarioResponse = await request.post('/sincronizacion/cuestionarios', {
      headers: { 'X-Source-System': 'desktop-app' },
      data: [CUESTIONARIO_MOCK],
    });
    await expect(cuestionarioResponse).toBeOK();

    const exportResponse = await request.post('/rrhh/export');
    expect([200, 202]).toContain(exportResponse.status());
    const exportBody = await exportResponse.json();
    const traceId = exportBody.traceId ?? exportBody.trace_id;
    expect(traceId).toMatch(UUID_REGEX);
    const pacientesCount =
      exportBody.pacientes ?? exportBody.pacientes_count ?? exportBody.pacientesCount;
    expect(pacientesCount).toBeGreaterThanOrEqual(1);
    const cuestionariosCount =
      exportBody.cuestionarios ?? exportBody.cuestionarios_count ?? exportBody.cuestionariosCount;
    expect(cuestionariosCount).toBeGreaterThanOrEqual(1);

    const remotePath = exportBody.remotePath ?? exportBody.remote_path;
    if (remotePath) {
      expect(remotePath).toMatch(/^\d{8}\/rrhh\//);
    }

    const stagingDir = exportBody.stagingDir ?? exportBody.staging_dir;
    expect(typeof stagingDir === 'string' && stagingDir.length > 0).toBeTruthy();
    const archiveDir = exportBody.archiveDir ?? exportBody.archive_dir;
    expect(typeof archiveDir === 'string' && archiveDir.length > 0).toBeTruthy();

    const importResponse = await request.post('/rrhh/import');
    expect(importResponse.status()).toBe(202);
    const importBody = await importResponse.json();
    expect(importBody.processedDrops).toBeGreaterThanOrEqual(0);
    expect(importBody.pacientesImported).toBeGreaterThanOrEqual(0);
    expect(importBody.cuestionariosImported).toBeGreaterThanOrEqual(0);

    if (Array.isArray(importBody.archivedDrops)) {
      for (const drop of importBody.archivedDrops) {
        expect(drop).toMatch(/^\d{8}\/rrhh\//);
      }
    }

    expect(Array.isArray(importBody.failedDrops)).toBeTruthy();
  });
});
