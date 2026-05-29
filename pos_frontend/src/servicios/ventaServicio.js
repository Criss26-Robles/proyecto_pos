'use strict';
const axios = require('axios');
const { BACKEND_URL } = require('../config/config');

async function confirmarVenta(carrito) {
  const response = await axios.post(`${BACKEND_URL}/ventas`, carrito);
  return response.data;
}

async function obtenerHistorial() {
  const response = await axios.get(`${BACKEND_URL}/ventas`);
  return response.data;
}

async function obtenerDetalle(id) {
  const response = await axios.get(`${BACKEND_URL}/ventas/${id}`);
  return response.data;
}

module.exports = { confirmarVenta, obtenerHistorial, obtenerDetalle };