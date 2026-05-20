
# Tareas de Implementación — Backend POS (Java 17 + Spring Boot 3.2.x)

## Visión General

Implementación del backend del Sistema POS con arquitectura hexagonal (Ports & Adapters), principios SOLID e inversión de dependencias. La persistencia es completamente en memoria (HashMap). El orden de implementación sigue la dirección del dominio hacia la infraestructura.

**Tecnologías**: Java 17, Spring Boot 3.2.x, Maven 3.9.x, JUnit 5, Mockito 5.x, jqwik 1.8.x

---

## Fase 1: Configuración del Proyecto

- [ ] 1. Configurar el proyecto Maven
  - [ ] 1.1 Crear `pom.xml` con groupId `com.pos`, artifactId `pos-backend`, versión `1.0.0`
    - Establecer `<java.version>17</java.version>` y parent `spring-boot-starter-parent 3.2.x`
    - Agregar dependencia `spring-boot-starter-web`
    - Agregar dependencia `spring-boot-starter-validation`
    - Agregar dependencia `spring-boot-devtools` (scope runtime, optional true)
    - Agregar dependencia `spring-boot-starter-test` (scope test)
    - Agregar dependencia `net.jqwik:jqwik:1.8.x` (scope test)
    - _Requerimientos: 11.3, 7.3_
  - [ ] 1.2 Crear estructura de directorios Maven estándar
    - Crear `src/main/java/com/pos/` y `src/test/java/com/pos/`
    - Crear `src/main/resources/application.properties`
    - _Requerimientos: 11.1_
  - [ ] 1.3 Crear clase principal `PosBackendApplication.java` en `com.pos`
    - Anotación `@SpringBootApplication`
    - Método `main` con `SpringApplication.run(PosBackendApplication.class, args)`
    - _Requerimientos: 11.1_
  - [ ] 1.4 Configurar `application.properties`
    - `server.port=8080`
    - `spring.application.name=pos-backend`
    - `logging.level.com.pos=DEBUG`
    - _Requerimientos: 5.7, 11.6_
  - **Criterio de verificación**: `mvn compile` ejecuta sin errores de compilación.


---

## Fase 2: Capa de Dominio — Entidades

- [ ] 2. Implementar entidades del dominio en `com.pos.dominio.modelo`
  - [ ] 2.1 Crear `Producto.java`
    - Campos privados: `UUID id`, `String codigo`, `String nombre`, `BigDecimal precio`, `Integer stock`
    - Constructor con todos los campos
    - Getters y setters para todos los campos
    - Método `toString()` para logging
    - _Requerimientos: 1.1, 4.1_
  - [ ] 2.2 Crear `ItemCarrito.java`
    - Campos privados: `Producto producto`, `Integer cantidad`, `BigDecimal subtotal`
    - Constructor con `(Producto producto, Integer cantidad, BigDecimal subtotal)`
    - Getters y setters para todos los campos
    - _Requerimientos: 2.8, 4.1_
  - [ ] 2.3 Crear `Carrito.java`
    - Campo privado: `List<ItemCarrito> items` (inicializado como `new ArrayList<>()`)
    - Campo privado: `BigDecimal total` (inicializado como `BigDecimal.ZERO`)
    - Método `agregarItem(ItemCarrito item)`: agrega o incrementa cantidad si el producto ya existe
    - Método `actualizarItem(UUID productoId, int cantidad)`: actualiza cantidad y recalcula subtotal
    - Método `eliminarItem(UUID productoId)`: elimina el ítem con el productoId dado
    - Método `vaciar()`: limpia la lista de ítems y resetea total a ZERO
    - Método `recalcularTotal()`: suma todos los subtotales con `BigDecimal` (sin pérdida de precisión)
    - Método `estaVacio()`: retorna `items.isEmpty()`
    - Getters para `items` y `total`
    - _Requerimientos: 2.1, 2.2, 2.3, 2.5, 2.8, 2.9, 4.1_
  - [ ] 2.4 Crear `Venta.java`
    - Campos privados: `UUID id`, `List<ItemCarrito> items`, `BigDecimal total`, `LocalDateTime fechaHora`
    - Constructor con todos los campos
    - Getters para todos los campos
    - _Requerimientos: 3.1, 3.8, 4.1_
  - **Criterio de verificación**: Las entidades compilan sin errores; `Carrito.recalcularTotal()` retorna la suma correcta de subtotales.


