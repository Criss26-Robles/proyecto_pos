
# Tareas de Implementación — Frontend POS (Node.js 20 + Express.js 4.18.x)

## Visión General

Implementación del frontend del Sistema POS con Express.js, Handlebars y Axios. La arquitectura separa la lógica de comunicación HTTP (módulos de servicio) de la lógica de enrutamiento (rutas Express) y la presentación (vistas Handlebars). Se aplica el patrón PRG (Post/Redirect/Get) para operaciones de formulario.

**Tecnologías**: Node.js 20 LTS, Express.js 4.18.x, Handlebars (hbs) 4.x, Axios 1.6.x, dotenv 16.x, Jest, axios-mock-adapter

---

## Fase 1: Inicialización del Proyecto

- [ ] 1. Configurar el proyecto Node.js
  - [ ] 1.1 Crear `package.json`
    - `"name": "pos-frontend"`, `"version": "1.0.0"`
    - Script `"start": "node src/app.js"`
    - Script `"dev": "nodemon src/app.js"`
    - Script `"test": "jest"`
    - Dependencias: `"express": "4.18.x"`, `"hbs": "4.x"`, `"axios": "1.6.x"`, `"dotenv": "16.x"`
    - DevDependencies: `"nodemon": "3.x"`, `"jest": "^29.x"`, `"axios-mock-adapter": "^1.x"`
    - _Requerimientos: 6.1, 11.4_
  - [ ] 1.2 Crear `.env.example`
    - Contenido: `PORT=3000` y `BACKEND_URL=http://localhost:8080`
    - _Requerimientos: 6.2, 11.7_
  - [ ] 1.3 Crear `.gitignore`
    - Excluir `node_modules/` y `.env`
    - _Requerimientos: 6.3_
  - [ ] 1.4 Crear `.env` (copia de `.env.example` para desarrollo local)
    - Contenido: `PORT=3000` y `BACKEND_URL=http://localhost:8080`
    - _Requerimientos: 5.2, 5.5_
  - [ ] 1.5 Crear estructura de directorios
    - `src/config/`, `src/servicios/`, `src/rutas/`
    - `views/layouts/`, `public/css/`, `public/js/`
    - _Requerimientos: 11.2_
  - **Criterio de verificación**: `npm install` ejecuta sin errores; la estructura de carpetas existe.


---

## Fase 2: Configuración Centralizada

- [ ] 2. Crear módulo de configuración centralizada
  - [ ] 2.1 Crear `src/config/config.js`
    - Primera línea: `require('dotenv').config()`
    - Exportar objeto con:
      - `PORT: process.env.PORT || 3000`
      - `BACKEND_URL: process.env.BACKEND_URL || 'http://localhost:8080'`
    - Agregar comentario JSDoc describiendo el módulo
    - _Requerimientos: 5.2, 5.5, 10.2_
  - **Criterio de verificación**: `require('./src/config/config')` retorna `{ PORT: 3000, BACKEND_URL: 'http://localhost:8080' }` con el `.env` de ejemplo.

---

## Fase 3: Módulos de Servicio (Cliente HTTP)

