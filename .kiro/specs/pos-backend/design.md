# Design — Sistema POS Serverless

## Overview

Backend serverless del sistema POS implementado con AWS SAM. Expone una API REST mediante API Gateway, procesa las peticiones con funciones Lambda en Node.js 20.x y persiste los datos en DynamoDB. Toda la infraestructura está definida como código en `template.yaml`.

---

## Architecture

### Diagrama de capas

```
Cliente (Frontend Node.js / Postman / Navegador)
        |
        | HTTPS
        v
API Gateway (REST API — pos-api)
https://rfs00jdfqb.execute-api.us-east-1.amazonaws.com/Prod
        |
        |---> GET  /productos      --> pos-listar-productos  --> DynamoDB (pos-productos)
        |---> POST /productos      --> pos-crear-producto    --> DynamoDB (pos-productos)
        |---> GET  /productos/{id} --> pos-obtener-producto  --> DynamoDB (pos-productos)
        |---> GET  /ventas         --> pos-listar-ventas     --> DynamoDB (pos-ventas)
        |---> POST /ventas         --> pos-crear-venta       --> DynamoDB (pos-ventas)
        |---> GET  /ventas/{id}    --> pos-obtener-venta     --> DynamoDB (pos-ventas)
        |---> POST /admin/inicializar --> pos-inicializar-datos --> DynamoDB (pos-productos)
```

### Servicios AWS utilizados

| Servicio | Uso |
|----------|-----|
| API Gateway | Expone endpoints HTTP públicos, enruta requests a Lambdas, maneja CORS |
| AWS Lambda (Node.js 20.x) | Lógica de negocio serverless, una función por operación |
| DynamoDB | Base de datos NoSQL administrada, modo PAY_PER_REQUEST |
| IAM | Permisos mínimos por rol de Lambda (principio de mínimo privilegio) |
| AWS SAM | Infraestructura como código — `template.yaml` + `sam deploy` |
| S3 | Almacenamiento temporal de artefactos durante el despliegue |

### Flujo de una petición

1. Cliente hace `GET /productos?q=coca`
2. API Gateway recibe la petición y la enruta a `pos-listar-productos`
3. Lambda ejecuta `ScanCommand` en DynamoDB con filtro por nombre
4. DynamoDB retorna los items que coinciden
5. Lambda construye la respuesta JSON con headers CORS
6. API Gateway retorna `200 OK` al cliente

---

## Components and Interfaces

### Estructura del proyecto

```
sales-api-serverless/
  template.yaml                    ← Definición de infraestructura SAM
  samconfig.toml                   ← Configuración de despliegue
  layers/
    dependencias/
      package.json                 ← Dependencias compartidas (uuid)
  src/
    productos/
      listar.js                    ← GET /productos
      crear.js                     ← POST /productos
      obtener.js                   ← GET /productos/{id}
    ventas/
      listar.js                    ← GET /ventas
      crear.js                     ← POST /ventas
      obtener.js                   ← GET /ventas/{id}
    admin/
      inicializar.js               ← POST /admin/inicializar
  tests/
    productos/
      listar.test.js               ← Pruebas unitarias GET /productos
    ventas/
      crear.test.js                ← Pruebas unitarias POST /ventas
```

### Funciones Lambda

| Función | Handler | Método | Ruta |
|---------|---------|--------|------|
| `pos-listar-productos` | `listar.handler` | GET | `/productos` |
| `pos-crear-producto` | `crear.handler` | POST | `/productos` |
| `pos-obtener-producto` | `obtener.handler` | GET | `/productos/{id}` |
| `pos-listar-ventas` | `listar.handler` | GET | `/ventas` |
| `pos-crear-venta` | `crear.handler` | POST | `/ventas` |
| `pos-obtener-venta` | `obtener.handler` | GET | `/ventas/{id}` |
| `pos-inicializar-datos` | `inicializar.handler` | POST | `/admin/inicializar` |

### Contratos de endpoints

**GET /productos**
```
Query params: ?q=texto (opcional) — filtra por nombre o código de barras
              ?nombre=texto (opcional) — alias compatible con backend Spring Boot
Response 200: [{ "id": "uuid", "nombre": "string", "codigo_barras": "string", "precio": number, "stock": number }]
```

**POST /productos**
```
Request:  { "nombre": "string", "codigo_barras": "string", "precio": number, "stock": number }
Response 201: { "id": "uuid", "nombre": "string", "codigo_barras": "string", "precio": number, "stock": number, "creadoEn": "ISO8601" }
Response 400: { "mensaje": "nombre, codigo_barras y precio son obligatorios" }
```

