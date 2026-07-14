export function decodificarUsuarioId(token) {
  if (!token) return null
  try {
    const payload = token.split('.')[1]
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(json).sub ?? null
  } catch {
    return null
  }
}
