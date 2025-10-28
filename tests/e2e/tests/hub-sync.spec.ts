import { test, expect } from '@playwright/test';

const PACIENTE_BASE = {
  pacienteId: '3f9f55bc-3b92-4bf2-8c3c-61116941a9bd',
  nif: '87654321B',
  nombre: 'Lucía',
  apellidos: 'Prevengos',
  fechaNacimiento: '1992-07-21',
  sexo: 'F',
  telefono: '+34911111222',
  email: 'lucia.prevengos@example.com',
  empresaId: '1c6431e6-0a60-4c3f-a1e5-91dc66712b43',
  centroId: '18a9d2af-6530-42cf-a93a-5b625492159a',
  externoRef: 'EXT-200',
  createdAt: '2024-03-04T09:30:00Z',
  updatedAt: '2024-03-04T11:15:00Z',
  lastModified: '2024-03-04T11:15:00Z',
};

const CUESTIONARIO_BASE = {
  cuestionarioId: '94dfb93e-27a2-4c69-86f9-9b7031af8720',
  pacienteId: PACIENTE_BASE.pacienteId,
  plantillaCodigo: 'AUTO-CS-01',
  estado: 'validado',
  respuestas: JSON.stringify([
    {
      pregunta_codigo: 'peso',
      valor: '65',
      unidad: 'kg',
      metadata: { instrumento: 'balanza-medica' },
    },
  ]),
  firmas: JSON.stringify(['dr.prevengos']),
  adjuntos: JSON.stringify(['informe-prevengos.pdf']),
  createdAt: '2024-03-04T11:30:00Z',
  updatedAt: '2024-03-04T11:30:00Z',
  lastModified: '2024-03-04T11:30:00Z',
};

test.describe('Hub PRL sincronización', () => {
  test('procesa lotes y expone eventos incrementales', async ({ request }) => {
    const health = await request.get('/actuator/health');
    await expect(health).toBeOK();
    await expect(await health.json()).toEqual({ status: 'UP' });

    const pushPayload = {
      source: 'android-app',
      correlationId: '11111111-2222-3333-4444-555555555555',
      pacientes: [PACIENTE_BASE],
      cuestionarios: [CUESTIONARIO_BASE],
    };

    const pushResponse = await request.post('/sincronizacion/push', {
      data: pushPayload,
    });
    await expect(pushResponse).toBeOK();
    const pushBody = await pushResponse.json();
    expect(pushBody.processedPacientes).toBe(1);
    expect(pushBody.processedCuestionarios).toBe(1);
    expect(pushBody.lastSyncToken).toBeGreaterThan(0);

    const pull = await request.get('/sincronizacion/pull', {
      params: { limit: 10 },
    });
    await expect(pull).toBeOK();
    const pullBody = await pull.json();
    expect(pullBody.pacientes.length).toBeGreaterThanOrEqual(1);
    expect(pullBody.cuestionarios.length).toBeGreaterThanOrEqual(1);
    const pacienteRemoto = pullBody.pacientes.find((p: any) => p.pacienteId === PACIENTE_BASE.pacienteId);
    expect(pacienteRemoto).toBeTruthy();
    expect(pacienteRemoto.nif).toBe(PACIENTE_BASE.nif);

    const cuestionarioRemoto = pullBody.cuestionarios.find((c: any) => c.cuestionarioId === CUESTIONARIO_BASE.cuestionarioId);
    expect(cuestionarioRemoto).toBeTruthy();
    expect(cuestionarioRemoto.estado).toBe(CUESTIONARIO_BASE.estado);

    const nextToken: number = pullBody.nextSyncToken;
    expect(nextToken).toBeGreaterThan(0);

    const secondPull = await request.get('/sincronizacion/pull', {
      params: { limit: 5, syncToken: String(nextToken) },
    });
    await expect(secondPull).toBeOK();
    const secondBody = await secondPull.json();
    expect(secondBody.pacientes).toHaveLength(0);
    expect(secondBody.cuestionarios).toHaveLength(0);
    expect(secondBody.nextSyncToken).toBe(nextToken);
  });

  test('permite reintentos idempotentes del mismo lote', async ({ request }) => {
    const payload = {
      source: 'desktop-app',
      correlationId: '99999999-aaaa-bbbb-cccc-dddddddddddd',
      pacientes: [PACIENTE_BASE],
      cuestionarios: [],
    };

    const primera = await request.post('/sincronizacion/push', { data: payload });
    await expect(primera).toBeOK();
    const primeraBody = await primera.json();

    const segunda = await request.post('/sincronizacion/push', { data: payload });
    await expect(segunda).toBeOK();
    const segundaBody = await segunda.json();

    expect(segundaBody.lastSyncToken).toBeGreaterThanOrEqual(primeraBody.lastSyncToken);
    expect(segundaBody.processedPacientes).toBe(1);
  });
});
