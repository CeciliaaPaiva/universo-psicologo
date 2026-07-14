import { api } from './api'

export function buscarPsicologos(areaAtuacao) {
  return api.get('/marketplace/psicologos', { params: areaAtuacao ? { areaAtuacao } : {} })
}

export function buscarPsicologoPorId(id) {
  return api.get(`/marketplace/psicologos/${id}`)
}