- [ ] 3. Implementar módulos de servicio en `src/servicios/`
  - [ ] 3.1 Crear `src/servicios/productoServicio.js`
    - Importar `axios` y `{ BACKEND_URL }` de `../config/config`
    - Función `async obtenerTodos()`:
      - `GET ${BACKEND_URL}/api/productos`
      - Retorna `response.data`
      - JSDoc: descripción, `@returns {Promise<Array>}`
    - Función `async buscarPorNombre(nombre)`:
      - `GET ${BACKEND_URL}/api/productos` con `params: { nombre }`
      - Retorna `response.data`
      - JSDoc: `@param {string} nombre`, `@returns {Promise<Array>}`
    - `module.exports = { obtenerTodos, buscarPorNombre }`
    - _Requerimientos: 5.1, 7.3, 7.5_
  - [ ] 3.2 Crear `src/servicios/carritoServicio.js`
    - Importar `axios` y `{ BACKEND_URL }` de `../config/config`
    - Función `async obtenerCarrito()`:
      - `GET ${BACKEND_URL}/api/carrito`
      - Retorna `response.data`
      - JSDoc completo
    - Función `async agregarItem(productoId, cantidad)`:
      - `POST ${BACKEND_URL}/api/carrito/items` con body `{ productoId, cantidad }`
      - Retorna `response.data`
      - JSDoc: `@param {string} productoId`, `@param {number} cantidad`
    - Función `async actualizarItem(productoId, cantidad)`:
      - `PUT ${BACKEND_URL}/api/carrito/items/${productoId}` con body `{ cantidad }`
      - Retorna `response.data`
    - Función `async eliminarItem(productoId)`:
      - `DELETE ${BACKEND_URL}/api/carrito/items/${productoId}`
      - Retorna `response.data`
    - Función `async vaciarCarrito()`:
      - `DELETE ${BACKEND_URL}/api/carrito`
      - Retorna `response.data`
    - `module.exports = { obtenerCarrito, agregarItem, actualizarItem, eliminarItem, vaciarCarrito }`
    - _Requerimientos: 5.1, 7.3, 7.5_
  - [ ] 3.3 Crear `src/servicios/ventaServicio.js`
    - Importar `axios` y `{ BACKEND_URL }` de `../config/config`
    - Función `async confirmarVenta()`:
      - `POST ${BACKEND_URL}/api/ventas` sin body
      - Retorna `response.data`
      - JSDoc: `@returns {Promise<Object>} VentaDTO con id, items, total y fechaHora`
    - Función `async obtenerHistorial()`:
      - `GET ${BACKEND_URL}/api/ventas`
      - Retorna `response.data`
    - Función `async obtenerDetalle(id)`:
      - `GET ${BACKEND_URL}/api/ventas/${id}`
      - Retorna `response.data`
      - JSDoc: `@param {string} id - UUID de la venta`
    - `module.exports = { confirmarVenta, obtenerHistorial, obtenerDetalle }`
    - _Requerimientos: 5.1, 7.3, 7.5_
  - **Criterio de verificación**: Los módulos exportan las funciones correctas; no hay errores de sintaxis.


---

## Fase 4: Layout Principal y Vistas Handlebars

- [ ] 4. Crear el layout principal
  - [ ] 4.1 Crear `views/layouts/main.hbs`
    - Estructura HTML5 completa con `lang="es"`
    - `<meta charset="UTF-8">` y `<title>Sistema POS</title>`
    - Bootstrap 5.3.x CDN en `<head>`: `https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css`
    - `<link rel="stylesheet" href="/css/estilos.css">`
    - Navbar con clase `navbar navbar-dark bg-dark`:
      - Brand: `🛒 Sistema POS` con enlace a `/`
      - Botón `btn btn-outline-light` → "Punto de Venta" (`href="/"`)
      - Botón `btn btn-outline-light` → "Historial de Ventas" (`href="/historial"`)
    - `<div class="container-fluid mt-3">{{{body}}}</div>`
    - Bootstrap JS CDN antes de `</body>`
    - `<script src="/js/main.js"></script>`
    - _Requerimientos: 6.5, 10.6_

