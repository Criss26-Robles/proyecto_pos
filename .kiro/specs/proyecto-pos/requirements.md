# Documento de Requerimientos — Sistema POS (Punto de Venta)

## Introducción

El Sistema POS (Point of Sale / Punto de Venta) es una aplicación de dos capas compuesta por un **backend** desarrollado en Java con Spring Boot y un **frontend** desarrollado en Node.js con Express.js. El backend expone una API REST y aplica arquitectura hexagonal (Ports & Adapters), principios SOLID e inversión de dependencias. El frontend consume dicha API REST y presenta la interfaz de usuario al operador de caja. La base de datos es estática (datos en memoria / mocks) para esta fase del proyecto.

---

## Glosario

- **Sistema_POS**: El sistema completo de punto de venta, compuesto por backend y frontend.
- **Backend**: Módulo Java + Spring Boot que contiene la lógica de negocio y expone la API REST.
- **Frontend**: Módulo Node.js + Express.js que sirve la interfaz web y consume la API REST del Backend.
- **API_REST**: Interfaz de comunicación HTTP/JSON entre Frontend y Backend.
- **Producto**: Artículo disponible para la venta, identificado por código, nombre, precio y stock.
- **Carrito**: Colección temporal de ítems seleccionados por el operador antes de confirmar una venta.
- **Item_Carrito**: Unidad dentro del Carrito que asocia un Producto con una cantidad.
- **Venta**: Transacción comercial confirmada que registra los productos vendidos, cantidades, subtotales y total.
- **Operador**: Usuario del sistema que opera la caja registradora (cajero).
- **Repositorio**: Componente de la capa de dominio que abstrae el acceso a datos (puerto de salida en arquitectura hexagonal).
- **Caso_de_Uso**: Componente de la capa de aplicación que orquesta la lógica de negocio.
- **Controlador**: Adaptador de entrada HTTP que recibe peticiones REST y delega en los Casos_de_Uso.
- **Datos_en_Memoria**: Implementación del Repositorio que almacena datos en estructuras en memoria (sin base de datos real).
- **DTO**: Objeto de transferencia de datos usado en la API REST.
- **Puerto**: Interfaz Java que define el contrato entre capas en la arquitectura hexagonal.
- **Adaptador**: Implementación concreta de un Puerto.

---

## Requerimientos

---

### Requerimiento 1: Gestión del Catálogo de Productos (Backend)

**Historia de Usuario:** Como Operador, quiero consultar el catálogo de productos disponibles, para poder seleccionar artículos al momento de realizar una venta.

#### Criterios de Aceptación

1. THE Backend SHALL exponer un endpoint `GET /api/productos` que retorne la lista completa de productos disponibles en formato JSON.
2. WHEN el Backend recibe una petición `GET /api/productos/{id}`, THE Backend SHALL retornar el Producto correspondiente al identificador proporcionado en formato JSON.
3. IF el identificador proporcionado en `GET /api/productos/{id}` no corresponde a ningún Producto registrado, THEN THE Backend SHALL retornar una respuesta HTTP 404 con un mensaje de error descriptivo.
4. THE Backend SHALL inicializar el catálogo de Productos con al menos diez (10) productos de ejemplo en los Datos_en_Memoria al arrancar la aplicación.
5. WHEN el Backend recibe una petición `GET /api/productos` con un parámetro de búsqueda `nombre`, THE Backend SHALL retornar únicamente los Productos cuyo nombre contenga el texto proporcionado (búsqueda insensible a mayúsculas/minúsculas).
6. THE Repositorio SHALL exponer un Puerto de salida con los métodos `findAll()`, `findById(id)` y `findByNombre(nombre)` para desacoplar la lógica de negocio del mecanismo de almacenamiento.
7. THE Datos_en_Memoria SHALL implementar el Puerto del Repositorio de Productos sin depender de ninguna tecnología de base de datos externa.

---

### Requerimiento 2: Gestión del Carrito de Compras (Backend)

**Historia de Usuario:** Como Operador, quiero agregar, modificar y eliminar productos en un carrito de compras, para preparar la venta antes de confirmarla.

