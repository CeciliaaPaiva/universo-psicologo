import { api } from './api'

export function listarProntuarios() {
  return api.get('/prontuario/pacientes')
}

export function criarProntuario(codinome) {
  return api.post('/prontuario/pacientes', { codinome })
}

export function atualizarCodinome(codinome, novoCodinome) {
  return api.put(`/prontuario/${encodeURIComponent(codinome)}`, { novoCodinome })
}

export function listarAnotacoes(codinome, busca) {
  return api.get(`/prontuario/${encodeURIComponent(codinome)}/anotacoes`, { params: busca ? { busca } : {} })
}

export function criarAnotacao(codinome, conteudo) {
  return api.post(`/prontuario/${encodeURIComponent(codinome)}/anotacoes`, { conteudo })
}
