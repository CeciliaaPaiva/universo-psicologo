import { api } from './api'

export function criarSessao({ slotId, modalidade, tipoAtendimento }) {
  return api.post('/agenda/sessoes', { slotId, modalidade, tipoAtendimento })
}

export function listarAgendamentos() {
  return api.get('/agenda/sessoes')
}
