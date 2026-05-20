# Requerimientos — Backend POS (Java + Spring Boot)

## Introducción

El Backend del Sistema POS es un servicio REST desarrollado en **Java 17** con **Spring Boot 3.x**. Aplica **arquitectura hexagonal (Ports & Adapters)**, principios **SOLID** e **inversión de dependencias**. La persistencia es completamente en memoria (datos mock) para esta fase del proyecto. Expone una API REST consumida por el Frontend Node.js.

---

## Glosario

- **Backend**: Módulo Java + Spring Boot que contiene la lógica de negocio y expone la API REST.
- **Producto**: Entidad de dominio con atributos: `id` (UUID), `codigo` (String), `nombre` (String), `precio` (BigDecimal), `stock` (Integer).
- **Carrito**: Entidad de dominio que agrupa Items_Carrito de la sesión activa.
- **Item_Carrito**: Entidad de dominio con atributos: `producto` (Producto), `cantidad` (Integer), `subtotal` (BigDecimal).
- **Venta**: Entidad de dominio con atributos: `id` (UUID), `items` (List<Item_Carrito>), `total` (BigDecimal), `fechaHora` (LocalDateTime).
- **Puerto_Entrada**: Interfaz Java en la capa de aplicación que define el contrato de un Caso_de_Uso.
- **Puerto_Salida**: Interfaz Java en la capa de dominio que define el contrato de acceso a datos.
- **Adaptador_Entrada**: Controlador REST que implementa el Puerto_Entrada.
- **Adaptador_Salida**: Implementación en memoria que implementa el Puerto_Salida.
- **Caso_de_Uso**: Clase de la capa de aplicación que orquesta la lógica de negocio.
- **DTO**: Objeto de transferencia de datos para la API REST (Request/Response).
- **Datos_en_Memoria**: Implementación de los Puertos_Salida usando estructuras Java en memoria (HashMap, ArrayList).

---

## Tecnologías y Dependencias

| Tecnología | Versión | Propósito |
|---|---|---|
| Java | 17 | Lenguaje principal |
| Spring Boot | 3.2.x | Framework principal |
| Spring Web (MVC) | 3.2.x | API REST |
| Spring Validation | 3.2.x | Validación de DTOs |
| Spring Boot DevTools | 3.2.x | Recarga en caliente |
| JUnit 5 | 5.10.x | Pruebas unitarias |
| Mockito | 5.x | Mocks en pruebas |
| SLF4J + Logback | incluido | Logging |
| Maven | 3.9.x | Gestión de dependencias |

---

## Estructura de Paquetes

```
pos_backend/
└── src/
    ├── main/
    │   ├── java/com/pos/
    │   │   ├── dominio/
    │   │   │   ├── modelo/          ← Entidades: Producto, Carrito, ItemCarrito, Venta
    │   │   │   └── puerto/          ← Puertos de salida (interfaces)
    │   │   │       ├── ProductoRepositorioPuerto.java
    │   │   │       ├── CarritoRepositorioPuerto.java
    │   │   │       └── VentaRepositorioPuerto.java
    │   │   ├── aplicacion/
    │   │   │   ├── puerto/          ← Puertos de entrada (interfaces de casos de uso)
    │   │   │   │   ├── ProductoServicioPuerto.java
    │   │   │   │   ├── CarritoServicioPuerto.java
    │   │   │   │   └── VentaServicioPuerto.java
    │   │   │   └── servicio/        ← Implementaciones de casos de uso
    │   │   │       ├── ProductoServicio.java
    │   │   │       ├── CarritoServicio.java
    │   │   │       └── VentaServicio.java
    │   │   └── infraestructura/
    │   │       ├── adaptador/
    │   │       │   ├── entrada/     ← Controladores REST
    │   │       │   │   ├── ProductoControlador.java
    │   │       │   │   ├── CarritoControlador.java
    │   │       │   │   └── VentaControlador.java
    │   │       │   └── salida/      ← Repositorios en memoria
    │   │       │       ├── ProductoRepositorioMemoria.java
    │   │       │       ├── CarritoRepositorioMemoria.java
    │   │       │       └── VentaRepositorioMemoria.java
    │   │       ├── config/          ← Configuración Spring (CORS, Beans)
    │   │       │   └── AppConfig.java
    │   │       └── dto/             ← DTOs de Request y Response
    │   │           ├── ProductoDTO.java
    │   │           ├── AgregarItemRequest.java
    │   │           ├── ActualizarItemRequest.java
    │   │           ├── CarritoDTO.java
    │   │           ├── ItemCarritoDTO.java
    │   │           └── VentaDTO.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/pos/
            ├── aplicacion/          ← Pruebas unitarias de casos de uso
            └── infraestructura/     ← Pruebas de integración de controladores
```

---

## Requerimientos Funcionales

### Requerimiento 1: Gestión del Catálogo de Productos

