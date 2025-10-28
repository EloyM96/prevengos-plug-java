import { test, expect } from '@playwright/test';
import { readFileSync } from 'fs';
import path from 'path';

const repoRoot = path.resolve(__dirname, '../../..');

const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const isoDateRegex = /^\d{4}-\d{2}-\d{2}$/;
const isoDateTimeRegex = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z$/;
const nifRegex = /^\d{8}[A-Z]$/;
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const phoneRegex = /^\+[0-9]{9,15}$/;

function readCsv(fileRelativePath: string) {
  const absolutePath = path.join(repoRoot, fileRelativePath);
  const raw = readFileSync(absolutePath, 'utf-8').trim();
  const lines = raw.split(/\r?\n/);
  const headers = lines[0].split(';');
  const records = lines.slice(1).map((line) => line.split(';'));
  return { headers, records };
}

function recordFrom(headers: string[], values: string[]) {
  return headers.reduce<Record<string, string>>((acc, header, index) => {
    acc[header] = values[index] ?? '';
    return acc;
  }, {});
}

function parseJsonField(rawValue: string) {
  const trimmed = rawValue.trim();
  if (!trimmed) {
    return [];
  }

  const withoutWrappingQuotes = trimmed.replace(/^"|"$/g, '');
  const normalized = withoutWrappingQuotes.replace(/\\"/g, '"');
  return JSON.parse(normalized);
}

test.describe('Intercambio CSV Prevengos Plug', () => {
  test('plantilla pacientes.csv mantiene columnas y formatos documentados', () => {
    const expectedHeaders = [
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
    ];

    const { headers, records } = readCsv('contracts/csv/rrhh/pacientes.example.csv');
    expect(headers).toEqual(expectedHeaders);
    expect(records).toHaveLength(1);

    const record = recordFrom(headers, records[0]);

    expect(record.paciente_id).toMatch(uuidRegex);
    expect(record.nif).toMatch(nifRegex);
    expect(record.nombre.length).toBeGreaterThan(0);
    expect(record.apellidos.length).toBeGreaterThan(0);
    expect(record.fecha_nacimiento).toMatch(isoDateRegex);
    expect(['M', 'F', 'X']).toContain(record.sexo);
    expect(record.telefono).toMatch(phoneRegex);
    expect(record.email).toMatch(emailRegex);
    expect(record.empresa_id).toMatch(uuidRegex);
    expect(record.centro_id).toMatch(uuidRegex);
    expect(record.externo_ref.length).toBeGreaterThan(0);
    expect(record.created_at).toMatch(isoDateTimeRegex);
    expect(record.updated_at).toMatch(isoDateTimeRegex);
    expect(new Date(record.updated_at).getTime()).toBeGreaterThanOrEqual(
      new Date(record.created_at).getTime(),
    );
  });

  test('plantilla cuestionarios.csv valida referencias y estructuras JSON', () => {
    const expectedHeaders = [
      'cuestionario_id',
      'paciente_id',
      'plantilla_codigo',
      'estado',
      'respuestas',
      'firmas',
      'adjuntos',
      'created_at',
      'updated_at',
    ];

    const { headers, records } = readCsv('contracts/csv/rrhh/cuestionarios.example.csv');
    expect(headers).toEqual(expectedHeaders);
    expect(records).toHaveLength(1);

    const record = recordFrom(headers, records[0]);

    expect(record.cuestionario_id).toMatch(uuidRegex);
    expect(record.paciente_id).toMatch(uuidRegex);
    expect(record.plantilla_codigo.length).toBeGreaterThan(0);
    expect(['borrador', 'completado', 'validado']).toContain(record.estado);

    const respuestas = parseJsonField(record.respuestas);
    expect(Array.isArray(respuestas)).toBeTruthy();
    expect(respuestas.length).toBeGreaterThan(0);
    expect(respuestas[0]).toMatchObject({ pregunta_codigo: expect.any(String) });

    const firmas = parseJsonField(record.firmas);
    expect(Array.isArray(firmas)).toBeTruthy();

    const adjuntos = parseJsonField(record.adjuntos);
    expect(Array.isArray(adjuntos)).toBeTruthy();

    expect(record.created_at).toMatch(isoDateTimeRegex);
    expect(record.updated_at).toMatch(isoDateTimeRegex);
    expect(new Date(record.updated_at).getTime()).toBeGreaterThanOrEqual(
      new Date(record.created_at).getTime(),
    );
  });

  test('documentación de formatos CSV sigue apuntando a las plantillas y pruebas oficiales', () => {
    const doc = readFileSync(path.join(repoRoot, 'docs/integrations/csv-formatos.md'), 'utf-8');

    [
      '`pacientes.csv`',
      '`cuestionarios.csv`',
      '`contracts/csv/rrhh/pacientes.example.csv`',
      '`contracts/csv/rrhh/cuestionarios.example.csv`',
      '`tests/e2e/tests/csv-exchange.spec.ts`',
    ].forEach((snippet) => {
      expect(doc).toContain(snippet);
    });
  });
});
