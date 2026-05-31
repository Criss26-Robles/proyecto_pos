# Requirements Document

## Introduction

Frontend web del sistema POS construido con Node.js y Express. Consume el API Gateway de AWS para buscar productos y registrar ventas. Diseñado para uso en caja registradora con atajos de teclado y tema oscuro.

## Requirements

### RF-01: Vista de productos

**User Story:** Como cajero, quiero buscar productos por nombre o código de barras para agregarlos al carrito rápidamente.

#### Acceptance Criteria

1. GIVEN el cajero escribe un código numérico y presiona Enter WHEN el sistema busca el producto THEN el producto se agrega directamente al carrito sin mostrar tabla de resultados
2. GIVEN el cajero escribe texto con letras y presiona Enter WHEN el sistema busca productos THEN se muestra una tabla con todas las coincidencias por nombre
3. GIVEN la página principal carga WHEN el DOM está listo THEN no se muestran productos — solo el input de búsqueda
4. GIVEN el cajero busca un término sin resultados WHEN el API retorna lista vacía THEN se muestra el mensaje "No se encontraron productos"

---

### RF-02: Carrito de compras

**User Story:** Como cajero, quiero gestionar los productos en el carrito para preparar el cobro.

#### Acceptance Criteria

1. GIVEN un producto está en el carrito WHEN el cajero hace clic en "+" THEN la cantidad aumenta en 1 y el subtotal se actualiza
2. GIVEN un producto está en el carrito WHEN el cajero hace clic en "-" con cantidad mayor a 1 THEN la cantidad disminuye en 1
3. GIVEN un producto está en el carrito WHEN el cajero hace clic en "X" THEN el producto se elimina del carrito
4. GIVEN hay productos en el carrito WHEN se modifica cualquier cantidad THEN el total general se recalcula automáticamente

---

### RF-03: Registro de venta

**User Story:** Como cajero, quiero registrar ventas con diferentes métodos de pago para completar el cobro.

#### Acceptance Criteria

1. GIVEN el carrito tiene productos y el cajero presiona F4 WHEN ingresa un monto mayor al total THEN el sistema calcula el cambio y muestra el resultado antes de confirmar
2. GIVEN el cajero presiona F2 WHEN selecciona un método de pago (Nequi, Davivienda, Daviplata, Transferencia) THEN la venta se registra con ese método
3. GIVEN la venta se registra exitosamente WHEN el API retorna 201 THEN el carrito se vacía y se muestra mensaje de éxito
4. GIVEN el API no está disponible WHEN se intenta registrar la venta THEN se muestra un mensaje de error descriptivo al usuario
5. GIVEN el carrito está vacío WHEN el cajero intenta cobrar THEN se muestra el mensaje "El carrito está vacío"

---

### RF-04: Navegación

**User Story:** Como cajero, quiero navegar entre las secciones del POS usando atajos de teclado para mayor velocidad.

#### Acceptance Criteria

1. GIVEN el cajero presiona F5 WHEN está en cualquier parte de la vista principal THEN el foco se mueve al input de búsqueda
2. GIVEN el cajero presiona F6 WHEN está en la vista principal THEN navega al historial de ventas
3. GIVEN el cajero presiona F7 WHEN está en la vista principal THEN navega a crear producto
4. GIVEN el cajero presiona F3 WHEN hay productos en el carrito THEN el carrito se vacía completamente
5. GIVEN hay un modal abierto WHEN el cajero presiona ESC THEN el modal se cierra

---

### RF-05: Historial de ventas

**User Story:** Como cajero, quiero ver el historial de ventas para consultar transacciones anteriores.

#### Acceptance Criteria

1. GIVEN el cajero navega a /historial WHEN el API responde con ventas THEN se muestra la lista con id, fecha, total y método de pago
2. GIVEN el API falla al cargar el historial WHEN hay un error de red THEN se muestra el mensaje "No se pudo cargar el historial"

---

### RF-06: Crear producto

**User Story:** Como administrador, quiero agregar nuevos productos al catálogo desde el frontend.

#### Acceptance Criteria

1. GIVEN el formulario tiene nombre, código de barras y precio WHEN se envía THEN el producto se crea en el API y se muestra mensaje de éxito
2. GIVEN falta algún campo obligatorio WHEN se intenta enviar THEN se muestra el mensaje "Todos los campos son obligatorios"

---

### RNF-01: Configuración

1. GIVEN el proyecto se configura WHEN se define la URL del API THEN debe leerse desde `process.env.API_BASE_URL`, nunca hardcodeada en el código

### RNF-02: Manejo de errores

1. GIVEN cualquier llamada al API WHEN ocurre un error THEN debe estar envuelta en try/catch y mostrar mensaje al usuario

### RNF-03: HTML5 semántico

1. GIVEN el markup se genera WHEN se renderizan las vistas THEN debe usar etiquetas semánticas: nav, main, table, form, button, section

### RNF-04: CSS propio

1. GIVEN los estilos se aplican WHEN se carga la página THEN debe existir un archivo estilos.css con tema oscuro usando variables CSS, flexbox y grid propios

## Glossary

- **POS**: Point of Sale — sistema de punto de venta
- **API Gateway**: Servicio de AWS que expone los endpoints REST
- **Lambda**: Función serverless de AWS que procesa las peticiones
- **DynamoDB**: Base de datos NoSQL de AWS donde se persisten productos y ventas
- **Carrito**: Estado temporal de los productos seleccionados antes de confirmar la venta
- **SDD**: Spec-Driven Development — metodología donde los specs se escriben antes del código
