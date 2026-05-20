# Diseño Técnico — Frontend POS (Node.js 20 + Express.js 4.18.x)

## 1. Visión General

El frontend del Sistema POS es una aplicación web desarrollada en **Node.js 20 LTS** con **Express.js 4.18.x**. Sirve la interfaz de usuario al Operador de caja mediante vistas renderizadas en el servidor con **Handlebars (hbs)**. Consume la API REST del Backend Java a través de módulos de servicio centralizados que usan **Axios**. Se ejecuta en el puerto `3000`.

---

## 2. Diagrama de Arquitectura del Frontend

```
┌─────────────────────────────────────────────────────────────────┐
│                    NAVEGADOR (Operador)                         │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  HTML renderizado (Bootstrap 5)                          │  │
│  │  public/js/main.js (interacciones dinámicas mínimas)     │  │
│  └──────────────────────────────────────────────────────────┘  │
└───────────────────────────┬─────────────────────────────────────┘
                            │  HTTP GET/POST (puerto 3000)
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    EXPRESS.JS (src/app.js)                      │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  RUTAS (src/rutas/)                                     │   │
│  │  productoRutas.js  ──▶  GET /                           │   │
│  │  carritoRutas.js   ──▶  POST /carrito/items             │   │
│  │                         POST /carrito/items/:id/actualizar│  │
│  │                         POST /carrito/items/:id/eliminar │   │
│  │                         POST /carrito/vaciar             │   │
│  │  ventaRutas.js     ──▶  POST /ventas                    │   │
│  │                         GET  /historial                  │   │
│  │                         GET  /historial/:id              │   │
│  └──────────────────────────┬──────────────────────────────┘   │
│                             │  delega en                        │
│  ┌──────────────────────────▼──────────────────────────────┐   │
│  │  SERVICIOS (src/servicios/)                             │   │
│  │  productoServicio.js  ──▶  Axios → GET /api/productos   │   │
│  │  carritoServicio.js   ──▶  Axios → /api/carrito/*       │   │
│  │  ventaServicio.js     ──▶  Axios → /api/ventas/*        │   │
│  └──────────────────────────┬──────────────────────────────┘   │
│                             │  HTTP/JSON                        │
└─────────────────────────────┼───────────────────────────────────┘
                              │  (puerto 8080)
                              ▼
                    ┌─────────────────────┐
                    │  BACKEND Java/Spring │
                    │  API REST            │
                    └─────────────────────┘
```

---

## 3. Estructura de Carpetas Detallada

```
pos_frontend/
├── package.json                    ← Dependencias y scripts npm
├── .env                            ← Variables de entorno (no en git)
├── .env.example                    ← Plantilla de variables de entorno
├── .gitignore                      ← Excluye node_modules/ y .env
│
├── src/
│   ├── app.js                      ← Punto de entrada: configura Express,
│   │                                  motor de plantillas, rutas y middleware
│   ├── config/
│   │   └── config.js               ← Lee .env, exporta BACKEND_URL y PORT
│   │
│   ├── servicios/
│   │   ├── productoServicio.js     ← Llamadas HTTP a /api/productos
│   │   ├── carritoServicio.js      ← Llamadas HTTP a /api/carrito
│   │   └── ventaServicio.js        ← Llamadas HTTP a /api/ventas
│   │
│   └── rutas/
│       ├── productoRutas.js        ← Rutas Express: GET /
│       ├── carritoRutas.js         ← Rutas Express: POST /carrito/*
│       └── ventaRutas.js           ← Rutas Express: POST /ventas, GET /historial
│
├── views/
│   ├── layouts/
│   │   └── main.hbs                ← Layout principal (navbar + {{{body}}})
│   ├── index.hbs                   ← Página principal: catálogo + carrito
│   ├── venta-confirmacion.hbs      ← Confirmación de venta exitosa
│   ├── historial.hbs               ← Lista de ventas
│   ├── venta-detalle.hbs           ← Detalle de una venta
│   └── error.hbs                   ← Página de error genérica
│
└── public/
    ├── css/
    │   └── estilos.css             ← Estilos personalizados (complementa Bootstrap)
    └── js/
        └── main.js                 ← JS del cliente: deshabilitar botón, etc.
```

