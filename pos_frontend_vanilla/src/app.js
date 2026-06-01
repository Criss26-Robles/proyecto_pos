/**
 * Lógica principal del sistema POS — Vanilla JS
 * Maneja estado del carrito, búsqueda de productos, modales y atajos de teclado.
 */
import { listar } from './services/productosService.js';
import { crear } from './services/ventasService.js';

// ─── ESTADO ──────────────────────────────────────────────────────────────────
let carrito = [];

// ─── REFERENCIAS DOM ─────────────────────────────────────────────────────────
const inputBusqueda    = document.getElementById('inputBusqueda');
const tablaResultados  = document.getElementById('tablaResultados');
const resultadoBusqueda = document.getElementById('resultadoBusqueda');
const carritoContenido = document.getElementById('carritoContenido');
const cartTotalRow     = document.getElementById('cartTotalRow');
const cartTotal        = document.getElementById('cartTotal');
const badgeCount       = document.getElementById('badgeCount');
const btnVaciar        = document.getElementById('btn-vaciar');

// ─── UTILIDADES ───────────────────────────────────────────────────────────────

function mostrarAlerta(mensaje, tipo) {
  const alerta = document.createElement('div');
  alerta.style.cssText = 'position:fixed; top:20px; right:20px; padding:16px 24px; border-radius:8px; z-index:9999; font-weight:600;';
  if (tipo === 'error') {
    alerta.style.background = '#3a1a1a';
    alerta.style.border = '1px solid #ff4444';
    alerta.style.color = '#ff4444';
    alerta.textContent = '[!] ' + mensaje;
  } else {
    alerta.style.background = '#1a3a2a';
    alerta.style.border = '1px solid #00ff88';
    alerta.style.color = '#00ff88';
    alerta.textContent = '[OK] ' + mensaje;
  }
  document.body.appendChild(alerta);
  setTimeout(() => alerta.remove(), 3000);
}

function calcularTotal() {
  return carrito.reduce((sum, item) => sum + item.precioUnitario * item.cantidad, 0);
}

// ─── CARRITO ──────────────────────────────────────────────────────────────────

function agregarAlCarrito(producto) {
  const existente = carrito.find(i => i.productoId === producto.id);
  if (existente) {
    existente.cantidad += 1;
  } else {
    carrito.push({
      productoId: producto.id,
      nombreProducto: producto.nombre,
      precioUnitario: parseFloat(producto.precio),
      cantidad: 1,
    });
  }
  renderizarCarrito();
  mostrarAlerta(producto.nombre + ' agregado al carrito', 'ok');
}

function eliminarDelCarrito(productoId) {
  carrito = carrito.filter(i => i.productoId !== productoId);
  renderizarCarrito();
}

function actualizarCantidad(productoId, delta) {
  const item = carrito.find(i => i.productoId === productoId);
  if (!item) return;
  item.cantidad += delta;
  if (item.cantidad <= 0) eliminarDelCarrito(productoId);
  else renderizarCarrito();
}

window.vaciarCarrito = function() {
  if (carrito.length === 0) return;
  mostrarConfirmacion('Vaciar Carrito', '¿Seguro que desea vaciar el carrito?', () => {
    carrito = [];
    renderizarCarrito();
  });
};

function renderizarCarrito() {
  const total = calcularTotal();

  if (carrito.length === 0) {
    carritoContenido.innerHTML = '<div class="cart-empty"><p>El carrito esta vacio</p><small>Agrega productos del catalogo</small></div>';
    cartTotalRow.style.display = 'none';
    badgeCount.style.display = 'none';
    btnVaciar.style.display = 'none';
    return;
  }

  carritoContenido.innerHTML = '<div class="cart-items">' + carrito.map((item, idx) => `
    <div class="cart-item">
      <div class="cart-item-info">
        <span class="cart-item-name">${item.nombreProducto}</span>
        <span class="cart-item-price">$ ${item.precioUnitario.toFixed(2)} c/u</span>
      </div>
      <div class="cart-item-controls">
        <button class="qty-btn qty-minus" data-id="${item.productoId}">-</button>
        <span style="font-family:monospace; font-size:0.85rem; min-width:24px; text-align:center;">${item.cantidad}</span>
        <button class="qty-btn qty-plus" data-id="${item.productoId}">+</button>
        <span class="cart-item-subtotal">$ ${(item.precioUnitario * item.cantidad).toFixed(2)}</span>
        <button class="btn-remove" data-id="${item.productoId}">X</button>
      </div>
    </div>
  `).join('') + '</div>';

  cartTotal.textContent = '$ ' + total.toFixed(2);
  cartTotalRow.style.display = 'flex';
  badgeCount.textContent = carrito.length;
  badgeCount.style.display = 'inline';
  btnVaciar.style.display = 'block';

  // Event listeners
  document.querySelectorAll('.qty-minus').forEach(btn =>
    btn.addEventListener('click', () => actualizarCantidad(btn.dataset.id, -1))
  );
  document.querySelectorAll('.qty-plus').forEach(btn =>
    btn.addEventListener('click', () => actualizarCantidad(btn.dataset.id, 1))
  );
  document.querySelectorAll('.btn-remove').forEach(btn =>
    btn.addEventListener('click', () => eliminarDelCarrito(btn.dataset.id))
  );
}