---

## Fase 3: Capa de Dominio — Puertos de Salida

- [ ] 3. Definir puertos de salida (interfaces de repositorio) en `com.pos.dominio.puerto`
  - [ ] 3.1 Crear `ProductoRepositorioPuerto.java`
    - Interfaz pública con métodos:
      - `List<Producto> findAll()`
      - `Optional<Producto> findById(UUID id)`
      - `List<Producto> findByNombre(String nombre)`
    - Sin anotaciones de Spring (es una interfaz de dominio puro)
    - _Requerimientos: 1.6, 4.3_
  - [ ] 3.2 Crear `CarritoRepositorioPuerto.java`
    - Interfaz pública con métodos:
      - `Carrito obtener()`
      - `void guardar(Carrito carrito)`
      - `void vaciar()`
    - _Requerimientos: 2.10, 4.3_
  - [ ] 3.3 Crear `VentaRepositorioPuerto.java`
    - Interfaz pública con métodos:
      - `Venta guardar(Venta venta)`
      - `List<Venta> findAll()`
      - `Optional<Venta> findById(UUID id)`
    - _Requerimientos: 3.9, 4.3_
  - **Criterio de verificación**: Las interfaces compilan sin errores; no importan clases de `infraestructura`.

---

## Fase 4: Capa de Aplicación — Puertos de Entrada

- [ ] 4. Definir puertos de entrada (interfaces de casos de uso) en `com.pos.aplicacion.puerto`
  - [ ] 4.1 Crear `ProductoServicioPuerto.java`
    - Interfaz pública con métodos:
      - `List<ProductoDTO> listarTodos()`
      - `ProductoDTO buscarPorId(UUID id)`
      - `List<ProductoDTO> buscarPorNombre(String nombre)`
    - Importar `ProductoDTO` de `com.pos.infraestructura.dto`
    - _Requerimientos: 4.2_
  - [ ] 4.2 Crear `CarritoServicioPuerto.java`
    - Interfaz pública con métodos:
      - `CarritoDTO obtenerCarrito()`
      - `CarritoDTO agregarItem(UUID productoId, int cantidad)`
      - `CarritoDTO actualizarItem(UUID productoId, int cantidad)`
      - `CarritoDTO eliminarItem(UUID productoId)`
      - `CarritoDTO vaciarCarrito()`
    - _Requerimientos: 4.2_
  - [ ] 4.3 Crear `VentaServicioPuerto.java`
    - Interfaz pública con métodos:
      - `VentaDTO confirmarVenta()`
      - `List<VentaDTO> listarVentas()`
      - `VentaDTO buscarVentaPorId(UUID id)`
    - _Requerimientos: 4.2_
  - **Criterio de verificación**: Las interfaces compilan sin errores; los DTOs referenciados existen.


---

## Fase 5: Infraestructura — Adaptadores de Salida (Repositorios en Memoria)

- [ ] 5. Implementar adaptadores de salida en `com.pos.infraestructura.adaptador.salida`
  - [ ] 5.1 Crear `ProductoRepositorioMemoria.java`
    - Anotación `@Repository`
    - Implementa `ProductoRepositorioPuerto`
    - Campo privado: `Map<UUID, Producto> almacen = new HashMap<>()`
    - `findAll()`: retorna `new ArrayList<>(almacen.values())`
    - `findById(UUID id)`: retorna `Optional.ofNullable(almacen.get(id))`
    - `findByNombre(String nombre)`: filtra con stream y `toLowerCase().contains(nombre.toLowerCase())`
    - Método `void guardar(Producto p)` (package-private) para uso de `DatosIniciales`
    - _Requerimientos: 1.7, 4.5_
  - [ ] 5.2 Crear `CarritoRepositorioMemoria.java`
    - Anotación `@Repository`
    - Implementa `CarritoRepositorioPuerto`
    - Campo privado: `Carrito carrito = new Carrito()` (singleton en memoria)
    - `obtener()`: retorna `this.carrito`
    - `guardar(Carrito c)`: asigna `this.carrito = c`
    - `vaciar()`: asigna `this.carrito = new Carrito()`
    - _Requerimientos: 4.5_
  - [ ] 5.3 Crear `VentaRepositorioMemoria.java`
    - Anotación `@Repository`
    - Implementa `VentaRepositorioPuerto`
    - Campo privado: `Map<UUID, Venta> almacen = new HashMap<>()`
    - `guardar(Venta v)`: inserta en el mapa y retorna la venta
    - `findAll()`: retorna `new ArrayList<>(almacen.values())`
    - `findById(UUID id)`: retorna `Optional.ofNullable(almacen.get(id))`
    - _Requerimientos: 4.5_
  - **Criterio de verificación**: Los repositorios compilan e implementan correctamente todas las interfaces de sus puertos.

