'use strict';

const { DynamoDBClient } = require('@aws-sdk/client-dynamodb');
const { DynamoDBDocumentClient, PutCommand, ScanCommand } = require('@aws-sdk/lib-dynamodb');
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

const PRODUCTOS_INICIALES = [
  { codigo_barras: 'P001', nombre: 'Coca-Cola 600ml',       precio: 15.00, stock: 100 },
  { codigo_barras: 'P002', nombre: 'Pepsi 600ml',           precio: 14.00, stock: 80  },
  { codigo_barras: 'P003', nombre: 'Agua Mineral 500ml',    precio: 8.00,  stock: 200 },
  { codigo_barras: 'P004', nombre: 'Jugo de Naranja 1L',    precio: 22.00, stock: 50  },
  { codigo_barras: 'P005', nombre: 'Leche Entera 1L',       precio: 18.00, stock: 60  },
  { codigo_barras: 'P006', nombre: 'Pan Integral',          precio: 25.00, stock: 40  },
  { codigo_barras: 'P007', nombre: 'Galletas Oreo',         precio: 12.00, stock: 120 },
  { codigo_barras: 'P008', nombre: 'Chocolate Snickers',    precio: 20.00, stock: 90  },
  { codigo_barras: 'P009', nombre: 'Café Instantáneo 200g', precio: 45.00, stock: 30  },
  { codigo_barras: 'P010', nombre: 'Cereal Corn Flakes 500g', precio: 55.00, stock: 25 },
];

/**
 * POST /admin/inicializar
 * Carga el catálogo inicial de productos (solo si la tabla está vacía)
 */
exports.handler = async (event) => {
  try {
    const existentes = await dynamo.send(
      new ScanCommand({ TableName: TABLA, Select: 'COUNT' })
    );

    if (existentes.Count > 0) {
      return respuesta(200, {
        mensaje: `La tabla ya tiene ${existentes.Count} productos. No se realizaron cambios.`,
      });
    }

    const promesas = PRODUCTOS_INICIALES.map((p) =>
      dynamo.send(
        new PutCommand({
          TableName: TABLA,
          Item: {
            id: uuidv4(),
            ...p,
            creadoEn: new Date().toISOString(),
          },
        })
      )
    );

    await Promise.all(promesas);

    return respuesta(201, {
      mensaje: `${PRODUCTOS_INICIALES.length} productos cargados exitosamente.`,
    });
  } catch (err) {
    console.error('Error inicializando datos:', err);
    return respuesta(500, { mensaje: 'Error interno del servidor' });
  }
};
