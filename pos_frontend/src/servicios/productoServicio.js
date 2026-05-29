'use strict';
const axios = require('axios');
const { BACKEND_URL } = require('../config/config');

async function obtenerTodos() {
  return [];
}

async function buscarPorNombre(nombre) {
  const url = `${BACKEND_URL}/productos`;
  console.log('Llamando a:', url, 'con q:', nombre);
  const response = await axios.get(url, {
    params: { q: nombre },
  });
  return response.data;
}

module.exports = { obtenerTodos, buscarPorNombre };