**Historia de Usuario:** Como Operador, quiero consultar el catálogo de productos disponibles, para poder seleccionar artículos al momento de realizar una venta.

#### Criterios de Aceptación

1. THE Backend SHALL exponer un endpoint `GET /api/productos` que retorne la lista completa de productos en formato JSON.
2. WHEN el Backend recibe `GET /api/productos/{id}`, THE Backend SHALL retornar el Producto correspondiente en formato JSON con HTTP 200.
3. IF el `{id}` de `GET /api/productos/{id}` no existe, THEN THE Backend SHALL retornar HTTP 404 con mensaje `"Producto no encontrado con id: {id}"`.
4. THE Backend SHALL inicializar los Datos_en_Memoria con al menos 10 productos de ejemplo al arrancar la aplicación.
5. WHEN el Backend recibe `GET /api/productos?nombre={texto}`, THE Backend SHALL retornar los Productos cuyo nombre contenga `{texto}` (insensible a mayúsculas/minúsculas).
6. THE Puerto_Salida `ProductoRepositorioPuerto` SHALL declarar los métodos: `List<Producto> findAll()`, `Optional<Producto> findById(UUID id)`, `List<Producto> findByNombre(String nombre)`.
7. THE Datos_en_Memoria `ProductoRepositorioMemoria` SHALL implementar `ProductoRepositorioPuerto` usando un `HashMap<UUID, Producto>` como almacenamiento.

---

### Requerimiento 2: Gestión del Carrito de Compras

**Historia de Usuario:** Como Operador, quiero agregar, modificar y eliminar productos en un carrito, para preparar la venta antes de confirmarla.

#### Criterios de Aceptación

1. WHEN el Backend recibe `POST /api/carrito/items` con `{ "productoId": "...", "cantidad": N }`, THE Backend SHALL agregar el Item_Carrito al Carrito activo y retornar el Carrito actualizado con HTTP 200.
2. WHEN el Backend recibe `PUT /api/carrito/items/{productoId}` con `{ "cantidad": N }`, THE Backend SHALL actualizar la cantidad del Item_Carrito y retornar el Carrito actualizado con HTTP 200.
3. WHEN el Backend recibe `DELETE /api/carrito/items/{productoId}`, THE Backend SHALL eliminar el Item_Carrito del Carrito activo y retornar el Carrito actualizado con HTTP 200.
4. WHEN el Backend recibe `GET /api/carrito`, THE Backend SHALL retornar el Carrito activo con todos sus Items_Carrito, subtotales y total en formato JSON con HTTP 200.
5. WHEN el Backend recibe `DELETE /api/carrito`, THE Backend SHALL vaciar el Carrito activo y retornar el Carrito vacío con HTTP 200.
6. IF el `productoId` de `POST /api/carrito/items` no existe en el catálogo, THEN THE Backend SHALL retornar HTTP 404 con mensaje `"Producto no encontrado con id: {productoId}"`.
7. IF la `cantidad` de `POST /api/carrito/items` o `PUT /api/carrito/items/{productoId}` es menor o igual a cero, THEN THE Backend SHALL retornar HTTP 400 con mensaje `"La cantidad debe ser mayor a cero"`.
8. THE Backend SHALL calcular el subtotal de cada Item_Carrito como `precio * cantidad` usando `BigDecimal` con escala de 2 decimales.
9. THE Backend SHALL calcular el total del Carrito sumando todos los subtotales de los Items_Carrito.
10. THE Caso_de_Uso `CarritoServicio` SHALL depender únicamente de `ProductoRepositorioPuerto` y `CarritoRepositorioPuerto`, sin referencias a implementaciones concretas.

---

### Requerimiento 3: Procesamiento de Ventas

**Historia de Usuario:** Como Operador, quiero confirmar el carrito para registrar una venta, para que quede constancia de la transacción.

#### Criterios de Aceptación

1. WHEN el Backend recibe `POST /api/ventas` con el Carrito activo no vacío, THE Backend SHALL crear una Venta con los Items_Carrito actuales, el total y la marca de tiempo (`LocalDateTime.now()`), y retornar HTTP 201 con el detalle de la Venta.
2. WHEN una Venta es confirmada, THE Backend SHALL vaciar el Carrito activo automáticamente.
3. IF el Carrito activo está vacío al recibir `POST /api/ventas`, THEN THE Backend SHALL retornar HTTP 400 con mensaje `"El carrito está vacío. Agregue productos antes de confirmar la venta."`.
4. WHEN el Backend recibe `GET /api/ventas`, THE Backend SHALL retornar la lista de todas las Ventas registradas en los Datos_en_Memoria con HTTP 200.
5. WHEN el Backend recibe `GET /api/ventas/{id}`, THE Backend SHALL retornar el detalle completo de la Venta con HTTP 200.
6. IF el `{id}` de `GET /api/ventas/{id}` no existe, THEN THE Backend SHALL retornar HTTP 404 con mensaje `"Venta no encontrada con id: {id}"`.
7. THE Backend SHALL asignar un UUID único a cada Venta en el momento de su creación mediante `UUID.randomUUID()`.
8. THE Caso_de_Uso `VentaServicio` SHALL depender únicamente de `CarritoRepositorioPuerto` y `VentaRepositorioPuerto`.

