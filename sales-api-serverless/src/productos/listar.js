'use strict';

const { DynamoDBClient } = require('@aws-sdk/client-dynamodb');
const { DynamoDBDocumentClient, ScanCommand } = require('@aws-sdk/lib-dynamodb');

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
 * GET /productos
 * GET /productos?nombre=texto
 * GET /productos?q=texto  (compatibilidad con frontend)
 */
exports.handler = async (event) => {
  try {
    const params = event.queryStringParameters || {};
    const filtro = params.nombre || params.q || null;

    const result = await dynamo.send(new ScanCommand({ TableName: TABLA }));
    let productos = result.Items || [];

    if (filtro) {
      const texto = filtro.toLowerCase();
      productos = productos.filter(
        (p) =>
          (p.nombre && p.nombre.toLowerCase().includes(texto)) ||
          (p.codigo_barras && p.codigo_barras.toLowerCase().includes(texto))
      );
    }

    return respuesta(200, productos);
  } catch (err) {
    console.error('Error listando productos:', err);
    return respuesta(500, { mensaje: 'Error interno del servidor' });
  }
};
