# Implementation Plan: Frontend Sistema POS

## Overview

Lista de tareas de implementación del frontend derivadas del design.md, organizadas en fases de ejecución. Cada tarea es trazable a un requisito funcional del requirements.md.

## Tasks

### Fase 1: Configuración del proyecto

- [x] 1. Inicializar proyecto Node.js con package.json
  - Instalar dependencias: express, express-handlebars, express-session, axios, dotenv
  - Configurar .env con API_BASE_URL
  - Configurar .gitignore (node_modules, .env)
  - **Trazabilidad:** RNF-01

- [x] 2. Configurar Express y Handlebars en app.js
  - Configurar motor de vistas Handlebars con helpers personalizados
  - Configurar express-session para manejo de carrito
  - Configurar middleware express.json y express.urlencoded
  - Configurar carpeta public para archivos estáticos
  - **Trazabilidad:** RNF-01, RF-02

### Fase 2: Layout y estilos

- [x] 3. Crear layout base (views/layouts/main.hbs)
  - Navbar con links: Punto de Venta, Historial, Nuevo Producto
  - Incluir Bootstrap 5 desde CDN
  - Incluir estilos.css y main.js
  - Usar etiquetas semánticas: nav, main
  - **Trazabilidad:** RNF-03, RF-04

- [x] 4. Crear estilos CSS (public/css/estilos.css)
  - Tema oscuro con variables CSS (background #0d0d1a, verde #00ff88)
  - Estilos para pos-card, pos-table, btn-add, btn-confirm
  - Flexbox para cart-item-controls
  - Grid para layout principal (col-lg-8 / col-lg-4)
  - **Trazabilidad:** RNF-04

### Fase 3: Vista principal POS

- [x] 5. Implementar input único inteligente (public/js/main.js)
  - Detecta código de barras (numérico) vs nombre (texto)
  - fetch GET /buscar?q= con async/await y try/catch
  - Si 1 resultado: agregar directo al carrito
  - Si múltiples: mostrar tabla de resultados
  - **Trazabilidad:** RF-01

- [x] 6. Implementar carrito de compras (views/index.hbs)
  - Mostrar items con nombre, precio unitario y subtotal
  - Botones +/- para modificar cantidad
  - Botón X para eliminar producto
  - Total general visible
  - **Trazabilidad:** RF-02

- [x] 7. Implementar modales de cobro
  - Modal F4: cobro efectivo con monto recibido y cálculo de cambio
  - Modal F2: selección de método de pago (Nequi, Davivienda, Daviplata, Transferencia)
  - Modal de resultados de búsqueda
  - **Trazabilidad:** RF-03

- [x] 8. Implementar atajos de teclado (public/js/main.js)
  - F2: abrir modal método de pago
  - F3: vaciar carrito
  - F4: abrir modal efectivo
  - F5: enfocar input de búsqueda
  - F6: ir a historial
  - F7: ir a crear producto
  - ESC: cerrar modal
  - **Trazabilidad:** RF-04

### Fase 4: Rutas del servidor

- [x] 9. Implementar rutas en app.js
  - GET / → vista principal sin carga inicial de productos
  - GET /buscar → proxy a API Gateway GET /productos
  - POST /carrito/agregar-ajax → agrega producto al carrito en sesión
  - POST /carrito/items/:id/actualizar → actualiza cantidad
  - POST /carrito/items/:id/eliminar → elimina producto
  - POST /carrito/vaciar → vacía el carrito
  - POST /ventas → proxy a API Gateway POST /ventas
  - **Trazabilidad:** RF-01, RF-02, RF-03

### Fase 5: Vistas adicionales

- [x] 10. Implementar historial de ventas (views/historial.hbs)
  - GET /historial → consumir GET /ventas del API
  - Mostrar ventas con id, fecha, total, metodoPago
  - Manejo de error si el API falla
  - **Trazabilidad:** RF-05

- [x] 11. Implementar crear producto (views/crear-producto.hbs)
  - Formulario con nombre, codigo_barras, precio, stock
  - POST /productos/crear → proxy a API Gateway POST /productos
  - Mensaje de éxito o error
  - **Trazabilidad:** RF-06

### Fase 6: Documentación y publicación

- [x] 12. Crear README.md
  - Descripción de arquitectura cliente-servidor
  - Justificación del framework
  - Instrucciones: npm install, configurar .env, node app.js
  - Capturas del sistema funcionando
  - Sección SDD
  - **Trazabilidad:** Entregable del parcial

- [ ] 13. Subir repositorio a GitHub
  - Verificar .gitignore (node_modules, .env)
  - Crear repositorio público
  - Subir código con specs y README
  - Verificar que no hay credenciales expuestas
  - **Trazabilidad:** Entregable del parcial

## Task Dependency Graph

```json
{
  "waves": [
    { "wave": 0, "tasks": ["1"] },
    { "wave": 1, "tasks": ["2"] },
    { "wave": 2, "tasks": ["3", "4"] },
    { "wave": 3, "tasks": ["5", "6", "7", "8"] },
    { "wave": 4, "tasks": ["9"] },
    { "wave": 5, "tasks": ["10", "11"] },
    { "wave": 6, "tasks": ["12", "13"] }
  ]
}
```

## Notes

- La URL del API Gateway nunca debe hardcodearse — siempre desde `process.env.API_BASE_URL`
- El carrito vive en `express-session`, no en DynamoDB ni localStorage
- Los specs en `.kiro/specs/pos-frontend/` deben estar completos antes de implementar
- No subir `node_modules/` ni `.env` al repositorio
