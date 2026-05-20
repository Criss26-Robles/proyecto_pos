# Diseño Técnico — Backend POS (Java 17 + Spring Boot 3.2.x)

## 1. Visión General

El backend del Sistema POS es un servicio REST desarrollado en **Java 17** con **Spring Boot 3.2.x**. Aplica **arquitectura hexagonal (Ports & Adapters)**, principios **SOLID** e **inversión de dependencias**. La persistencia es completamente en memoria (HashMap/ArrayList) para esta fase. Expone una API REST consumida por el Frontend Node.js en el puerto `8080`.

---

## 2. Diagrama de Arquitectura Hexagonal

```
                    ┌─────────────────────────────────────────────┐
                    │              INFRAESTRUCTURA                │
                    │                                             │
  HTTP Request ────▶│  ┌─────────────────────────────────────┐   │
                    │  │  Adaptadores de ENTRADA (REST)       │   │
                    │  │  ProductoControlador                 │   │
                    │  │  CarritoControlador                  │   │
                    │  │  VentaControlador                    │   │
                    │  └──────────────┬──────────────────────┘   │
                    │                 │ implementa Puerto Entrada  │
                    │  ┌──────────────▼──────────────────────┐   │
                    │  │           APLICACIÓN                 │   │
                    │  │  ┌──────────────────────────────┐   │   │
                    │  │  │  Puertos de ENTRADA           │   │   │
                    │  │  │  ProductoServicioPuerto       │   │   │
                    │  │  │  CarritoServicioPuerto        │   │   │
                    │  │  │  VentaServicioPuerto          │   │   │
                    │  │  └──────────────┬───────────────┘   │   │
                    │  │                 │ implementa          │   │
                    │  │  ┌──────────────▼───────────────┐   │   │
                    │  │  │  Casos de Uso (Servicios)     │   │   │
                    │  │  │  ProductoServicio             │   │   │
                    │  │  │  CarritoServicio              │   │   │
                    │  │  │  VentaServicio                │   │   │
                    │  │  └──────────────┬───────────────┘   │   │
                    │  └─────────────────┼───────────────────┘   │
                    │                    │ usa Puerto Salida       │
                    │  ┌─────────────────▼───────────────────┐   │
                    │  │              DOMINIO                  │   │
                    │  │  ┌──────────────────────────────┐   │   │
                    │  │  │  Puertos de SALIDA            │   │   │
                    │  │  │  ProductoRepositorioPuerto    │   │   │
                    │  │  │  CarritoRepositorioPuerto     │   │   │
                    │  │  │  VentaRepositorioPuerto       │   │   │
                    │  │  └──────────────────────────────┘   │   │
                    │  │  Entidades: Producto, Carrito,       │   │
                    │  │            ItemCarrito, Venta        │   │
                    │  └──────────────────────────────────────┘   │
                    │                    ▲                         │
                    │  ┌─────────────────┴───────────────────┐   │
                    │  │  Adaptadores de SALIDA (Memoria)     │   │
                    │  │  ProductoRepositorioMemoria          │   │
                    │  │  CarritoRepositorioMemoria           │   │
                    │  │  VentaRepositorioMemoria             │   │
                    │  └─────────────────────────────────────┘   │
                    └─────────────────────────────────────────────┘
```

---

## 3. Estructura de Paquetes Detallada

