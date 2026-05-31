'use strict';

jest.mock('@aws-sdk/client-dynamodb', () => ({
  DynamoDBClient: jest.fn().mockImplementation(() => ({})),
}));

const mockSend = jest.fn();
jest.mock('@aws-sdk/lib-dynamodb', () => ({
  DynamoDBDocumentClient: {
    from: jest.fn().mockReturnValue({ send: mockSend }),
  },
  PutCommand: jest.fn().mockImplementation((params) => params),
  GetCommand: jest.fn().mockImplementation((params) => params),
}));

jest.mock('uuid', () => ({ v4: () => 'uuid-test-1234' }));

const { handler } = require('../../src/ventas/crear');

describe('Lambda POST /ventas - crear', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    process.env.VENTAS_TABLE = 'pos-ventas-test';
    process.env.PRODUCTOS_TABLE = 'pos-productos-test';
  });

  test('respuesta exitosa - registra venta correctamente', async () => {
    mockSend.mockResolvedValueOnce({});

    const event = {
      body: JSON.stringify({
        productos: [
          { productoId: 'prod-1', nombre: 'Coca-Cola 600ml', cantidad: 2, precioUnitario: 15 },
        ],
        total: 30,
        metodoPago: 'efectivo',
        fecha: '2026-05-31T15:00:00.000Z',
      }),
    };

    const resultado = await handler(event);

    expect(resultado.statusCode).toBe(201);
    const body = JSON.parse(resultado.body);
    expect(body.id).toBe('uuid-test-1234');
    expect(body.total).toBe(30);
    expect(body.metodoPago).toBe('efectivo');
    expect(body.productos).toHaveLength(1);
  });

  test('carrito vacío - retorna 400', async () => {
    const event = {
      body: JSON.stringify({
        productos: [],
        total: 0,
        metodoPago: 'efectivo',
      }),
    };

    const resultado = await handler(event);

    expect(resultado.statusCode).toBe(400);
    const body = JSON.parse(resultado.body);
    expect(body.mensaje).toBe('El carrito está vacío');
  });

  test('body sin productos - retorna 400', async () => {
    const event = {
      body: JSON.stringify({ total: 0, metodoPago: 'efectivo' }),
    };

    const resultado = await handler(event);

    expect(resultado.statusCode).toBe(400);
  });

  test('método de pago por defecto es efectivo', async () => {
    mockSend.mockResolvedValueOnce({});

    const event = {
      body: JSON.stringify({
        productos: [
          { productoId: 'prod-1', nombre: 'Pepsi', cantidad: 1, precioUnitario: 14 },
        ],
        total: 14,
      }),
    };

    const resultado = await handler(event);

    expect(resultado.statusCode).toBe(201);
    const body = JSON.parse(resultado.body);
    expect(body.metodoPago).toBe('efectivo');
  });

  test('error de conexión a DynamoDB - retorna 500', async () => {
    mockSend.mockRejectedValueOnce(new Error('DynamoDB unavailable'));

    const event = {
      body: JSON.stringify({
        productos: [
          { productoId: 'prod-1', nombre: 'Agua', cantidad: 1, precioUnitario: 8 },
        ],
        total: 8,
        metodoPago: 'Nequi',
      }),
    };

    const resultado = await handler(event);

    expect(resultado.statusCode).toBe(500);
    const body = JSON.parse(resultado.body);
    expect(body.mensaje).toBe('Error interno del servidor');
  });

  test('headers CORS presentes en la respuesta', async () => {
    mockSend.mockResolvedValueOnce({});

    const event = {
      body: JSON.stringify({
        productos: [{ productoId: 'p1', nombre: 'Test', cantidad: 1, precioUnitario: 10 }],
        total: 10,
        metodoPago: 'efectivo',
      }),
    };

    const resultado = await handler(event);

    expect(resultado.headers['Access-Control-Allow-Origin']).toBe('*');
  });
});
