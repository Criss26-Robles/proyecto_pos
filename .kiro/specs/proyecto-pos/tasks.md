# Plan de Implementación: Sistema POS (Punto de Venta)

## Visión General

Implementación completa del Sistema POS en dos módulos independientes:
- **Backend**: Java 17 + Spring Boot 3.2.x con arquitectura hexagonal (Ports & Adapters)
- **Frontend**: Node.js 20 LTS + Express.js 4.18.x con Handlebars y Axios

La implementación sigue el orden: dominio → puertos → adaptadores → controladores → pruebas, tanto en backend como en frontend.

---

## Tareas

### MÓDULO BACKEND

- [~] 1. Configurar el proyecto Maven (pom.xml)
  - Crear `pos_backend/pom.xml` con groupId `com.pos`, artifactId `pos-backend`, Java 17
  - Agregar dependencias: `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-devtools`
  - Agregar dependencias de prueba: `spring-boot-starter-test` (JUnit 5 + Mockito), `jqwik 1.8.x`
  - Crear `src/main/resources/application.properties` con `server.port=8080`
  - Crear clase principal `PosBackendApplication.java` con `@SpringBootApplication`
  - _Requerimientos: 11.1, 11.3, 11.6_

- [~] 2. Implementar entidades del dominio
  - [~] 2.1 Crear entidad `Producto.java` en `com.pos.dominio.modelo`
    - Campos: `UUID id`, `String codigo`, `String nombre`, `BigDecimal precio`, `Integer stock`
    - Constructor con todos los campos, getters y setters
    - _Requerimientos: 1.1, 4.1_
  - [~] 2.2 Crear entidad `ItemCarrito.java` en `com.pos.dominio.modelo`
    - Campos: `Producto producto`, `Integer cantidad`, `BigDecimal subtotal`
    - Constructor con todos los campos, getters y setters
    - _Requerimientos: 2.8, 4.1_
  - [~] 2.3 Crear entidad `Carrito.java` en `com.pos.dominio.modelo`
    - Campo: `List<ItemCarrito> items`, `BigDecimal total`
    - Métodos de dominio: `agregarItem`, `actualizarItem`, `eliminarItem`, `vaciar`, `recalcularTotal`, `estaVacio`
    - _Requerimientos: 2.1, 2.2, 2.3, 2.5, 4.1_
  - [~] 2.4 Crear entidad `Venta.java` en `com.pos.dominio.modelo`
    - Campos: `UUID id`, `List<ItemCarrito> items`, `BigDecimal total`, `LocalDateTime fechaHora`
    - Constructor con todos los campos, getters y setters
    - _Requerimientos: 3.1, 3.8, 4.1_

- [~] 3. Definir puertos de salida (interfaces de repositorio)
  - [~] 3.1 Crear interfaz `ProductoRepositorioPuerto.java` en `com.pos.dominio.puerto`
    - Métodos: `List<Producto> findAll()`, `Optional<Producto> findById(UUID id)`, `List<Producto> findByNombre(String nombre)`
    - _Requerimientos: 1.6, 4.3_
  - [~] 3.2 Crear interfaz `CarritoRepositorioPuerto.java` en `com.pos.dominio.puerto`
    - Métodos: `Carrito obtener()`, `void guardar(Carrito carrito)`, `void vaciar()`
    - _Requerimientos: 2.10, 4.3_
  - [~] 3.3 Crear interfaz `VentaRepositorioPuerto.java` en `com.pos.dominio.puerto`
    - Métodos: `Venta guardar(Venta venta)`, `List<Venta> findAll()`, `Optional<Venta> findById(UUID id)`
    - _Requerimientos: 3.9, 4.3_

