import { api } from './api'

export function listarCobrancas() {
  return api.get('/financeiro/cobrancas')
}

export function pagarCobranca(id) {
  return api.post(`/financeiro/cobrancas/${id}/pagar`)
}

export function cancelarCobranca(id) {
  return api.post(`/financeiro/cobrancas/${id}/cancelar`)
}

export function buscarRelatorio(inicio, fim) {
  return api.get('/financeiro/relatorio', { params: { inicio, fim } })
}
