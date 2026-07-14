import { api } from './api'

export function buscarPerfilPaciente() {
  return api.get('/usuarios/paciente/perfil')
}

export function atualizarPerfilPaciente(dados, foto) {
  const formData = new FormData()
  formData.append('dados', new Blob([JSON.stringify(dados)], { type: 'application/json' }))
  if (foto) {
    formData.append('foto', foto)
  }
  return api.put('/usuarios/paciente/perfil', formData)
}

export function buscarAnamnese() {
  return api.get('/usuarios/paciente/anamnese')
}

export function atualizarAnamnese(dados) {
  return api.put('/usuarios/paciente/anamnese', dados)
}
