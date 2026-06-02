/**
 * Lógica principal del sistema POS — Vanilla JS
 */
import { listar } from './services/productosService.js';
import { crear } from './services/ventasService.js';

// ─── ESTADO ──────────────────────────────────────────────────────────────────
let carrito = [];
let sugerencias = [];
let indiceSugerencia = -1;
let timeoutBusqueda = null;
let dropdown = null;

// ─── CONTEXTO DE TECLADO ─────────────────────────────────────────────────────
// Solo un contexto activo a la vez: null | 'tabla' | 'metodo' | 'confirmacion'
let contextoTeclado = null;

// ─── REFERENCIAS DOM ─────────────────────────────────────────────────────────
const inputBusqueda     = document.getElementById('inputBusqueda');
const tablaResultados   = document.getElementById('tablaResultados');
const resultadoBusqueda = document.getElementById('resultadoBusqueda');
const carritoContenido  = document.getElementById('carritoContenido');
const cartTotalRow      = document.getElementById('cartTotalRow');
const cartTotal         = document.getElementById('cartTotal');
const badgeCount        = document.getElementById('badgeCount');
const btnVaciar         = document.getElementById('btn-vaciar');
const columnaCarrito    = document.getElementById('columnaCarrito');
const columnaBusqueda   = document.getElementById('columnaBusqueda');

// ─── MANEJADOR GLOBAL DE TECLADO ─────────────────────────────────────────────

document.addEventListener('keydown', function(e) {
  // ESC siempre cierra todo
  if (e.key === 'Escape') {
    e.preventDefault();
    ['modalEfectivo', 'modalMetodoPago', 'modalConfirmacion'].forEach(id => {
      document.getElementById(id).style.display = 'none';
    });
    cerrarDropdown();
    resultadoBusqueda.style.display = 'none';
    contextoTeclado = null;
    inputBusqueda.focus();
    return;
  }

  // Delegar según contexto activo
  if (contextoTeclado === 'tabla') {
    manejarTeclaTabla(e);
    return;
  }
  if (contextoTeclado === 'metodo') {
    manejarTeclaMetodo(e);
    return;
  }
  if (contextoTeclado === 'confirmacion') {
    if (e.key === 'Enter') {
      e.preventDefault();
      document.getElementById('btnConfirmarFinal')?.click();
    }
    return;
  }

  // Atajos globales (sin contexto activo)
  if (e.key === 'F4') { e.preventDefault(); abrirF4(); }
  if (e.key === 'F2') { e.preventDefault(); abrirModalMetodoPago(); }
  if (e.key === 'F3') { e.preventDefault(); vaciarCarrito(); }
  if (e.key === 'F5') {
    e.preventDefault();
    inputBusqueda.focus();
    inputBusqueda.select();
  }
});

// ─── HANDLERS DE CONTEXTO ────────────────────────────────────────────────────

let filaActiva = 0;

function manejarTeclaTabla(e) {
  const filas = document.querySelectorAll('#tablaResultados .product-row');
  if (e.key === 'ArrowDown') {
    e.preventDefault();
    resaltarFila(Math.min(filaActiva + 1, filas.length - 1));
  } else if (e.key === 'ArrowUp') {
    e.preventDefault();
    resaltarFila(Math.max(filaActiva - 1, 0));
  } else if (e.key === 'Enter') {
    e.preventDefault();
    agregarFilaActiva();
  }
}

let metodoActivo = 0;

function manejarTeclaMetodo(e) {
  const btns = document.querySelectorAll('#modalMetodoPago .btn-metodo');
  if (e.key === 'ArrowDown') {
    e.preventDefault();
    resaltarMetodo(Math.min(metodoActivo + 1, btns.length - 1));
  } else if (e.key === 'ArrowUp') {
    e.preventDefault();
    resaltarMetodo(Math.max(metodoActivo - 1, 0));
  } else if (e.key === 'Enter') {
    e.preventDefault();
    const metodo = btns[metodoActivo]?.dataset.metodo;
    if (metodo) seleccionarMetodo(metodo);
  }
}