#### Criterios de Aceptación

1. WHEN el Backend recibe una petición `POST /api/carrito/items` con un identificador de Producto y una cantidad válida, THE Backend SHALL agregar el Item_Carrito correspondiente al Carrito activo de la sesión.
2. WHEN el Backend recibe una petición `PUT /api/carrito/items/{productoId}` con una nueva cantidad, THE Backend SHALL actualizar la cantidad del Item_Carrito correspondiente en el Carrito activo.
3. WHEN el Backend recibe una petición `DELETE /api/carrito/items/{productoId}`, THE Backend SHALL eliminar el Item_Carrito correspondiente del Carrito activo.
4. WHEN el Backend recibe una petición `GET /api/carrito`, THE Backend SHALL retornar el estado actual del Carrito activo, incluyendo todos los Items_Carrito con sus subtotales calculados y el total acumulado.
5. WHEN el Backend recibe una petición `DELETE /api/carrito`, THE Backend SHALL vaciar completamente el Carrito activo, eliminando todos los Items_Carrito.
6. IF el identificador de Producto proporcionado en `POST /api/carrito/items` no existe en el catálogo, THEN THE Backend SHALL retornar una respuesta HTTP 404 con un mensaje de error descriptivo.
7. IF la cantidad proporcionada en `POST /api/carrito/items` o `PUT /api/carrito/items/{productoId}` es menor o igual a cero, THEN THE Backend SHALL retornar una respuesta HTTP 400 con un mensaje de error descriptivo.
8. THE Backend SHALL calcular el subtotal de cada Item_Carrito multiplicando el precio unitario del Producto por la cantidad indicada.
9. THE Backend SHALL calcular el total del Carrito sumando los subtotales de todos los Items_Carrito presentes.
10. THE Caso_de_Uso de gestión del Carrito SHALL depender únicamente del Puerto del Repositorio de Productos y del Puerto del Repositorio del Carrito, sin referencias directas a implementaciones concretas.

---

### Requerimiento 3: Procesamiento de Ventas (Backend)

**Historia de Usuario:** Como Operador, quiero confirmar el carrito de compras para registrar una venta, para que quede constancia de la transacción realizada.

#### Criterios de Aceptación

1. WHEN el Backend recibe una petición `POST /api/ventas` con el Carrito activo no vacío, THE Backend SHALL crear un registro de Venta con los Items_Carrito actuales, el total calculado y la marca de tiempo de la transacción.
2. WHEN una Venta es confirmada exitosamente, THE Backend SHALL retornar una respuesta HTTP 201 con el detalle completo de la Venta registrada en formato JSON.
3. WHEN una Venta es confirmada exitosamente, THE Backend SHALL vaciar el Carrito activo automáticamente.
4. IF el Carrito activo está vacío al recibir `POST /api/ventas`, THEN THE Backend SHALL retornar una respuesta HTTP 400 con el mensaje "El carrito está vacío. Agregue productos antes de confirmar la venta."
5. WHEN el Backend recibe una petición `GET /api/ventas`, THE Backend SHALL retornar la lista de todas las Ventas registradas en los Datos_en_Memoria en formato JSON.
6. WHEN el Backend recibe una petición `GET /api/ventas/{id}`, THE Backend SHALL retornar el detalle completo de la Venta correspondiente al identificador proporcionado.
7. IF el identificador proporcionado en `GET /api/ventas/{id}` no corresponde a ninguna Venta registrada, THEN THE Backend SHALL retornar una respuesta HTTP 404 con un mensaje de error descriptivo.
8. THE Backend SHALL asignar un identificador único (UUID) a cada Venta en el momento de su creación.
9. THE Caso_de_Uso de procesamiento de Ventas SHALL depender únicamente de los Puertos definidos, sin referencias directas a implementaciones concretas de persistencia.

---

### Requerimiento 4: Arquitectura Hexagonal del Backend

**Historia de Usuario:** Como desarrollador, quiero que el backend aplique arquitectura hexagonal (Ports & Adapters), para garantizar la separación de responsabilidades, la testabilidad y la mantenibilidad del código.

