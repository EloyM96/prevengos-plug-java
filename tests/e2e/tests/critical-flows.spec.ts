import { test, expect } from '@playwright/test';

test.describe('Flujos críticos previos a Go-Live', () => {
  test('la landing responde con código 200 y contenido accesible', async ({ page, baseURL }) => {
    await page.goto(baseURL ?? 'https://example.com');
    await expect(page).toHaveTitle(/Example Domain/i);
    const heading = page.getByRole('heading', { level: 1 });
    await expect(heading).toContainText(/example domain/i);
  });

  test('soporta navegación básica y links seguros', async ({ page, baseURL }) => {
    await page.goto(baseURL ?? 'https://example.com');
    const moreInformation = page.getByRole('link', { name: /more information/i });
    await expect(moreInformation).toHaveAttribute('rel', /noopener|noreferrer/);
  });
});
