# Tasks — Frontend Sistema POS

## Descripcion
Lista de tareas de implementacion del frontend, derivadas del design.md.

---

## Fase 1: Configuracion

### TASK-01: Configurar proyecto Node.js + Express
- [x] Inicializar proyecto con package.json
- [x] Instalar dependencias: express, express-handlebars, express-session, axios, dotenv
- [x] Configurar archivo .env con API_BASE_URL
- [x] Configurar .gitignore (node_modules, .env)

### TASK-02: Configurar Express y Handlebars
- [x] Configurar motor de vistas Handlebars en app.js
- [x] Configurar express-session para manejo de carrito
- [x] Configurar middleware express.json y express.urlencoded
- [x] Configurar carpeta public para archivos estaticos

---

## Fase 2: Layout y Estilos

### TASK-03: Crear layout base (main.hbs)
- [x] Navbar con links: Punto de Venta, Historial, Nuevo Producto
- [x] Incluir Bootstrap 5 desde CDN
- [x] Incluir estilos.css y main.js

### TASK-04: Crear estilos CSS (estilos.css)
- [x] Tema oscuro con variables CSS
- [x] Estilos para pos-card, pos-table, btn-add, btn-confirm
- [x] Estilos para carrito y modales
- [x] Flexbox para cart-item-controls
- [x] stock-badge con color verde y stock-low en naranja

---

## Fase 3: Vista Principal POS

### TASK-05: Implementar input unico inteligente
- [x] Input que detecta codigo de barras (numerico) vs nombre (texto)
- [x] Busqueda con fetch GET /buscar?q= usando async/await
- [x] Si 1 resultado: agregar directo al carrito
- [x] Si multiples: mostrar tabla de resultados
- [x] Manejo de errores con try/catch

### TASK-06: Implementar carrito de compras
- [x] Mostrar items con nombre, precio unitario y subtotal
- [x] Botones +/- para modificar cantidad
- [x] Boton X para eliminar producto
- [x] Total general visible
- [x] Sesion server-side con express-session

### TASK-07: Implementar modales de cobro
- [x] Modal F4: cobro efectivo con monto recibido y calculo de cambio
- [x] Modal F2: seleccion de metodo de pago
- [x] Modal de confirmacion antes de procesar venta
- [x] Modal de resultados de busqueda

### TASK-08: Implementar atajos de teclado
- [x] F2: abrir modal metodo de pago
- [x] F3: vaciar carrito
- [x] F4: abrir modal efectivo
- [x] F5: enfocar input de busqueda
- [x] F6: ir a historial
- [x] F7: ir a crear producto
- [x] ESC: cerrar modal o volver al inicio

---

## Fase 4: Rutas del Servidor

### TASK-09: Implementar rutas en app.js
- [x] GET / -> vista principal sin carga inicial de productos
- [x] GET /buscar -> proxy a API Gateway GET /productos
- [x] POST /carrito/agregar-ajax -> agrega producto al carrito
- [x] POST /carrito/items/:id/actualizar -> actualiza cantidad
- [x] POST /carrito/items/:id/eliminar -> elimina producto
- [x] POST /carrito/vaciar -> vacia el carrito
- [x] POST /ventas -> proxy a API Gateway POST /ventas

---

## Fase 5: Vistas Adicionales

### TASK-10: Implementar historial de ventas
- [x] GET /historial -> consumir GET /ventas del API
- [x] Mostrar ventas con id, fecha, total, metodoPago

### TASK-11: Implementar crear producto
- [x] Formulario con nombre, codigo_barras, precio, stock
- [x] POST /productos/crear -> proxy a API Gateway POST /productos
- [x] Mensaje de exito o error

---

## Fase 6: Documentacion y GitHub

### TASK-12: Crear README.md
- [x] Descripcion de arquitectura cliente-servidor
- [x] Justificacion del framework
- [x] Instrucciones: npm install, configurar .env, node app.js
- [x] Capturas del sistema funcionando
- [x] Seccion SDD

### TASK-13: Subir repositorio a GitHub
- [ ] Crear .gitignore (node_modules, .env)
- [ ] Subir codigo a repositorio publico
- [ ] Verificar que no hay credenciales expuestas
