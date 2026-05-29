# Design — Frontend Sistema POS

## Framework Elegido: Node.js + Express con Handlebars

### Justificacion
Se eligio Node.js con Express y Handlebars porque:
- Express es minimalista y permite control total sobre las rutas y middleware
- Handlebars permite separar la logica del servidor de las vistas HTML
- El rendering server-side es mas rapido para un POS que no necesita SPA
- JavaScript en frontend y backend reduce la curva de aprendizaje
- Express-session maneja el carrito en servidor, mas seguro que localStorage

---

## Arquitectura Cliente-Servidor

Navegador (HTML5 + CSS + JS)
  |
  | HTTP (puerto 3000)
  v
Frontend (Node.js + Express)
  |
  | HTTP/JSON (fetch + axios)
  v
API Gateway (AWS)
  |
  v
Lambdas + DynamoDB (AWS)

---

## Estructura de Componentes

pos_frontend/
  app.js                  <- Servidor Express, rutas, logica de carrito
  .env                    <- API_BASE_URL (no hardcodeada)
  views/
    index.hbs             <- Vista principal POS (input + carrito)
    historial.hbs         <- Historial de ventas
    crear-producto.hbs    <- Formulario nuevo producto
    layouts/main.hbs      <- Layout base con navbar
  public/
    css/estilos.css       <- Estilos tema oscuro
    js/main.js            <- JavaScript global (Escape, atajos)

---

## Flujo de Navegacion

/ (index)     <- Vista principal POS
/buscar       <- GET, devuelve JSON de productos
/historial    <- GET, muestra ventas
/productos/crear <- GET/POST, crear producto
/carrito/agregar-ajax <- POST, agrega al carrito
/carrito/items/:id/actualizar <- POST, actualiza cantidad
/carrito/items/:id/eliminar <- POST, elimina del carrito
/carrito/vaciar <- POST, vacia el carrito
/ventas       <- POST, confirma venta

---

## Contrato con el API Gateway

URL Base: process.env.API_BASE_URL

| Endpoint | Metodo | Descripcion |
|---|---|---|
| /productos | GET | Buscar productos por nombre o codigo de barras |
| /productos | POST | Crear nuevo producto |
| /ventas | POST | Registrar venta |
| /ventas | GET | Consultar historial |

### Request POST /ventas
{ productos: [{productoId, nombre, cantidad, precioUnitario}], total, metodoPago, fecha }

### Response exitoso
{ ventaId, productos, total, metodoPago, fecha }

### Response error
{ error: 'mensaje descriptivo' }

---

## JavaScript — Asincronismo y fetch

El frontend usa fetch con async/await para consumir el API:

- buscarProducto(): fetch GET /buscar?q= con try/catch
- agregarAlCarrito(): fetch POST /carrito/agregar-ajax
- enviarVenta(): crea form dinamico y hace POST /ventas

---

## CSS — Box Model y Flexbox

- Tema oscuro: background #0d0d1a, colores verdes #00ff88
- Layout principal: CSS Grid con col-lg-8 y col-lg-4
- Carrito: Flexbox para items
- Modales: position fixed, display flex, z-index
- Box model: padding y margin en todos los componentes