---

## Fase 6: Infraestructura — Datos Iniciales

- [ ] 6. Cargar datos iniciales de productos
  - [ ] 6.1 Crear `DatosIniciales.java` en `com.pos.infraestructura.config`
    - Anotación `@Component`, implementa `CommandLineRunner`
    - Inyectar `ProductoRepositorioMemoria` (o `ProductoRepositorioPuerto`)
    - En el método `run()`, crear y guardar 10 productos de ejemplo:
      - PROD-001: Coca-Cola 600ml, $15.50, stock 100
      - PROD-002: Pepsi 600ml, $14.00, stock 80
      - PROD-003: Agua Mineral 500ml, $8.00, stock 150
      - PROD-004: Jugo de Naranja 1L, $22.00, stock 60
      - PROD-005: Leche Entera 1L, $18.50, stock 90
      - PROD-006: Pan de Caja (bimbo), $35.00, stock 40
      - PROD-007: Galletas Oreo 200g, $28.00, stock 70
      - PROD-008: Chocolate Snickers, $12.00, stock 120
      - PROD-009: Papas Sabritas 45g, $16.00, stock 85
      - PROD-010: Chicles Trident, $9.50, stock 200
    - Cada producto con `UUID.randomUUID()` como ID
    - _Requerimientos: 1.4_
  - **Criterio de verificación**: Al arrancar la aplicación, `GET /api/productos` retorna exactamente 10 productos.


---

## Fase 7: Infraestructura — DTOs y Excepciones

- [ ] 7. Crear DTOs en `com.pos.infraestructura.dto`
  - [ ] 7.1 Crear `ProductoDTO.java`
    - Campos públicos o con getters/setters: `UUID id`, `String codigo`, `String nombre`, `BigDecimal precio`, `Integer stock`
    - Constructor con todos los campos y constructor vacío (para Jackson)
    - _Requerimientos: 5.1_
  - [ ] 7.2 Crear `ItemCarritoDTO.java`
    - Campos: `ProductoDTO producto`, `Integer cantidad`, `BigDecimal subtotal`
    - Constructor con todos los campos y constructor vacío
    - _Requerimientos: 5.1_
  - [ ] 7.3 Crear `CarritoDTO.java`
    - Campos: `List<ItemCarritoDTO> items`, `BigDecimal total`
    - Constructor con todos los campos y constructor vacío
    - _Requerimientos: 5.1_
  - [ ] 7.4 Crear `VentaDTO.java`
    - Campos: `UUID id`, `List<ItemCarritoDTO> items`, `BigDecimal total`, `LocalDateTime fechaHora`
    - Constructor con todos los campos y constructor vacío
    - _Requerimientos: 5.1_
  - [ ] 7.5 Crear `AgregarItemRequest.java`
    - Campo `@NotNull UUID productoId`
    - Campo `@Positive Integer cantidad`
    - Constructor vacío y getters/setters
    - _Requerimientos: 6.1_
  - [ ] 7.6 Crear `ActualizarItemRequest.java`
    - Campo `@Positive Integer cantidad`
    - Constructor vacío y getters/setters
    - _Requerimientos: 6.2_

