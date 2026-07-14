import { api } from './api'

export function listarNotificacoes() {
  return api.get('/notificacoes')
}

export function contarNaoLidas() {
  return api.get('/notificacoes/nao-lidas')
}

export function marcarNotificacaoLida(id) {
  return api.post(`/notificacoes/${id}/lida`)
}

export function marcarTodasLidas() {
  return api.post('/notificacoes/lidas')
}
