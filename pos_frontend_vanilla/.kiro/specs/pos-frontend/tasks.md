# Implementation Plan: Frontend Sistema POS Vanilla JS

## Overview

Lista de tareas de implementación del frontend Vanilla JS, derivadas del design.md. Cada tarea es trazable a un requisito del requirements.md. Los specs se escribieron antes del código siguiendo la metodología SDD.

## Tasks

### Fase 1: Configuración

- [x] 1. Crear estructura de carpetas y archivos base
  - `index.html`, `css/estilos.css`, `src/config.js`, `src/app.js`
  - `src/services/productosService.js`, `src/services/ventasService.js`
  - `.gitignore`, `README.md`
  - **Trazabilidad:** RNF-05

- [x] 2. Configurar URL del API Gateway en src/config.js
  - Exportar `API_BASE_URL` como constante
  - Nunca hardcodear la URL en los servicios
  - **Trazabilidad:** RNF-01

### Fase 2: HTML5 semántico

- [x] 3. Crear estructura HTML5 en index.html
  - `<header>` con título del sistema
  - `<main>` con dos `<section>`: productos y carrito
  - `<nav>` para navegación entre vistas
  - `<table>` para listado de productos
  - `<form>` para selección de método de pago
  - `<footer>` con información del sistema
  - **Trazabilidad:** RNF-03

### Fase 3: CSS propio

- [x] 4. Crear estilos en css/estilos.css
  - Variables CSS: `--bg-primary`, `--color-accent`, `--text-primary`
  - Tema oscuro: background `#0d0d1a`, acento verde `#00ff88`
  - Layout con CSS Grid: dos columnas (productos | carrito)
  - Flexbox para items del carrito
  - Box model: padding y margin explícitos en todos los componentes
  - Estilos para botones, tabla, mensajes de éxito/error
  - **Trazabilidad:** RNF-04

### Fase 4: Servicios JavaScript

- [x] 5. Implementar productosService.js
  - `listar()`: fetch GET `${API_BASE_URL}/productos`
  - async/await con try/catch
  - Lanza error si `!response.ok`
  - **Trazabilidad:** RF-01

- [x] 6. Implementar ventasService.js
  - `crear(payload)`: fetch POST `${API_BASE_URL}/ventas`
  - Headers: `Content-Type: application/json`
  - Body: `JSON.stringify(payload)`
  - async/await con try/catch
  - **Trazabilidad:** RF-03

### Fase 5: Lógica principal

- [x] 7. Implementar estado del carrito en app.js
  - Array `carrito = []` como estado en memoria
  - `agregarAlCarrito(producto)`: agrega o incrementa cantidad
  - `eliminarDelCarrito(productoId)`: filtra el array
  - `calcularTotal()`: reduce el carrito
  - **Trazabilidad:** RF-02

- [x] 8. Implementar renderizado de productos
  - `cargarProductos()`: llama productosService.listar()
  - `renderizarProductos(productos)`: genera HTML con map()
  - Event listeners en botones "Agregar"
  - Manejo de lista vacía y error
  - **Trazabilidad:** RF-01

- [x] 9. Implementar renderizado del carrito
  - `renderizarCarrito()`: genera HTML del carrito actualizado
  - Muestra nombre, cantidad, subtotal por item
  - Botón eliminar por item
  - Total general visible
  - **Trazabilidad:** RF-02

- [x] 10. Implementar confirmación de venta
  - `confirmarVenta()`: valida carrito, llama ventasService.crear()
  - Construye payload con productos, total, metodoPago, fecha
  - Mensaje de éxito → vacía carrito, recarga productos
  - Mensaje de error → muestra descripción
  - **Trazabilidad:** RF-03, RF-04

- [x] 11. Implementar notificaciones al usuario
  - `mostrarMensaje(texto, tipo)`: muestra div con clase success/error
  - Auto-oculta después de 4 segundos
  - **Trazabilidad:** RF-03, RNF-02

### Fase 6: Documentación y GitHub

- [x] 12. Crear README.md
  - Arquitectura cliente-servidor
  - Justificación de Vanilla JS
  - Instrucciones: abrir index.html en navegador
  - Configurar URL del API Gateway en src/config.js
  - Capturas del sistema funcionando
  - Sección SDD
  - **Trazabilidad:** Entregable del parcial

- [ ] 13. Subir a GitHub y tomar capturas
  - Captura: listado de productos cargado desde el API
  - Captura: venta exitosa con respuesta visible
  - Captura: manejo de error
  - **Trazabilidad:** Entregable del parcial

## Task Dependency Graph

```json
{
  "waves": [
    { "wave": 0, "tasks": ["1", "2"] },
    { "wave": 1, "tasks": ["3", "4"] },
    { "wave": 2, "tasks": ["5", "6"] },
    { "wave": 3, "tasks": ["7", "8", "9"] },
    { "wave": 4, "tasks": ["10", "11"] },
    { "wave": 5, "tasks": ["12", "13"] }
  ]
}
```

## Notes

- Vanilla JS no requiere `npm install` ni servidor — abrir `index.html` directamente en el navegador
- El estado del carrito vive en memoria (array JS) — se reinicia al recargar la página
- La URL del API Gateway se configura en `src/config.js` — un solo lugar para cambiarla
- No subir `node_modules/` ni `.env` — este proyecto no tiene ninguno de los dos