- [ ] 8. Crear excepciones de dominio en `com.pos.infraestructura.excepcion`
  - [ ] 8.1 Crear `ProductoNoEncontradoException.java`
    - Extiende `RuntimeException`
    - Constructor `(String id)` con mensaje `"Producto no encontrado con id: " + id`
    - _Requerimientos: 1.3, 5.3_
  - [ ] 8.2 Crear `VentaNoEncontradaException.java`
    - Extiende `RuntimeException`
    - Constructor `(String id)` con mensaje `"Venta no encontrada con id: " + id`
    - _Requerimientos: 3.7, 5.3_
  - [ ] 8.3 Crear `CarritoVacioException.java`
    - Extiende `RuntimeException`
    - Constructor sin parámetros con mensaje `"El carrito está vacío. Agregue productos antes de confirmar la venta."`
    - _Requerimientos: 3.4, 5.3_
  - [ ] 8.4 Crear `CantidadInvalidaException.java`
    - Extiende `RuntimeException`
    - Constructor sin parámetros con mensaje `"La cantidad debe ser mayor a cero"`
    - _Requerimientos: 2.7, 5.3_
  - **Criterio de verificación**: Todos los DTOs y excepciones compilan sin errores.


---

## Fase 8: Capa de Aplicación — Casos de Uso (Servicios)

- [ ] 9. Implementar casos de uso en `com.pos.aplicacion.servicio`
  - [ ] 9.1 Crear `ProductoServicio.java`
    - Anotación `@Service`
    - Implementa `ProductoServicioPuerto`
    - Inyectar `ProductoRepositorioPuerto` por constructor (no `@Autowired` en campo)
    - `listarTodos()`: llama `repositorio.findAll()` y mapea cada `Producto` a `ProductoDTO`
    - `buscarPorId(UUID id)`: llama `repositorio.findById(id)`, lanza `ProductoNoEncontradoException` si no existe
    - `buscarPorNombre(String nombre)`: llama `repositorio.findByNombre(nombre)` y mapea a `List<ProductoDTO>`
    - Método privado `mapearADTO(Producto p)` para reutilizar el mapeo
    - _Requerimientos: 1.1, 1.2, 1.3, 1.5, 4.4, 4.8_
  - [ ] 9.2 Crear `CarritoServicio.java`
    - Anotación `@Service`
    - Implementa `CarritoServicioPuerto`
    - Inyectar `ProductoRepositorioPuerto` y `CarritoRepositorioPuerto` por constructor
    - `obtenerCarrito()`: llama `carritoRepo.obtener()` y mapea a `CarritoDTO`
    - `agregarItem(UUID productoId, int cantidad)`:
      - Valida `cantidad > 0`, lanza `CantidadInvalidaException` si no
      - Busca producto con `productoRepo.findById(productoId)`, lanza `ProductoNoEncontradoException` si no existe
      - Crea `ItemCarrito` con subtotal = `precio.multiply(BigDecimal.valueOf(cantidad)).setScale(2, HALF_UP)`
      - Obtiene carrito, agrega ítem, recalcula total, guarda y retorna `CarritoDTO`
    - `actualizarItem(UUID productoId, int cantidad)`:
      - Valida `cantidad > 0`, lanza `CantidadInvalidaException` si no
      - Obtiene carrito, actualiza ítem, recalcula total, guarda y retorna `CarritoDTO`
    - `eliminarItem(UUID productoId)`: obtiene carrito, elimina ítem, recalcula total, guarda y retorna `CarritoDTO`
    - `vaciarCarrito()`: llama `carritoRepo.vaciar()` y retorna `CarritoDTO` vacío
    - Método privado `mapearADTO(Carrito c)` para reutilizar el mapeo
    - _Requerimientos: 2.1–2.10, 4.4, 4.5, 4.8_
  - [ ] 9.3 Crear `VentaServicio.java`
    - Anotación `@Service`
    - Implementa `VentaServicioPuerto`
    - Inyectar `CarritoRepositorioPuerto` y `VentaRepositorioPuerto` por constructor
    - `confirmarVenta()`:
      - Obtiene carrito con `carritoRepo.obtener()`
      - Si `carrito.estaVacio()`, lanza `CarritoVacioException`
      - Crea `Venta` con `UUID.randomUUID()`, copia de ítems, total del carrito y `LocalDateTime.now()`
      - Guarda venta con `ventaRepo.guardar(venta)`
      - Vacía carrito con `carritoRepo.vaciar()`
      - Retorna `VentaDTO`
    - `listarVentas()`: llama `ventaRepo.findAll()` y mapea a `List<VentaDTO>`
    - `buscarVentaPorId(UUID id)`: llama `ventaRepo.findById(id)`, lanza `VentaNoEncontradaException` si no existe
    - Método privado `mapearADTO(Venta v)` para reutilizar el mapeo
    - _Requerimientos: 3.1–3.9, 4.4, 4.5, 4.8_
  - **Criterio de verificación**: Los servicios compilan; no importan clases concretas de `infraestructura.adaptador`.


