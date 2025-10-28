import { test, expect } from '@playwright/test';
import fs from 'fs';
import path from 'path';

const repoRoot = path.resolve(__dirname, '..', '..', '..');

function readCsv(relativePath: string) {
  const absolutePath = path.join(repoRoot, relativePath);
  const raw = fs.readFileSync(absolutePath, 'utf-8').trim();
  const [headerLine, ...rows] = raw.split(/\r?\n/);
  const header = headerLine.split(';');
  const records = rows.map((line) => line.split(';'));
  return { header, records };
}

test.describe('Intercambio CSV RRHH', () => {
  test('las plantillas de exportación respetan las columnas documentadas', () => {
    const pacientes = readCsv('contracts/csv/rrhh/pacientes.example.csv');
    const cuestionarios = readCsv('contracts/csv/rrhh/cuestionarios.example.csv');

    expect(pacientes.header).toEqual([
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

    const paciente = pacientes.records[0];
    expect(paciente[0]).toMatch(/^[0-9a-fA-F-]{36}$/);
    expect(paciente[1]).toMatch(/^[0-9A-Z]{9}$/);
    expect(paciente[4]).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    expect(paciente[11]).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z$/);

    expect(cuestionarios.header).toEqual([
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

    const cuestionario = cuestionarios.records[0];
    expect(cuestionario[0]).toMatch(/^[0-9a-fA-F-]{36}$/);
    expect(cuestionario[3]).toBe('validado');
    expect(cuestionario[4]).toContain('pregunta_codigo');
    expect(cuestionario[6]).toContain('informe-revision.pdf');
  });

  test('los CSV importados incluyen metadatos de Prevengos', () => {
    const importado = readCsv('tests/e2e/fixtures/csv/import/pacientes_resultado.csv');

    expect(importado.header).toEqual([
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
      'estado_prevengos',
      'procesado_en',
      'observaciones',
    ]);

    const fila = importado.records[0];
    expect(fila[13]).toMatch(/^(apto|no_apto|pendiente)$/);
    expect(new Date(fila[14]).toISOString()).toBe('2024-03-05T10:15:00.000Z');
    expect(fila[15]).toContain('Sin incidencias');
  });
});