#### Criterios de Aceptación

1. THE Backend SHALL organizar su código en tres capas claramente separadas: `dominio` (entidades y puertos), `aplicacion` (casos de uso) y `infraestructura` (adaptadores, controladores y configuración).
2. THE Backend SHALL definir Puertos de entrada como interfaces Java en la capa de `aplicacion` para cada Caso_de_Uso expuesto.
3. THE Backend SHALL definir Puertos de salida como interfaces Java en la capa de `dominio` para cada operación de persistencia requerida.
4. THE Controlador SHALL implementar el Adaptador de entrada HTTP y depender únicamente de los Puertos de entrada definidos en la capa de `aplicacion`.
5. THE Datos_en_Memoria SHALL implementar los Puertos de salida definidos en la capa de `dominio` sin que la capa de `dominio` conozca la implementación concreta.
6. THE Backend SHALL utilizar inyección de dependencias de Spring para vincular los Puertos con sus Adaptadores correspondientes, sin instanciación directa (`new`) en las capas de dominio y aplicación.
7. THE Backend SHALL aplicar el Principio de Responsabilidad Única (SRP): cada clase tendrá una única razón para cambiar.
8. THE Backend SHALL aplicar el Principio de Inversión de Dependencias (DIP): las capas de alto nivel (dominio, aplicación) no dependerán de las capas de bajo nivel (infraestructura).
9. THE Backend SHALL aplicar el Principio Abierto/Cerrado (OCP): las entidades de dominio y los casos de uso serán extensibles sin modificar su código existente.

---

### Requerimiento 5: API REST del Backend

**Historia de Usuario:** Como desarrollador del frontend, quiero que el backend exponga una API REST bien definida, para poder consumirla de forma predecible y documentada.

#### Criterios de Aceptación

1. THE API_REST SHALL utilizar el formato JSON para todas las peticiones y respuestas.
2. THE API_REST SHALL retornar códigos de estado HTTP semánticamente correctos: 200 (OK), 201 (Created), 400 (Bad Request), 404 (Not Found), 500 (Internal Server Error).
3. THE Backend SHALL habilitar CORS para permitir peticiones desde el origen del Frontend (`http://localhost:3000`) durante el desarrollo.
4. THE API_REST SHALL exponer los siguientes endpoints:
   - `GET    /api/productos`
   - `GET    /api/productos/{id}`
   - `GET    /api/carrito`
   - `POST   /api/carrito/items`
   - `PUT    /api/carrito/items/{productoId}`
   - `DELETE /api/carrito/items/{productoId}`
   - `DELETE /api/carrito`
   - `POST   /api/ventas`
   - `GET    /api/ventas`
   - `GET    /api/ventas/{id}`
5. IF el Backend recibe una petición a un endpoint no definido, THEN THE API_REST SHALL retornar una respuesta HTTP 404 con un mensaje de error descriptivo.
6. IF ocurre un error interno no controlado en el Backend, THEN THE API_REST SHALL retornar una respuesta HTTP 500 con un mensaje de error genérico sin exponer detalles internos del sistema.
7. THE Backend SHALL ejecutarse en el puerto `8080` por defecto.

---

### Requerimiento 6: Interfaz de Usuario — Catálogo de Productos (Frontend)

**Historia de Usuario:** Como Operador, quiero ver el catálogo de productos en la interfaz web, para poder seleccionar los artículos que deseo vender.

#### Criterios de Aceptación

1. WHEN el Operador accede a la página principal del Frontend, THE Frontend SHALL mostrar la lista completa de productos obtenida desde `GET /api/productos` del Backend.
2. THE Frontend SHALL mostrar para cada Producto: código, nombre, precio unitario y stock disponible.
3. WHEN el Operador ingresa texto en el campo de búsqueda de productos, THE Frontend SHALL filtrar y mostrar únicamente los Productos cuyo nombre contenga el texto ingresado, consultando `GET /api/productos?nombre={texto}`.
4. IF el Backend retorna un error al consultar el catálogo de productos, THEN THE Frontend SHALL mostrar un mensaje de error visible al Operador indicando que no fue posible cargar el catálogo.
5. THE Frontend SHALL presentar el catálogo de productos en formato de tabla o cuadrícula con encabezados claramente identificados.

