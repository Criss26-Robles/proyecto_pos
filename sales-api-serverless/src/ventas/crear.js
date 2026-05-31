'use strict';

const { DynamoDBClient } = require('@aws-sdk/client-dynamodb');
const { DynamoDBDocumentClient, PutCommand, GetCommand } = require('@aws-sdk/lib-dynamodb');
const { v4: uuidv4 } = require('uuid');

const client = new DynamoDBClient({});
const dynamo = DynamoDBDocumentClient.from(client);

const VENTAS_TABLA = process.env.VENTAS_TABLE;
const PRODUCTOS_TABLA = process.env.PRODUCTOS_TABLE;

const respuesta = (statusCode, body) => ({
  statusCode,
  headers: {
    'Content-Type': 'application/json',
    'Access-Control-Allow-Origin': '*',
  },
  body: JSON.stringify(body),
});

/**
 * POST /ventas
 * Body: { productos: [{ productoId, nombre, cantidad, precioUnitario }], total, metodoPago }
 */
exports.handler = async (event) => {
  try {
    const body = JSON.parse(event.body || '{}');
    const { productos, total, metodoPago, fecha } = body;

    if (!productos || productos.length === 0) {
      return respuesta(400, { mensaje: 'El carrito está vacío' });
    }

    const venta = {
      id: uuidv4(),
      productos,
      total: parseFloat(total) || productos.reduce(
        (sum, p) => sum + p.precioUnitario * p.cantidad, 0
      ),
      metodoPago: metodoPago || 'efectivo',
      fecha: fecha || new Date().toISOString(),
    };

    await dynamo.send(new PutCommand({ TableName: VENTAS_TABLA, Item: venta }));

    return respuesta(201, venta);
  } catch (err) {
    console.error('Error creando venta:', err);
    return respuesta(500, { mensaje: 'Error interno del servidor' });
  }
};
