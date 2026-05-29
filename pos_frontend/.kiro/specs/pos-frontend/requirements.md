# Requirements — Frontend Sistema POS

## Descripcion General
Frontend web del sistema POS construido con Node.js y Express. Consume el API Gateway de AWS para buscar productos y registrar ventas.

---

## Requisitos Funcionales

### RF-01: Vista de productos
- El sistema debe mostrar un input unico para buscar productos
- Si el input es numerico: busca por codigo de barras y agrega directo al carrito
- Si el input tiene letras: busca por nombre y muestra tabla con coincidencias
- La tabla de resultados solo aparece cuando hay multiples coincidencias
- La pagina principal NO carga todos los productos al inicio

### RF-02: Carrito de compras
- El usuario puede agregar productos al carrito
- El usuario puede modificar la cantidad de cada producto
- El usuario puede eliminar productos del carrito
- El carrito muestra subtotal por producto y total general

### RF-03: Registro de venta
- F4: cobro en efectivo con calculo de cambio automatico
- F2: seleccion de metodo de pago (Nequi, Davivienda, Daviplata, Transferencia)
- Al confirmar, se envia POST /ventas al API Gateway
- Mensaje de exito al registrar venta
- Mensaje de error cuando el API falle

### RF-04: Navegacion
- Historial de ventas (GET /ventas)
- Crear producto (POST /productos)
- Atajos de teclado: F2, F3, F4, F5, F6, F7, ESC

---

## Requisitos No Funcionales

### RNF-01: Configuracion
- La URL base del API Gateway debe estar en variable de entorno (.env)
- No hardcodear la URL en cada llamada

### RNF-02: Manejo de errores
- Todos los fetch usan try/catch
- Errores de red muestran mensaje al usuario
- Errores HTTP 400/500 muestran mensaje descriptivo

### RNF-03: HTML5 semantico
- Uso de etiquetas semanticas: nav, main, table, form, button
- No usar solo divs

### RNF-04: CSS
- Tema oscuro personalizado
- Uso de flexbox y grid
- Estilos propios en estilos.css

---

## Criterios de Aceptacion

### CA-01: Busqueda por codigo de barras
- Input: 750123456789 (numerico) + Enter
- Expected: producto agregado directo al carrito

### CA-02: Busqueda por nombre
- Input: leche + Enter
- Expected: tabla con productos que contienen 'leche'

### CA-03: Venta exitosa con F4
- Carrito con productos, presionar F4
- Ingresar monto mayor al total
- Expected: cambio calculado, venta registrada, carrito vaciado

### CA-04: Venta exitosa con F2
- Carrito con productos, presionar F2
- Seleccionar Nequi
- Expected: venta registrada con metodoPago=Nequi

### CA-05: Manejo de error de API
- API no disponible
- Expected: mensaje de error visible al usuario

### CA-06: Pagina principal sin productos
- Cargar pagina principal
- Expected: solo input visible, sin tabla de productos