```
pos_backend/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/pos/
    │   │   │
    │   │   ├── dominio/
    │   │   │   ├── modelo/
    │   │   │   │   ├── Producto.java          ← Entidad de dominio
    │   │   │   │   ├── ItemCarrito.java        ← Entidad de dominio
    │   │   │   │   ├── Carrito.java            ← Entidad de dominio (raíz de agregado)
    │   │   │   │   └── Venta.java              ← Entidad de dominio
    │   │   │   └── puerto/
    │   │   │       ├── ProductoRepositorioPuerto.java   ← Puerto de salida
    │   │   │       ├── CarritoRepositorioPuerto.java    ← Puerto de salida
    │   │   │       └── VentaRepositorioPuerto.java      ← Puerto de salida
    │   │   │
    │   │   ├── aplicacion/
    │   │   │   ├── puerto/
    │   │   │   │   ├── ProductoServicioPuerto.java      ← Puerto de entrada
    │   │   │   │   ├── CarritoServicioPuerto.java       ← Puerto de entrada
    │   │   │   │   └── VentaServicioPuerto.java         ← Puerto de entrada
    │   │   │   └── servicio/
    │   │   │       ├── ProductoServicio.java            ← Caso de uso
    │   │   │       ├── CarritoServicio.java             ← Caso de uso
    │   │   │       └── VentaServicio.java               ← Caso de uso
    │   │   │
    │   │   └── infraestructura/
    │   │       ├── adaptador/
    │   │       │   ├── entrada/
    │   │       │   │   ├── ProductoControlador.java     ← REST Controller
    │   │       │   │   ├── CarritoControlador.java      ← REST Controller
    │   │       │   │   └── VentaControlador.java        ← REST Controller
    │   │       │   └── salida/
    │   │       │       ├── ProductoRepositorioMemoria.java  ← Adaptador salida
    │   │       │       ├── CarritoRepositorioMemoria.java   ← Adaptador salida
    │   │       │       └── VentaRepositorioMemoria.java     ← Adaptador salida
    │   │       ├── config/
    │   │       │   ├── AppConfig.java          ← Configuración CORS y Beans
    │   │       │   └── DatosIniciales.java     ← Carga de productos de ejemplo
    │   │       ├── dto/
    │   │       │   ├── ProductoDTO.java
    │   │       │   ├── AgregarItemRequest.java
    │   │       │   ├── ActualizarItemRequest.java
    │   │       │   ├── ItemCarritoDTO.java
    │   │       │   ├── CarritoDTO.java
    │   │       │   └── VentaDTO.java
    │   │       └── excepcion/
    │   │           ├── ProductoNoEncontradoException.java
    │   │           ├── VentaNoEncontradaException.java
    │   │           ├── CarritoVacioException.java
    │   │           ├── CantidadInvalidaException.java
    │   │           └── ManejadorExcepcionesGlobal.java  ← @ControllerAdvice
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/pos/
            ├── aplicacion/
            │   ├── ProductoServicioTest.java
            │   ├── CarritoServicioTest.java
            │   └── VentaServicioTest.java
            └── infraestructura/
                ├── ProductoControladorTest.java
                ├── CarritoControladorTest.java
                └── VentaControladorTest.java
```

---

## 4. Diseño de Entidades y Modelos de Datos

### 4.1 Producto

```java
// com.pos.dominio.modelo.Producto
public class Producto {
    private UUID id;
    private String codigo;      // Ej: "PROD-001"
    private String nombre;      // Ej: "Coca-Cola 600ml"
    private BigDecimal precio;  // Escala 2, ej: 15.50
    private Integer stock;      // Unidades disponibles
}
```

### 4.2 ItemCarrito

```java
// com.pos.dominio.modelo.ItemCarrito
public class ItemCarrito {
    private Producto producto;
    private Integer cantidad;       // > 0
    private BigDecimal subtotal;    // precio × cantidad, escala 2
}
```

### 4.3 Carrito

```java
// com.pos.dominio.modelo.Carrito
public class Carrito {
    private List<ItemCarrito> items;  // Lista mutable
    private BigDecimal total;         // Σ subtotales, recalculado en cada operación

    // Métodos de dominio:
    // agregarItem(ItemCarrito)
    // actualizarItem(UUID productoId, int cantidad)
    // eliminarItem(UUID productoId)
    // vaciar()
    // recalcularTotal()
    // estaVacio(): boolean
}
```

### 4.4 Venta

```java
// com.pos.dominio.modelo.Venta
public class Venta {
    private UUID id;                    // UUID.randomUUID()
    private List<ItemCarrito> items;    // Snapshot del carrito
    private BigDecimal total;
    private LocalDateTime fechaHora;    // LocalDateTime.now()
}
```

### 4.5 DTOs de la API

