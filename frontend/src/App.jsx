import { useEffect, useState } from 'react'
import { login, logout, procesarRetorno, getTokens, getAccessToken, decodificarJwt, estaExpirado } from './auth.js'
import {
  listarSolicitudes,
  crearSolicitud,
  actualizarSolicitud,
  eliminarSolicitud,
  decidirSolicitud,
} from './api.js'

export default function App() {
  const [tokens, setTokens] = useState(getTokens())
  const [error, setError] = useState(null)
  const [solicitudes, setSolicitudes] = useState([])
  const [cargando, setCargando] = useState(false)
  const [editando, setEditando] = useState(null)
  const [form, setForm] = useState({ fechaInicio: '', fechaFin: '', motivo: '' })

  useEffect(() => {
    procesarRetorno()
      .then((nuevos) => nuevos && setTokens(nuevos))
      .catch((e) => setError(e.message))
  }, [])

  const accessClaims = decodificarJwt(getAccessToken())
  const sesionActiva = Boolean(tokens) && !estaExpirado(getAccessToken())
  const esAprobador = accessClaims?.['cognito:groups']?.includes('aprobadores') ?? false

  async function cargar() {
    setCargando(true)
    const resultado = await listarSolicitudes()
    if (resultado.ok) {
      setSolicitudes(resultado.cuerpo)
    } else {
      setError(`${resultado.status}: ${JSON.stringify(resultado.cuerpo)}`)
    }
    setCargando(false)
  }

  useEffect(() => {
    if (sesionActiva) cargar()
  }, [sesionActiva])

  async function enviarFormulario(e) {
    e.preventDefault()
    setError(null)
    const resultado = editando
      ? await actualizarSolicitud(editando, form)
      : await crearSolicitud(form)

    if (resultado.ok) {
      setForm({ fechaInicio: '', fechaFin: '', motivo: '' })
      setEditando(null)
      cargar()
    } else {
      setError(`${resultado.status}: ${JSON.stringify(resultado.cuerpo)}`)
    }
  }

  function empezarEdicion(s) {
    setEditando(s.id)
    setForm({ fechaInicio: s.fechaInicio, fechaFin: s.fechaFin, motivo: s.motivo })
  }

  async function eliminar(id) {
    const resultado = await eliminarSolicitud(id)
    if (resultado.ok) cargar()
    else setError(`${resultado.status}: ${JSON.stringify(resultado.cuerpo)}`)
  }

  async function decidir(id, estado) {
    const comentario = window.prompt(
      estado === 'APROBADA' ? 'Comentario de aprobación:' : 'Motivo del rechazo:'
    )
    if (!comentario) return
    const resultado = await decidirSolicitud(id, { estado, comentario })
    if (resultado.ok) cargar()
    else setError(`${resultado.status}: ${JSON.stringify(resultado.cuerpo)}`)
  }

  return (
    <main>
      <h1>Solicitudes de Vacaciones</h1>
      {error && <p className="error">{error}</p>}

      {!sesionActiva ? (
        <button onClick={login}>Iniciar sesión con Cognito</button>
      ) : (
        <>
          <p>
            Sesión iniciada como <strong>{accessClaims?.email}</strong>
            {' '}({esAprobador ? 'aprobador' : 'solicitante'})
            {' '}<button onClick={logout}>Cerrar sesión</button>
          </p>

          {!esAprobador && (
            <section>
              <h2>{editando ? `Editar solicitud #${editando}` : 'Nueva solicitud'}</h2>
              <form onSubmit={enviarFormulario}>
                <label>
                  Fecha inicio:
                  <input
                    type="date"
                    value={form.fechaInicio}
                    onChange={(e) => setForm({ ...form, fechaInicio: e.target.value })}
                    required
                  />
                </label>
                <label>
                  Fecha fin:
                  <input
                    type="date"
                    value={form.fechaFin}
                    onChange={(e) => setForm({ ...form, fechaFin: e.target.value })}
                    required
                  />
                </label>
                <label>
                  Motivo:
                  <input
                    type="text"
                    value={form.motivo}
                    onChange={(e) => setForm({ ...form, motivo: e.target.value })}
                    required
                  />
                </label>
                <button type="submit">{editando ? 'Guardar' : 'Crear'}</button>
                {editando && (
                  <button type="button" onClick={() => { setEditando(null); setForm({ fechaInicio: '', fechaFin: '', motivo: '' }) }}>
                    Cancelar
                  </button>
                )}
              </form>
            </section>
          )}

          <section>
            <h2>{esAprobador ? 'Todas las solicitudes' : 'Mis solicitudes'}</h2>
            {cargando && <p>Cargando…</p>}
            <ul>
              {solicitudes.map((s) => (
                <li key={s.id}>
                  <strong>#{s.id}</strong> — {s.solicitanteEmail} — {s.fechaInicio} a {s.fechaFin}
                  {' '}— {s.motivo} — <em>{s.estado}</em>
                  {s.comentarioAprobador && <p>Comentario: {s.comentarioAprobador}</p>}

                  {!esAprobador && s.estado === 'PENDIENTE' && (
                    <>
                      {' '}<button onClick={() => empezarEdicion(s)}>Editar</button>
                      {' '}<button onClick={() => eliminar(s.id)}>Eliminar</button>
                    </>
                  )}

                  {esAprobador && s.estado === 'PENDIENTE' && (
                    <>
                      {' '}<button onClick={() => decidir(s.id, 'APROBADA')}>Aprobar</button>
                      {' '}<button onClick={() => decidir(s.id, 'RECHAZADA')}>Rechazar</button>
                    </>
                  )}
                </li>
              ))}
            </ul>
          </section>
        </>
      )}
    </main>
  )
}