---

### Requerimiento 7: Interfaz de Usuario — Gestión del Carrito (Frontend)

**Historia de Usuario:** Como Operador, quiero gestionar el carrito de compras desde la interfaz web, para agregar, modificar y eliminar productos antes de confirmar la venta.

#### Criterios de Aceptación

1. WHEN el Operador hace clic en el botón "Agregar al carrito" de un Producto, THE Frontend SHALL enviar una petición `POST /api/carrito/items` al Backend con el identificador del Producto y la cantidad indicada (por defecto 1).
2. WHEN el Operador modifica la cantidad de un Item_Carrito en la interfaz, THE Frontend SHALL enviar una petición `PUT /api/carrito/items/{productoId}` al Backend con la nueva cantidad.
3. WHEN el Operador hace clic en el botón "Eliminar" de un Item_Carrito, THE Frontend SHALL enviar una petición `DELETE /api/carrito/items/{productoId}` al Backend y actualizar la vista del carrito.
4. WHEN el Operador hace clic en el botón "Vaciar carrito", THE Frontend SHALL enviar una petición `DELETE /api/carrito` al Backend y limpiar la vista del carrito.
5. THE Frontend SHALL mostrar en todo momento el estado actualizado del Carrito, incluyendo: nombre del producto, cantidad, precio unitario, subtotal por ítem y total acumulado.
6. IF el Backend retorna un error al agregar un producto al carrito, THEN THE Frontend SHALL mostrar un mensaje de error visible al Operador con la descripción del problema.
7. THE Frontend SHALL actualizar la vista del Carrito después de cada operación exitosa (agregar, modificar, eliminar, vaciar) consultando `GET /api/carrito`.

---

### Requerimiento 8: Interfaz de Usuario — Confirmación de Venta (Frontend)

**Historia de Usuario:** Como Operador, quiero confirmar la venta desde la interfaz web, para registrar la transacción y obtener el comprobante.

#### Criterios de Aceptación

1. WHEN el Operador hace clic en el botón "Confirmar Venta", THE Frontend SHALL enviar una petición `POST /api/ventas` al Backend.
2. WHEN el Backend retorna una respuesta HTTP 201 tras confirmar la venta, THE Frontend SHALL mostrar un resumen de la Venta registrada con: identificador de venta, lista de productos vendidos, cantidades, subtotales y total.
3. WHEN la venta es confirmada exitosamente, THE Frontend SHALL limpiar la vista del Carrito y mostrar el estado vacío.
4. IF el Backend retorna HTTP 400 al intentar confirmar la venta (carrito vacío), THEN THE Frontend SHALL mostrar el mensaje de error recibido al Operador sin redirigir la página.
5. IF el Backend retorna un error al confirmar la venta, THEN THE Frontend SHALL mostrar un mensaje de error visible al Operador indicando que la venta no pudo ser procesada.
6. THE Frontend SHALL deshabilitar el botón "Confirmar Venta" mientras el Carrito esté vacío para prevenir envíos innecesarios.

---

### Requerimiento 9: Historial de Ventas (Frontend)

**Historia de Usuario:** Como Operador, quiero consultar el historial de ventas realizadas, para tener registro de las transacciones del día.

#### Criterios de Aceptación

1. WHEN el Operador accede a la sección de historial de ventas, THE Frontend SHALL consultar `GET /api/ventas` y mostrar la lista de todas las Ventas registradas.
2. THE Frontend SHALL mostrar para cada Venta en el historial: identificador, fecha y hora de la transacción y total.
3. WHEN el Operador hace clic en una Venta del historial, THE Frontend SHALL consultar `GET /api/ventas/{id}` y mostrar el detalle completo de la Venta seleccionada.
4. IF no existen Ventas registradas, THEN THE Frontend SHALL mostrar el mensaje "No hay ventas registradas aún." en la sección de historial.
5. IF el Backend retorna un error al consultar el historial, THEN THE Frontend SHALL mostrar un mensaje de error visible al Operador.