- [~] 4. Definir puertos de entrada (interfaces de casos de uso)
  - [~] 4.1 Crear interfaz `ProductoServicioPuerto.java` en `com.pos.aplicacion.puerto`
    - Métodos: `List<ProductoDTO> listarTodos()`, `ProductoDTO buscarPorId(UUID id)`, `List<ProductoDTO> buscarPorNombre(String nombre)`
    - _Requerimientos: 4.2_
  - [~] 4.2 Crear interfaz `CarritoServicioPuerto.java` en `com.pos.aplicacion.puerto`
    - Métodos: `CarritoDTO obtenerCarrito()`, `CarritoDTO agregarItem(UUID, int)`, `CarritoDTO actualizarItem(UUID, int)`, `CarritoDTO eliminarItem(UUID)`, `CarritoDTO vaciarCarrito()`
    - _Requerimientos: 4.2_
  - [~] 4.3 Crear interfaz `VentaServicioPuerto.java` en `com.pos.aplicacion.puerto`
    - Métodos: `VentaDTO confirmarVenta()`, `List<VentaDTO> listarVentas()`, `VentaDTO buscarVentaPorId(UUID id)`
    - _Requerimientos: 4.2_

- [~] 5. Implementar adaptadores de salida (repositorios en memoria)
  - [~] 5.1 Crear `ProductoRepositorioMemoria.java` en `com.pos.infraestructura.adaptador.salida`
    - Implementa `ProductoRepositorioPuerto` con `HashMap<UUID, Producto>`
    - Anotación `@Repository`
    - _Requerimientos: 1.7, 4.5_
  - [~] 5.2 Crear `CarritoRepositorioMemoria.java` en `com.pos.infraestructura.adaptador.salida`
    - Implementa `CarritoRepositorioPuerto` con carrito singleton en memoria
    - Anotación `@Repository`
    - _Requerimientos: 4.5_
  - [~] 5.3 Crear `VentaRepositorioMemoria.java` en `com.pos.infraestructura.adaptador.salida`
    - Implementa `VentaRepositorioPuerto` con `HashMap<UUID, Venta>`
    - Anotación `@Repository`
    - _Requerimientos: 4.5_

- [~] 6. Cargar datos iniciales de productos
  - Crear `DatosIniciales.java` en `com.pos.infraestructura.config` con `@Component` e `implements CommandLineRunner`
  - Inyectar `ProductoRepositorioPuerto` e insertar 10 productos de ejemplo con UUID, código, nombre, precio y stock
  - _Requerimientos: 1.4_

- [~] 7. Crear DTOs y excepciones de dominio
  - [~] 7.1 Crear DTOs en `com.pos.infraestructura.dto`
    - `ProductoDTO.java`, `ItemCarritoDTO.java`, `CarritoDTO.java`, `VentaDTO.java`
    - `AgregarItemRequest.java` con `@NotNull UUID productoId` y `@Positive Integer cantidad`
    - `ActualizarItemRequest.java` con `@Positive Integer cantidad`
    - _Requerimientos: 6.1, 6.2, 5.1_
  - [~] 7.2 Crear excepciones de dominio en `com.pos.infraestructura.excepcion`
    - `ProductoNoEncontradoException.java` (extends RuntimeException)
    - `VentaNoEncontradaException.java` (extends RuntimeException)
    - `CarritoVacioException.java` (extends RuntimeException)
    - `CantidadInvalidaException.java` (extends RuntimeException)
    - _Requerimientos: 5.3_

- [~] 8. Implementar casos de uso (servicios de aplicación)
  - [~] 8.1 Crear `ProductoServicio.java` en `com.pos.aplicacion.servicio`
    - Implementa `ProductoServicioPuerto`, inyecta `ProductoRepositorioPuerto`
    - Anotación `@Service`; mapeo Producto → ProductoDTO
    - _Requerimientos: 1.1, 1.2, 1.5, 4.4, 4.5_
  - [~] 8.2 Crear `CarritoServicio.java` en `com.pos.aplicacion.servicio`
    - Implementa `CarritoServicioPuerto`, inyecta `ProductoRepositorioPuerto` y `CarritoRepositorioPuerto`
    - Lanza `ProductoNoEncontradoException` y `CantidadInvalidaException` según corresponda
    - Anotación `@Service`; mapeo Carrito → CarritoDTO
    - _Requerimientos: 2.1–2.10, 4.4, 4.5_
  - [~] 8.3 Crear `VentaServicio.java` en `com.pos.aplicacion.servicio`
    - Implementa `VentaServicioPuerto`, inyecta `CarritoRepositorioPuerto` y `VentaRepositorioPuerto`
    - Lanza `CarritoVacioException` y `VentaNoEncontradaException` según corresponda
    - Anotación `@Service`; mapeo Venta → VentaDTO
    - _Requerimientos: 3.1–3.9, 4.4, 4.5_

