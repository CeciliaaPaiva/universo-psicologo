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

export function listarSessoesPsicologo() {
  return api.get('/agenda/sessoes/psicologo')
}

export function marcarSessaoRealizada(id) {
  return api.post(`/agenda/sessoes/${id}/realizar`)
}

export function buscarAnamnesePaciente(pacienteId) {
  return api.get(`/usuarios/psicologo/pacientes/${pacienteId}/anamnese`)
}
