import { test, expect } from '@playwright/test';

const PACIENTE_BASE = {
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

const CUESTIONARIO_BASE = {
  cuestionario_id: '94dfb93e-27a2-4c69-86f9-9b7031af8720',
  paciente_id: PACIENTE_BASE.paciente_id,
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

test.describe('Hub PRL sincronización', () => {
  test('procesa lotes y expone eventos incrementales', async ({ request }) => {
    const health = await request.get('/actuator/health');
    await expect(health).toBeOK();
    await expect(await health.json()).toEqual({ status: 'UP' });

    const pacienteResponse = await request.post('/sincronizacion/pacientes', {
      headers: { 'X-Source-System': 'android-app' },
      data: [PACIENTE_BASE],
    });
    await expect(pacienteResponse).toBeOK();
    const pacienteBody = await pacienteResponse.json();
    expect(pacienteBody.processed).toBe(1);
    expect(pacienteBody.identifiers).toEqual([PACIENTE_BASE.paciente_id]);

    const cuestionarioResponse = await request.post('/sincronizacion/cuestionarios', {
      headers: { 'X-Source-System': 'desktop-app' },
      data: [CUESTIONARIO_BASE],
    });
    await expect(cuestionarioResponse).toBeOK();
    const cuestionarioBody = await cuestionarioResponse.json();
    expect(cuestionarioBody.processed).toBe(1);
    expect(cuestionarioBody.identifiers).toEqual([CUESTIONARIO_BASE.cuestionario_id]);

    const pull = await request.get('/sincronizacion/pull', {
      params: { limit: 10 },
    });
    await expect(pull).toBeOK();
    const pullBody = await pull.json();
    expect(pullBody.events.length).toBeGreaterThanOrEqual(2);

    const pacienteEvent = pullBody.events.find((event: any) => event.eventType === 'paciente-upserted');
    const cuestionarioEvent = pullBody.events.find((event: any) => event.eventType === 'cuestionario-upserted');

    expect(pacienteEvent).toBeTruthy();
    expect(pacienteEvent.payload.nif).toBe(PACIENTE_BASE.nif);
    expect(pacienteEvent.metadata.channel).toBe('prevengos.notifications');

    expect(cuestionarioEvent).toBeTruthy();
    expect(cuestionarioEvent.payload.cuestionario_id).toBe(CUESTIONARIO_BASE.cuestionario_id);

    const nextToken: number = pullBody.nextSyncToken;
    expect(nextToken).toBeGreaterThan(0);

    const secondPull = await request.get('/sincronizacion/pull', {
      params: { limit: 5, syncToken: String(nextToken) },
    });
    await expect(secondPull).toBeOK();
    const secondBody = await secondPull.json();
    expect(secondBody.events).toHaveLength(0);
    expect(secondBody.nextSyncToken).toBe(nextToken);
  });

  test('rechaza actualizaciones obsoletas con detalles de conflicto', async ({ request }) => {
    const primeraRespuesta = await request.post('/sincronizacion/pacientes', {
      headers: { 'X-Source-System': 'android-app' },
      data: [PACIENTE_BASE],
    });
    await expect(primeraRespuesta).toBeOK();

    const conflictiva = {
      ...PACIENTE_BASE,
      updated_at: '2024-02-01T08:00:00Z',
      telefono: '+34910000000',
    };

    const conflicto = await request.post('/sincronizacion/pacientes', {
      headers: { 'X-Source-System': 'legacy-system' },
      data: [conflictiva],
    });
    expect(conflicto.status()).toBe(409);
    const body = await conflicto.json();
    expect(body.message).toContain('Conflicto');
    expect(body.conflict.paciente_id).toBe(PACIENTE_BASE.paciente_id);
    expect(body.conflict.incoming_updated_at).toBe(conflictiva.updated_at);
  });
});