**POST /ventas**
```
Request:  {
  "productos": [{ "productoId": "uuid", "nombre": "string", "cantidad": number, "precioUnitario": number }],
  "total": number,
  "metodoPago": "efectivo | Nequi | Davivienda | Daviplata | Transferencia",
  "fecha": "ISO8601"
}
Response 201: { "id": "uuid", "productos": [...], "total": number, "metodoPago": "string", "fecha": "string" }
Response 400: { "mensaje": "El carrito está vacío" }
```

**GET /ventas**
```
Response 200: [{ "id": "uuid", "total": number, "metodoPago": "string", "fecha": "string", "productos": [...] }]
```

### Estructura del template.yaml

```yaml
Resources:
  DependenciasLayer      # Capa Lambda con uuid
  ProductosTable         # DynamoDB tabla pos-productos (PK: id)
  VentasTable            # DynamoDB tabla pos-ventas (PK: id)
  ListarProductosFunction   # Lambda + IAM Role + API Gateway Event
  CrearProductoFunction     # Lambda + IAM Role + API Gateway Event
  ObtenerProductoFunction   # Lambda + IAM Role + API Gateway Event
  ListarVentasFunction      # Lambda + IAM Role + API Gateway Event
  CrearVentaFunction        # Lambda + IAM Role + API Gateway Event
  ObtenerVentaFunction      # Lambda + IAM Role + API Gateway Event
  InicializarDatosFunction  # Lambda + IAM Role + API Gateway Event
  PosApi                    # API Gateway RestApi con CORS
```

---

## Data Models

### Tabla pos-productos (DynamoDB)

```json
{
  "id": "uuid-string",
  "nombre": "Coca-Cola 600ml",
  "codigo_barras": "P001",
  "precio": 15,
  "stock": 100,
  "creadoEn": "2026-05-31T15:19:12.999Z"
}
```

- Clave de partición: `id` (String)
- Sin clave de ordenación
- Modo de capacidad: PAY_PER_REQUEST

### Tabla pos-ventas (DynamoDB)

```json
{
  "id": "uuid-string",
  "productos": [
    {
      "productoId": "uuid-string",
      "nombre": "Coca-Cola 600ml",
      "cantidad": 2,
      "precioUnitario": 15
    }
  ],
  "total": 30,
  "metodoPago": "efectivo",
  "fecha": "2026-05-31T15:33:10.866Z"
}
```

- Los productos están **embebidos** en el documento de la venta (modelo NoSQL — sin JOINs)
- Clave de partición: `id` (String)
- Modo de capacidad: PAY_PER_REQUEST

---

## Correctness Properties

Property 1: Cada Lambda tiene permisos IAM mínimos — solo puede acceder a la tabla DynamoDB que necesita.
**Validates: Requirements 4.1**

Property 2: La URL del API Gateway nunca aparece hardcodeada en el código Lambda — se pasa como variable de entorno `PRODUCTOS_TABLE` y `VENTAS_TABLE`.
**Validates: Requirements 4.4**

Property 3: Todos los handlers están envueltos en try/catch — cualquier error retorna 500 con mensaje descriptivo.
**Validates: Requirements 3.8**

Property 4: Los productos de una venta se almacenan embebidos en el documento — sin tablas relacionales intermedias.
**Validates: Requirements 3.3**

---

## Error Handling

| Escenario | HTTP | Respuesta |
|-----------|------|-----------|
| Productos encontrados | 200 | Array de productos |
| Tabla vacía o sin coincidencias | 200 | `[]` |
| Carrito vacío en POST /ventas | 400 | `{"mensaje": "El carrito está vacío"}` |
| Campos obligatorios faltantes | 400 | `{"mensaje": "... son obligatorios"}` |
| Error de conexión DynamoDB | 500 | `{"mensaje": "Error interno del servidor"}` |
| Recurso no encontrado | 404 | `{"mensaje": "... no encontrado"}` |

---

## Testing Strategy

- Framework: Jest 29
- Mocks: `jest.mock('@aws-sdk/client-dynamodb')` y `jest.mock('@aws-sdk/lib-dynamodb')`
- Sin conexión real a AWS — DynamoDB completamente simulado
- Cobertura: 97% de statements

Casos cubiertos por Lambda:

**GET /productos:**
- Respuesta exitosa con productos
- Tabla vacía retorna `[]`
- Filtro por `?q=` retorna solo coincidencias
- Filtro por `?nombre=` compatibilidad
- Error de conexión retorna 500
- Headers CORS presentes

**POST /ventas:**
- Venta registrada con 201
- Carrito vacío retorna 400
- Body sin productos retorna 400
- Método de pago por defecto es efectivo
- Error de conexión retorna 500
- Headers CORS presentes