```java
// AgregarItemRequest
{
    @NotNull UUID productoId;
    @Positive Integer cantidad;
}

// ActualizarItemRequest
{
    @Positive Integer cantidad;
}

// ProductoDTO
{
    UUID id;
    String codigo;
    String nombre;
    BigDecimal precio;
    Integer stock;
}

// ItemCarritoDTO
{
    ProductoDTO producto;
    Integer cantidad;
    BigDecimal subtotal;
}

// CarritoDTO
{
    List<ItemCarritoDTO> items;
    BigDecimal total;
}

// VentaDTO
{
    UUID id;
    List<ItemCarritoDTO> items;
    BigDecimal total;
    LocalDateTime fechaHora;
}
```

---

## 5. Contratos de Interfaces (Puertos)

### 5.1 Puertos de Salida (Dominio)

```java
// com.pos.dominio.puerto.ProductoRepositorioPuerto
public interface ProductoRepositorioPuerto {
    List<Producto> findAll();
    Optional<Producto> findById(UUID id);
    List<Producto> findByNombre(String nombre);
}

// com.pos.dominio.puerto.CarritoRepositorioPuerto
public interface CarritoRepositorioPuerto {
    Carrito obtener();
    void guardar(Carrito carrito);
    void vaciar();
}

// com.pos.dominio.puerto.VentaRepositorioPuerto
public interface VentaRepositorioPuerto {
    Venta guardar(Venta venta);
    List<Venta> findAll();
    Optional<Venta> findById(UUID id);
}
```

### 5.2 Puertos de Entrada (Aplicación)

```java
// com.pos.aplicacion.puerto.ProductoServicioPuerto
public interface ProductoServicioPuerto {
    List<ProductoDTO> listarTodos();
    ProductoDTO buscarPorId(UUID id);
    List<ProductoDTO> buscarPorNombre(String nombre);
}

// com.pos.aplicacion.puerto.CarritoServicioPuerto
public interface CarritoServicioPuerto {
    CarritoDTO obtenerCarrito();
    CarritoDTO agregarItem(UUID productoId, int cantidad);
    CarritoDTO actualizarItem(UUID productoId, int cantidad);
    CarritoDTO eliminarItem(UUID productoId);
    CarritoDTO vaciarCarrito();
}

// com.pos.aplicacion.puerto.VentaServicioPuerto
public interface VentaServicioPuerto {
    VentaDTO confirmarVenta();
    List<VentaDTO> listarVentas();
    VentaDTO buscarVentaPorId(UUID id);
}
```

---

## 6. Diseño de la API REST

### 6.1 Productos

#### GET /api/productos
- **Descripción**: Retorna todos los productos del catálogo.
- **Response 200**:
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "codigo": "PROD-001",
    "nombre": "Coca-Cola 600ml",
    "precio": 15.50,
    "stock": 100
  }
]
```

#### GET /api/productos?nombre={texto}
- **Descripción**: Filtra productos por nombre (insensible a mayúsculas).
- **Response 200**: Lista filtrada de `ProductoDTO`.

#### GET /api/productos/{id}
- **Descripción**: Retorna un producto por su UUID.
- **Response 200**: `ProductoDTO`
- **Response 404**:
```json
{ "error": "Producto no encontrado con id: {id}", "timestamp": "...", "status": 404 }
```

### 6.2 Carrito

#### GET /api/carrito
- **Descripción**: Retorna el estado actual del carrito.
- **Response 200**:
```json
{
  "items": [
    {
      "producto": { "id": "...", "codigo": "PROD-001", "nombre": "Coca-Cola 600ml", "precio": 15.50, "stock": 100 },
      "cantidad": 2,
      "subtotal": 31.00
    }
  ],
  "total": 31.00
}
```

#### POST /api/carrito/items
- **Descripción**: Agrega un ítem al carrito.
- **Request Body**:
```json
{ "productoId": "550e8400-e29b-41d4-a716-446655440000", "cantidad": 2 }
```
- **Response 200**: `CarritoDTO` actualizado.
- **Response 400**: Cantidad inválida.
- **Response 404**: Producto no encontrado.

#### PUT /api/carrito/items/{productoId}
- **Descripción**: Actualiza la cantidad de un ítem.
- **Request Body**: `{ "cantidad": 3 }`
- **Response 200**: `CarritoDTO` actualizado.
- **Response 400**: Cantidad inválida.
- **Response 404**: Producto no encontrado en el carrito.

#### DELETE /api/carrito/items/{productoId}
- **Descripción**: Elimina un ítem del carrito.
- **Response 200**: `CarritoDTO` actualizado.

#### DELETE /api/carrito
- **Descripción**: Vacía el carrito completamente.
- **Response 200**: `CarritoDTO` vacío `{ "items": [], "total": 0.00 }`.

### 6.3 Ventas

#### POST /api/ventas
- **Descripción**: Confirma la venta con el carrito activo.
- **Response 201**:
```json
{
  "id": "7f3a9c12-...",
  "items": [...],
  "total": 62.00,
  "fechaHora": "2024-01-15T10:30:00"
}
```
- **Response 400**:
```json
{ "error": "El carrito está vacío. Agregue productos antes de confirmar la venta.", "timestamp": "...", "status": 400 }
```

#### GET /api/ventas
- **Descripción**: Lista todas las ventas registradas.
- **Response 200**: `List<VentaDTO>`

#### GET /api/ventas/{id}
- **Descripción**: Retorna el detalle de una venta.
- **Response 200**: `VentaDTO`
- **Response 404**: Venta no encontrada.

---

## 7. Flujos de Datos Principales

### Flujo 1: Agregar Ítem al Carrito

```
POST /api/carrito/items
  │
  ▼
