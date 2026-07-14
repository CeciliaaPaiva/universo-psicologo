import { api } from './api'

export function listarContatos() {
  return api.get('/mensagens/contatos')
}

export function listarConversa(outroId) {
  return api.get(`/mensagens/${outroId}`)
}

export function enviarMensagem(outroId, conteudo) {
  return api.post(`/mensagens/${outroId}`, { conteudo })
}
