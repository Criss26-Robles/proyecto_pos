# Requirements Document

## Introduction

Frontend web del sistema POS construido con Vanilla JS (HTML5 + CSS + JavaScript puro). Consume el API Gateway de AWS para listar productos y registrar ventas. No requiere servidor — se ejecuta directamente en el navegador abriendo `index.html`.

## Requirements

### RF-01: Vista de productos

**User Story:** Como cajero, quiero ver el listado de productos disponibles para seleccionarlos y agregarlos al carrito.

#### Acceptance Criteria

1. GIVEN la página carga WHEN el DOM está listo THEN se hace fetch GET /productos y se muestra el listado con nombre, precio y botón "Agregar"
2. GIVEN el API retorna productos WHEN se renderizan THEN cada producto muestra nombre, precio formateado y botón de selección
3. GIVEN el API falla WHEN fetch lanza error THEN se muestra mensaje de error visible al usuario
4. GIVEN el API retorna lista vacía WHEN se renderiza THEN se muestra "No hay productos disponibles"

---

### RF-02: Carrito de compras

**User Story:** Como cajero, quiero gestionar los productos seleccionados antes de registrar la venta.

#### Acceptance Criteria

1. GIVEN el cajero hace clic en "Agregar" WHEN el producto no está en el carrito THEN se agrega con cantidad 1
2. GIVEN el producto ya está en el carrito WHEN se agrega de nuevo THEN la cantidad aumenta en 1
3. GIVEN hay productos en el carrito WHEN se muestra el carrito THEN cada item tiene nombre, cantidad, subtotal y botón eliminar
4. GIVEN el cajero hace clic en eliminar WHEN el item está en el carrito THEN se elimina y el total se actualiza
5. GIVEN hay items en el carrito WHEN cambia cualquier cantidad THEN el total general se recalcula automáticamente

---

### RF-03: Registro de venta

**User Story:** Como cajero, quiero registrar la venta con los productos del carrito.

#### Acceptance Criteria

1. GIVEN el carrito tiene productos WHEN el cajero hace clic en "Confirmar venta" THEN se hace fetch POST /ventas con los datos del carrito
2. GIVEN el API retorna 201 WHEN la venta se registra THEN se muestra mensaje de éxito y el carrito se vacía
3. GIVEN el carrito está vacío WHEN el cajero intenta confirmar THEN se muestra mensaje "El carrito está vacío"
4. GIVEN el API falla WHEN fetch lanza error THEN se muestra mensaje de error descriptivo
5. GIVEN el API retorna error 400/500 WHEN se procesa la respuesta THEN se muestra el mensaje de error al usuario

---

### RF-04: Selección de método de pago

**User Story:** Como cajero, quiero seleccionar el método de pago antes de confirmar la venta.

#### Acceptance Criteria

1. GIVEN el cajero va a confirmar WHEN se muestra el formulario THEN hay opciones: efectivo, Nequi, Davivienda, Daviplata, Transferencia
2. GIVEN el cajero selecciona un método WHEN confirma la venta THEN el POST /ventas incluye el metodoPago seleccionado

---

### RNF-01: Configuración de URL

1. GIVEN el proyecto se configura WHEN se define la URL del API THEN debe leerse desde `src/config.js`, nunca hardcodeada en cada llamada

### RNF-02: Manejo de errores

1. GIVEN cualquier fetch al API WHEN ocurre un error THEN debe estar envuelto en try/catch y mostrar mensaje al usuario

### RNF-03: HTML5 semántico

1. GIVEN el markup se escribe WHEN se estructura la página THEN debe usar etiquetas semánticas: header, main, section, article, nav, table, form, button

### RNF-04: CSS propio

1. GIVEN los estilos se aplican WHEN carga la página THEN debe existir `css/estilos.css` con tema oscuro usando variables CSS, flexbox y grid propios — no solo clases de Bootstrap

### RNF-05: Sin servidor

1. GIVEN el proyecto se entrega WHEN el profesor lo abre THEN debe funcionar abriendo `index.html` directamente en el navegador sin necesidad de `npm start`

## Glossary

- **Vanilla JS**: JavaScript puro sin frameworks — usando solo las APIs nativas del navegador
- **fetch**: API nativa del navegador para hacer peticiones HTTP asíncronas
- **async/await**: Sintaxis para manejar promesas de forma legible
- **API Gateway**: URL base del backend desplegado en AWS
- **Carrito**: Estado temporal en memoria (array) de los productos seleccionados
- **SDD**: Spec-Driven Development — los specs se escriben antes del código
