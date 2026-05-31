# Design — Frontend Sistema POS

## Overview

Frontend web del sistema POS construido con Node.js + Express + Handlebars. Implementa rendering server-side para una interfaz de caja registradora con tema oscuro, atajos de teclado y consumo del API Gateway de AWS.

### Framework Elegido: Node.js + Express con Handlebars

Se eligió Node.js con Express y Handlebars porque:
- Express es minimalista y permite control total sobre rutas y middleware sin abstracciones innecesarias
- Handlebars separa la lógica del servidor de las vistas HTML, cumpliendo el principio de separación de responsabilidades
- El rendering server-side es más adecuado para un POS que no requiere SPA — la página se renderiza completa en cada acción
- JavaScript en frontend y backend reduce la curva de aprendizaje y unifica el lenguaje del proyecto
- Express-session maneja el carrito en el servidor, evitando manipulación desde el cliente

---

## Architecture

### Diagrama de capas

```
Navegador (HTML5 + CSS + JS vanilla)
        |
        | HTTP puerto 3000
        v
Frontend (Node.js + Express + Handlebars)
        |
        | HTTP/JSON — axios (server-side) + fetch (client-side)
        v
API Gateway (AWS) — https://rfs00jdfqb.execute-api.us-east-1.amazonaws.com/Prod
        |
        v
AWS Lambda (Node.js 20.x)
        |
        v
DynamoDB (pos-productos, pos-ventas)
```

### Flujo de una búsqueda

1. Usuario escribe en el input y presiona Enter
2. JavaScript del navegador hace `fetch GET /buscar?q=texto`
3. Express recibe la petición y llama a `axios GET API_BASE_URL/productos?q=texto`
4. Lambda consulta DynamoDB y retorna JSON
5. Express retorna el JSON al navegador
6. JavaScript actualiza el DOM con los resultados

### Flujo de una venta

1. Usuario presiona F4 (efectivo) o F2 (otro método)
2. JavaScript del navegador hace `POST /ventas` con los datos del carrito
3. Express llama a `axios POST API_BASE_URL/ventas`
4. Lambda guarda la venta en DynamoDB y retorna confirmación
5. Express vacía la sesión del carrito y redirige con mensaje de éxito

---

## Components and Interfaces

### Vistas (Handlebars)

| Vista | Ruta | Descripción |
|-------|------|-------------|
| `index.hbs` | `GET /` | Vista principal POS: input de búsqueda + carrito |
| `historial.hbs` | `GET /historial` | Historial de ventas desde API |
| `crear-producto.hbs` | `GET/POST /productos/crear` | Formulario para agregar productos |
| `layouts/main.hbs` | — | Layout base con navbar y scripts |

### Rutas Express (app.js)

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/` | Renderiza vista principal |
| GET | `/buscar?q=` | Proxy a `GET /productos` del API |
| GET | `/historial` | Proxy a `GET /ventas` del API |
| GET | `/productos/crear` | Renderiza formulario |
| POST | `/productos/crear` | Proxy a `POST /productos` del API |
| POST | `/carrito/agregar-ajax` | Agrega ítem a la sesión |
| POST | `/carrito/items/:id/actualizar` | Actualiza cantidad en sesión |
| POST | `/carrito/items/:id/eliminar` | Elimina ítem de la sesión |
| POST | `/carrito/vaciar` | Vacía la sesión del carrito |
| POST | `/ventas` | Proxy a `POST /ventas` del API |

### Servicios (src/servicios/)

| Servicio | Responsabilidad |
|----------|----------------|
| `productoServicio.js` | Llama a `GET /productos` del API con axios |
| `ventaServicio.js` | Llama a `POST /ventas` y `GET /ventas` del API con axios |
| `carritoServicio.js` | Gestiona el estado del carrito en memoria de sesión |

### JavaScript del navegador (public/js/main.js)

- `buscarProducto()` — `fetch GET /buscar?q=` con `async/await` y `try/catch`
- `agregarAlCarrito()` — `fetch POST /carrito/agregar-ajax`
- Atajos de teclado: F2, F3, F4, F5, F6, F7, ESC
- Modales: efectivo (F4), método de pago (F2), resultados de búsqueda

### Contrato con el API Gateway

URL Base: `process.env.API_BASE_URL` (nunca hardcodeada en el código)

**GET /productos?q=texto**
```json
Response 200: [{ "id": "uuid", "nombre": "Coca-Cola", "precio": 15, "codigo_barras": "P001", "stock": 100 }]
```

**POST /productos**
```json
Request:  { "nombre": "string", "codigo_barras": "string", "precio": number, "stock": number }
Response 201: { "id": "uuid", "nombre": "...", ... }
```

**POST /ventas**
```json
Request:  { "productos": [{ "productoId": "uuid", "nombre": "string", "cantidad": number, "precioUnitario": number }], "total": number, "metodoPago": "efectivo|Nequi|Davivienda|Daviplata|Transferencia", "fecha": "ISO8601" }
Response 201: { "id": "uuid", "productos": [...], "total": number, "metodoPago": "string", "fecha": "string" }
Response 400: { "mensaje": "El carrito está vacío" }
```

**GET /ventas**
```json
Response 200: [{ "id": "uuid", "total": number, "metodoPago": "string", "fecha": "string", "productos": [...] }]
```

---

## Data Models

### Sesión del carrito (express-session)

```javascript
req.session.carrito = {
  items: [
    {
      productoId: "string",       // ID del producto
      nombreProducto: "string",   // Nombre para mostrar
      precioUnitario: number,     // Precio unitario
      cantidad: number            // Cantidad en carrito
    }
  ],
  total: number                   // Total calculado
}
```

### Producto (desde API)

```javascript
{
  id: "uuid",
  nombre: "string",
  codigo_barras: "string",
  precio: number,
  stock: number,
  creadoEn: "ISO8601"
}
```

### Venta (enviada al API)

```javascript
{
  productos: [{ productoId, nombre, cantidad, precioUnitario }],
  total: number,
  metodoPago: "efectivo | Nequi | Davivienda | Daviplata | Transferencia",
  fecha: "ISO8601"
}
```

---

## Error Handling

| Escenario | Comportamiento |
|-----------|---------------|
| API no disponible | `try/catch` en axios → mensaje de error en vista |
| HTTP 400 del API | Muestra `error.mensaje` al usuario |
| HTTP 500 del API | Muestra mensaje genérico "Error del servidor" |
| Carrito vacío al cobrar | Redirige con `?error=El+carrito+esta+vacio` |
| Producto no encontrado | Muestra "No se encontraron productos" en la tabla |
| Error de red en fetch | `catch` en JS del navegador → alerta al usuario |

---

## Correctness Properties

Property 1: El total del carrito siempre se recalcula desde los ítems, nunca se confía en el valor almacenado.
**Validates: Requirements 2.4**

Property 2: La URL del API nunca aparece hardcodeada en el código fuente — siempre desde `process.env.API_BASE_URL`.
**Validates: Requirements 7.1**

Property 3: El carrito se vacía en sesión después de confirmar una venta exitosa.
**Validates: Requirements 3.3**

Property 4: Todos los `fetch` y llamadas `axios` están envueltos en `try/catch`.
**Validates: Requirements 7.2**

---

## Testing Strategy

- Prueba manual end-to-end: buscar producto → agregar al carrito → confirmar venta → verificar en historial
- Prueba de error: desconectar API y verificar que el mensaje de error aparece
- Prueba de atajos: F2, F4, F5, ESC en la vista principal