- [~] 9. Implementar manejador global de excepciones y configuración
  - [~] 9.1 Crear `ManejadorExcepcionesGlobal.java` con `@ControllerAdvice`
    - Manejar: `ProductoNoEncontradoException` → 404, `VentaNoEncontradaException` → 404
    - Manejar: `CarritoVacioException` → 400, `CantidadInvalidaException` → 400
    - Manejar: `MethodArgumentNotValidException` → 400 con lista de campos inválidos
    - Manejar: `Exception.class` → 500 con mensaje genérico
    - Estructura de respuesta: `{ "error": "...", "timestamp": "...", "status": N }`
    - _Requerimientos: 5.3, 5.5, 5.6, 6.3_
  - [~] 9.2 Crear `AppConfig.java` con `@Configuration` e `implements WebMvcConfigurer`
    - Configurar CORS para `http://localhost:3000` en `/api/**` con métodos GET, POST, PUT, DELETE
    - _Requerimientos: 5.2_

- [~] 10. Implementar controladores REST
  - [~] 10.1 Crear `ProductoControlador.java` en `com.pos.infraestructura.adaptador.entrada`
    - `@RestController`, `@RequestMapping("/api/productos")`, inyecta `ProductoServicioPuerto`
    - `GET /api/productos` → `listarTodos()` o `buscarPorNombre(nombre)` según parámetro
    - `GET /api/productos/{id}` → `buscarPorId(id)`
    - _Requerimientos: 1.1, 1.2, 1.5, 4.4, 5.1, 5.4_
  - [~] 10.2 Crear `CarritoControlador.java` en `com.pos.infraestructura.adaptador.entrada`
    - `@RestController`, `@RequestMapping("/api/carrito")`, inyecta `CarritoServicioPuerto`
    - `GET /api/carrito`, `POST /api/carrito/items` (con `@Valid`), `PUT /api/carrito/items/{productoId}` (con `@Valid`)
    - `DELETE /api/carrito/items/{productoId}`, `DELETE /api/carrito`
    - _Requerimientos: 2.1–2.5, 4.4, 5.1, 5.4, 6.4_
  - [~] 10.3 Crear `VentaControlador.java` en `com.pos.infraestructura.adaptador.entrada`
    - `@RestController`, `@RequestMapping("/api/ventas")`, inyecta `VentaServicioPuerto`
    - `POST /api/ventas` → HTTP 201, `GET /api/ventas`, `GET /api/ventas/{id}`
    - _Requerimientos: 3.1, 3.2, 3.4, 3.5, 3.6, 4.4, 5.1, 5.4_

- [~] 11. Checkpoint Backend — Verificar compilación y arranque
  - Ejecutar `mvn compile` sin errores
  - Ejecutar `mvn spring-boot:run` y verificar que el servidor arranca en el puerto 8080
  - Verificar que `GET http://localhost:8080/api/productos` retorna los 10 productos iniciales
  - Asegurarse de que todos los tests pasan; consultar al usuario si surgen dudas.

- [~] 12. Escribir pruebas unitarias de casos de uso
  - [~] 12.1 Crear `ProductoServicioTest.java` en `src/test/java/com/pos/aplicacion`
    - Casos: listarTodos, buscarPorId existente, buscarPorId inexistente (excepción), buscarPorNombre con/sin coincidencias
    - Usar Mockito para simular `ProductoRepositorioPuerto`
    - _Requerimientos: 7.1, 12.1_
  - [~] 12.2 Crear `CarritoServicioTest.java` en `src/test/java/com/pos/aplicacion`
    - Casos: agregarItem nuevo, producto inexistente (excepción), cantidad inválida (excepción), actualizarItem, eliminarItem, vaciarCarrito, cálculo de subtotal y total
    - Usar Mockito para simular `ProductoRepositorioPuerto` y `CarritoRepositorioPuerto`
    - _Requerimientos: 7.1, 12.1_
  - [~] 12.3 Crear `VentaServicioTest.java` en `src/test/java/com/pos/aplicacion`
    - Casos: confirmarVenta carrito no vacío, confirmarVenta carrito vacío (excepción), listarVentas, buscarVentaPorId existente/inexistente
    - Usar Mockito para simular `CarritoRepositorioPuerto` y `VentaRepositorioPuerto`
    - _Requerimientos: 7.1, 12.1_

