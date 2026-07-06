import { api } from './api'

export function buscarPerfilPsicologo() {
  return api.get('/usuarios/psicologo/perfil')
}

export function atualizarPerfilPsicologo(dados, foto) {
  const formData = new FormData()
  formData.append('dados', new Blob([JSON.stringify(dados)], { type: 'application/json' }))
  if (foto) {
    formData.append('foto', foto)
  }
  return api.put('/usuarios/psicologo/perfil', formData)
}
