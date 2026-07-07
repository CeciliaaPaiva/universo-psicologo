import { api } from './api'

export function criarSessao({ slotId, modalidade }) {
  return api.post('/agenda/sessoes', { slotId, modalidade })
}

export function listarAgendamentos() {
  return api.get('/agenda/sessoes')
}
