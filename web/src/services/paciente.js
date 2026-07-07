import { api } from './api'

export function buscarPerfilPaciente() {
  return api.get('/usuarios/paciente/perfil')
}

export function atualizarPerfilPaciente(dados) {
  return api.put('/usuarios/paciente/perfil', dados)
}