---

## 4. Diseño de Módulos

### 4.1 app.js — Punto de Entrada

```javascript
// src/app.js
const express = require('express');
const hbs = require('hbs');
const path = require('path');
const { PORT } = require('./config/config');

const productoRutas = require('./rutas/productoRutas');
const carritoRutas  = require('./rutas/carritoRutas');
const ventaRutas    = require('./rutas/ventaRutas');

const app = express();

// Motor de plantillas
app.set('view engine', 'hbs');
app.set('views', path.join(__dirname, '../views'));
hbs.registerPartials(path.join(__dirname, '../views/layouts'));

// Middleware
app.use(express.urlencoded({ extended: true }));
app.use(express.json());
app.use(express.static(path.join(__dirname, '../public')));

// Rutas
app.use('/', productoRutas);
app.use('/carrito', carritoRutas);
app.use('/', ventaRutas);

// Manejo de errores 404
app.use((req, res) => {
    res.status(404).render('error', { mensaje: 'Página no encontrada.' });
});

app.listen(PORT, () => console.log(`Frontend en http://localhost:${PORT}`));
```

### 4.2 config/config.js

```javascript
// src/config/config.js
require('dotenv').config();

module.exports = {
    PORT: process.env.PORT || 3000,
    BACKEND_URL: process.env.BACKEND_URL || 'http://localhost:8080'
};
```

### 4.3 Módulos de Servicio

#### productoServicio.js

```javascript
// src/servicios/productoServicio.js
const axios = require('axios');
const { BACKEND_URL } = require('../config/config');

/**
 * Obtiene todos los productos del catálogo.
 * @returns {Promise<Array>} Lista de productos.
 */
async function obtenerTodos() {
    const response = await axios.get(`${BACKEND_URL}/api/productos`);
    return response.data;
}

/**
 * Busca productos por nombre (insensible a mayúsculas).
 * @param {string} nombre - Texto de búsqueda.
 * @returns {Promise<Array>} Lista de productos filtrados.
 */
async function buscarPorNombre(nombre) {
    const response = await axios.get(`${BACKEND_URL}/api/productos`, {
        params: { nombre }
    });
    return response.data;
}

module.exports = { obtenerTodos, buscarPorNombre };
```

#### carritoServicio.js

```javascript
// src/servicios/carritoServicio.js
const axios = require('axios');
const { BACKEND_URL } = require('../config/config');

/**
 * Obtiene el estado actual del carrito.
 * @returns {Promise<Object>} CarritoDTO con items y total.
 */
async function obtenerCarrito() { ... }

/**
 * Agrega un producto al carrito.
 * @param {string} productoId - UUID del producto.
 * @param {number} cantidad - Cantidad a agregar (> 0).
 * @returns {Promise<Object>} CarritoDTO actualizado.
 */
async function agregarItem(productoId, cantidad) { ... }

/**
 * Actualiza la cantidad de un ítem en el carrito.
 * @param {string} productoId - UUID del producto.
 * @param {number} cantidad - Nueva cantidad (> 0).
 * @returns {Promise<Object>} CarritoDTO actualizado.
 */
async function actualizarItem(productoId, cantidad) { ... }

/**
 * Elimina un ítem del carrito.
 * @param {string} productoId - UUID del producto.
 * @returns {Promise<Object>} CarritoDTO actualizado.
 */
async function eliminarItem(productoId) { ... }

/**
 * Vacía el carrito completamente.
 * @returns {Promise<Object>} CarritoDTO vacío.
 */
async function vaciarCarrito() { ... }

module.exports = { obtenerCarrito, agregarItem, actualizarItem, eliminarItem, vaciarCarrito };
```

#### ventaServicio.js

```javascript
// src/servicios/ventaServicio.js
const axios = require('axios');
const { BACKEND_URL } = require('../config/config');