---

## Fase 9: Infraestructura — Manejador de Excepciones y Configuración CORS

- [ ] 10. Implementar manejador global de excepciones
  - [ ] 10.1 Crear `ManejadorExcepcionesGlobal.java` en `com.pos.infraestructura.excepcion`
    - Anotación `@ControllerAdvice` y `@RestController`
    - Crear clase interna o record `ErrorRespuesta` con campos `String error`, `String timestamp`, `int status`
    - `@ExceptionHandler(ProductoNoEncontradoException.class)` → HTTP 404 + `ErrorRespuesta`
    - `@ExceptionHandler(VentaNoEncontradaException.class)` → HTTP 404 + `ErrorRespuesta`
    - `@ExceptionHandler({CarritoVacioException.class, CantidadInvalidaException.class})` → HTTP 400 + `ErrorRespuesta`
    - `@ExceptionHandler(MethodArgumentNotValidException.class)` → HTTP 400 + `ErrorRespuesta` con lista de campos inválidos concatenados
    - `@ExceptionHandler(Exception.class)` → HTTP 500 + `ErrorRespuesta` con mensaje genérico (sin stack trace)
    - Usar `LocalDateTime.now().toString()` para el campo `timestamp`
    - Registrar cada excepción con `log.error(...)` usando SLF4J
    - _Requerimientos: 5.3, 5.5, 5.6, 6.3, 12.6_
  - [ ] 10.2 Crear `AppConfig.java` en `com.pos.infraestructura.config`
    - Anotación `@Configuration`, implementa `WebMvcConfigurer`
    - Sobreescribir `addCorsMappings(CorsRegistry registry)`:
      - `registry.addMapping("/api/**").allowedOrigins("http://localhost:3000").allowedMethods("GET", "POST", "PUT", "DELETE").allowedHeaders("*")`
    - _Requerimientos: 5.2_
  - **Criterio de verificación**: Las respuestas de error tienen la estructura `{ "error": "...", "timestamp": "...", "status": N }`.


---

## Fase 10: Infraestructura — Controladores REST

- [ ] 11. Implementar controladores REST en `com.pos.infraestructura.adaptador.entrada`
  - [ ] 11.1 Crear `ProductoControlador.java`
    - Anotaciones: `@RestController`, `@RequestMapping("/api/productos")`
    - Inyectar `ProductoServicioPuerto` por constructor
    - `@GetMapping` sin path: si `@RequestParam(required=false) String nombre` es null/vacío → `listarTodos()`, si no → `buscarPorNombre(nombre)`
    - `@GetMapping("/{id}")`: parsear `UUID.fromString(id)` y llamar `buscarPorId(id)`
    - Registrar con SLF4J: método HTTP, URI y código de respuesta
    - _Requerimientos: 1.1, 1.2, 1.3, 1.5, 4.4, 5.1, 5.4, 12.6_
  - [ ] 11.2 Crear `CarritoControlador.java`
    - Anotaciones: `@RestController`, `@RequestMapping("/api/carrito")`
    - Inyectar `CarritoServicioPuerto` por constructor
    - `@GetMapping`: retorna `obtenerCarrito()` con HTTP 200
    - `@PostMapping("/items")`: recibe `@Valid @RequestBody AgregarItemRequest`, llama `agregarItem()`
    - `@PutMapping("/items/{productoId}")`: recibe `@Valid @RequestBody ActualizarItemRequest`, llama `actualizarItem()`
    - `@DeleteMapping("/items/{productoId}")`: llama `eliminarItem(UUID.fromString(productoId))`
    - `@DeleteMapping`: llama `vaciarCarrito()`
    - Registrar con SLF4J cada petición
    - _Requerimientos: 2.1–2.5, 4.4, 5.1, 5.4, 6.4, 12.6_
  - [ ] 11.3 Crear `VentaControlador.java`
    - Anotaciones: `@RestController`, `@RequestMapping("/api/ventas")`
    - Inyectar `VentaServicioPuerto` por constructor
    - `@PostMapping`: llama `confirmarVenta()` y retorna `ResponseEntity.status(201).body(ventaDTO)`
    - `@GetMapping`: retorna `listarVentas()` con HTTP 200
    - `@GetMapping("/{id}")`: parsear UUID y llamar `buscarVentaPorId(id)`
    - Registrar con SLF4J cada petición
    - _Requerimientos: 3.1, 3.2, 3.4, 3.5, 3.6, 4.4, 5.1, 5.4, 12.6_
  - **Criterio de verificación**: `mvn spring-boot:run` arranca sin errores; `GET /api/productos` retorna HTTP 200 con 10 productos.

