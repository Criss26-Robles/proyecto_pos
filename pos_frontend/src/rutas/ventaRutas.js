'use strict';

const express = require('express');
const router = express.Router();
const ventaServicio = require('../servicios/ventaServicio');

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
 * POST /ventas — Confirmar la venta con el carrito activo.
 * En caso de error 400 (carrito vacío), redirige a / con mensaje inline.
 * En caso de error de red, renderiza error.hbs.
 */
router.post('/ventas', async (req, res) => {
  try {
    const venta = await ventaServicio.confirmarVenta();
    res.redirect(`/ventas/confirmacion/${venta.id}`);
  } catch (err) {
    const status = err.response && err.response.status;
    if (status === 400) {
      res.redirect('/?error=' + encodeURIComponent(extraerMensajeError(err)));
    } else {
      res.render('error', { mensaje: extraerMensajeError(err) });
    }
  }
});

/**
 * GET /ventas/confirmacion/:id — Mostrar el comprobante de venta confirmada.
 */
router.get('/ventas/confirmacion/:id', async (req, res) => {
  try {
    const venta = await ventaServicio.obtenerDetalle(req.params.id);
    res.render('venta-confirmacion', { venta });
  } catch (err) {
    res.render('error', { mensaje: extraerMensajeError(err) });
  }
});

/**
 * GET /historial — Listar todas las ventas registradas.
 */
router.get('/historial', async (req, res) => {
  try {
    const ventas = await ventaServicio.obtenerHistorial();
    res.render('historial', { ventas });
  } catch (err) {
    res.render('error', { mensaje: extraerMensajeError(err) });
  }
});

/**
 * GET /historial/:id — Mostrar el detalle de una venta específica.
 */
router.get('/historial/:id', async (req, res) => {
  try {
    const venta = await ventaServicio.obtenerDetalle(req.params.id);
    res.render('venta-detalle', { venta });
  } catch (err) {
    res.render('error', { mensaje: extraerMensajeError(err) });
  }
});

module.exports = router;