/**
 * Confirma la venta con el carrito activo.
 * @returns {Promise<Object>} VentaDTO con id, items, total y fechaHora.
 */
async function confirmarVenta() { ... }

/**
 * Obtiene el historial de todas las ventas.
 * @returns {Promise<Array>} Lista de VentaDTO.
 */
async function obtenerHistorial() { ... }

/**
 * Obtiene el detalle de una venta por su ID.
 * @param {string} id - UUID de la venta.
 * @returns {Promise<Object>} VentaDTO completo.
 */
async function obtenerDetalle(id) { ... }

module.exports = { confirmarVenta, obtenerHistorial, obtenerDetalle };
```

---

## 5. Diseño de Rutas Express

### 5.1 productoRutas.js

```javascript
// src/rutas/productoRutas.js
const router = require('express').Router();
const productoServicio = require('../servicios/productoServicio');
const carritoServicio  = require('../servicios/carritoServicio');

// GET / — Página principal: catálogo + carrito
router.get('/', async (req, res) => {
    try {
        const nombre = req.query.nombre || '';
        const productos = nombre
            ? await productoServicio.buscarPorNombre(nombre)
            : await productoServicio.obtenerTodos();
        const carrito = await carritoServicio.obtenerCarrito();
        res.render('index', { productos, carrito, busqueda: nombre });
    } catch (error) {
        const mensaje = extraerMensajeError(error,
            'No fue posible cargar el catálogo de productos. Intente nuevamente.');
        res.render('error', { mensaje });
    }
});
```

### 5.2 carritoRutas.js

```javascript
// src/rutas/carritoRutas.js
// POST /carrito/items              → agregarItem
// POST /carrito/items/:id/actualizar → actualizarItem
// POST /carrito/items/:id/eliminar   → eliminarItem
// POST /carrito/vaciar               → vaciarCarrito
// Todos redirigen a / tras éxito, o re-renderizan con error inline
```

### 5.3 ventaRutas.js

```javascript
// src/rutas/ventaRutas.js
// POST /ventas          → confirmarVenta → redirige a /ventas/confirmacion/:id
// GET  /ventas/confirmacion/:id → renderiza venta-confirmacion.hbs
// GET  /historial       → renderiza historial.hbs
// GET  /historial/:id   → renderiza venta-detalle.hbs
```

---

## 6. Diseño de Vistas (Handlebars)

### 6.1 Layout Principal — main.hbs

```html
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Sistema POS</title>
    <!-- Bootstrap 5 CDN -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.x/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="/css/estilos.css">
</head>
<body>
    <nav class="navbar navbar-dark bg-dark">
        <div class="container-fluid">
            <a class="navbar-brand" href="/">🛒 Sistema POS</a>
            <div>
                <a class="btn btn-outline-light me-2" href="/">Punto de Venta</a>
                <a class="btn btn-outline-light" href="/historial">Historial de Ventas</a>
            </div>
        </div>
    </nav>
    <div class="container-fluid mt-3">
        {{{body}}}
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.x/dist/js/bootstrap.bundle.min.js"></script>
    <script src="/js/main.js"></script>
