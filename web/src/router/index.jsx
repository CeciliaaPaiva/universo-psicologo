import { createBrowserRouter } from 'react-router-dom'
import { PrivateRoute } from './PrivateRoute'
import { LoginPage } from '@/pages/LoginPage'
import { RegisterPacientePage } from '@/pages/RegisterPacientePage'
import { RegisterPsicologoPage } from '@/pages/RegisterPsicologoPage'
import { AprovacoesPage } from '@/pages/AprovacoesPage'
import { AgendaPage } from '@/pages/AgendaPage'
import { PlantaoPage } from '@/pages/PlantaoPage'
import { PerfilPsicologoPage } from '@/pages/PerfilPsicologoPage'
import { ProntuarioPage } from '@/pages/ProntuarioPage'
import { ProntuarioDetalhePage } from '@/pages/ProntuarioDetalhePage'
import { PsicologoLayout } from '@/layouts/PsicologoLayout'
import { MarketplacePage } from '@/pages/MarketplacePage'
import { PsicologoPublicProfilePage } from '@/pages/PsicologoPublicProfilePage'
import { AgendamentosPage } from '@/pages/AgendamentosPage'
import { PerfilPacientePage } from '@/pages/PerfilPacientePage'
import { PacienteLayout } from '@/layouts/PacienteLayout'
import { ChatbotPage } from '@/pages/ChatbotPage'
import { SessoesPage } from '@/pages/SessoesPage'
import { FinanceiroPage } from '@/pages/FinanceiroPage'
import { CobrancasPage } from '@/pages/CobrancasPage'

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  { path: '/cadastro/paciente', element: <RegisterPacientePage /> },
  { path: '/cadastro/psicologo', element: <RegisterPsicologoPage /> },
  { path: '/chatbot', element: <ChatbotPage /> },
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
          { path: '/sessoes', element: <SessoesPage /> },
          { path: '/financeiro', element: <FinanceiroPage /> },
          { path: '/plantao', element: <PlantaoPage /> },
          { path: '/perfil', element: <PerfilPsicologoPage /> },
          { path: '/prontuario', element: <ProntuarioPage /> },
          { path: '/prontuario/:codinome', element: <ProntuarioDetalhePage /> },
          { path: '/', element: <AgendaPage /> },
        ],
      },
    ],
  },
  {
    element: <PrivateRoute role="PACIENTE" />,
    children: [
      {
        element: <PacienteLayout />,
        children: [
          { path: '/marketplace', element: <MarketplacePage /> },
          { path: '/marketplace/:id', element: <PsicologoPublicProfilePage /> },
          { path: '/agendamentos', element: <AgendamentosPage /> },
          { path: '/cobrancas', element: <CobrancasPage /> },
          { path: '/perfil-paciente', element: <PerfilPacientePage /> },
        ],
      },
    ],
  },
  { path: '*', element: <LoginPage /> },
])