// ─── AUTOCOMPLETE ─────────────────────────────────────────────────────────────

function initDropdown() {
  dropdown = document.createElement('div');
  dropdown.id = 'autocomplete-dropdown';
  Object.assign(dropdown.style, {
    position: 'absolute',
    top: '100%',
    left: '0',
    right: '0',
    background: '#111827',
    border: '1px solid rgba(0,232,122,0.4)',
    borderTop: 'none',
    borderRadius: '0 0 6px 6px',
    zIndex: '9999',
    maxHeight: '280px',
    overflowY: 'auto',
    display: 'none',
    boxShadow: '0 8px 24px rgba(0,0,0,0.6)',
  });
  inputBusqueda.parentElement.style.position = 'relative';
  inputBusqueda.parentElement.appendChild(dropdown);
}

function renderDropdown() {
  if (!dropdown) return;
  if (sugerencias.length === 0) { dropdown.style.display = 'none'; return; }

  dropdown.innerHTML = sugerencias.map((p, i) => `
    <div class="ac-item" data-idx="${i}" style="
      padding: 10px 16px;
      cursor: pointer;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid rgba(255,255,255,0.06);
      background: ${i === indiceSugerencia ? 'rgba(0,232,122,0.12)' : 'transparent'};
      color: #e2e8f0;
      font-size: 0.875rem;
    ">
      <span>${p.nombre}</span>
      <span style="font-family:monospace; font-size:0.78rem; color:#00e87a;">$ ${parseFloat(p.precio).toFixed(2)}</span>
    </div>
  `).join('');

  dropdown.style.display = 'block';

  dropdown.querySelectorAll('.ac-item').forEach(item => {
    item.addEventListener('mouseenter', () => {
      indiceSugerencia = parseInt(item.dataset.idx);
      renderDropdown();
    });
    item.addEventListener('click', () => seleccionarSugerencia(parseInt(item.dataset.idx)));
  });
}

function cerrarDropdown() {
  sugerencias = [];
  indiceSugerencia = -1;
  if (dropdown) dropdown.style.display = 'none';
}

function seleccionarSugerencia(idx) {
  const producto = sugerencias[idx];
  if (!producto) return;
  agregarAlCarrito(producto);
  inputBusqueda.value = '';
  cerrarDropdown();
  inputBusqueda.focus();
}

// ─── BÚSQUEDA ─────────────────────────────────────────────────────────────────

inputBusqueda.addEventListener('input', function() {
  const q = this.value.trim();
  clearTimeout(timeoutBusqueda);
  if (q.length < 2) { cerrarDropdown(); return; }
  timeoutBusqueda = setTimeout(async () => {
    try {
      sugerencias = await listar(q);
      indiceSugerencia = -1;
      renderDropdown();
    } catch (err) {
      cerrarDropdown();
    }
  }, 250);
});

inputBusqueda.addEventListener('keydown', async function(e) {
  // Si hay un modal abierto, no procesar aquí
  if (contextoTeclado && contextoTeclado !== 'tabla') return;

  if (sugerencias.length > 0) {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      indiceSugerencia = Math.min(indiceSugerencia + 1, sugerencias.length - 1);
      renderDropdown();
      return;
    }
    if (e.key === 'ArrowUp') {
      e.preventDefault();
      indiceSugerencia = Math.max(indiceSugerencia - 1, 0);
      renderDropdown();
      return;
    }
    if (e.key === 'Enter') {
      e.preventDefault();
      if (indiceSugerencia >= 0) {
        seleccionarSugerencia(indiceSugerencia);
      } else if (sugerencias.length === 1) {
        seleccionarSugerencia(0);
      } else {
        mostrarTablaResultados(sugerencias);
        cerrarDropdown();
        inputBusqueda.value = '';
      }
      return;
    }
    if (e.key === 'Escape') {
      cerrarDropdown();
      return;
    }
  } else if (e.key === 'Enter') {
    e.preventDefault();
    await buscarProducto(this.value.trim());
  }
});

