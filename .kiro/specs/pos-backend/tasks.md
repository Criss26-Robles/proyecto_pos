# Implementation Plan: Backend Sistema POS Serverless

## Overview

Lista de tareas de implementación del backend serverless, derivadas del design.md. Cada tarea es trazable a un requisito funcional del requirements.md. El código se escribió después de tener los specs completos, siguiendo la metodología SDD.

## Tasks

### Fase 1: Infraestructura

- [x] 1. Crear template.yaml con AWS SAM
  - Definir API Gateway con CORS habilitado
  - Definir 7 funciones Lambda con sus rutas
  - Definir 2 tablas DynamoDB (pos-productos, pos-ventas) con id como PK
  - Configurar variables de entorno PRODUCTOS_TABLE y VENTAS_TABLE
  - Configurar capa de dependencias con uuid
  - **Trazabilidad:** RF-01, RF-02, RNF-01

- [x] 2. Desplegar infraestructura con sam deploy
  - Ejecutar `sam build`
  - Ejecutar `sam deploy --guided`
  - Verificar en consola AWS que existen Lambdas, API Gateway y tablas DynamoDB
  - **Trazabilidad:** RNF-01, RNF-02

### Fase 2: Lambda Productos

- [x] 3. Implementar GET /productos (src/productos/listar.js)
  - Recibir query params `q` y `nombre`
  - Hacer ScanCommand en DynamoDB
  - Filtrar por nombre o código de barras si hay parámetro
  - Retornar JSON con headers CORS
  - **Trazabilidad:** RF-01

- [x] 4. Implementar POST /productos (src/productos/crear.js)
  - Validar campos obligatorios: nombre, codigo_barras, precio
  - Generar UUID con la capa de dependencias
  - Hacer PutCommand en DynamoDB
  - Retornar producto creado con status 201
  - **Trazabilidad:** RF-04

- [x] 5. Implementar GET /productos/{id} (src/productos/obtener.js)
  - Extraer id de pathParameters
  - Hacer GetCommand en DynamoDB
  - Retornar 404 si no existe
  - **Trazabilidad:** RF-01

### Fase 3: Lambda Ventas

- [x] 6. Implementar POST /ventas (src/ventas/crear.js)
  - Validar que productos no esté vacío
  - Generar UUID para la venta
  - Hacer PutCommand en DynamoDB con productos embebidos
  - Retornar venta con status 201
  - **Trazabilidad:** RF-02, RF-03

- [x] 7. Implementar GET /ventas (src/ventas/listar.js)
  - Hacer ScanCommand en tabla pos-ventas
  - Ordenar por fecha descendente
  - Retornar lista con status 200
  - **Trazabilidad:** RF-02

- [x] 8. Implementar GET /ventas/{id} (src/ventas/obtener.js)
  - Extraer id de pathParameters
  - Hacer GetCommand en DynamoDB
  - Retornar 404 si no existe
  - **Trazabilidad:** RF-02

### Fase 4: Admin

- [x] 9. Implementar POST /admin/inicializar (src/admin/inicializar.js)
  - Verificar si la tabla ya tiene productos
  - Si está vacía, cargar 10 productos iniciales con PutCommand
  - Si ya tiene datos, retornar mensaje informativo
  - **Trazabilidad:** RF-05

### Fase 5: Pruebas unitarias

- [x] 10. Escribir pruebas para GET /productos (tests/productos/listar.test.js)
  - Mock de DynamoDBClient y DynamoDBDocumentClient con jest.mock
  - Caso: respuesta exitosa con productos
  - Caso: tabla vacía retorna []
  - Caso: filtro por ?q= retorna coincidencias
  - Caso: filtro por ?nombre= compatibilidad
  - Caso: error de conexión retorna 500
  - Caso: headers CORS presentes
  - **Trazabilidad:** RF-01 CA-01 a CA-05

- [x] 11. Escribir pruebas para POST /ventas (tests/ventas/crear.test.js)
  - Mock de DynamoDBClient y DynamoDBDocumentClient con jest.mock
  - Caso: venta registrada con status 201
  - Caso: carrito vacío retorna 400
  - Caso: body sin productos retorna 400
  - Caso: metodoPago por defecto es efectivo
  - Caso: error de conexión retorna 500
  - Caso: headers CORS presentes
  - **Trazabilidad:** RF-02 CA-05 a CA-08

### Fase 6: Documentación y GitHub

- [x] 12. Crear README.md del backend
  - Descripción de arquitectura con diagrama
  - Instrucciones de despliegue (sam build → sam deploy)
  - URL del API Gateway
  - Capturas de Postman (GET /productos, POST /ventas, error)
  - Captura de pruebas unitarias ejecutándose
  - Sección SDD
  - **Trazabilidad:** Entregable del parcial

- [x] 13. Subir repositorio a GitHub
  - Verificar .gitignore (node_modules, .env, .aws-sam/build)
  - Subir código con specs en .kiro/specs/pos-backend/
  - Verificar que no hay credenciales expuestas
  - **Trazabilidad:** Entregable del parcial

## Task Dependency Graph

```json
{
  "waves": [
    { "wave": 0, "tasks": ["1"] },
    { "wave": 1, "tasks": ["2"] },
    { "wave": 2, "tasks": ["3", "4", "5"] },
    { "wave": 3, "tasks": ["6", "7", "8"] },
    { "wave": 4, "tasks": ["9"] },
    { "wave": 5, "tasks": ["10", "11"] },
    { "wave": 6, "tasks": ["12", "13"] }
  ]
}
```

## Notes

- Las tablas DynamoDB usan modo PAY_PER_REQUEST — sin capacidad provisionada
- Los productos de una venta se almacenan embebidos — modelo NoSQL sin JOINs
- Las pruebas usan jest.mock para aislar DynamoDB — no requieren conexión a AWS
- El stack de CloudFormation se llama `pos-sistema` en la región `us-east-1`
- URL del API Gateway: `https://rfs00jdfqb.execute-api.us-east-1.amazonaws.com/Prod`