CarritoControlador.agregarItem(@Valid AgregarItemRequest)
  │  inyecta CarritoServicioPuerto
  ▼
CarritoServicio.agregarItem(productoId, cantidad)
  │  usa ProductoRepositorioPuerto.findById(productoId)
  │    → [no existe] → throw ProductoNoEncontradoException
  │    → [existe] → Producto p
  │  crea ItemCarrito(p, cantidad, p.precio × cantidad)
  │  usa CarritoRepositorioPuerto.obtener()
  │  carrito.agregarItem(item)
  │  carrito.recalcularTotal()
  │  usa CarritoRepositorioPuerto.guardar(carrito)
  │  mapea Carrito → CarritoDTO
  ▼
HTTP 200 + CarritoDTO
```

### Flujo 2: Confirmar Venta

```
POST /api/ventas
  │
  ▼
VentaControlador.confirmarVenta()
  │  inyecta VentaServicioPuerto
  ▼
VentaServicio.confirmarVenta()
  │  usa CarritoRepositorioPuerto.obtener()
  │    → [vacío] → throw CarritoVacioException
  │    → [no vacío] → Carrito c
  │  crea Venta(UUID.randomUUID(), c.items, c.total, LocalDateTime.now())
  │  usa VentaRepositorioPuerto.guardar(venta)
  │  usa CarritoRepositorioPuerto.vaciar()
  │  mapea Venta → VentaDTO
  ▼
HTTP 201 + VentaDTO
```

### Flujo 3: Búsqueda de Productos por Nombre

```
GET /api/productos?nombre=coca
  │
  ▼
ProductoControlador.buscarPorNombre(@RequestParam nombre)
  │  inyecta ProductoServicioPuerto
  ▼
ProductoServicio.buscarPorNombre("coca")
  │  usa ProductoRepositorioPuerto.findByNombre("coca")
  │  ProductoRepositorioMemoria filtra HashMap:
  │    producto.getNombre().toLowerCase().contains("coca")
  │  mapea List<Producto> → List<ProductoDTO>
  ▼
HTTP 200 + List<ProductoDTO>
```

---

## 8. Implementaciones en Memoria

### ProductoRepositorioMemoria

```java
@Repository
public class ProductoRepositorioMemoria implements ProductoRepositorioPuerto {
    private final Map<UUID, Producto> almacen = new HashMap<>();

    // Inicializado con @PostConstruct o CommandLineRunner en DatosIniciales
    // 10 productos de ejemplo precargados al arrancar

    @Override
    public List<Producto> findAll() {
        return new ArrayList<>(almacen.values());
    }

