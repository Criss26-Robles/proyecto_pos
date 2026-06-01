/**
 * Servicio de productos.
 * Consume GET /productos del API Gateway usando fetch nativo con async/await.
 */
import { API_BASE_URL } from '../config.js';

/**
 * Lista productos, opcionalmente filtrando por nombre o código de barras.
 * @param {string} [q] - Texto de búsqueda (opcional)
 * @returns {Promise<Array>} Lista de productos
 * @throws {Error} Si la respuesta no es exitosa o hay error de red
 */
export async function listar(q) {
  const url = q
    ? `${API_BASE_URL}/productos?q=${encodeURIComponent(q)}`
    : `${API_BASE_URL}/productos`;

  const response = await fetch(url);

  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.mensaje || `Error ${response.status}: No se pudieron cargar los productos`);
  }

  return response.json();
}
