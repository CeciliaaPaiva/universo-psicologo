import { NavLink, Outlet } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { NotificationBell } from '@/components/NotificationBell'
import { useAuthStore } from '@/store/authStore'

const LINKS = [
  { to: '/marketplace', label: 'Encontrar psicólogo' },
  { to: '/agendamentos', label: 'Meus agendamentos' },
  { to: '/cobrancas', label: 'Cobranças' },
  { to: '/perfil-paciente', label: 'Perfil' },
]

export function PacienteLayout() {
  const usuario = useAuthStore((s) => s.usuario)
  const clearAuth = useAuthStore((s) => s.clearAuth)

  return (
    <div className="min-h-screen">
      <header className="border-b">
        <div className="mx-auto flex max-w-3xl items-center justify-between p-4">
          <div className="flex items-center gap-6">
            <img src="/unipsi-logo.jpg" alt="Universo Psicólogo" className="h-14 w-14 object-contain" />
            <nav className="flex gap-4">
              {LINKS.map((link) => (
                <NavLink
                  key={link.to}
                  to={link.to}
                  className={({ isActive }) =>
                    `text-sm font-medium ${isActive ? 'text-primary underline underline-offset-4' : 'text-muted-foreground'}`
                  }
                >
                  {link.label}
                </NavLink>
              ))}
            </nav>
          </div>
          <div className="flex items-center gap-3">
            <NotificationBell />
            <span className="text-sm text-muted-foreground">{usuario?.nome}</span>
            <Button size="sm" variant="ghost" onClick={clearAuth}>
              Sair
            </Button>
          </div>
        </div>
      </header>
      <Outlet />
    </div>
  )
}