---

### Requerimiento 10: Comunicación Frontend–Backend

**Historia de Usuario:** Como desarrollador, quiero que el frontend consuma la API REST del backend de forma robusta, para garantizar una experiencia de usuario fluida y sin errores silenciosos.

#### Criterios de Aceptación

1. THE Frontend SHALL utilizar un módulo centralizado de cliente HTTP para todas las llamadas a la API_REST del Backend, evitando duplicación de lógica de comunicación.
2. THE Frontend SHALL configurar la URL base del Backend (`http://localhost:8080`) en un único archivo de configuración o variable de entorno.
3. WHEN el Backend no está disponible o retorna un error de red, THE Frontend SHALL mostrar un mensaje de error genérico al Operador indicando que el servicio no está disponible.
4. THE Frontend SHALL manejar los códigos de estado HTTP 400, 404 y 500 del Backend mostrando mensajes de error apropiados al Operador.
5. THE Frontend SHALL ejecutarse en el puerto `3000` por defecto.
6. THE Frontend SHALL servir las vistas HTML mediante Express.js utilizando un motor de plantillas (Handlebars o EJS).

---

### Requerimiento 11: Estructura del Proyecto y Configuración

**Historia de Usuario:** Como desarrollador, quiero que el proyecto tenga una estructura de carpetas clara y configuración estándar, para facilitar el desarrollo, la comprensión y el mantenimiento del código.

#### Criterios de Aceptación

1. THE Backend SHALL organizarse bajo la carpeta `pos_backend/` con la estructura de paquetes Maven estándar: `src/main/java/` y `src/test/java/`.
2. THE Frontend SHALL organizarse bajo la carpeta `pos_frontend/` con la estructura estándar de un proyecto Node.js: `package.json`, `src/`, `public/`, `views/`.
3. THE Backend SHALL incluir un archivo `pom.xml` con las dependencias de Spring Boot Web, Spring Boot DevTools y las dependencias de prueba (JUnit 5, Mockito).
4. THE Frontend SHALL incluir un archivo `package.json` con las dependencias de Express.js, el motor de plantillas seleccionado, Axios (o node-fetch) y nodemon para desarrollo.
5. THE Sistema_POS SHALL incluir un archivo `README.md` en la raíz del proyecto con instrucciones claras para levantar el backend y el frontend localmente.
6. THE Backend SHALL incluir un archivo `application.properties` con la configuración del puerto (`server.port=8080`) y el perfil de datos en memoria.
7. THE Frontend SHALL incluir un archivo `.env` (o equivalente) con la variable `BACKEND_URL=http://localhost:8080` para la configuración de la URL del Backend.

---

### Requerimiento 12: Calidad y Mantenibilidad del Código

**Historia de Usuario:** Como desarrollador, quiero que el código siga principios de calidad y buenas prácticas, para facilitar su evolución y mantenimiento futuro.

#### Criterios de Aceptación

1. THE Backend SHALL incluir pruebas unitarias para todos los Casos_de_Uso utilizando JUnit 5 y Mockito, con cobertura mínima del 80% en la capa de aplicación.
2. THE Backend SHALL incluir pruebas de integración para los Controladores utilizando `@WebMvcTest` de Spring Boot.
3. THE Frontend SHALL incluir comentarios de código en los módulos principales explicando la responsabilidad de cada componente.
4. THE Backend SHALL utilizar anotaciones de validación de Spring (`@Valid`, `@NotNull`, `@Positive`) en los DTOs de entrada para garantizar la integridad de los datos recibidos.
5. IF el Backend recibe un DTO de entrada con datos inválidos (campos nulos o valores fuera de rango), THEN THE Backend SHALL retornar una respuesta HTTP 400 con un mensaje descriptivo de los campos inválidos.
6. THE Backend SHALL registrar (log) todas las peticiones entrantes y los errores producidos utilizando SLF4J con Logback.
7. THE Frontend SHALL separar la lógica de negocio (llamadas a la API) de la lógica de presentación (renderizado de vistas) en módulos distintos.