document.addEventListener('click', (e) => {
  if (dropdown && !inputBusqueda.contains(e.target) && !dropdown.contains(e.target)) {
    cerrarDropdown();
  }
});

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
    columnaCarrito.style.display = 'none';
    columnaBusqueda.classList.remove('col-lg-8');
    columnaBusqueda.classList.add('col-lg-12');
    return;
  }
  carritoContenido.innerHTML = '<div class="cart-items">' + carrito.map((item) => `
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
  columnaCarrito.style.display = 'block';
  columnaBusqueda.classList.remove('col-lg-12');
  columnaBusqueda.classList.add('col-lg-8');
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

function resaltarFila(idx) {
  document.querySelectorAll('#tablaResultados .product-row').forEach((f, i) => {
    f.style.background = i === idx ? 'rgba(0,232,122,0.1)' : '';
    f.style.outline = i === idx ? '1px solid #00e87a' : '';
  });
  filaActiva = idx;
}

function agregarFilaActiva() {
  const filas = document.querySelectorAll('#tablaResultados .product-row');
  const fila = filas[filaActiva];
  if (!fila) return;
  agregarAlCarrito({ id: fila.dataset.id, nombre: fila.dataset.nombre, precio: fila.dataset.precio });
  setTimeout(() => resaltarFila(filaActiva), 50);
}

function mostrarTablaResultados(productos) {
  tablaResultados.innerHTML = productos.map((p, idx) => `
    <tr class="product-row" data-idx="${idx}" data-id="${p.id}" data-nombre="${p.nombre}" data-precio="${p.precio}" style="cursor:pointer;">
      <td><span class="code-badge">${p.codigo_barras || ''}</span></td>
      <td class="product-name">${p.nombre}</td>
      <td class="text-end price-cell">$ ${parseFloat(p.precio).toFixed(2)}</td>
      <td class="text-center"><span class="stock-badge">${p.stock || 0}</span></td>
      <td class="text-center"><button class="btn-add" tabindex="-1">+</button></td>
    </tr>
  `).join('');

  resultadoBusqueda.style.display = 'block';
  filaActiva = 0;
  resaltarFila(0);
  contextoTeclado = 'tabla';

  document.querySelectorAll('#tablaResultados .product-row').forEach(fila => {
    fila.addEventListener('click', () => {
      resaltarFila(parseInt(fila.dataset.idx));
      agregarFilaActiva();
    });
  });
}

// ─── MODALES ──────────────────────────────────────────────────────────────────

window.cerrarModal = function(id) {
  document.getElementById(id).style.display = 'none';
  contextoTeclado = null;
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
  contextoTeclado = null;
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

function resaltarMetodo(idx) {
  const btns = document.querySelectorAll('#modalMetodoPago .btn-metodo');
  btns.forEach((b, i) => b.classList.toggle('activo', i === idx));
  metodoActivo = idx;
}

function abrirModalMetodoPago() {
  if (carrito.length === 0) { mostrarAlerta('El carrito esta vacio', 'error'); return; }
  abrirModal('modalMetodoPago');
  metodoActivo = 0;
  resaltarMetodo(0);
  contextoTeclado = 'metodo';

  document.querySelectorAll('#modalMetodoPago .btn-metodo').forEach((btn, idx) => {
    btn.onclick = () => seleccionarMetodo(btn.dataset.metodo);
    btn.onmouseenter = () => resaltarMetodo(idx);
  });
}

function mostrarConfirmacion(titulo, detalle, onConfirmar) {
  document.getElementById('confirmacionTitulo').textContent = titulo;
  document.getElementById('confirmacionDetalle').textContent = detalle;
  document.getElementById('btnConfirmarFinal').onclick = () => {
    cerrarModal('modalConfirmacion');
    contextoTeclado = null;
    onConfirmar();
  };
  contextoTeclado = 'confirmacion';
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

// ─── INICIALIZACIÓN ───────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
  initDropdown();
  renderizarCarrito();
  columnaBusqueda.classList.add('col-lg-12');
  inputBusqueda.focus();
});
