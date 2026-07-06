import { api } from './api'

export function listarSlots() {
  return api.get('/agenda/slots')
}

export function criarSlots(slots) {
  return api.post('/agenda/slots', slots)
}

export function cancelarSlot(id, motivo) {
  return api.delete(`/agenda/slots/${id}`, { params: motivo ? { motivo } : {} })
}

export function obterUrlGoogleCalendar() {
  return api.get('/agenda/google/auth-url')
}
