import { api } from './api'

export function statusPlantao() {
  return api.get('/plantao/status')
}

export function criarDisponibilidade(dados) {
  return api.post('/plantao/disponibilidade', dados)
}

export function ativarDisponibilidade(id, ativo) {
  return api.patch(`/plantao/${id}/ativar`, { ativo })
}