- [ ] 5. Crear vistas de la aplicación
  - [ ] 5.1 Crear `views/index.hbs` — Página principal (catálogo + carrito)
    - Estructura de dos columnas con Bootstrap grid (`col-md-8` y `col-md-4`)
    - **Columna izquierda — Catálogo**:
      - Título "Catálogo de Productos"
      - Formulario de búsqueda: `<form method="GET" action="/">` con `<input name="nombre">` y botón "Buscar"
      - Tabla Bootstrap con encabezados: "Código", "Nombre", "Precio", "Stock", "Acción"
      - Iteración `{{#each productos}}` para filas de la tabla
      - Botón "Agregar al carrito" como `<form method="POST" action="/carrito/items">` con `<input type="hidden" name="productoId" value="{{id}}">` y `<input type="hidden" name="cantidad" value="1">`
    - **Columna derecha — Carrito**:
      - Título "Carrito"
      - Mostrar mensaje de error inline si `{{error}}` existe: `<div class="alert alert-danger">{{error}}</div>`
      - Tabla con ítems del carrito: nombre, cantidad (con formulario de actualización), precio unitario, subtotal, botón "Eliminar"
      - Fila de total: `TOTAL: {{carrito.total}}`
      - `<span id="carrito-vacio" data-vacio="{{#if carrito.items.length}}false{{else}}true{{/if}}"></span>`
      - Botón "Vaciar Carrito": `<form method="POST" action="/carrito/vaciar">`
      - Botón "Confirmar Venta" con `id="btn-confirmar-venta"`: `<form method="POST" action="/ventas">`
    - _Requerimientos: 1.1–1.5, 2.5, 2.6, 3.4, 3.6, 6.5, 6.6_
  - [ ] 5.2 Crear `views/historial.hbs` — Historial de ventas
    - Título "Historial de Ventas"
    - Condicional: si `{{ventas.length}}` es 0, mostrar `<p>No hay ventas registradas aún.</p>`
    - Tabla Bootstrap con encabezados: "ID", "Fecha y Hora", "Total", "Acción"
    - Iteración `{{#each ventas}}` para filas
    - ID: mostrar solo los primeros 8 caracteres del UUID (usar helper Handlebars o truncar en la ruta)
    - Botón "Ver" como enlace `<a href="/historial/{{id}}">Ver</a>`
    - _Requerimientos: 4.1–4.4_
  - [ ] 5.3 Crear `views/venta-detalle.hbs` — Detalle de venta
    - Título "Detalle de Venta"
    - Mostrar: ID completo, fecha y hora formateada
    - Tabla con ítems: "Producto", "Cantidad", "Precio Unitario", "Subtotal"
    - Fila de total al final de la tabla
    - Enlace `<a href="/historial">← Volver al Historial</a>`
    - _Requerimientos: 4.3_
  - [ ] 5.4 Crear `views/venta-confirmacion.hbs` — Confirmación de venta
    - Alerta de éxito Bootstrap: `<div class="alert alert-success">`
    - Mostrar: ID de venta, tabla de productos vendidos con cantidades y subtotales, total
    - Enlace "← Volver al Punto de Venta" (`href="/"`)
    - _Requerimientos: 3.2_
  - [ ] 5.5 Crear `views/error.hbs` — Página de error genérica
    - Alerta de peligro Bootstrap: `<div class="alert alert-danger">`
    - Ícono de advertencia y mensaje `{{mensaje}}`
    - Enlace "← Volver al inicio" (`href="/"`)
    - _Requerimientos: 1.4, 4.5, 5.3_
  - **Criterio de verificación**: Las vistas renderizan sin errores de sintaxis Handlebars.


---

## Fase 5: Estilos CSS y JavaScript del Cliente

- [ ] 6. Crear archivos estáticos
  - [ ] 6.1 Crear `public/css/estilos.css`
    - Estilos para la tabla del catálogo (hover, bordes)
    - Estilos para el panel del carrito (sombra, fondo claro)
    - Estilos para el navbar (altura, padding)
    - Estilos responsivos para pantallas pequeñas
    - _Requerimientos: 6.6_
  - [ ] 6.2 Crear `public/js/main.js`
    - Listener `DOMContentLoaded`
    - Obtener elemento `#btn-confirmar-venta` y `#carrito-vacio`
    - Si `carritoVacio.dataset.vacio === 'true'`, establecer `btnConfirmar.disabled = true`
    - Comentario explicando la responsabilidad del módulo
    - _Requerimientos: 3.6_
  - **Criterio de verificación**: El botón "Confirmar Venta" aparece deshabilitado cuando el carrito está vacío.