---

### Requerimiento 4: Arquitectura Hexagonal

**Historia de Usuario:** Como desarrollador, quiero que el backend aplique arquitectura hexagonal, para garantizar separación de responsabilidades y testabilidad.

#### Criterios de Aceptación

1. THE Backend SHALL separar el código en tres capas: `dominio`, `aplicacion` e `infraestructura`, cada una en su propio paquete Java.
2. THE Backend SHALL definir Puertos_Entrada como interfaces en `aplicacion/puerto/` para cada Caso_de_Uso.
3. THE Backend SHALL definir Puertos_Salida como interfaces en `dominio/puerto/` para cada operación de persistencia.
4. THE Controlador SHALL inyectar únicamente el Puerto_Entrada correspondiente (no la implementación concreta del Caso_de_Uso).
5. THE Caso_de_Uso SHALL inyectar únicamente los Puertos_Salida (no las implementaciones concretas de los repositorios).
6. THE Backend SHALL usar `@Component`, `@Service` y `@Repository` de Spring para registrar los Adaptadores como beans, vinculando automáticamente los Puertos con sus implementaciones.
7. THE Backend SHALL aplicar SRP: cada clase tendrá una única responsabilidad claramente definida.
8. THE Backend SHALL aplicar DIP: las capas de dominio y aplicación no importarán clases de la capa de infraestructura.
9. THE Backend SHALL aplicar OCP: los Casos_de_Uso serán extensibles mediante nuevas implementaciones de Puertos sin modificar el código existente.

---

### Requerimiento 5: API REST y Manejo de Errores

**Historia de Usuario:** Como desarrollador del frontend, quiero que el backend exponga una API REST bien definida con manejo de errores consistente.

#### Criterios de Aceptación

1. THE API_REST SHALL usar JSON para todas las peticiones y respuestas con `Content-Type: application/json`.
2. THE Backend SHALL habilitar CORS para el origen `http://localhost:3000` en todos los endpoints.
3. THE Backend SHALL implementar un `@ControllerAdvice` global para manejar excepciones y retornar respuestas de error con estructura uniforme: `{ "error": "mensaje", "timestamp": "...", "status": N }`.
4. IF el Backend recibe una petición a un endpoint no definido, THEN THE API_REST SHALL retornar HTTP 404 con estructura de error uniforme.
5. IF ocurre una excepción no controlada, THEN THE API_REST SHALL retornar HTTP 500 con mensaje genérico sin exponer stack traces.
6. THE Backend SHALL ejecutarse en el puerto `8080` configurado en `application.properties`.
7. THE Backend SHALL registrar con SLF4J cada petición entrante (método HTTP, URI, código de respuesta) y cada excepción producida.

---

### Requerimiento 6: Validación de Datos de Entrada

**Historia de Usuario:** Como desarrollador, quiero que el backend valide los datos de entrada, para garantizar la integridad de las operaciones.

#### Criterios de Aceptación

1. THE DTO `AgregarItemRequest` SHALL declarar `productoId` como `@NotNull` y `cantidad` como `@Positive`.
2. THE DTO `ActualizarItemRequest` SHALL declarar `cantidad` como `@Positive`.
3. IF el Backend recibe un DTO con campos inválidos, THEN THE Backend SHALL retornar HTTP 400 con un mensaje que liste los campos inválidos y sus restricciones.
4. THE Backend SHALL usar la anotación `@Valid` en los parámetros de los métodos del Controlador que reciban DTOs de entrada.

---

### Requerimiento 7: Pruebas Automatizadas

**Historia de Usuario:** Como desarrollador, quiero que el backend incluya pruebas automatizadas, para garantizar la correctitud de la lógica de negocio.

#### Criterios de Aceptación

1. THE Backend SHALL incluir pruebas unitarias para `ProductoServicio`, `CarritoServicio` y `VentaServicio` usando JUnit 5 y Mockito, con cobertura mínima del 80% en la capa de aplicación.
2. THE Backend SHALL incluir pruebas de integración para `ProductoControlador`, `CarritoControlador` y `VentaControlador` usando `@WebMvcTest` y `MockMvc`.
3. WHEN se ejecuta `mvn test`, THE Backend SHALL ejecutar todas las pruebas y reportar los resultados sin errores de compilación.
4. THE Pruebas_Unitarias SHALL usar Mockito para simular los Puertos_Salida, verificando que los Casos_de_Uso invocan los métodos correctos con los parámetros esperados.
