'use strict';

const express = require('express');
const router = express.Router();
const productoServicio = require('../servicios/productoServicio');
const carritoServicio = require('../servicios/carritoServicio');

function extraerMensajeError(err) {
  if (err.response && err.response.data && err.response.data.error) {
    return err.response.data.error;
  }
  return err.message || 'Error desconocido';
}

router.get('/', async (req, res) => {
  try {
    const { nombre, error } = req.query;
    const [productos, carrito] = await Promise.all([
      nombre && nombre.trim() ? productoServicio.buscarPorNombre(nombre.trim()) : Promise.resolve([]),
      carritoServicio.obtenerCarrito(),
    ]);
    res.render('index', {
      productos,
      carrito,
      carritoVacio: !carrito.items || carrito.items.length === 0,
      busqueda: nombre || '',
      error: error || null,
    });
  } catch (err) {
    res.render('index', {
      productos: [],
      carrito: { items: [], total: 0 },
      carritoVacio: true,
      busqueda: '',
      error: extraerMensajeError(err),
    });
  }
});

module.exports = router;