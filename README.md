# Sistema POS — Punto de Venta

Sistema de Punto de Venta (POS) desarrollado como proyecto académico siguiendo la metodología **Spec-Driven Development (SDD)**. Implementa una arquitectura serverless en AWS con un frontend Vanilla JS que consume directamente el API Gateway.

---

## Arquitectura General

```
Navegador (Vanilla JS)
        |
        | fetch() — async/await
        v
API Gateway (AWS)
https://rfs00jdfqb.execute-api.us-east-1.amazonaws.com/Prod
        |
        v
AWS Lambda (Node.js 20.x)
        |
        v
DynamoDB (pos-productos / pos-ventas)
```

---

## Estructura del Repositorio

```
proyecto_pos/
  .kiro/specs/pos-backend/        ← Specs SDD del backend (requirements, design, tasks)
  pos_backend/                    ← Backend Spring Boot con arquitectura hexagonal (referencia)
  pos_frontend/                   ← Frontend Node.js + Express + Handlebars (versión original)
  pos_frontend_vanilla/           ← Frontend Vanilla JS (entregable del parcial)
  sales-api-serverless/           ← Backend serverless AWS SAM (entregable del parcial)
```

---

## Backend Serverless — `sales-api-serverless/`

Backend desplegado en AWS usando SAM (Serverless Application Model).

### Tecnologías
- **AWS SAM** — Infraestructura como código
- **AWS Lambda** — Node.js 20.x, 7 funciones
- **AWS API Gateway** — REST API con CORS
- **AWS DynamoDB** — 2 tablas NoSQL (PAY_PER_REQUEST)

### Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/productos` | Lista productos o filtra por `?q=nombre` |
| GET | `/productos/{id}` | Obtiene producto por ID |
| POST | `/productos` | Crea nuevo producto |
| GET | `/ventas` | Historial de ventas |
| GET | `/ventas/{id}` | Obtiene venta por ID |
| POST | `/ventas` | Registra nueva venta |
| POST | `/admin/inicializar` | Carga catálogo inicial |

### URL base del API Gateway
```
https://rfs00jdfqb.execute-api.us-east-1.amazonaws.com/Prod
```

### Despliegue

```bash
cd sales-api-serverless

# Instalar dependencias de la capa
cd layers/dependencias && npm install && cd ../..

# Construir
sam build

# Desplegar
sam deploy --guided
```

### Pruebas unitarias

```bash
cd sales-api-serverless
npm install
npm test
```

**Resultados:** 12 tests pasando, cobertura 97%. DynamoDB mockeado con Jest.

---

## Frontend Vanilla JS — `pos_frontend_vanilla/`

Frontend del parcial — HTML5 semántico + CSS3 + JavaScript ES6+ puro. Sin frameworks, sin servidor, sin dependencias de producción.

### Tecnologías
- **HTML5** — Estructura semántica (header, nav, main, section, table, form)
- **CSS3** — Variables CSS, Grid, Flexbox, tema oscuro
- **JavaScript ES6+** — Módulos, fetch, async/await, try/catch

### Cómo ejecutar

```bash
cd pos_frontend_vanilla
python3 -m http.server 5500
```

Abre `http://localhost:5500`

### Configurar URL del API Gateway

Edita `pos_frontend_vanilla/src/config.js`:

```javascript
export const API_BASE_URL = 'https://rfs00jdfqb.execute-api.us-east-1.amazonaws.com/Prod';
```

### Atajos de teclado

| Atajo | Acción |
|-------|--------|
| Enter | Buscar producto / agregar al carrito |
| ↑↓ | Navegar resultados / métodos de pago |
| F2 | Abrir modal método de pago |
| F3 | Vaciar carrito |
| F4 | Cobro en efectivo con cambio |
| F5 | Enfocar buscador |
| ESC | Cerrar modal / tabla de resultados |

---

## Frontend Express — `pos_frontend/`

Versión original del frontend con Node.js + Express + Handlebars (server-side rendering).

```bash
cd pos_frontend
npm install
npm start
```

Abre `http://localhost:3000`

---

## Specs SDD

Los specs están escritos antes del código siguiendo la metodología Spec-Driven Development:

| Specs | Ubicación |
|-------|-----------|
| Backend | `.kiro/specs/pos-backend/` |
| Frontend Vanilla JS | `pos_frontend_vanilla/.kiro/specs/pos-frontend/` |
| Frontend Express | `pos_frontend/.kiro/specs/pos-frontend/` |

Cada carpeta contiene:
- `requirements.md` — Requisitos funcionales y criterios de aceptación
- `design.md` — Arquitectura, contratos del API, modelos de datos
- `tasks.md` — Tareas de implementación en orden de ejecución

---

## Modelo de Datos NoSQL

### pos-productos
```json
{
  "id": "uuid",
  "nombre": "Coca-Cola 600ml",
  "codigo_barras": "P001",
  "precio": 15,
  "stock": 100,
  "creadoEn": "2026-05-31T15:19:12.999Z"
}
```

### pos-ventas
```json
{
  "id": "uuid",
  "productos": [
    { "productoId": "uuid", "nombre": "Coca-Cola", "cantidad": 2, "precioUnitario": 15 }
  ],
  "total": 30,
  "metodoPago": "efectivo",
  "fecha": "2026-05-31T15:33:10.866Z"
}
```

Los productos están **embebidos** en el documento de la venta — sin JOINs, sin tablas relacionales intermedias.

---

## Repositorio

**GitHub:** https://github.com/Criss26-Robles/proyecto_pos

**Estudiante:** Cristhian Robles  
**Asignatura:** Desarrollo avanzado de aplicaciones en red  
**Programa:** Ingeniería de Software