// ─── BÚSQUEDA ─────────────────────────────────────────────────────────────────

async function buscarProducto(query) {
  if (!query.trim()) return;
  try {
    const productos = await listar(query);
    if (productos.length === 0) {
      mostrarAlerta('No se encontraron productos para: ' + query, 'error');
      resultadoBusqueda.style.display = 'none';
    } else if (productos.length === 1) {
      agregarAlCarrito(productos[0]);
      resultadoBusqueda.style.display = 'none';
    } else {
      mostrarTablaResultados(productos);
    }
    inputBusqueda.value = '';
  } catch (err) {
    mostrarAlerta('Error al buscar productos: ' + err.message, 'error');
  }
}

function mostrarTablaResultados(productos) {
  tablaResultados.innerHTML = productos.map(p => `
    <tr class="product-row" style="cursor:pointer;">
      <td><span class="code-badge">${p.codigo_barras || ''}</span></td>
      <td class="product-name">${p.nombre}</td>
      <td class="text-end price-cell">$ ${parseFloat(p.precio).toFixed(2)}</td>
      <td class="text-center"><span class="stock-badge">${p.stock || 0}</span></td>
      <td class="text-center">
        <button class="btn-add" data-id="${p.id}" data-nombre="${p.nombre}" data-precio="${p.precio}">+</button>
      </td>
    </tr>
  `).join('');

  resultadoBusqueda.style.display = 'block';

  document.querySelectorAll('#tablaResultados .btn-add').forEach(btn => {
    btn.addEventListener('click', () => {
      agregarAlCarrito({ id: btn.dataset.id, nombre: btn.dataset.nombre, precio: btn.dataset.precio });
      resultadoBusqueda.style.display = 'none';
      inputBusqueda.value = '';
      inputBusqueda.focus();
    });
  });

  document.querySelectorAll('#tablaResultados .product-row').forEach((row, idx) => {
    row.addEventListener('click', (e) => {
      if (e.target.classList.contains('btn-add')) return;
      const btn = row.querySelector('.btn-add');
      agregarAlCarrito({ id: btn.dataset.id, nombre: btn.dataset.nombre, precio: btn.dataset.precio });
      resultadoBusqueda.style.display = 'none';
      inputBusqueda.value = '';
      inputBusqueda.focus();
    });
  });
}

// ─── MODALES ──────────────────────────────────────────────────────────────────

window.cerrarModal = function(id) {
  document.getElementById(id).style.display = 'none';
  inputBusqueda.focus();
};

function abrirModal(id) {
  document.getElementById(id).style.display = 'flex';
}

window.abrirF4 = function() {
  if (carrito.length === 0) { mostrarAlerta('El carrito esta vacio', 'error'); return; }
  document.getElementById('totalMostrar').textContent = '$ ' + calcularTotal().toFixed(2);
  document.getElementById('montoRecibido').value = '';
  document.getElementById('cambioBox').style.display = 'none';
  abrirModal('modalEfectivo');
  setTimeout(() => document.getElementById('montoRecibido').focus(), 100);
};