</body>
</html>
```

### 6.2 index.hbs — Página Principal

Estructura de dos columnas:
- **Columna izquierda (8/12)**: Catálogo de productos en tabla con búsqueda
- **Columna derecha (4/12)**: Carrito activo con totales y botón "Confirmar Venta"

```
┌─────────────────────────────────────────────────────────────────┐
│  NAVBAR: [Sistema POS]  [Punto de Venta]  [Historial de Ventas] │
├──────────────────────────────────┬──────────────────────────────┤
│  CATÁLOGO DE PRODUCTOS           │  CARRITO                     │
│  [Buscar: ____________] [Buscar] │  ┌──────────────────────┐   │
│                                  │  │ Producto | Cant | Sub │   │
│  Código | Nombre | Precio | Stock│  │ Coca-Cola|  2   |31.00│   │
│  PROD-001 Coca-Cola 15.50  100   │  │ [−] [+]  [Eliminar]  │   │
│  [Agregar al carrito]            │  ├──────────────────────┤   │
│  PROD-002 Pepsi     14.00   80   │  │ TOTAL:         31.00 │   │
│  [Agregar al carrito]            │  ├──────────────────────┤   │
│  ...                             │  │ [Vaciar Carrito]     │   │
│                                  │  │ [Confirmar Venta]    │   │
│                                  │  └──────────────────────┘   │
└──────────────────────────────────┴──────────────────────────────┘
```

### 6.3 historial.hbs — Historial de Ventas

```
┌─────────────────────────────────────────────────────────────────┐
│  HISTORIAL DE VENTAS                                            │
│                                                                 │
│  ID (8 chars) │ Fecha y Hora          │ Total    │ Acción      │
│  7f3a9c12     │ 15/01/2024 10:30:00   │ $62.00   │ [Ver]       │
│  a1b2c3d4     │ 15/01/2024 11:15:00   │ $45.50   │ [Ver]       │
└─────────────────────────────────────────────────────────────────┘
```

### 6.4 venta-detalle.hbs — Detalle de Venta

```
┌─────────────────────────────────────────────────────────────────┐
│  DETALLE DE VENTA                                               │
│  ID: 7f3a9c12-...                                               │
│  Fecha: 15/01/2024 10:30:00                                     │
│                                                                 │
│  Producto        │ Cantidad │ Precio Unit. │ Subtotal           │
│  Coca-Cola 600ml │    2     │    $15.50    │   $31.00           │
│  Pepsi 600ml     │    2     │    $14.00    │   $28.00           │
│  ─────────────────────────────────────────────────────         │
│                                    TOTAL:    $59.00             │
│                                                                 │
│  [← Volver al Historial]                                        │
└─────────────────────────────────────────────────────────────────┘
```

### 6.5 error.hbs — Página de Error

```
┌─────────────────────────────────────────────────────────────────┐
│  ⚠️  Error                                                      │
│  {{mensaje}}                                                    │
│  [← Volver al inicio]                                           │
└─────────────────────────────────────────────────────────────────┘
```

---

## 7. Flujos de Navegación

```
GET /
  ├── [Buscar productos] → GET /?nombre=X → re-renderiza index.hbs
  ├── [Agregar al carrito] → POST /carrito/items → redirect GET /
  ├── [Actualizar cantidad] → POST /carrito/items/:id/actualizar → redirect GET /
  ├── [Eliminar ítem] → POST /carrito/items/:id/eliminar → redirect GET /
  ├── [Vaciar carrito] → POST /carrito/vaciar → redirect GET /
  └── [Confirmar Venta] → POST /ventas
        ├── [éxito 201] → redirect GET /ventas/confirmacion/:id
        └── [error 400] → re-renderiza index.hbs con mensaje de error

GET /historial
  └── [Ver detalle] → GET /historial/:id → renderiza venta-detalle.hbs

