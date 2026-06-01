# Design — Frontend Sistema POS Vanilla JS

## Overview

Frontend web del sistema POS implementado con Vanilla JS (HTML5 + CSS3 + JavaScript ES6+). Sin frameworks, sin dependencias de producción, sin servidor. El navegador consume directamente el API Gateway de AWS usando `fetch` con `async/await`.

### Framework Elegido: Vanilla JS

Se eligió Vanilla JS porque:
- **Control total del DOM**: sin abstracciones intermedias, cada elemento HTML se manipula directamente con `document.querySelector` y `innerHTML` — esto demuestra comprensión real del navegador
- **Sin build step**: no requiere webpack, vite ni compilación — el profesor abre `index.html` y funciona inmediatamente
- **fetch nativo**: el navegador tiene `fetch` incorporado — no se necesita axios ni ninguna librería externa para consumir el API
- **Peso cero**: sin `node_modules`, sin bundler, sin transpilación — el proyecto pesa kilobytes, no megabytes
- **Evaluación directa de fundamentos**: el parcial evalúa HTML5, CSS y JS — Vanilla JS expone exactamente esos fundamentos sin capas de abstracción

---

## Architecture

### Diagrama de capas

```
Navegador (index.html + CSS + JS vanilla)
        |
        | fetch() nativo — async/await
        v
API Gateway (AWS)
https://rfs00jdfqb.execute-api.us-east-1.amazonaws.com/Prod
        |
        v
AWS Lambda (Node.js 20.x)
        |
        v
DynamoDB (pos-productos, pos-ventas)
```

### Flujo de carga de productos

1. `DOMContentLoaded` dispara `cargarProductos()`
2. `productosService.listar()` hace `fetch GET /productos`
3. La respuesta JSON se pasa a `renderizarProductos(productos)`
4. Se genera HTML con `map()` y se inserta con `innerHTML`
5. Se agregan event listeners a cada botón "Agregar"

### Flujo de registro de venta

1. Cajero hace clic en "Confirmar venta"
2. Se valida que el carrito no esté vacío
3. `ventasService.crear(payload)` hace `fetch POST /ventas` con `JSON.stringify`
4. Si respuesta ok → mensaje de éxito, carrito se vacía, productos se recargan
5. Si error → mensaje de error visible al usuario

---

## Components and Interfaces

### Estructura de archivos

```
pos_frontend_vanilla/
  index.html                    ← Estructura HTML5 semántica
  css/
    estilos.css                 ← Tema oscuro, variables CSS, flexbox, grid
  src/
    config.js                   ← URL base del API Gateway
    services/
      productosService.js       ← fetch GET /productos
      ventasService.js          ← fetch POST /ventas
    app.js                      ← Lógica principal, estado del carrito, eventos
  .kiro/
    specs/pos-frontend/
      requirements.md
      design.md
      tasks.md
  README.md
  .gitignore
```

### Módulos JavaScript

**src/config.js**
```javascript
export const API_BASE_URL = 'https://rfs00jdfqb.execute-api.us-east-1.amazonaws.com/Prod';
```

**src/services/productosService.js**
```javascript
// listar(): fetch GET /productos → Promise<Producto[]>
// Lanza error si la respuesta no es ok
```

**src/services/ventasService.js**
```javascript
// crear(payload): fetch POST /ventas → Promise<Venta>
// Lanza error si la respuesta no es ok
```

**src/app.js**
```javascript
// Estado: carrito = []
// cargarProductos(): llama productosService.listar(), renderiza
// agregarAlCarrito(producto): actualiza estado y DOM
// eliminarDelCarrito(id): filtra carrito y re-renderiza
// calcularTotal(): reduce carrito
// confirmarVenta(): llama ventasService.crear(), maneja éxito/error
// mostrarMensaje(texto, tipo): muestra notificación al usuario
```

### Contrato con el API Gateway

URL Base: `API_BASE_URL` desde `src/config.js`

**GET /productos**
```
Response 200: [{ "id": "uuid", "nombre": "string", "precio": number, "codigo_barras": "string", "stock": number }]
Response 500: { "mensaje": "Error interno del servidor" }
```

**POST /ventas**
```
Request: {
  "productos": [{ "productoId": "uuid", "nombre": "string", "cantidad": number, "precioUnitario": number }],
  "total": number,
  "metodoPago": "efectivo | Nequi | Davivienda | Daviplata | Transferencia",
  "fecha": "ISO8601"
}
Response 201: { "id": "uuid", "productos": [...], "total": number, "metodoPago": "string", "fecha": "string" }
Response 400: { "mensaje": "El carrito está vacío" }
```

---

## Data Models

### Estado del carrito (en memoria — array JS)

```javascript
carrito = [
  {
    productoId: "uuid",
    nombre: "Coca-Cola 600ml",
    precioUnitario: 15,
    cantidad: 2
  }
]
```

### Producto (desde API)

```javascript
{
  id: "uuid",
  nombre: "string",
  precio: number,
  codigo_barras: "string",
  stock: number
}
```

### Payload de venta (enviado al API)

```javascript
{
  productos: [{ productoId, nombre, cantidad, precioUnitario }],
  total: number,
  metodoPago: "string",
  fecha: "ISO8601"
}
```

---

## Correctness Properties

Property 1: La URL del API nunca aparece hardcodeada en los servicios — siempre se importa desde `src/config.js`.
**Validates: Requirements 5.1**

Property 2: El total del carrito siempre se recalcula desde el array de items con `reduce()` — nunca se acumula manualmente.
**Validates: Requirements 2.5**

Property 3: Todos los `fetch` están envueltos en `try/catch` — cualquier error de red o HTTP muestra mensaje al usuario.
**Validates: Requirements 3.4**

Property 4: El carrito se vacía después de una venta exitosa — el estado no persiste entre ventas.
**Validates: Requirements 3.2**

---

## Error Handling

| Escenario | Comportamiento |
|-----------|---------------|
| API no disponible | `catch` en fetch → mensaje "Error al conectar con el servidor" |
| HTTP 400 del API | Lee `response.json().mensaje` → muestra al usuario |
| HTTP 500 del API | Mensaje genérico "Error del servidor" |
| Carrito vacío al confirmar | Validación local → mensaje "El carrito está vacío" |
| Lista de productos vacía | Muestra "No hay productos disponibles" |

---

## Testing Strategy

- Prueba manual end-to-end: cargar página → ver productos → agregar al carrito → confirmar venta → ver mensaje de éxito
- Prueba de error: desconectar internet o cambiar URL del API → verificar mensaje de error
- Prueba de carrito vacío: intentar confirmar sin productos → verificar mensaje
