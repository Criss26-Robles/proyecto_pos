# Requerimientos — Frontend POS (Node.js + Express.js)

## Introducción

El Frontend del Sistema POS es una aplicación web desarrollada en **Node.js** con **Express.js**. Sirve la interfaz de usuario al Operador de caja y consume la API REST del Backend Java. Aplica separación de responsabilidades (módulo de cliente HTTP separado de la lógica de presentación), principios SOLID e inversión de dependencias mediante módulos desacoplados.

---

## Glosario

- **Frontend**: Módulo Node.js + Express.js que sirve la interfaz web y consume la API_REST del Backend.
- **Operador**: Usuario del sistema que opera la caja registradora (cajero).
- **API_REST**: Interfaz HTTP/JSON del Backend consumida por el Frontend.
- **Cliente_HTTP**: Módulo centralizado del Frontend responsable de todas las llamadas a la API_REST.
- **Vista**: Plantilla HTML renderizada por el motor de plantillas (Handlebars o EJS).
- **Controlador_Frontend**: Módulo Express.js que maneja las rutas y delega en el Cliente_HTTP.
- **Carrito_Vista**: Representación visual del estado actual del Carrito en la interfaz.
- **Producto_Vista**: Representación visual de un Producto en el catálogo.
- **Venta_Vista**: Representación visual de una Venta confirmada o del historial.
- **Motor_Plantillas**: Motor de renderizado de vistas HTML (Handlebars `hbs` o EJS).
- **Variable_Entorno**: Configuración externalizada en archivo `.env` (ej. `BACKEND_URL`).

---

## Tecnologías y Dependencias

| Tecnología | Versión | Propósito |
|---|---|---|
| Node.js | 20.x LTS | Entorno de ejecución |
| Express.js | 4.18.x | Framework web |
| Handlebars (hbs) | 4.x | Motor de plantillas HTML |
| Axios | 1.6.x | Cliente HTTP para consumir la API REST |
| dotenv | 16.x | Carga de variables de entorno |
| nodemon | 3.x | Recarga automática en desarrollo |
| Bootstrap | 5.3.x (CDN) | Estilos CSS responsivos |

---

## Estructura de Carpetas

```
pos_frontend/
├── package.json
├── .env
├── .env.example
├── src/
│   ├── app.js                  ← Punto de entrada Express
│   ├── config/
│   │   └── config.js           ← Configuración centralizada (BACKEND_URL, PORT)
│   ├── servicios/
│   │   ├── productoServicio.js ← Cliente HTTP para /api/productos
│   │   ├── carritoServicio.js  ← Cliente HTTP para /api/carrito
│   │   └── ventaServicio.js    ← Cliente HTTP para /api/ventas
│   └── rutas/
│       ├── productoRutas.js    ← Rutas Express para catálogo
│       ├── carritoRutas.js     ← Rutas Express para carrito
│       └── ventaRutas.js       ← Rutas Express para ventas
├── views/
│   ├── layouts/
│   │   └── main.hbs            ← Layout principal con navbar
│   ├── index.hbs               ← Página principal (catálogo + carrito)
│   ├── historial.hbs           ← Historial de ventas
│   ├── venta-detalle.hbs       ← Detalle de una venta
│   └── error.hbs               ← Página de error genérica
└── public/
    ├── css/
    │   └── estilos.css         ← Estilos personalizados
    └── js/
        └── main.js             ← JavaScript del cliente (interacciones dinámicas)
```

---

## Requerimientos Funcionales

### Requerimiento 1: Visualización del Catálogo de Productos

**Historia de Usuario:** Como Operador, quiero ver el catálogo de productos en la interfaz web, para seleccionar los artículos que deseo vender.

#### Criterios de Aceptación

1. WHEN el Operador accede a `GET /` (página principal), THE Frontend SHALL consultar `GET {BACKEND_URL}/api/productos` y renderizar la lista de productos en la vista `index.hbs`.
2. THE Frontend SHALL mostrar para cada Producto_Vista: código, nombre, precio unitario formateado como moneda y stock disponible.
3. WHEN el Operador ingresa texto en el campo de búsqueda y envía el formulario, THE Frontend SHALL consultar `GET {BACKEND_URL}/api/productos?nombre={texto}` y re-renderizar el catálogo con los resultados filtrados.
4. IF el Backend retorna un error al consultar el catálogo, THEN THE Frontend SHALL renderizar la vista `error.hbs` con el mensaje "No fue posible cargar el catálogo de productos. Intente nuevamente."
5. THE Frontend SHALL presentar el catálogo en una tabla HTML con encabezados: "Código", "Nombre", "Precio", "Stock", "Acción".