- [~] 13. Escribir pruebas de integración de controladores
  - [~] 13.1 Crear `ProductoControladorTest.java` en `src/test/java/com/pos/infraestructura`
    - Usar `@WebMvcTest(ProductoControlador.class)` y `MockMvc`
    - Verificar HTTP 200 para GET /api/productos y GET /api/productos/{id}
    - Verificar HTTP 404 para producto inexistente
    - _Requerimientos: 7.2, 12.2_
  - [~] 13.2 Crear `CarritoControladorTest.java` en `src/test/java/com/pos/infraestructura`
    - Usar `@WebMvcTest(CarritoControlador.class)` y `MockMvc`
    - Verificar HTTP 200 para todas las operaciones del carrito
    - Verificar HTTP 400 para cantidad inválida y HTTP 404 para producto inexistente
    - _Requerimientos: 7.2, 12.2_
  - [~] 13.3 Crear `VentaControladorTest.java` en `src/test/java/com/pos/infraestructura`
    - Usar `@WebMvcTest(VentaControlador.class)` y `MockMvc`
    - Verificar HTTP 201 para POST /api/ventas exitoso
    - Verificar HTTP 400 para carrito vacío y HTTP 404 para venta inexistente
    - _Requerimientos: 7.2, 12.2_

- [~] 14. Escribir pruebas basadas en propiedades (jqwik)
  - [ ]* 14.1 Propiedad 1: Cálculo de subtotal e invariante del total del carrito
    - **Propiedad 1: Cálculo de Subtotal e Invariante del Total del Carrito**
    - **Valida: Requerimientos 2.8, 2.9**
    - Usar `@Property(tries = 100)` con `@ForAll @Positive BigDecimal precio` y `@ForAll @Positive int cantidad`
  - [ ]* 14.2 Propiedad 2: Agregar ítem incrementa la cardinalidad del carrito
    - **Propiedad 2: Agregar Ítem Incrementa la Cardinalidad del Carrito**
    - **Valida: Requerimientos 2.1**
  - [ ]* 14.3 Propiedad 3: Rechazo de cantidades no positivas
    - **Propiedad 3: Rechazo de Cantidades No Positivas**
    - **Valida: Requerimientos 2.7**
    - Usar `@ForAll @Negative int cantidad` y `@ForAll int cero` (= 0)
  - [ ]* 14.4 Propiedad 4: Vaciado automático del carrito tras confirmar venta
    - **Propiedad 4: Vaciado Automático del Carrito tras Confirmar Venta**
    - **Valida: Requerimientos 3.3, 3.1**
  - [ ]* 14.5 Propiedad 5: Búsqueda de productos insensible a mayúsculas
    - **Propiedad 5: Búsqueda de Productos Insensible a Mayúsculas**
    - **Valida: Requerimientos 1.5**
    - Usar `@ForAll String texto` con variaciones de mayúsculas/minúsculas
  - [ ]* 14.6 Propiedad 6: Unicidad de IDs de venta
    - **Propiedad 6: Unicidad de IDs de Venta**
    - **Valida: Requerimientos 3.8**
    - Generar N confirmaciones y verificar que todos los UUIDs son distintos

- [~] 15. Checkpoint Backend Final — Ejecutar suite completa de pruebas
  - Ejecutar `mvn test` y verificar que todas las pruebas pasan sin errores
  - Asegurarse de que todos los tests pasan; consultar al usuario si surgen dudas.

---

### MÓDULO FRONTEND

