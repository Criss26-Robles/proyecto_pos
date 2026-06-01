# Requirements Document

## Introduction

Backend serverless del sistema POS construido con AWS SAM. Expone endpoints REST mediante API Gateway, ejecuta la lógica en funciones Lambda Node.js 20.x y persiste los datos en DynamoDB. Implementado siguiendo el enfoque Spec-Driven Development (SDD).

## Requirements

### RF-01: Buscar productos

**User Story:** Como cajero, quiero buscar productos por nombre o código de barras para agregarlos al carrito.

#### Acceptance Criteria

1. GIVEN el cliente hace GET /productos sin parámetros WHEN Lambda consulta DynamoDB THEN retorna todos los productos con status 200
2. GIVEN el cliente hace GET /productos?q=leche WHEN Lambda filtra por nombre THEN retorna solo productos cuyo nombre contenga "leche" (case insensitive) con status 200
3. GIVEN el cliente hace GET /productos?q=P001 WHEN Lambda filtra por código de barras THEN retorna el producto con ese código con status 200
4. GIVEN no hay productos en la tabla WHEN Lambda hace Scan THEN retorna array vacío `[]` con status 200
5. GIVEN DynamoDB no está disponible WHEN Lambda intenta conectarse THEN retorna status 500 con `{"mensaje": "Error interno del servidor"}`

---

### RF-02: Registrar venta

**User Story:** Como cajero, quiero registrar una venta con los productos del carrito para completar el cobro.

#### Acceptance Criteria

1. GIVEN el cliente hace POST /ventas con productos válidos WHEN Lambda guarda en DynamoDB THEN retorna status 201 con el objeto de la venta incluyendo id, productos, total, metodoPago y fecha
2. GIVEN el body tiene productos vacíos `[]` WHEN Lambda valida THEN retorna status 400 con `{"mensaje": "El carrito está vacío"}`
3. GIVEN el body no tiene campo productos WHEN Lambda valida THEN retorna status 400
4. GIVEN metodoPago no se envía WHEN Lambda procesa THEN usa "efectivo" como valor por defecto
5. GIVEN DynamoDB no está disponible WHEN Lambda intenta guardar THEN retorna status 500 con `{"mensaje": "Error interno del servidor"}`

---

### RF-03: Estructura NoSQL correcta

**User Story:** Como arquitecto, quiero que los datos se almacenen en formato NoSQL para aprovechar DynamoDB.

#### Acceptance Criteria

1. GIVEN una venta se registra WHEN se guarda en DynamoDB THEN los productos están embebidos como array dentro del documento de la venta — sin tablas relacionales intermedias
2. GIVEN un producto se crea WHEN se guarda en DynamoDB THEN tiene los campos id, nombre, codigo_barras, precio, stock y creadoEn en el mismo documento

---

### RF-04: Crear producto

**User Story:** Como administrador, quiero agregar nuevos productos al catálogo.

#### Acceptance Criteria

1. GIVEN el cliente hace POST /productos con nombre, codigo_barras y precio WHEN Lambda valida y guarda THEN retorna status 201 con el producto creado incluyendo id generado
2. GIVEN falta nombre, codigo_barras o precio WHEN Lambda valida THEN retorna status 400 con mensaje descriptivo

---

### RF-05: Inicializar catálogo

**User Story:** Como administrador, quiero cargar los productos iniciales al desplegar el sistema.

#### Acceptance Criteria

1. GIVEN la tabla está vacía WHEN se llama POST /admin/inicializar THEN se cargan 10 productos y retorna status 201
2. GIVEN la tabla ya tiene productos WHEN se llama POST /admin/inicializar THEN no modifica nada y retorna status 200 con mensaje informativo

---

### RNF-01: Serverless

1. GIVEN cualquier petición llega WHEN se procesa THEN debe ejecutarse en AWS Lambda sin servidores administrados

### RNF-02: Escalabilidad

1. GIVEN la carga aumenta WHEN DynamoDB y Lambda reciben más peticiones THEN escalan automáticamente sin configuración adicional

### RNF-03: Latencia

1. GIVEN una petición normal llega WHEN Lambda responde THEN el tiempo de respuesta debe ser menor a 30 segundos (timeout configurado)

### RNF-04: Seguridad

1. GIVEN cada Lambda se despliega WHEN se asignan permisos IAM THEN solo tiene acceso a la tabla DynamoDB que necesita — principio de mínimo privilegio
2. GIVEN el código se sube a GitHub WHEN se revisa el repositorio THEN no debe haber credenciales AWS ni archivos .env con datos sensibles

## Glossary

- **SAM**: Serverless Application Model — herramienta de AWS para definir infraestructura serverless como código
- **Lambda**: Función serverless de AWS que ejecuta código sin administrar servidores
- **API Gateway**: Servicio de AWS que expone endpoints HTTP y los conecta con Lambdas
- **DynamoDB**: Base de datos NoSQL de AWS con escalado automático
- **PK**: Partition Key — clave primaria de una tabla DynamoDB
- **SDD**: Spec-Driven Development — metodología donde los specs se escriben antes del código
- **CORS**: Cross-Origin Resource Sharing — headers que permiten al frontend consumir la API desde otro dominio
- **IAM**: Identity and Access Management — sistema de permisos de AWS
