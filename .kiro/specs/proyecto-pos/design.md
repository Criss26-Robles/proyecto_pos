# Diseño Técnico — Sistema POS (Punto de Venta)

## Visión General

El Sistema POS es una aplicación de dos capas: un **backend** Java/Spring Boot que expone una API REST con arquitectura hexagonal, y un **frontend** Node.js/Express.js que sirve la interfaz web al operador de caja. La comunicación entre capas es exclusivamente HTTP/JSON. La persistencia es en memoria (HashMap/ArrayList) para esta fase del proyecto.

---

## Arquitectura General del Sistema

```
┌─────────────────────────────────────────────────────────────────┐
│                        OPERADOR (Navegador)                     │
└───────────────────────────┬─────────────────────────────────────┘
                            │  HTTP (puerto 3000)
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    FRONTEND (Node.js / Express.js)              │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────────────┐ │
│  │  Rutas       │──▶│  Servicios   │──▶│  Vistas (Handlebars) │ │
│  │  Express     │   │  (Axios)     │   │  index / historial   │ │
│  └──────────────┘   └──────┬───────┘   └──────────────────────┘ │
└─────────────────────────────┼───────────────────────────────────┘
                              │  HTTP/JSON (puerto 8080)
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    BACKEND (Java / Spring Boot)                 │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  INFRAESTRUCTURA                                        │   │
│  │  ┌──────────────────┐    ┌──────────────────────────┐  │   │
│  │  │  Controladores   │    │  Repositorios en Memoria │  │   │
│  │  │  REST (Entrada)  │    │  (Salida)                │  │   │
│  │  └────────┬─────────┘    └──────────────┬───────────┘  │   │
│  └───────────┼──────────────────────────────┼─────────────┘   │
│              │ Puerto Entrada               │ Puerto Salida    │
│  ┌───────────▼──────────────────────────────▼─────────────┐   │
│  │  APLICACIÓN                                             │   │
│  │  ┌──────────────────────────────────────────────────┐  │   │
│  │  │  Servicios / Casos de Uso                        │  │   │
│  │  │  ProductoServicio / CarritoServicio / VentaServicio│  │   │
│  │  └──────────────────────────────────────────────────┘  │   │
│  └─────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  DOMINIO                                                │   │
│  │  Producto / Carrito / ItemCarrito / Venta               │   │
│  │  Puertos de Salida (interfaces)                         │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Componentes e Interfaces

### Backend — Puertos de Entrada (Capa Aplicación)

| Interfaz | Métodos principales |
|---|---|
| `ProductoServicioPuerto` | `listarTodos()`, `buscarPorId(UUID)`, `buscarPorNombre(String)` |
| `CarritoServicioPuerto` | `obtenerCarrito()`, `agregarItem(UUID, int)`, `actualizarItem(UUID, int)`, `eliminarItem(UUID)`, `vaciarCarrito()` |
| `VentaServicioPuerto` | `confirmarVenta()`, `listarVentas()`, `buscarVentaPorId(UUID)` |

### Backend — Puertos de Salida (Capa Dominio)

| Interfaz | Métodos principales |
|---|---|
| `ProductoRepositorioPuerto` | `findAll()`, `findById(UUID)`, `findByNombre(String)` |
| `CarritoRepositorioPuerto` | `obtener()`, `guardar(Carrito)`, `vaciar()` |
| `VentaRepositorioPuerto` | `guardar(Venta)`, `findAll()`, `findById(UUID)` |

### Frontend — Módulos de Servicio

| Módulo | Responsabilidad |
|---|---|
| `productoServicio.js` | Llamadas HTTP a `/api/productos` |
| `carritoServicio.js` | Llamadas HTTP a `/api/carrito` |
| `ventaServicio.js` | Llamadas HTTP a `/api/ventas` |

---

## Modelos de Datos

### Producto
```
id        : UUID
codigo    : String
nombre    : String
precio    : BigDecimal (escala 2)
stock     : Integer
```

### ItemCarrito
```
producto  : Producto
cantidad  : Integer (> 0)
subtotal  : BigDecimal = precio × cantidad
```

### Carrito
```
items     : List<ItemCarrito>
total     : BigDecimal = Σ subtotales
```

### Venta
```
id        : UUID
items     : List<ItemCarrito>
total     : BigDecimal
fechaHora : LocalDateTime
```

---

## API REST — Contratos

| Método | Endpoint | Request Body | Response | Códigos HTTP |
|---|---|---|---|---|
| GET | `/api/productos` | — | `List<ProductoDTO>` | 200 |
| GET | `/api/productos?nombre=X` | — | `List<ProductoDTO>` | 200 |
| GET | `/api/productos/{id}` | — | `ProductoDTO` | 200, 404 |
| GET | `/api/carrito` | — | `CarritoDTO` | 200 |
| POST | `/api/carrito/items` | `AgregarItemRequest` | `CarritoDTO` | 200, 400, 404 |
| PUT | `/api/carrito/items/{productoId}` | `ActualizarItemRequest` | `CarritoDTO` | 200, 400, 404 |
| DELETE | `/api/carrito/items/{productoId}` | — | `CarritoDTO` | 200, 404 |
| DELETE | `/api/carrito` | — | `CarritoDTO` | 200 |
| POST | `/api/ventas` | — | `VentaDTO` | 201, 400 |
| GET | `/api/ventas` | — | `List<VentaDTO>` | 200 |
| GET | `/api/ventas/{id}` | — | `VentaDTO` | 200, 404 |

### Estructura de Error Uniforme
```json
{
  "error": "Mensaje descriptivo",
  "timestamp": "2024-01-15T10:30:00",
  "status": 404
}
```

---

## Flujos de Datos Principales

### Flujo 1: Agregar Producto al Carrito
```
Operador → [clic "Agregar"] → Frontend (POST /carrito/items)
  → carritoServicio.js → POST http://localhost:8080/api/carrito/items
  → CarritoControlador → CarritoServicioPuerto.agregarItem(productoId, cantidad)
  → CarritoServicio → ProductoRepositorioPuerto.findById(productoId)
    → [si no existe] → lanza ProductoNoEncontradoException → HTTP 404
    → [si existe] → crea ItemCarrito, calcula subtotal
  → CarritoRepositorioPuerto.guardar(carrito)
  → retorna CarritoDTO → Frontend → re-renderiza vista