---

### Requerimiento 2: Gestión del Carrito desde la Interfaz

**Historia de Usuario:** Como Operador, quiero gestionar el carrito de compras desde la interfaz web, para agregar, modificar y eliminar productos antes de confirmar la venta.

#### Criterios de Aceptación

1. WHEN el Operador hace clic en "Agregar al carrito" de un Producto_Vista, THE Frontend SHALL enviar `POST /carrito/items` (ruta interna Express) que delegue en `POST {BACKEND_URL}/api/carrito/items` con el `productoId` y cantidad 1, y redirigir a la página principal con el Carrito_Vista actualizado.
2. WHEN el Operador modifica la cantidad de un Item en el Carrito_Vista y confirma, THE Frontend SHALL enviar `POST /carrito/items/{productoId}/actualizar` que delegue en `PUT {BACKEND_URL}/api/carrito/items/{productoId}` con la nueva cantidad.
3. WHEN el Operador hace clic en "Eliminar" de un Item del Carrito_Vista, THE Frontend SHALL enviar `POST /carrito/items/{productoId}/eliminar` que delegue en `DELETE {BACKEND_URL}/api/carrito/items/{productoId}`.
4. WHEN el Operador hace clic en "Vaciar carrito", THE Frontend SHALL enviar `POST /carrito/vaciar` que delegue en `DELETE {BACKEND_URL}/api/carrito`.
5. THE Frontend SHALL mostrar el Carrito_Vista en la misma página del catálogo con: nombre del producto, cantidad, precio unitario, subtotal por ítem y total acumulado.
6. IF el Backend retorna HTTP 400 o HTTP 404 al operar el carrito, THEN THE Frontend SHALL mostrar el mensaje de error recibido en la página actual sin redirigir.
7. THE Frontend SHALL actualizar el Carrito_Vista después de cada operación exitosa consultando `GET {BACKEND_URL}/api/carrito`.

---

### Requerimiento 3: Confirmación de Venta

**Historia de Usuario:** Como Operador, quiero confirmar la venta desde la interfaz web, para registrar la transacción y obtener el comprobante.

#### Criterios de Aceptación

1. WHEN el Operador hace clic en "Confirmar Venta", THE Frontend SHALL enviar `POST /ventas` (ruta interna Express) que delegue en `POST {BACKEND_URL}/api/ventas`.
2. WHEN el Backend retorna HTTP 201, THE Frontend SHALL redirigir a una vista de confirmación mostrando: ID de venta, lista de productos vendidos con cantidades y subtotales, y total de la venta.
3. WHEN la venta es confirmada exitosamente, THE Frontend SHALL mostrar el Carrito_Vista vacío al regresar a la página principal.
4. IF el Backend retorna HTTP 400 (carrito vacío), THEN THE Frontend SHALL mostrar el mensaje de error en la página principal sin redirigir.
5. IF el Backend retorna un error al confirmar la venta, THEN THE Frontend SHALL mostrar el mensaje "La venta no pudo ser procesada. Intente nuevamente." en la página actual.
6. THE Frontend SHALL deshabilitar el botón "Confirmar Venta" (atributo `disabled`) cuando el Carrito_Vista no tenga ítems.

---

### Requerimiento 4: Historial de Ventas

**Historia de Usuario:** Como Operador, quiero consultar el historial de ventas realizadas, para tener registro de las transacciones del día.

#### Criterios de Aceptación