Cualquier error de red → renderiza error.hbs
```

---

## 8. Contratos de Servicio (Interfaces de los Módulos)

### productoServicio.js

| Función | Parámetros | Retorna | Errores |
|---|---|---|---|
| `obtenerTodos()` | — | `Promise<ProductoDTO[]>` | Lanza error si backend no disponible |
| `buscarPorNombre(nombre)` | `nombre: string` | `Promise<ProductoDTO[]>` | Lanza error si backend no disponible |

### carritoServicio.js

| Función | Parámetros | Retorna | Errores |
|---|---|---|---|
| `obtenerCarrito()` | — | `Promise<CarritoDTO>` | Lanza error de red |
| `agregarItem(productoId, cantidad)` | `productoId: string`, `cantidad: number` | `Promise<CarritoDTO>` | Lanza AxiosError con response.data.error |
| `actualizarItem(productoId, cantidad)` | `productoId: string`, `cantidad: number` | `Promise<CarritoDTO>` | Lanza AxiosError con response.data.error |
| `eliminarItem(productoId)` | `productoId: string` | `Promise<CarritoDTO>` | Lanza AxiosError con response.data.error |
| `vaciarCarrito()` | — | `Promise<CarritoDTO>` | Lanza error de red |

### ventaServicio.js

| Función | Parámetros | Retorna | Errores |
|---|---|---|---|
| `confirmarVenta()` | — | `Promise<VentaDTO>` | Lanza AxiosError con response.data.error |
| `obtenerHistorial()` | — | `Promise<VentaDTO[]>` | Lanza error de red |
| `obtenerDetalle(id)` | `id: string` | `Promise<VentaDTO>` | Lanza AxiosError con response.data.error |

### Función auxiliar de extracción de errores

```javascript
// Usada en todas las rutas para extraer el mensaje de error de Axios
function extraerMensajeError(error, mensajePorDefecto) {
    if (error.response && error.response.data && error.response.data.error) {
        return error.response.data.error;
    }
    return mensajePorDefecto || 'El servicio no está disponible. Verifique que el backend esté en ejecución.';
}
```

---

## 9. Manejo de Errores

### Estrategia por Tipo de Error

| Tipo de Error | Comportamiento |
|---|---|
| Error de red (backend no disponible) | Renderiza `error.hbs` con mensaje genérico |
| HTTP 400 (datos inválidos) | Muestra mensaje inline en la vista actual (no redirige) |
| HTTP 404 (recurso no encontrado) | Muestra mensaje inline o renderiza `error.hbs` |
| HTTP 500 (error interno del backend) | Renderiza `error.hbs` con mensaje genérico |
| Ruta no encontrada en el frontend | Renderiza `error.hbs` con "Página no encontrada" |

### Patrón de Manejo en Rutas

```javascript
router.post('/carrito/items', async (req, res) => {
    try {
        const { productoId, cantidad } = req.body;
        await carritoServicio.agregarItem(productoId, parseInt(cantidad));
        res.redirect('/');
    } catch (error) {
        // Error HTTP del backend (400, 404, etc.)
        if (error.response) {
            const mensajeError = error.response.data?.error || 'Error al agregar el producto.';
            // Re-renderiza la página con el error inline
            try {
                const productos = await productoServicio.obtenerTodos();
                const carrito = await carritoServicio.obtenerCarrito();
                res.render('index', { productos, carrito, error: mensajeError });
            } catch {
                res.render('error', { mensaje: mensajeError });
            }
        } else {
            // Error de red
            res.render('error', {
                mensaje: 'El servicio no está disponible. Verifique que el backend esté en ejecución.'
            });
        }
    }
});
```

---

## 10. Configuración del Proyecto

### package.json

```json
{
  "name": "pos-frontend",
  "version": "1.0.0",
  "scripts": {
    "start": "node src/app.js",
    "dev": "nodemon src/app.js"
  },
  "dependencies": {
    "axios": "1.6.x",
    "dotenv": "16.x",
    "express": "4.18.x",
    "hbs": "4.x"
  },
  "devDependencies": {
    "nodemon": "3.x"
  }
}
```

### .env.example

```
PORT=3000
BACKEND_URL=http://localhost:8080
```

### .gitignore

```
node_modules/
.env
```

---

## 11. JavaScript del Cliente (public/js/main.js)

El JavaScript del cliente es mínimo, ya que la lógica principal se maneja en el servidor. Su responsabilidad es:

1. **Deshabilitar el botón "Confirmar Venta"** cuando el carrito está vacío.
2. **Confirmación de acciones destructivas** (vaciar carrito).

```javascript
// public/js/main.js
document.addEventListener('DOMContentLoaded', () => {
    // Deshabilitar botón Confirmar Venta si el carrito está vacío
    const btnConfirmar = document.getElementById('btn-confirmar-venta');
    const carritoVacio = document.getElementById('carrito-vacio');
    if (btnConfirmar && carritoVacio) {
        btnConfirmar.disabled = carritoVacio.dataset.vacio === 'true';
    }
});
```

---

## 12. Propiedades de Corrección

*Una propiedad es una característica o comportamiento que debe mantenerse verdadero en todas las ejecuciones válidas del sistema.*

### Propiedad 1: Formateo de Moneda en Vistas

*Para cualquier* valor numérico de precio o total, la representación en la vista debe incluir el símbolo de moneda y exactamente 2 decimales.

**Valida: Requerimientos Frontend 1.2, 4.2**

### Propiedad 2: Extracción de Mensaje de Error HTTP

*Para cualquier* respuesta de error del backend con estructura `{ "error": "...", "status": N }`, la función `extraerMensajeError` debe retornar el campo `error` de la respuesta.

**Valida: Requerimientos Frontend 5.4**

### Propiedad 3: Truncado de UUID en Historial

*Para cualquier* UUID de venta, la representación en el historial debe mostrar exactamente los primeros 8 caracteres del UUID.

**Valida: Requerimientos Frontend 4.2**

---

## 13. Estrategia de Pruebas

### Pruebas Manuales de Flujos de Usuario

| Flujo | Pasos | Resultado esperado |
|---|---|---|
| Ver catálogo | Acceder a `GET /` | Lista de productos renderizada |
| Buscar producto | Ingresar texto y enviar formulario | Lista filtrada |
| Agregar al carrito | Clic en "Agregar" | Carrito actualizado, misma página |
| Actualizar cantidad | Modificar cantidad y confirmar | Carrito actualizado |
| Eliminar ítem | Clic en "Eliminar" | Ítem removido del carrito |
| Vaciar carrito | Clic en "Vaciar carrito" | Carrito vacío |
| Confirmar venta | Clic en "Confirmar Venta" | Vista de confirmación con detalle |
| Ver historial | Acceder a `GET /historial` | Lista de ventas |
| Ver detalle de venta | Clic en "Ver" | Detalle completo de la venta |
| Backend no disponible | Iniciar frontend sin backend | Vista de error con mensaje descriptivo |
| Carrito vacío → confirmar | Intentar confirmar sin ítems | Botón deshabilitado / mensaje de error |

### Pruebas de Módulos de Servicio

Los módulos de servicio pueden probarse con **Jest** + **axios-mock-adapter**:

```javascript
// Ejemplo: productoServicio.test.js
const MockAdapter = require('axios-mock-adapter');
const axios = require('axios');
const { obtenerTodos } = require('../src/servicios/productoServicio');

