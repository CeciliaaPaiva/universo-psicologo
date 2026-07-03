import { createBrowserRouter } from 'react-router-dom'
import { PrivateRoute } from './PrivateRoute'
import { LoginPage } from '@/pages/LoginPage'
import { RegisterPacientePage } from '@/pages/RegisterPacientePage'
import { RegisterPsicologoPage } from '@/pages/RegisterPsicologoPage'
import { AprovacoesPage } from '@/pages/AprovacoesPage'

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  { path: '/cadastro/paciente', element: <RegisterPacientePage /> },
  { path: '/cadastro/psicologo', element: <RegisterPsicologoPage /> },
  {
    element: <PrivateRoute role="ADMIN" />,
    children: [{ path: '/admin/aprovacoes', element: <AprovacoesPage /> }],
  },
  { path: '*', element: <LoginPage /> },
])