1. WHEN el Operador accede a `GET /historial`, THE Frontend SHALL consultar `GET {BACKEND_URL}/api/ventas` y renderizar la lista de ventas en la vista `historial.hbs`.
2. THE Frontend SHALL mostrar para cada Venta_Vista en el historial: ID (primeros 8 caracteres del UUID), fecha y hora formateada y total formateado como moneda.
3. WHEN el Operador hace clic en una Venta del historial, THE Frontend SHALL acceder a `GET /historial/{id}` que consulte `GET {BACKEND_URL}/api/ventas/{id}` y renderice la vista `venta-detalle.hbs` con el detalle completo.
4. IF no existen ventas registradas, THEN THE Frontend SHALL mostrar el mensaje "No hay ventas registradas aún." en la vista `historial.hbs`.
5. IF el Backend retorna un error al consultar el historial, THEN THE Frontend SHALL renderizar la vista `error.hbs` con un mensaje descriptivo.

---

### Requerimiento 5: Comunicación con el Backend

**Historia de Usuario:** Como desarrollador, quiero que el frontend consuma la API REST del backend de forma robusta y centralizada, para garantizar mantenibilidad y consistencia.

#### Criterios de Aceptación

1. THE Frontend SHALL centralizar todas las llamadas HTTP al Backend en los módulos `productoServicio.js`, `carritoServicio.js` y `ventaServicio.js` dentro de `src/servicios/`.
2. THE Frontend SHALL leer la URL base del Backend desde la Variable_Entorno `BACKEND_URL` definida en el archivo `.env`, con valor por defecto `http://localhost:8080`.
3. WHEN el Backend no está disponible o retorna un error de red, THE Frontend SHALL capturar la excepción y renderizar la vista `error.hbs` con el mensaje "El servicio no está disponible. Verifique que el backend esté en ejecución."
4. THE Frontend SHALL manejar los códigos HTTP 400, 404 y 500 del Backend extrayendo el mensaje de error de la respuesta JSON y pasándolo a la vista correspondiente.
5. THE Frontend SHALL ejecutarse en el puerto `3000` configurado mediante la Variable_Entorno `PORT` (valor por defecto: `3000`).
6. THE Frontend SHALL usar el Motor_Plantillas Handlebars (`hbs`) para renderizar todas las vistas del servidor.

---

### Requerimiento 6: Estructura y Configuración del Proyecto

**Historia de Usuario:** Como desarrollador, quiero que el frontend tenga una estructura clara y configuración estándar, para facilitar el desarrollo y mantenimiento.

#### Criterios de Aceptación

1. THE Frontend SHALL incluir un `package.json` con los scripts: `"start": "node src/app.js"` y `"dev": "nodemon src/app.js"`.
2. THE Frontend SHALL incluir un archivo `.env.example` con las variables: `PORT=3000` y `BACKEND_URL=http://localhost:8080`.
3. THE Frontend SHALL incluir un archivo `.gitignore` que excluya `node_modules/` y `.env`.
4. THE Frontend SHALL servir archivos estáticos (CSS, JS del cliente) desde la carpeta `public/` mediante `express.static`.
5. THE Frontend SHALL incluir un layout principal `views/layouts/main.hbs` con: barra de navegación con enlaces a "Punto de Venta" e "Historial de Ventas", y bloque de contenido dinámico.
6. THE Frontend SHALL aplicar Bootstrap 5 (CDN) para los estilos de la interfaz, garantizando una presentación responsiva y profesional.

---

### Requerimiento 7: Calidad y Separación de Responsabilidades

**Historia de Usuario:** Como desarrollador, quiero que el frontend aplique separación de responsabilidades y principios SOLID, para facilitar su evolución y mantenimiento.

#### Criterios de Aceptación

1. THE Frontend SHALL separar la lógica de comunicación con la API (módulos `servicios/`) de la lógica de enrutamiento Express (módulos `rutas/`) y de la presentación (módulos `views/`).
2. THE Controlador_Frontend SHALL depender de los módulos de `servicios/` a través de sus interfaces exportadas, sin instanciar clientes HTTP directamente en las rutas.
3. THE Frontend SHALL incluir comentarios JSDoc en los módulos de `servicios/` describiendo cada función, sus parámetros y el valor retornado.
4. THE Frontend SHALL manejar todos los errores de las llamadas HTTP con bloques `try/catch` y propagar mensajes de error descriptivos a las vistas.
5. THE Frontend SHALL usar `async/await` para todas las operaciones asíncronas de comunicación con el Backend.