---

## Fase 6: Rutas Express

- [ ] 7. Implementar rutas Express en `src/rutas/`
  - [ ] 7.1 Crear `src/rutas/productoRutas.js`
    - Importar `express.Router()`, `productoServicio` y `carritoServicio`
    - Definir función auxiliar `extraerMensajeError(error, mensajePorDefecto)`:
      - Si `error.response?.data?.error` existe, retornarlo
      - Si no, retornar `mensajePorDefecto || 'El servicio no está disponible...'`
    - `router.get('/', async (req, res) => { ... })`:
      - Leer `req.query.nombre || ''`
      - Si hay nombre: `productoServicio.buscarPorNombre(nombre)`, si no: `productoServicio.obtenerTodos()`
      - Obtener carrito con `carritoServicio.obtenerCarrito()`
      - `res.render('index', { productos, carrito, busqueda: nombre })`
      - En `catch`: `res.render('error', { mensaje: extraerMensajeError(error, 'No fue posible cargar el catálogo...') })`
    - `module.exports = router`
    - _Requerimientos: 1.1–1.4, 5.4, 7.1, 7.4_
  - [ ] 7.2 Crear `src/rutas/carritoRutas.js`
    - Importar `express.Router()`, `carritoServicio` y `productoServicio`
    - Reutilizar o importar `extraerMensajeError`
    - `router.post('/items', async (req, res) => { ... })`:
      - Extraer `productoId` y `cantidad` de `req.body`
      - `await carritoServicio.agregarItem(productoId, parseInt(cantidad))`
      - En éxito: `res.redirect('/')`
      - En error HTTP (error.response): re-renderizar `index` con `error` inline (obtener productos y carrito primero)
      - En error de red: `res.render('error', { mensaje: '...' })`
    - `router.post('/items/:id/actualizar', async (req, res) => { ... })`:
      - Extraer `id` de `req.params` y `cantidad` de `req.body`
      - `await carritoServicio.actualizarItem(id, parseInt(cantidad))`
      - En éxito: `res.redirect('/')`; en error: manejo inline
    - `router.post('/items/:id/eliminar', async (req, res) => { ... })`:
      - `await carritoServicio.eliminarItem(req.params.id)`
      - En éxito: `res.redirect('/')`; en error: manejo inline
    - `router.post('/vaciar', async (req, res) => { ... })`:
      - `await carritoServicio.vaciarCarrito()`
      - En éxito: `res.redirect('/')`; en error: `res.render('error', { ... })`
    - `module.exports = router`
    - _Requerimientos: 2.1–2.7, 5.4, 7.1, 7.4_
  - [ ] 7.3 Crear `src/rutas/ventaRutas.js`
    - Importar `express.Router()`, `ventaServicio`, `carritoServicio` y `productoServicio`
    - `router.post('/ventas', async (req, res) => { ... })`:
      - `const venta = await ventaServicio.confirmarVenta()`
      - En éxito (HTTP 201): `res.redirect('/ventas/confirmacion/' + venta.id)`
      - En error HTTP 400: re-renderizar `index` con mensaje de error inline
      - En error de red: `res.render('error', { mensaje: '...' })`
    - `router.get('/ventas/confirmacion/:id', async (req, res) => { ... })`:
      - `const venta = await ventaServicio.obtenerDetalle(req.params.id)`
      - `res.render('venta-confirmacion', { venta })`
    - `router.get('/historial', async (req, res) => { ... })`:
      - `const ventas = await ventaServicio.obtenerHistorial()`
      - Mapear cada venta para truncar el ID a 8 caracteres: `{ ...v, idCorto: v.id.substring(0, 8) }`
      - `res.render('historial', { ventas })`
      - En error: `res.render('error', { mensaje: '...' })`
    - `router.get('/historial/:id', async (req, res) => { ... })`:
      - `const venta = await ventaServicio.obtenerDetalle(req.params.id)`
      - `res.render('venta-detalle', { venta })`
      - En error: `res.render('error', { mensaje: '...' })`
    - `module.exports = router`
    - _Requerimientos: 3.1–3.5, 4.1–4.5, 5.4, 7.1, 7.4_
  - **Criterio de verificación**: Las rutas compilan sin errores; el patrón PRG funciona correctamente.


