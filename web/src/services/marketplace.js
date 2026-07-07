import { api } from './api'

export function buscarPsicologos(especialidade) {
  return api.get('/marketplace/psicologos', { params: especialidade ? { especialidade } : {} })
}

export function buscarPsicologoPorId(id) {
  return api.get(`/marketplace/psicologos/${id}`)
}