document.getElementById('montoRecibido').addEventListener('input', function() {
  const total = calcularTotal();
  const recibido = parseFloat(this.value) || 0;
  const cambio = recibido - total;
  const cambioBox = document.getElementById('cambioBox');
  if (recibido > 0) {
    cambioBox.style.display = 'block';
    document.getElementById('cambioMostrar').textContent = '$ ' + cambio.toFixed(2);
    cambioBox.style.borderColor = cambio >= 0 ? '#00ff88' : '#ff4444';
    document.getElementById('cambioMostrar').style.color = cambio >= 0 ? '#00ff88' : '#ff4444';
  } else {
    cambioBox.style.display = 'none';
  }
});

document.getElementById('montoRecibido').addEventListener('keypress', function(e) {
  if (e.key === 'Enter') {
    e.preventDefault();
    if (document.getElementById('cambioBox').style.display === 'block') confirmarEfectivo();
  }
});

window.confirmarEfectivo = function() {
  const total = calcularTotal();
  const recibido = parseFloat(document.getElementById('montoRecibido').value) || 0;
  if (recibido < total) { mostrarAlerta('Monto insuficiente', 'error'); return; }
  const cambio = recibido - total;
  cerrarModal('modalEfectivo');
  mostrarConfirmacion(
    'Confirmar Venta en Efectivo',
    'Total: $ ' + total.toFixed(2) + ' | Cambio: $ ' + cambio.toFixed(2),
    () => enviarVenta('efectivo')
  );
};

window.seleccionarMetodo = function(metodo) {
  cerrarModal('modalMetodoPago');
  mostrarConfirmacion(
    'Confirmar Pago con ' + metodo,
    'Se registrara la venta con ' + metodo,
    () => enviarVenta(metodo)
  );
};

function mostrarConfirmacion(titulo, detalle, onConfirmar) {
  document.getElementById('confirmacionTitulo').textContent = titulo;
  document.getElementById('confirmacionDetalle').textContent = detalle;
  document.getElementById('btnConfirmarFinal').onclick = () => {
    cerrarModal('modalConfirmacion');
    onConfirmar();
  };
  abrirModal('modalConfirmacion');
}

// ─── VENTAS ───────────────────────────────────────────────────────────────────

async function enviarVenta(metodoPago) {
  const payload = {
    productos: carrito.map(i => ({
      productoId: i.productoId,
      nombre: i.nombreProducto,
      cantidad: i.cantidad,
      precioUnitario: i.precioUnitario,
    })),
    total: calcularTotal(),
    metodoPago,
    fecha: new Date().toISOString(),
  };

  try {
    const venta = await crear(payload);
    mostrarAlerta('Venta registrada! ID: ' + venta.id, 'ok');
    carrito = [];
    renderizarCarrito();
  } catch (err) {
    mostrarAlerta('Error al registrar la venta: ' + err.message, 'error');
  }
}

// ─── ATAJOS DE TECLADO ────────────────────────────────────────────────────────

document.addEventListener('keydown', function(e) {
  if (e.key === 'F4') { e.preventDefault(); abrirF4(); }
  if (e.key === 'F2') {
    e.preventDefault();
    if (carrito.length === 0) { mostrarAlerta('El carrito esta vacio', 'error'); return; }
    abrirModal('modalMetodoPago');
  }
  if (e.key === 'F3') {
    e.preventDefault();
    vaciarCarrito();
  }
  if (e.key === 'F5') {
    e.preventDefault();
    inputBusqueda.focus();
    inputBusqueda.select();
  }
  if (e.key === 'Escape') {
    e.preventDefault();
    ['modalEfectivo', 'modalMetodoPago', 'modalConfirmacion'].forEach(id => {
      document.getElementById(id).style.display = 'none';
    });
    resultadoBusqueda.style.display = 'none';
    inputBusqueda.focus();
  }
});

document.addEventListener('keypress', function(e) {
  if (e.key === 'Enter') {
    const modalConf = document.getElementById('modalConfirmacion');
    if (modalConf && modalConf.style.display === 'flex') {
      e.preventDefault();
      document.getElementById('btnConfirmarFinal')?.click();
    }
  }
});

// ─── INICIALIZACIÓN ───────────────────────────────────────────────────────────

inputBusqueda.addEventListener('keypress', async function(e) {
  if (e.key !== 'Enter') return;
  await buscarProducto(this.value.trim());
});

document.addEventListener('DOMContentLoaded', () => {
  renderizarCarrito();
  inputBusqueda.focus();
});
