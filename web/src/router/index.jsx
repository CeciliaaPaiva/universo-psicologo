import { createBrowserRouter } from 'react-router-dom'
import { PrivateRoute } from './PrivateRoute'
import { LoginPage } from '@/pages/LoginPage'
import { RegisterPacientePage } from '@/pages/RegisterPacientePage'
import { RegisterPsicologoPage } from '@/pages/RegisterPsicologoPage'
import { AprovacoesPage } from '@/pages/AprovacoesPage'
import { AgendaPage } from '@/pages/AgendaPage'
import { PlantaoPage } from '@/pages/PlantaoPage'
import { PerfilPsicologoPage } from '@/pages/PerfilPsicologoPage'
import { PsicologoLayout } from '@/layouts/PsicologoLayout'

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  { path: '/cadastro/paciente', element: <RegisterPacientePage /> },
  { path: '/cadastro/psicologo', element: <RegisterPsicologoPage /> },
  {
    element: <PrivateRoute role="ADMIN" />,
    children: [{ path: '/admin/aprovacoes', element: <AprovacoesPage /> }],
  },
  {
    element: <PrivateRoute role="PSICOLOGO" />,
    children: [
      {
        element: <PsicologoLayout />,
        children: [
          { path: '/agenda', element: <AgendaPage /> },
          { path: '/plantao', element: <PlantaoPage /> },
          { path: '/perfil', element: <PerfilPsicologoPage /> },
          { path: '/', element: <AgendaPage /> },
        ],
      },
    ],
  },
  { path: '*', element: <LoginPage /> },
])
