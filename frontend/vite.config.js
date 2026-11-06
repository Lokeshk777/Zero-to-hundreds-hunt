import { resolve } from 'path';
import { defineConfig } from 'vite';

export default defineConfig({
  build: {
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'index.html'),
        login: resolve(__dirname, 'pages/login.html'),
        signup: resolve(__dirname, 'pages/signup.html'),
        dashboard: resolve(__dirname, 'pages/dashboard.html'),
        forgotPassword: resolve(__dirname, 'pages/forgot-password.html')
      }
    }
  }
});