```

### Flujo 2: Confirmar Venta
```
Operador → [clic "Confirmar Venta"] → Frontend (POST /ventas)
  → ventaServicio.js → POST http://localhost:8080/api/ventas
  → VentaControlador → VentaServicioPuerto.confirmarVenta()
  → VentaServicio → CarritoRepositorioPuerto.obtener()
    → [si vacío] → lanza CarritoVacioException → HTTP 400
    → [si no vacío] → crea Venta con UUID, items, total, fechaHora
  → VentaRepositorioPuerto.guardar(venta)
  → CarritoRepositorioPuerto.vaciar()
  → retorna VentaDTO (HTTP 201) → Frontend → redirige a vista confirmación
```

### Flujo 3: Consultar Historial
```
Operador → [clic "Historial"] → Frontend (GET /historial)
  → ventaServicio.js → GET http://localhost:8080/api/ventas
  → VentaControlador → VentaServicioPuerto.listarVentas()
  → VentaServicio → VentaRepositorioPuerto.findAll()
  → retorna List<VentaDTO> → Frontend → renderiza historial.hbs
```

---

## Manejo de Errores

### Backend
- `@ControllerAdvice` global captura todas las excepciones
- Excepciones de dominio: `ProductoNoEncontradoException` (404), `CarritoVacioException` (400), `CantidadInvalidaException` (400), `VentaNoEncontradaException` (404)
- Errores de validación `@Valid`: HTTP 400 con lista de campos inválidos
- Excepciones no controladas: HTTP 500 con mensaje genérico

### Frontend
- Bloques `try/catch` en todos los servicios
- Errores de red (backend no disponible): renderiza `error.hbs`
- Errores HTTP 400/404: muestra mensaje inline en la vista actual
- Errores HTTP 500: renderiza `error.hbs` con mensaje genérico

---

## Propiedades de Corrección

*Una propiedad es una característica o comportamiento que debe mantenerse verdadero en todas las ejecuciones válidas del sistema — esencialmente, una declaración formal sobre lo que el sistema debe hacer. Las propiedades sirven como puente entre las especificaciones legibles por humanos y las garantías de corrección verificables por máquina.*

### Propiedad 1: Cálculo de Subtotal e Invariante del Total del Carrito

*Para cualquier* precio P (BigDecimal positivo con escala 2) y cantidad entera positiva N, el subtotal del ItemCarrito debe ser exactamente `P.multiply(BigDecimal.valueOf(N)).setScale(2, HALF_UP)`. Además, para cualquier carrito con una lista de ítems, el campo `total` debe ser igual a la suma de todos los `subtotal` de sus ítems (sin pérdida de precisión con BigDecimal).

**Valida: Requerimientos 2.8, 2.9**

### Propiedad 2: Agregar Ítem Incrementa la Cardinalidad del Carrito

*Para cualquier* carrito con K ítems de productos distintos, agregar un producto nuevo (cuyo ID no está presente en el carrito) con una cantidad positiva debe resultar en un carrito con exactamente K+1 ítems, y el total debe reflejar el nuevo subtotal incluido.

**Valida: Requerimientos 2.1**

### Propiedad 3: Rechazo de Cantidades No Positivas

*Para cualquier* cantidad entera ≤ 0, la operación de agregar o actualizar un ítem en el carrito debe ser rechazada (lanzar excepción / retornar HTTP 400) y el estado del carrito debe permanecer idéntico al estado previo a la operación.

**Valida: Requerimientos 2.7**

### Propiedad 4: Vaciado Automático del Carrito tras Confirmar Venta

*Para cualquier* carrito no vacío, después de invocar `confirmarVenta()` exitosamente, el carrito debe quedar con cero ítems y total igual a `0.00`. La venta creada debe contener exactamente los mismos ítems y total que tenía el carrito antes de la confirmación.

**Valida: Requerimientos 3.3, 3.1**

### Propiedad 5: Búsqueda de Productos Insensible a Mayúsculas

*Para cualquier* texto de búsqueda T y cualquier conjunto de productos, todos los productos cuyo nombre contenga T (en cualquier combinación de mayúsculas/minúsculas) deben aparecer en los resultados, y ningún producto cuyo nombre no contenga T debe aparecer en los resultados.

**Valida: Requerimientos 1.5**

### Propiedad 6: Unicidad de IDs de Venta

*Para cualquier* secuencia de N confirmaciones de venta (N ≥ 2), todos los UUIDs asignados a las ventas deben ser distintos entre sí (el conjunto de IDs tiene cardinalidad N).

**Valida: Requerimientos 3.8**

---

## Estrategia de Pruebas

### Pruebas Unitarias (JUnit 5 + Mockito)
- Cubren los Casos de Uso: `ProductoServicio`, `CarritoServicio`, `VentaServicio`
- Usan Mockito para simular los Puertos de Salida
- Cobertura mínima del 80% en la capa de aplicación
- Verifican comportamiento correcto y manejo de excepciones

### Pruebas de Integración (Spring Boot Test / @WebMvcTest)
- Cubren los Controladores REST con MockMvc
- Verifican códigos HTTP, estructura de respuesta JSON y manejo de errores

### Pruebas Basadas en Propiedades (JUnit 5 + jqwik)
- Biblioteca: **jqwik** (property-based testing para Java)
- Mínimo 100 iteraciones por propiedad
- Cada prueba referencia la propiedad del documento de diseño
- Formato de etiqueta: `Feature: proyecto-pos, Property N: <texto>`
- Cubren las Propiedades 1–7 definidas en este documento

### Pruebas de Frontend
- Pruebas manuales de flujos de usuario (catálogo, carrito, venta, historial)
- Verificación de manejo de errores con backend simulado
