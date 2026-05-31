'use strict';

const { DynamoDBClient } = require('@aws-sdk/client-dynamodb');
const { DynamoDBDocumentClient, GetCommand } = require('@aws-sdk/lib-dynamodb');

const client = new DynamoDBClient({});
const dynamo = DynamoDBDocumentClient.from(client);

const TABLA = process.env.VENTAS_TABLE;

const respuesta = (statusCode, body) => ({
  statusCode,
  headers: {
    'Content-Type': 'application/json',
    'Access-Control-Allow-Origin': '*',
  },
  body: JSON.stringify(body),
});

/**
 * GET /ventas/{id}
 */
exports.handler = async (event) => {
  try {
    const { id } = event.pathParameters || {};

    if (!id) {
      return respuesta(400, { mensaje: 'ID de venta requerido' });
    }

    const result = await dynamo.send(
      new GetCommand({ TableName: TABLA, Key: { id } })
    );

    if (!result.Item) {
      return respuesta(404, { mensaje: `Venta con id '${id}' no encontrada` });
    }

    return respuesta(200, result.Item);
  } catch (err) {
    console.error('Error obteniendo venta:', err);
    return respuesta(500, { mensaje: 'Error interno del servidor' });
  }
};
