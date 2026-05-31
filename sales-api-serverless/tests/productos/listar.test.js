'use strict';

// Mock del cliente DynamoDB antes de importar el handler
jest.mock('@aws-sdk/client-dynamodb', () => ({
  DynamoDBClient: jest.fn().mockImplementation(() => ({})),
}));

const mockSend = jest.fn();
jest.mock('@aws-sdk/lib-dynamodb', () => ({
  DynamoDBDocumentClient: {
    from: jest.fn().mockReturnValue({ send: mockSend }),
  },
  ScanCommand: jest.fn().mockImplementation((params) => params),
}));

const { handler } = require('../../src/productos/listar');

describe('Lambda GET /productos - listar', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    process.env.PRODUCTOS_TABLE = 'pos-productos-test';
  });

  test('respuesta exitosa - retorna lista de productos', async () => {
    const productosEsperados = [
      { id: 'uuid-1', nombre: 'Coca-Cola 600ml', precio: 15, codigo_barras: 'P001', stock: 100 },
      { id: 'uuid-2', nombre: 'Pepsi 600ml', precio: 14, codigo_barras: 'P002', stock: 80 },
    ];

    mockSend.mockResolvedValueOnce({ Items: productosEsperados });

    const event = { queryStringParameters: null };
    const resultado = await handler(event);

    expect(resultado.statusCode).toBe(200);
    const body = JSON.parse(resultado.body);
    expect(body).toHaveLength(2);
    expect(body[0].nombre).toBe('Coca-Cola 600ml');
  });

  test('tabla vacía - retorna lista vacía', async () => {
    mockSend.mockResolvedValueOnce({ Items: [] });

    const event = { queryStringParameters: null };
    const resultado = await handler(event);

    expect(resultado.statusCode).toBe(200);
    const body = JSON.parse(resultado.body);
    expect(body).toHaveLength(0);
    expect(Array.isArray(body)).toBe(true);
  });

  test('filtro por nombre - retorna solo coincidencias', async () => {
    const todosLosProductos = [
      { id: 'uuid-1', nombre: 'Coca-Cola 600ml', precio: 15 },
      { id: 'uuid-2', nombre: 'Pepsi 600ml', precio: 14 },
      { id: 'uuid-3', nombre: 'Agua Mineral', precio: 8 },
    ];

    mockSend.mockResolvedValueOnce({ Items: todosLosProductos });

    const event = { queryStringParameters: { q: 'cola' } };
    const resultado = await handler(event);

    expect(resultado.statusCode).toBe(200);
    const body = JSON.parse(resultado.body);
    expect(body).toHaveLength(1);
    expect(body[0].nombre).toBe('Coca-Cola 600ml');
  });

  test('filtro por parámetro nombre - compatibilidad backend', async () => {
    const productos = [
      { id: 'uuid-1', nombre: 'Leche Entera 1L', precio: 18 },
    ];

    mockSend.mockResolvedValueOnce({ Items: productos });

    const event = { queryStringParameters: { nombre: 'leche' } };
    const resultado = await handler(event);

    expect(resultado.statusCode).toBe(200);
    const body = JSON.parse(resultado.body);
    expect(body).toHaveLength(1);
  });

  test('error de conexión a DynamoDB - retorna 500', async () => {
    mockSend.mockRejectedValueOnce(new Error('Connection timeout'));

    const event = { queryStringParameters: null };
    const resultado = await handler(event);

    expect(resultado.statusCode).toBe(500);
    const body = JSON.parse(resultado.body);
    expect(body.mensaje).toBe('Error interno del servidor');
  });

  test('headers CORS presentes en la respuesta', async () => {
    mockSend.mockResolvedValueOnce({ Items: [] });

    const event = { queryStringParameters: null };
    const resultado = await handler(event);

    expect(resultado.headers['Access-Control-Allow-Origin']).toBe('*');
    expect(resultado.headers['Content-Type']).toBe('application/json');
  });
});
