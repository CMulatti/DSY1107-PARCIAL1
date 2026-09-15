import { getAccessToken } from './auth.js'
import { getConfig } from './config.js'

/** Respuesta uniforme para poder mostrar SIEMPRE el código HTTP en pantalla. */
async function ejecutar(descripcion, promesa) {
  try {
    const respuesta = await promesa
    const texto = await respuesta.text()
    let cuerpo
    try {
      cuerpo = JSON.parse(texto)
    } catch {
      cuerpo = texto
    }
    return {descripcion, status: respuesta.status, ok: respuesta.ok, cuerpo}
  } catch (error) {
    return {descripcion, status: 0, ok: false, cuerpo: `Error de red o CORS: ${error.message}`}
  }
}

function headersConToken(extra = {}) {
  return {
    Authorization: `Bearer ${getAccessToken()}`,
    ...extra,
  }
}

/** Lista: el solicitante ve las suyas, el aprobador ve todas. El backend decide según el token. */
export function listarSolicitudes() {
  const { apiUrl } = getConfig()
  return ejecutar(
    'GET /solicitudes',
    fetch(`${apiUrl}/solicitudes`, { headers: headersConToken() })
  )
}

export function obtenerSolicitud(id) {
  const { apiUrl } = getConfig()
  return ejecutar(
    `GET /solicitudes/${id}`,
    fetch(`${apiUrl}/solicitudes/${id}`, { headers: headersConToken() })
  )
}

export function crearSolicitud({ fechaInicio, fechaFin, motivo }) {
  const { apiUrl } = getConfig()
  return ejecutar(
    'POST /solicitudes',
    fetch(`${apiUrl}/solicitudes`, {
      method: 'POST',
      headers: headersConToken({ 'Content-Type': 'application/json' }),
      body: JSON.stringify({ fechaInicio, fechaFin, motivo }),
    })
  )
}

export function actualizarSolicitud(id, { fechaInicio, fechaFin, motivo }) {
  const { apiUrl } = getConfig()
  return ejecutar(
    `PUT /solicitudes/${id}`,
    fetch(`${apiUrl}/solicitudes/${id}`, {
      method: 'PUT',
      headers: headersConToken({ 'Content-Type': 'application/json' }),
      body: JSON.stringify({ fechaInicio, fechaFin, motivo }),
    })
  )
}

export function eliminarSolicitud(id) {
  const { apiUrl } = getConfig()
  return ejecutar(
    `DELETE /solicitudes/${id}`,
    fetch(`${apiUrl}/solicitudes/${id}`, {
      method: 'DELETE',
      headers: headersConToken(),
    })
  )
}

/** Solo un aprobador puede llamar esto; si no tiene el scope, el API Gateway responde 403. */
export function decidirSolicitud(id, { estado, comentario }) {
  const { apiUrl } = getConfig()
  return ejecutar(
    `PUT /solicitudes/${id}/decision`,
    fetch(`${apiUrl}/solicitudes/${id}/decision`, {
      method: 'PUT',
      headers: headersConToken({ 'Content-Type': 'application/json' }),
      body: JSON.stringify({ estado, comentario }),
    })
  )
}