- [~] 16. Inicializar el proyecto Node.js
  - Crear `pos_frontend/package.json` con scripts `"start"` y `"dev"` (nodemon)
  - Agregar dependencias: `express 4.18.x`, `hbs 4.x`, `axios 1.6.x`, `dotenv 16.x`
  - Agregar devDependencies: `nodemon 3.x`, `jest`, `axios-mock-adapter`
  - Crear `.env.example` con `PORT=3000` y `BACKEND_URL=http://localhost:8080`
  - Crear `.gitignore` excluyendo `node_modules/` y `.env`
  - _Requerimientos: 6.2, 11.2, 11.4_

- [~] 17. Crear configuración centralizada y módulos de servicio
  - [~] 17.1 Crear `src/config/config.js`
    - Cargar `.env` con `require('dotenv').config()`
    - Exportar `PORT` (default 3000) y `BACKEND_URL` (default `http://localhost:8080`)
    - _Requerimientos: 5.2, 5.5, 10.2_
  - [~] 17.2 Crear `src/servicios/productoServicio.js`
    - Funciones `obtenerTodos()` y `buscarPorNombre(nombre)` con Axios y JSDoc
    - _Requerimientos: 5.1, 7.3, 7.5_
  - [~] 17.3 Crear `src/servicios/carritoServicio.js`
    - Funciones: `obtenerCarrito()`, `agregarItem(productoId, cantidad)`, `actualizarItem(productoId, cantidad)`, `eliminarItem(productoId)`, `vaciarCarrito()` con Axios y JSDoc
    - _Requerimientos: 5.1, 7.3, 7.5_
  - [~] 17.4 Crear `src/servicios/ventaServicio.js`
    - Funciones: `confirmarVenta()`, `obtenerHistorial()`, `obtenerDetalle(id)` con Axios y JSDoc
    - _Requerimientos: 5.1, 7.3, 7.5_

- [~] 18. Crear vistas Handlebars
  - [~] 18.1 Crear `views/layouts/main.hbs`
    - Navbar con Bootstrap 5 (CDN), enlaces a "Punto de Venta" (`/`) e "Historial de Ventas" (`/historial`)
    - Bloque `{{{body}}}` para contenido dinámico
    - Incluir `public/css/estilos.css` y `public/js/main.js`
    - _Requerimientos: 6.5, 10.6_
  - [~] 18.2 Crear `views/index.hbs`
    - Dos columnas: catálogo (8/12) con tabla y formulario de búsqueda; carrito (4/12) con ítems, total y botones
    - Botón "Confirmar Venta" con `id="btn-confirmar-venta"` y `data-vacio` para JS del cliente
    - Mostrar mensajes de error inline si existen
    - _Requerimientos: 1.1–1.5, 2.5, 2.6, 3.4, 3.6_
  - [~] 18.3 Crear `views/historial.hbs`
    - Tabla con columnas: ID (8 chars), Fecha y Hora, Total, Acción ("Ver")
    - Mensaje "No hay ventas registradas aún." cuando la lista está vacía
    - _Requerimientos: 4.1–4.4_
  - [~] 18.4 Crear `views/venta-detalle.hbs`
    - Mostrar ID completo, fecha, tabla de ítems con subtotales y total
    - Enlace "← Volver al Historial"
    - _Requerimientos: 4.3_
  - [~] 18.5 Crear `views/venta-confirmacion.hbs`
    - Mostrar resumen de venta: ID, lista de productos, cantidades, subtotales y total
    - Enlace para volver al punto de venta
    - _Requerimientos: 3.2_
  - [~] 18.6 Crear `views/error.hbs`
    - Mostrar mensaje de error con ícono de advertencia
    - Enlace "← Volver al inicio"
    - _Requerimientos: 1.4, 4.5, 5.3_

- [~] 19. Crear estilos CSS y JavaScript del cliente
  - [~] 19.1 Crear `public/css/estilos.css`
    - Estilos personalizados que complementan Bootstrap 5
    - _Requerimientos: 6.6_
  - [~] 19.2 Crear `public/js/main.js`
    - Deshabilitar botón "Confirmar Venta" cuando el carrito está vacío (leer `data-vacio`)
    - _Requerimientos: 3.6_