    @Override
    public Optional<Producto> findById(UUID id) {
        return Optional.ofNullable(almacen.get(id));
    }

    @Override
    public List<Producto> findByNombre(String nombre) {
        return almacen.values().stream()
            .filter(p -> p.getNombre().toLowerCase().contains(nombre.toLowerCase()))
            .collect(Collectors.toList());
    }
}
```

### CarritoRepositorioMemoria

```java
@Repository
public class CarritoRepositorioMemoria implements CarritoRepositorioPuerto {
    // Carrito singleton en memoria (sesión única)
    private Carrito carrito = new Carrito();

    @Override
    public Carrito obtener() { return carrito; }

    @Override
    public void guardar(Carrito c) { this.carrito = c; }

    @Override
    public void vaciar() { this.carrito = new Carrito(); }
}
```

### VentaRepositorioMemoria

```java
@Repository
public class VentaRepositorioMemoria implements VentaRepositorioPuerto {
    private final Map<UUID, Venta> almacen = new HashMap<>();

    @Override
    public Venta guardar(Venta v) {
        almacen.put(v.getId(), v);
        return v;
    }

    @Override
    public List<Venta> findAll() {
        return new ArrayList<>(almacen.values());
    }

    @Override
    public Optional<Venta> findById(UUID id) {
        return Optional.ofNullable(almacen.get(id));
    }
}
```

---

## 9. Manejo de Errores

### Jerarquía de Excepciones de Dominio

```
RuntimeException
├── ProductoNoEncontradoException   → HTTP 404
├── VentaNoEncontradaException      → HTTP 404
├── CarritoVacioException           → HTTP 400
└── CantidadInvalidaException       → HTTP 400
```

### ManejadorExcepcionesGlobal (@ControllerAdvice)

```java
@ControllerAdvice
public class ManejadorExcepcionesGlobal {

    @ExceptionHandler(ProductoNoEncontradoException.class)
    // → HTTP 404 + ErrorDTO

    @ExceptionHandler(VentaNoEncontradaException.class)
    // → HTTP 404 + ErrorDTO

    @ExceptionHandler({CarritoVacioException.class, CantidadInvalidaException.class})
    // → HTTP 400 + ErrorDTO

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // → HTTP 400 + ErrorDTO con lista de campos inválidos

