import { defineConfig, devices } from '@playwright/test';

const defaultBaseURL = 'http://127.0.0.1:3100';
const baseURL = process.env.E2E_BASE_URL ?? defaultBaseURL;
const useMockServer = !process.env.E2E_BASE_URL;

export default defineConfig({
  testDir: './tests',
  timeout: 30 * 1000,
  expect: {
    timeout: 5000,
  },
  reporter: [['list'], ['html', { outputFolder: 'playwright-report', open: 'never' }]],
  use: {
    baseURL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
  ],
  ...(useMockServer
    ? {
        webServer: {
          command: 'node fixtures/mock-hub-server.js',
          url: `${defaultBaseURL}/actuator/health`,
          reuseExistingServer: !process.env.CI,
          timeout: 30 * 1000,
        },
      }
    : {}),
});