---

## Fase 11: Checkpoint — Verificación de Compilación y Arranque

- [ ] 12. Checkpoint — Verificar compilación y arranque del backend
  - Ejecutar `mvn compile` y confirmar que no hay errores de compilación
  - Ejecutar `mvn spring-boot:run` y verificar que el servidor arranca en el puerto 8080
  - Verificar con curl o Postman:
    - `GET http://localhost:8080/api/productos` → HTTP 200, lista de 10 productos
    - `GET http://localhost:8080/api/carrito` → HTTP 200, carrito vacío `{"items":[],"total":0}`
    - `POST http://localhost:8080/api/carrito/items` con body inválido → HTTP 400
    - `GET http://localhost:8080/api/productos/id-inexistente` → HTTP 404
  - Asegurarse de que todos los endpoints responden correctamente; consultar al usuario si surgen dudas.


---

## Fase 12: Pruebas Unitarias (JUnit 5 + Mockito)

- [ ] 13. Escribir pruebas unitarias de la capa de aplicación
  - [ ] 13.1 Crear `ProductoServicioTest.java` en `src/test/java/com/pos/aplicacion`
    - Anotación `@ExtendWith(MockitoExtension.class)`
    - Mock: `@Mock ProductoRepositorioPuerto repositorio`
    - Instancia bajo prueba: `@InjectMocks ProductoServicio servicio`
    - Caso 1: `listarTodos_retornaListaCompleta` — mock retorna 3 productos, verificar que el DTO tiene 3 elementos
    - Caso 2: `buscarPorId_existente_retornaDTO` — mock retorna `Optional.of(producto)`, verificar campos del DTO
    - Caso 3: `buscarPorId_inexistente_lanzaExcepcion` — mock retorna `Optional.empty()`, verificar que lanza `ProductoNoEncontradoException`
    - Caso 4: `buscarPorNombre_conCoincidencias_retornaFiltrado` — mock retorna lista filtrada, verificar resultado
    - Caso 5: `buscarPorNombre_sinCoincidencias_retornaListaVacia` — mock retorna lista vacía, verificar resultado
    - _Requerimientos: 7.1, 12.1_
  - [ ] 13.2 Crear `CarritoServicioTest.java` en `src/test/java/com/pos/aplicacion`
    - Mocks: `ProductoRepositorioPuerto productoRepo`, `CarritoRepositorioPuerto carritoRepo`
    - Caso 1: `agregarItem_productoExistente_retornaCarritoActualizado`
    - Caso 2: `agregarItem_productoInexistente_lanzaProductoNoEncontradoException`
    - Caso 3: `agregarItem_cantidadCero_lanzaCantidadInvalidaException`
    - Caso 4: `agregarItem_cantidadNegativa_lanzaCantidadInvalidaException`
    - Caso 5: `actualizarItem_cantidadValida_retornaCarritoActualizado`
    - Caso 6: `eliminarItem_itemExistente_retornaCarritoSinItem`
    - Caso 7: `vaciarCarrito_retornaCarritoVacio`
    - Caso 8: `calcularSubtotal_precioYCantidad_retornaProductoCorrecto` — verificar `BigDecimal` exacto
    - _Requerimientos: 7.1, 12.1_
  - [ ] 13.3 Crear `VentaServicioTest.java` en `src/test/java/com/pos/aplicacion`
    - Mocks: `CarritoRepositorioPuerto carritoRepo`, `VentaRepositorioPuerto ventaRepo`
    - Caso 1: `confirmarVenta_carritoNoVacio_retornaVentaDTO` — verificar que la venta tiene los ítems del carrito
    - Caso 2: `confirmarVenta_carritoNoVacio_vaciaCaritoTrasConfirmar` — verificar que `carritoRepo.vaciar()` fue llamado
    - Caso 3: `confirmarVenta_carritoVacio_lanzaCarritoVacioException`
    - Caso 4: `listarVentas_retornaListaCompleta`
    - Caso 5: `buscarVentaPorId_existente_retornaVentaDTO`
    - Caso 6: `buscarVentaPorId_inexistente_lanzaVentaNoEncontradaException`
    - _Requerimientos: 7.1, 12.1_
  - **Criterio de verificación**: `mvn test -pl . -Dtest=*ServicioTest` ejecuta sin fallos; cobertura >= 80% en capa de aplicación.


