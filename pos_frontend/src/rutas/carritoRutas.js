'use strict';

const express = require('express');
const router = express.Router();
const carritoServicio = require('../servicios/carritoServicio');
const productoServicio = require('../servicios/productoServicio');

/**
 * Extrae el mensaje de error de una respuesta de Axios o error genérico.
 * @param {Error} err
 * @returns {string}
 */
function extraerMensajeError(err) {
  if (err.response && err.response.data && err.response.data.error) {
    return err.response.data.error;
  }
  return err.message || 'Error desconocido';
}

/**
 * POST /carrito/items — Agregar un producto al carrito.
 * En caso de error (400/404), re-renderiza la vista principal con el mensaje inline.
 */
router.post('/items', async (req, res) => {
  try {
    const { productoId, cantidad } = req.body;
    await carritoServicio.agregarItem(productoId, parseInt(cantidad, 10) || 1);
    res.redirect('/');
  } catch (err) {
    const [productos, carrito] = await Promise.all([
      productoServicio.obtenerTodos().catch(() => []),
      carritoServicio.obtenerCarrito().catch(() => ({ items: [], total: 0 })),
    ]);
    res.render('index', {
      productos,
      carrito,
      carritoVacio: !carrito.items || carrito.items.length === 0,
      busqueda: '',
      error: extraerMensajeError(err),
    });
  }
});

/**
 * POST /carrito/items/:id/actualizar — Actualizar la cantidad de un ítem.
 */
router.post('/items/:id/actualizar', async (req, res) => {
  try {
    const { cantidad } = req.body;
    await carritoServicio.actualizarItem(req.params.id, parseInt(cantidad, 10));
    res.redirect('/');
  } catch (err) {
    res.redirect('/?error=' + encodeURIComponent(extraerMensajeError(err)));
  }
});

/**
 * POST /carrito/items/:id/eliminar — Eliminar un ítem del carrito.
 */
router.post('/items/:id/eliminar', async (req, res) => {
  try {
    await carritoServicio.eliminarItem(req.params.id);
    res.redirect('/');
  } catch (err) {
    res.redirect('/?error=' + encodeURIComponent(extraerMensajeError(err)));
  }
});

/**
 * POST /carrito/vaciar — Vaciar completamente el carrito.
 */
router.post('/vaciar', async (req, res) => {
  try {
    await carritoServicio.vaciarCarrito();
    res.redirect('/');
  } catch (err) {
    res.redirect('/?error=' + encodeURIComponent(extraerMensajeError(err)));
  }
});

module.exports = router;
