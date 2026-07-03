import { api } from './api'

export function registrarPaciente(dados) {
  return api.post('/auth/register/paciente', dados)
}

export function registrarPsicologo(dados, curriculo) {
  const formData = new FormData()
  formData.append('dados', new Blob([JSON.stringify(dados)], { type: 'application/json' }))
  formData.append('curriculo', curriculo)
  return api.post('/auth/register/psicologo', formData)
}

export function login(email, senha) {
  return api.post('/auth/login', { email, senha })
}

export function listarAprovacoesPendentes() {
  return api.get('/admin/aprovacoes')
}

export function decidirAprovacao(id, decisao, motivo) {
  return api.put(`/admin/aprovacoes/${id}`, { decisao, motivo })
}