---

## Fase 7: Punto de Entrada Express

- [ ] 8. Crear el punto de entrada de la aplicación
  - [ ] 8.1 Crear `src/app.js`
    - Importar: `express`, `hbs`, `path`, `{ PORT }` de `./config/config`
    - Importar rutas: `productoRutas`, `carritoRutas`, `ventaRutas`
    - Crear instancia `const app = express()`
    - Configurar motor de plantillas:
      - `app.set('view engine', 'hbs')`
      - `app.set('views', path.join(__dirname, '../views'))`
      - `hbs.registerPartials(path.join(__dirname, '../views/layouts'))`
    - Configurar middleware:
      - `app.use(express.urlencoded({ extended: true }))` — para formularios HTML
      - `app.use(express.json())` — para JSON bodies
      - `app.use(express.static(path.join(__dirname, '../public')))` — archivos estáticos
    - Montar rutas:
      - `app.use('/', productoRutas)`
      - `app.use('/carrito', carritoRutas)`
      - `app.use('/', ventaRutas)`
    - Manejador 404 al final: `app.use((req, res) => res.status(404).render('error', { mensaje: 'Página no encontrada.' }))`
    - Iniciar servidor: `app.listen(PORT, () => console.log(...))`
    - _Requerimientos: 5.5, 5.6, 6.1, 6.4_
  - **Criterio de verificación**: `npm start` arranca el servidor en el puerto 3000 sin errores.

---

## Fase 8: Checkpoint — Verificación de Arranque y Flujos Básicos

- [ ] 9. Checkpoint — Verificar arranque y flujos básicos del frontend
  - Ejecutar `npm install` y `npm start`
  - Verificar que el servidor arranca en `http://localhost:3000`
  - Con el backend activo, verificar:
    - `GET http://localhost:3000/` → renderiza catálogo con 10 productos
    - Agregar un producto al carrito → carrito actualizado en la misma página
    - Confirmar venta → redirige a vista de confirmación
    - `GET http://localhost:3000/historial` → lista de ventas
  - Sin el backend activo, verificar:
    - `GET http://localhost:3000/` → renderiza `error.hbs` con mensaje descriptivo
  - Asegurarse de que todos los flujos básicos funcionan; consultar al usuario si surgen dudas.


---

## Fase 9: Pruebas de Módulos de Servicio (Jest + axios-mock-adapter)

