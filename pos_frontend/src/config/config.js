'use strict';
require('dotenv').config();

/**
 * Configuración centralizada de la aplicación.
 * Lee variables de entorno con valores por defecto.
 */
module.exports = {
  PORT: process.env.PORT || 3000,
  BACKEND_URL: process.env.BACKEND_URL || 'http://localhost:8080',
};
