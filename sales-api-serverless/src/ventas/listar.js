'use strict';

const { DynamoDBClient } = require('@aws-sdk/client-dynamodb');
const { DynamoDBDocumentClient, ScanCommand } = require('@aws-sdk/lib-dynamodb');

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
 * GET /ventas
 */
exports.handler = async (event) => {
  try {
    const result = await dynamo.send(new ScanCommand({ TableName: TABLA }));
    const ventas = (result.Items || []).sort(
      (a, b) => new Date(b.fecha) - new Date(a.fecha)
    );
    return respuesta(200, ventas);
  } catch (err) {
    console.error('Error listando ventas:', err);
    return respuesta(500, { mensaje: 'Error interno del servidor' });
  }
};
