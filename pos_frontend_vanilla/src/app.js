/**
 * Lógica principal del sistema POS.
 * Maneja el estado del carrito, renderizado del DOM y eventos del usuario.
 * Usa async/await para consumir el API Gateway mediante los servicios.
 */
import { listar } from './services/productosService.js';
import { crear } from './services/ventasService.js';

// ─── ESTADO ──────────────────────────────────────────────────────────────────
let carrito = [];

// ─── REFERENCIAS AL DOM ───────────────────────────────────────────────────────
const tablaProductos   = document.getElementById('tabla-productos');
const listaCarrito     = document.getElementById('lista-carrito');
const totalCarrito     = document.getElementById('total-carrito');
const btnConfirmar     = document.getElementById('btn-confirmar');
const selectMetodoPago = document.getElementById('metodo-pago');
const mensajeDiv       = document.getElementById('mensaje');
const loadingProductos = document.getElementById('loading-productos');

// ─── UTILIDADES ───────────────────────────────────────────────────────────────

/**
 * Muestra una notificación al usuario y la oculta después de 4 segundos.
 * @param {string} texto - Mensaje a mostrar
 * @param {'success'|'error'} tipo - Tipo de notificación
 */
function mostrarMensaje(texto, tipo) {
  mensajeDiv.textContent = texto;
  mensajeDiv.className = `mensaje ${tipo}`;
  mensajeDiv.style.display = 'block';
  setTimeout(() => {
    mensajeDiv.style.display = 'none';
  }, 4000);
}

/**
 * Calcula el total del carrito sumando precio × cantidad de cada item.
 * @returns {number} Total formateado con 2 decimales
 */
function calcularTotal() {
  return carrito.reduce((sum, item) => sum + item.precioUnitario * item.cantidad, 0);
}

// ─── CARRITO ──────────────────────────────────────────────────────────────────

/**
 * Agrega un producto al carrito. Si ya existe, incrementa la cantidad.
 * @param {Object} producto - Producto a agregar
 */
function agregarAlCarrito(producto) {
  const existente = carrito.find(item => item.productoId === producto.id);
  if (existente) {
    existente.cantidad += 1;
  } else {
    carrito.push({
      productoId: producto.id,
      nombre: producto.nombre,
      precioUnitario: producto.precio,
      cantidad: 1,
    });
  }
  renderizarCarrito();
}

/**
 * Elimina un producto del carrito por su ID.
 * @param {string} productoId - ID del producto a eliminar
 */
function eliminarDelCarrito(productoId) {
  carrito = carrito.filter(item => item.productoId !== productoId);
  renderizarCarrito();
}

/**
 * Renderiza el carrito en el DOM con los items actuales.
 */
function renderizarCarrito() {
  if (carrito.length === 0) {
    listaCarrito.innerHTML = '<tr><td colspan="4" class="carrito-vacio">El carrito está vacío</td></tr>';
    totalCarrito.textContent = '$0.00';
    return;
  }

  listaCarrito.innerHTML = carrito.map(item => `
    <tr>
      <td>${item.nombre}</td>
      <td class="text-center">${item.cantidad}</td>
      <td class="text-right">$${(item.precioUnitario * item.cantidad).toFixed(2)}</td>
      <td class="text-center">
        <button class="btn-eliminar" data-id="${item.productoId}" aria-label="Eliminar ${item.nombre}">✕</button>
      </td>
    </tr>
  `).join('');

  totalCarrito.textContent = `$${calcularTotal().toFixed(2)}`;

  // Event listeners para botones eliminar
  document.querySelectorAll('.btn-eliminar').forEach(btn => {
    btn.addEventListener('click', () => eliminarDelCarrito(btn.dataset.id));
  });
}

// ─── PRODUCTOS ────────────────────────────────────────────────────────────────

/**
 * Renderiza la tabla de productos en el DOM.
 * @param {Array} productos - Lista de productos del API
 */
function renderizarProductos(productos) {
  if (productos.length === 0) {
    tablaProductos.innerHTML = '<tr><td colspan="3" class="text-center">No hay productos disponibles</td></tr>';
    return;
  }

  tablaProductos.innerHTML = productos.map(p => `
    <tr>
      <td>${p.nombre}</td>
      <td class="text-right">$${parseFloat(p.precio).toFixed(2)}</td>
      <td class="text-center">
        <button class="btn-agregar" data-id="${p.id}" data-nombre="${p.nombre}" data-precio="${p.precio}" aria-label="Agregar ${p.nombre} al carrito">
          + Agregar
        </button>
      </td>
    </tr>
  `).join('');

  // Event listeners para botones agregar
  document.querySelectorAll('.btn-agregar').forEach(btn => {
    btn.addEventListener('click', () => {
      agregarAlCarrito({
        id: btn.dataset.id,
        nombre: btn.dataset.nombre,
        precio: parseFloat(btn.dataset.precio),
      });
      mostrarMensaje(`${btn.dataset.nombre} agregado al carrito`, 'success');
    });
  });
}

/**
 * Carga los productos desde el API Gateway y los renderiza.
 * Maneja errores de red y del servidor.
 */
async function cargarProductos() {
  loadingProductos.style.display = 'block';
  tablaProductos.innerHTML = '';

  try {
    const productos = await listar();
    renderizarProductos(productos);
  } catch (err) {
    console.error('Error cargando productos:', err);
    tablaProductos.innerHTML = `<tr><td colspan="3" class="error-texto">Error al cargar productos: ${err.message}</td></tr>`;
    mostrarMensaje(`Error al cargar productos: ${err.message}`, 'error');
  } finally {
    loadingProductos.style.display = 'none';
  }
}

// ─── VENTAS ───────────────────────────────────────────────────────────────────

/**
 * Confirma la venta con los items del carrito.
 * Valida el carrito, construye el payload y llama al servicio de ventas.
 */
async function confirmarVenta() {
  if (carrito.length === 0) {
    mostrarMensaje('El carrito está vacío. Agrega productos antes de confirmar.', 'error');
    return;
  }

  const metodoPago = selectMetodoPago.value;
  const total = calcularTotal();

  const payload = {
    productos: carrito.map(item => ({
      productoId: item.productoId,
      nombre: item.nombre,
      cantidad: item.cantidad,
      precioUnitario: item.precioUnitario,
    })),
    total,
    metodoPago,
    fecha: new Date().toISOString(),
  };

  btnConfirmar.disabled = true;
  btnConfirmar.textContent = 'Procesando...';

  try {
    const venta = await crear(payload);
    mostrarMensaje(`✓ Venta registrada exitosamente. ID: ${venta.id} | Total: $${total.toFixed(2)}`, 'success');
    carrito = [];
    renderizarCarrito();
  } catch (err) {
    console.error('Error registrando venta:', err);
    mostrarMensaje(`Error al registrar la venta: ${err.message}`, 'error');
  } finally {
    btnConfirmar.disabled = false;
    btnConfirmar.textContent = 'Confirmar venta';
  }
}

// ─── INICIALIZACIÓN ───────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
  // Cargar productos al iniciar
  cargarProductos();

  // Botón confirmar venta
  btnConfirmar.addEventListener('click', confirmarVenta);

  // Inicializar carrito vacío
  renderizarCarrito();
});