---

## Fase 13: Pruebas de Integración (@WebMvcTest + MockMvc)

- [ ] 14. Escribir pruebas de integración de controladores
  - [ ] 14.1 Crear `ProductoControladorTest.java` en `src/test/java/com/pos/infraestructura`
    - Anotación `@WebMvcTest(ProductoControlador.class)`
    - Mock: `@MockBean ProductoServicioPuerto servicio`
    - Caso 1: `GET /api/productos` → HTTP 200, JSON array con productos
    - Caso 2: `GET /api/productos?nombre=coca` → HTTP 200, lista filtrada
    - Caso 3: `GET /api/productos/{id}` con UUID válido → HTTP 200, ProductoDTO
    - Caso 4: `GET /api/productos/{id}` con UUID inexistente → HTTP 404, estructura de error
    - Verificar `Content-Type: application/json` en todas las respuestas
    - _Requerimientos: 7.2, 12.2_
  - [ ] 14.2 Crear `CarritoControladorTest.java` en `src/test/java/com/pos/infraestructura`
    - Anotación `@WebMvcTest(CarritoControlador.class)`
    - Mock: `@MockBean CarritoServicioPuerto servicio`
    - Caso 1: `GET /api/carrito` → HTTP 200, CarritoDTO
    - Caso 2: `POST /api/carrito/items` con body válido → HTTP 200, CarritoDTO actualizado
    - Caso 3: `POST /api/carrito/items` con `cantidad: 0` → HTTP 400, estructura de error
    - Caso 4: `POST /api/carrito/items` con `productoId: null` → HTTP 400, estructura de error
    - Caso 5: `PUT /api/carrito/items/{id}` con cantidad válida → HTTP 200
    - Caso 6: `DELETE /api/carrito/items/{id}` → HTTP 200
    - Caso 7: `DELETE /api/carrito` → HTTP 200, carrito vacío
    - _Requerimientos: 7.2, 12.2_
  - [ ] 14.3 Crear `VentaControladorTest.java` en `src/test/java/com/pos/infraestructura`
    - Anotación `@WebMvcTest(VentaControlador.class)`
    - Mock: `@MockBean VentaServicioPuerto servicio`
    - Caso 1: `POST /api/ventas` con carrito no vacío → HTTP 201, VentaDTO
    - Caso 2: `POST /api/ventas` con carrito vacío → HTTP 400, mensaje de error
    - Caso 3: `GET /api/ventas` → HTTP 200, lista de ventas
    - Caso 4: `GET /api/ventas/{id}` con UUID válido → HTTP 200, VentaDTO
    - Caso 5: `GET /api/ventas/{id}` con UUID inexistente → HTTP 404, estructura de error
    - _Requerimientos: 7.2, 12.2_
  - **Criterio de verificación**: `mvn test -Dtest=*ControladorTest` ejecuta sin fallos; todos los códigos HTTP son correctos.


---

## Fase 14: Pruebas Basadas en Propiedades (jqwik)