- [ ] 10. Escribir pruebas de los módulos de servicio
  - [ ]* 10.1 Crear `src/servicios/productoServicio.test.js`
    - Importar `MockAdapter` de `axios-mock-adapter`, `axios` y `{ obtenerTodos, buscarPorNombre }`
    - Crear `const mock = new MockAdapter(axios)` en `beforeEach`; `mock.reset()` en `afterEach`
    - Caso 1: `obtenerTodos retorna lista de productos`
      - `mock.onGet('http://localhost:8080/api/productos').reply(200, [{ id: '...', nombre: 'Coca-Cola', ... }])`
      - Verificar que `obtenerTodos()` retorna array con 1 elemento y `nombre === 'Coca-Cola'`
    - Caso 2: `buscarPorNombre retorna lista filtrada`
      - `mock.onGet('http://localhost:8080/api/productos').reply(200, [...])`
      - Verificar que se llama con `params: { nombre: 'coca' }`
    - Caso 3: `obtenerTodos lanza error cuando el backend no está disponible`
      - `mock.onGet(...).networkError()`
      - Verificar que la promesa es rechazada
    - _Requerimientos: 5.1, 5.4_
  - [ ]* 10.2 Crear `src/servicios/carritoServicio.test.js`
    - Crear mock de axios con `MockAdapter`
    - Caso 1: `obtenerCarrito retorna CarritoDTO`
      - Mock `GET /api/carrito` → `{ items: [], total: 0 }`
      - Verificar que retorna el objeto correcto
    - Caso 2: `agregarItem envía POST con productoId y cantidad`
      - Mock `POST /api/carrito/items` → `{ items: [...], total: 15.50 }`
      - Verificar que retorna el carrito actualizado
    - Caso 3: `agregarItem lanza AxiosError cuando el backend retorna 404`
      - Mock `POST /api/carrito/items` → reply 404 con `{ error: 'Producto no encontrado...' }`
      - Verificar que la promesa es rechazada con el error correcto
    - Caso 4: `actualizarItem envía PUT con la nueva cantidad`
    - Caso 5: `eliminarItem envía DELETE al endpoint correcto`
    - Caso 6: `vaciarCarrito envía DELETE a /api/carrito`
    - _Requerimientos: 5.1, 5.4_
  - [ ]* 10.3 Crear `src/servicios/ventaServicio.test.js`
    - Crear mock de axios con `MockAdapter`
    - Caso 1: `confirmarVenta retorna VentaDTO cuando el carrito no está vacío`
      - Mock `POST /api/ventas` → reply 201 con VentaDTO
      - Verificar que retorna el objeto con `id`, `items`, `total`, `fechaHora`
    - Caso 2: `confirmarVenta lanza AxiosError cuando el carrito está vacío`
      - Mock `POST /api/ventas` → reply 400 con `{ error: 'El carrito está vacío...' }`
      - Verificar que la promesa es rechazada
    - Caso 3: `obtenerHistorial retorna lista de ventas`
      - Mock `GET /api/ventas` → reply 200 con lista de VentaDTO
    - Caso 4: `obtenerDetalle retorna VentaDTO por ID`
      - Mock `GET /api/ventas/{id}` → reply 200 con VentaDTO
    - Caso 5: `obtenerDetalle lanza error cuando la venta no existe`
      - Mock `GET /api/ventas/{id}` → reply 404
    - _Requerimientos: 5.1, 5.4_
  - **Criterio de verificación**: `npm test -- --testPathPattern=servicios` ejecuta sin fallos; todos los casos pasan.

---

## Fase 10: Checkpoint Final

- [ ] 11. Checkpoint Final — Ejecutar suite completa de pruebas del frontend
  - Ejecutar `npm test` (o `npx jest`) y verificar que TODAS las pruebas pasan sin errores
  - Verificar que no hay errores de sintaxis en ningún módulo
  - Confirmar que el servidor arranca correctamente con `npm start`
  - Asegurarse de que todos los tests pasan; consultar al usuario si surgen dudas.

---

## Notas

- Las tareas marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido
- El patrón PRG (Post/Redirect/Get) se aplica en todas las operaciones de formulario exitosas
- Los formularios HTML solo soportan GET y POST; se usan rutas POST con sufijos `/actualizar` y `/eliminar`
- La función `extraerMensajeError` debe estar disponible en todas las rutas (definirla en un módulo compartido o repetirla)
- Axios lanza excepciones automáticamente para códigos HTTP 4xx/5xx (a diferencia de `fetch`)
- El JavaScript del cliente (`main.js`) es mínimo; la lógica principal está en el servidor
- Usar `async/await` en todas las funciones asíncronas (no callbacks ni `.then()`)
- Los módulos de servicio NO deben contener lógica de presentación (sin `res.render`)
