import { api } from './api'

export function enviarMensagem({ sessionId, mensagem, contato }) {
  return api.post('/chatbot/message', { sessionId, mensagem, contato })
}