    @ExceptionHandler(Exception.class)
    // → HTTP 500 + ErrorDTO genérico (sin stack trace)
}
```

### Estructura ErrorDTO

```json
{
  "error": "Mensaje descriptivo del error",
  "timestamp": "2024-01-15T10:30:00",
  "status": 404
}
```

---

## 10. Configuración

### application.properties

```properties
server.port=8080
spring.application.name=pos-backend
logging.level.com.pos=DEBUG
```

### AppConfig.java (CORS)

```java
@Configuration
public class AppConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*");
    }
}
```

---

## 11. Propiedades de Corrección

*Una propiedad es una característica o comportamiento que debe mantenerse verdadero en todas las ejecuciones válidas del sistema — esencialmente, una declaración formal sobre lo que el sistema debe hacer.*

### Propiedad 1: Cálculo de Subtotal

*Para cualquier* precio P (BigDecimal positivo) y cantidad N (entero positivo), el subtotal del ItemCarrito debe ser exactamente `P.multiply(BigDecimal.valueOf(N)).setScale(2, HALF_UP)`.

**Valida: Requerimientos 2.8**

### Propiedad 2: Consistencia del Total del Carrito

*Para cualquier* carrito con una lista de ítems, el campo `total` debe ser igual a la suma de todos los `subtotal` de sus ítems, calculado con `BigDecimal` (sin pérdida de precisión).

**Valida: Requerimientos 2.9**

### Propiedad 3: Agregar Ítem Incrementa el Carrito

*Para cualquier* carrito con K ítems de productos distintos, agregar un producto nuevo (cuyo ID no está en el carrito) debe resultar en un carrito con exactamente K+1 ítems.

**Valida: Requerimientos 2.1**

### Propiedad 4: Rechazo de Cantidades No Positivas

*Para cualquier* cantidad entera ≤ 0, la operación de agregar o actualizar un ítem debe lanzar `CantidadInvalidaException` y el carrito debe permanecer sin cambios.

**Valida: Requerimientos 2.7**

### Propiedad 5: Vaciado Automático tras Confirmar Venta

*Para cualquier* carrito no vacío, después de invocar `confirmarVenta()` exitosamente, `CarritoRepositorioPuerto.obtener().estaVacio()` debe retornar `true`.

**Valida: Requerimientos 3.3**

### Propiedad 6: Búsqueda Insensible a Mayúsculas

*Para cualquier* texto de búsqueda T y cualquier producto P tal que `P.nombre.toLowerCase().contains(T.toLowerCase())`, la búsqueda `findByNombre(T)` debe incluir P en los resultados.

**Valida: Requerimientos 1.5**

### Propiedad 7: Unicidad de IDs de Venta

*Para cualquier* secuencia de N confirmaciones de venta (N ≥ 2), todos los UUIDs asignados deben ser distintos entre sí.

**Valida: Requerimientos 3.8**

---

## 12. Estrategia de Pruebas

### Pruebas Unitarias (JUnit 5 + Mockito)

Cubren la capa de aplicación (`aplicacion/servicio/`). Usan Mockito para simular los Puertos de Salida.

**Cobertura mínima**: 80% en la capa de aplicación.

Casos a cubrir por servicio:

| Servicio | Casos de prueba |
|---|---|
| `ProductoServicio` | listarTodos retorna lista, buscarPorId existente, buscarPorId inexistente (excepción), buscarPorNombre con coincidencias, buscarPorNombre sin coincidencias |
| `CarritoServicio` | agregarItem nuevo, agregarItem producto inexistente (excepción), agregarItem cantidad inválida (excepción), actualizarItem, eliminarItem, vaciarCarrito, cálculo de subtotal y total |
| `VentaServicio` | confirmarVenta carrito no vacío, confirmarVenta carrito vacío (excepción), listarVentas, buscarVentaPorId existente, buscarVentaPorId inexistente (excepción) |

### Pruebas de Integración (@WebMvcTest + MockMvc)

Cubren los Controladores REST. Verifican:
- Códigos HTTP correctos (200, 201, 400, 404, 500)
- Estructura del JSON de respuesta
- Manejo de errores por `@ControllerAdvice`
- Validación de DTOs con `@Valid`

### Pruebas Basadas en Propiedades (jqwik)

Biblioteca: **jqwik** (integración nativa con JUnit 5).

```xml
<!-- pom.xml -->
<dependency>
    <groupId>net.jqwik</groupId>
    <artifactId>jqwik</artifactId>
    <version>1.8.x</version>
    <scope>test</scope>
</dependency>
```

Configuración: mínimo 100 iteraciones por propiedad (`@Property(tries = 100)`).

Etiqueta de referencia: `// Feature: proyecto-pos, Property N: <texto>`

Cada propiedad del diseño se implementa con un único test `@Property`.

---

## 13. Decisiones de Diseño y Justificaciones

| Decisión | Justificación |
|---|---|
| Arquitectura hexagonal | Desacopla la lógica de negocio de la infraestructura, facilitando pruebas unitarias sin Spring y el reemplazo futuro de la persistencia en memoria por una base de datos real. |
| Carrito como singleton en memoria | Simplifica la implementación para esta fase (un solo operador). En producción se reemplazaría por sesiones HTTP o un repositorio por sesión. |
| BigDecimal para precios y totales | Evita errores de precisión de punto flotante en cálculos monetarios. |
| UUID como identificador | Garantiza unicidad sin necesidad de secuencias de base de datos. |
| @ControllerAdvice global | Centraliza el manejo de errores, garantizando respuestas de error uniformes en toda la API. |
| DTOs separados de entidades | Evita exponer la estructura interna del dominio en la API REST y permite evolucionar ambas capas independientemente. |
| Datos iniciales con CommandLineRunner | Permite precargar productos de ejemplo al arrancar sin acoplar la lógica de inicialización a los repositorios. |