- [ ] 15. Escribir pruebas basadas en propiedades con jqwik
  - [ ]* 15.1 Propiedad 1: Cálculo de subtotal e invariante del total del carrito
    - Crear `CarritoServicioPropiedadesTest.java` en `src/test/java/com/pos/aplicacion`
    - Anotación `@ExtendWith(JqwikExtension.class)` (o usar `@Property` directamente con JUnit 5)
    - `@Property(tries = 100)` con `@ForAll @Positive BigDecimal precio` y `@ForAll @Positive int cantidad`
    - Verificar que `subtotal == precio.multiply(BigDecimal.valueOf(cantidad)).setScale(2, HALF_UP)`
    - Verificar que `carrito.total == suma de todos los subtotales`
    - Etiqueta: `// Feature: proyecto-pos, Property 1: Cálculo de Subtotal e Invariante del Total`
    - **Propiedad 1: Cálculo de Subtotal e Invariante del Total del Carrito**
    - **Valida: Requerimientos 2.8, 2.9**
  - [ ]* 15.2 Propiedad 2: Agregar ítem incrementa la cardinalidad del carrito
    - `@Property(tries = 100)` con carrito de K ítems de productos distintos
    - Agregar un producto nuevo (ID no presente) y verificar que el carrito tiene K+1 ítems
    - Verificar que el total refleja el nuevo subtotal
    - Etiqueta: `// Feature: proyecto-pos, Property 2: Agregar Ítem Incrementa Cardinalidad`
    - **Propiedad 2: Agregar Ítem Incrementa la Cardinalidad del Carrito**
    - **Valida: Requerimientos 2.1**
  - [ ]* 15.3 Propiedad 3: Rechazo de cantidades no positivas
    - `@Property(tries = 100)` con `@ForAll @IntRange(max = 0) int cantidad`
    - Verificar que `agregarItem` lanza `CantidadInvalidaException`
    - Verificar que el carrito permanece sin cambios tras el rechazo
    - Etiqueta: `// Feature: proyecto-pos, Property 3: Rechazo de Cantidades No Positivas`
    - **Propiedad 3: Rechazo de Cantidades No Positivas**
    - **Valida: Requerimientos 2.7**
  - [ ]* 15.4 Propiedad 4: Vaciado automático del carrito tras confirmar venta
    - `@Property(tries = 100)` con carrito no vacío de N ítems
    - Invocar `confirmarVenta()` y verificar que `carrito.estaVacio() == true`
    - Verificar que la venta creada contiene exactamente los mismos ítems y total que el carrito original
    - Etiqueta: `// Feature: proyecto-pos, Property 4: Vaciado Automático tras Confirmar Venta`
    - **Propiedad 4: Vaciado Automático del Carrito tras Confirmar Venta**
    - **Valida: Requerimientos 3.3, 3.1**
  - [ ]* 15.5 Propiedad 5: Búsqueda de productos insensible a mayúsculas
    - `@Property(tries = 100)` con `@ForAll String nombre` y variaciones de mayúsculas/minúsculas
    - Verificar que `findByNombre(T)` incluye todos los productos cuyo nombre contiene T (case-insensitive)
    - Verificar que no incluye productos cuyo nombre no contiene T
    - Etiqueta: `// Feature: proyecto-pos, Property 5: Búsqueda Insensible a Mayúsculas`
    - **Propiedad 5: Búsqueda de Productos Insensible a Mayúsculas**
    - **Valida: Requerimientos 1.5**
  - [ ]* 15.6 Propiedad 6: Unicidad de IDs de venta
    - `@Property(tries = 100)` generando N confirmaciones de venta (N entre 2 y 10)
    - Verificar que todos los UUIDs asignados son distintos entre sí
    - Etiqueta: `// Feature: proyecto-pos, Property 6: Unicidad de IDs de Venta`
    - **Propiedad 6: Unicidad de IDs de Venta**
    - **Valida: Requerimientos 3.8**
  - **Criterio de verificación**: `mvn test -Dtest=*PropiedadesTest` ejecuta sin fallos; cada propiedad pasa 100 iteraciones.

---

## Fase 15: Checkpoint Final

- [ ] 16. Checkpoint Final — Ejecutar suite completa de pruebas del backend
  - Ejecutar `mvn test` y verificar que TODAS las pruebas pasan sin errores
  - Verificar que no hay errores de compilación ni warnings críticos
  - Confirmar que el servidor arranca correctamente con `mvn spring-boot:run`
  - Asegurarse de que todos los tests pasan; consultar al usuario si surgen dudas.

---

## Notas

- Las tareas marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido
- El orden de implementación garantiza que cada fase depende de las anteriores
- Los servicios de aplicación NO deben importar clases de `infraestructura.adaptador` (DIP)
- Usar inyección por constructor (no `@Autowired` en campos) para facilitar las pruebas unitarias
- Usar `BigDecimal.setScale(2, RoundingMode.HALF_UP)` para todos los cálculos monetarios
- El carrito es un singleton en memoria (una sola sesión de operador para esta fase)
