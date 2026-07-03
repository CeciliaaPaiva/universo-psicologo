import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'

export function PrivateRoute({ role }) {
  const { accessToken, usuario } = useAuthStore()

  if (!accessToken) {
    return <Navigate to="/login" replace />
  }
  if (role && usuario?.role !== role) {
    return <Navigate to="/login" replace />
  }
  return <Outlet />
}