- [~] 20. Implementar rutas Express
  - [~] 20.1 Crear `src/rutas/productoRutas.js`
    - `GET /` → obtener productos (con/sin búsqueda) y carrito, renderizar `index.hbs`
    - Manejo de errores con `try/catch` y función `extraerMensajeError`
    - _Requerimientos: 1.1–1.4, 5.4, 7.1, 7.4_
  - [~] 20.2 Crear `src/rutas/carritoRutas.js`
    - `POST /carrito/items` → agregarItem → redirect `/`
    - `POST /carrito/items/:id/actualizar` → actualizarItem → redirect `/`
    - `POST /carrito/items/:id/eliminar` → eliminarItem → redirect `/`
    - `POST /carrito/vaciar` → vaciarCarrito → redirect `/`
    - Manejo de errores HTTP 400/404 con re-renderizado inline
    - _Requerimientos: 2.1–2.7, 5.4, 7.1, 7.4_
  - [ ] 20.3 Crear `src/rutas/ventaRutas.js`
    - `POST /ventas` → confirmarVenta → redirect `/ventas/confirmacion/:id`
    - `GET /ventas/confirmacion/:id` → renderizar `venta-confirmacion.hbs`
    - `GET /historial` → obtenerHistorial → renderizar `historial.hbs`
    - `GET /historial/:id` → obtenerDetalle → renderizar `venta-detalle.hbs`
    - Manejo de errores HTTP 400 inline y errores de red con `error.hbs`
    - _Requerimientos: 3.1–3.5, 4.1–4.5, 5.4, 7.1, 7.4_

- [ ] 21. Crear punto de entrada Express (app.js)
  - Configurar Express con motor de plantillas Handlebars (`hbs`)
  - Registrar partials desde `views/layouts/`
  - Configurar middleware: `express.urlencoded`, `express.json`, `express.static`
  - Montar rutas: `productoRutas` en `/`, `carritoRutas` en `/carrito`, `ventaRutas` en `/`
  - Agregar manejador 404 al final
  - Iniciar servidor en `PORT`
  - _Requerimientos: 5.5, 5.6, 6.1, 6.4_

- [ ] 22. Checkpoint Frontend — Verificar arranque y flujos básicos
  - Ejecutar `npm install` y `npm start`
  - Verificar que el frontend arranca en el puerto 3000
  - Verificar que `GET http://localhost:3000/` renderiza el catálogo (con backend activo)
  - Asegurarse de que todos los flujos básicos funcionan; consultar al usuario si surgen dudas.

- [ ] 23. Escribir pruebas de módulos de servicio (Jest)
  - [ ]* 23.1 Crear `productoServicio.test.js`
    - Probar `obtenerTodos()` y `buscarPorNombre()` con `axios-mock-adapter`
    - Verificar que se llama a la URL correcta y se retornan los datos esperados
    - _Requerimientos: 5.1, 5.4_
  - [ ]* 23.2 Crear `carritoServicio.test.js`
    - Probar todas las funciones del servicio con `axios-mock-adapter`
    - Verificar manejo de errores HTTP 400 y 404
    - _Requerimientos: 5.1, 5.4_
  - [ ]* 23.3 Crear `ventaServicio.test.js`
    - Probar `confirmarVenta()`, `obtenerHistorial()` y `obtenerDetalle()` con `axios-mock-adapter`
    - Verificar manejo de errores HTTP 400 y 404
    - _Requerimientos: 5.1, 5.4_

- [ ] 24. Checkpoint Final — Verificar suite completa de pruebas
  - Ejecutar `npm test -- --run` (o `npx jest`) y verificar que todas las pruebas pasan
  - Asegurarse de que todos los tests pasan; consultar al usuario si surgen dudas.

---

## Notas

- Las tareas marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido
- Cada tarea referencia los requerimientos específicos para trazabilidad
- Los checkpoints garantizan validación incremental del sistema
- Las pruebas de propiedades (jqwik) validan invariantes matemáticas del dominio
- Las pruebas unitarias (JUnit 5 + Mockito) validan casos específicos y condiciones de error
- Las pruebas de integración (`@WebMvcTest`) validan los contratos HTTP de la API REST
- Las pruebas de servicios frontend (Jest + axios-mock-adapter) validan la comunicación con el backend
