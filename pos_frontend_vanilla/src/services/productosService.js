/**
 * Servicio de productos.
 * Consume GET /productos del API Gateway usando fetch nativo con async/await.
 */
import { API_BASE_URL } from '../config.js';

/**
 * Lista todos los productos disponibles en el catálogo.
 * @returns {Promise<Array>} Lista de productos
 * @throws {Error} Si la respuesta no es exitosa o hay error de red
 */
export async function listar() {
  const response = await fetch(`${API_BASE_URL}/productos`);

  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.mensaje || `Error ${response.status}: No se pudieron cargar los productos`);
  }

  return response.json();
}
