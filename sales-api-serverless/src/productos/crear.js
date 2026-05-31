'use strict';

const { DynamoDBClient } = require('@aws-sdk/client-dynamodb');
const { DynamoDBDocumentClient, PutCommand } = require('@aws-sdk/lib-dynamodb');
const { v4: uuidv4 } = require('uuid');

const client = new DynamoDBClient({});
const dynamo = DynamoDBDocumentClient.from(client);

const TABLA = process.env.PRODUCTOS_TABLE;

const respuesta = (statusCode, body) => ({
  statusCode,
  headers: {
    'Content-Type': 'application/json',
    'Access-Control-Allow-Origin': '*',
  },
  body: JSON.stringify(body),
});

/**
 * POST /productos
 * Body: { nombre, codigo_barras, precio, stock }
 */
exports.handler = async (event) => {
  try {
    const body = JSON.parse(event.body || '{}');
    const { nombre, codigo_barras, precio, stock } = body;

    if (!nombre || !codigo_barras || precio === undefined) {
      return respuesta(400, { mensaje: 'nombre, codigo_barras y precio son obligatorios' });
    }

    const producto = {
      id: uuidv4(),
      nombre,
      codigo_barras,
      precio: parseFloat(precio),
      stock: parseInt(stock) || 0,
      creadoEn: new Date().toISOString(),
    };

    await dynamo.send(new PutCommand({ TableName: TABLA, Item: producto }));

    return respuesta(201, producto);
  } catch (err) {
    console.error('Error creando producto:', err);
    return respuesta(500, { mensaje: 'Error interno del servidor' });
  }
};