const mock = new MockAdapter(axios);

test('obtenerTodos retorna lista de productos', async () => {
    mock.onGet('http://localhost:8080/api/productos').reply(200, [
        { id: '...', codigo: 'PROD-001', nombre: 'Coca-Cola', precio: 15.50, stock: 100 }
    ]);
    const productos = await obtenerTodos();
    expect(productos).toHaveLength(1);
    expect(productos[0].nombre).toBe('Coca-Cola');
});
```

---

## 14. Decisiones de Diseño y Justificaciones

| Decisión | Justificación |
|---|---|
| Server-Side Rendering con Handlebars | Simplifica la arquitectura para esta fase: no requiere API separada para el frontend, el estado se obtiene del backend en cada petición. |
| POST para operaciones de carrito (no PUT/DELETE desde formularios HTML) | Los formularios HTML solo soportan GET y POST. Se usan rutas POST con sufijos `/actualizar` y `/eliminar` para simular PUT y DELETE. |
| Redirección tras operaciones exitosas (PRG pattern) | El patrón Post/Redirect/Get evita el reenvío del formulario al recargar la página. |
| Módulos de servicio centralizados | Desacopla la lógica de comunicación HTTP de las rutas Express, facilitando pruebas y el reemplazo futuro de Axios por otra librería. |
| Axios en lugar de fetch nativo | Axios simplifica el manejo de errores HTTP (lanza excepciones para códigos 4xx/5xx), tiene mejor soporte para interceptores y es más maduro en el ecosistema Node.js. |
| Bootstrap 5 vía CDN | Evita incluir archivos CSS/JS en el repositorio, simplifica la configuración y garantiza una interfaz responsiva y profesional sin esfuerzo adicional. |
| Variables de entorno con dotenv | Permite configurar la URL del backend sin modificar el código, facilitando el despliegue en diferentes entornos. |
