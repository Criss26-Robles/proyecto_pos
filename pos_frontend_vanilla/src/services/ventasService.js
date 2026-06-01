/**
 * Servicio de ventas.
 * Consume POST /ventas del API Gateway usando fetch nativo con async/await.
 */
import { API_BASE_URL } from '../config.js';

/**
 * Registra una nueva venta en el sistema.
 * @param {Object} payload - Datos de la venta
 * @param {Array}  payload.productos - Items del carrito
 * @param {number} payload.total - Total de la venta
 * @param {string} payload.metodoPago - Método de pago seleccionado
 * @param {string} payload.fecha - Fecha en formato ISO8601
 * @returns {Promise<Object>} Venta registrada con id generado
 * @throws {Error} Si la respuesta no es exitosa o hay error de red
 */
export async function crear(payload) {
  const response = await fetch(`${API_BASE_URL}/ventas`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.mensaje || `Error ${response.status}: No se pudo registrar la venta`);
  }

  return response.json();
}
