'use strict';

// Carrito en memoria (sesión del servidor)
let carrito = { items: [], total: 0 };

function obtenerCarrito() {
  return Promise.resolve(carrito);
}

function agregarItem(productoId, cantidad) {
  const item = carrito.items.find(i => i.productoId === productoId);
  if (item) {
    item.cantidad += parseInt(cantidad, 10);
  } else {
    carrito.items.push({ productoId, cantidad: parseInt(cantidad, 10) });
  }
  return Promise.resolve(carrito);
}

function actualizarItem(productoId, cantidad) {
  const item = carrito.items.find(i => i.productoId === productoId);
  if (item) item.cantidad = parseInt(cantidad, 10);
  return Promise.resolve(carrito);
}

function eliminarItem(productoId) {
  carrito.items = carrito.items.filter(i => i.productoId !== productoId);
  return Promise.resolve(carrito);
}

function vaciarCarrito() {
  carrito = { items: [], total: 0 };
  return Promise.resolve(carrito);
}

module.exports = { obtenerCarrito, agregarItem, actualizarItem, eliminarItem, vaciarCarrito };