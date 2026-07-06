import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useAuthStore } from '@/store/authStore'
import { login } from '@/services/auth'

const schema = z.object({
  email: z.string().email('E-mail inválido'),
  senha: z.string().min(1, 'Informe sua senha'),
})

const ROTA_POR_ROLE = {
  ADMIN: '/admin/aprovacoes',
  PSICOLOGO: '/agenda',
  PACIENTE: '/',
}

export function LoginPage() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((s) => s.setAuth)
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schema) })

  async function onSubmit(dados) {
    try {
      const { data } = await login(dados.email, dados.senha)
      setAuth(data)
      navigate(ROTA_POR_ROLE[data.role] ?? '/')
    } catch (error) {
      toast.error(error.response?.data?.mensagem ?? 'Não foi possível entrar')
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center p-4">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <CardTitle>Entrar</CardTitle>
          <CardDescription>Acesse sua conta na Universo Psicólogo</CardDescription>
        </CardHeader>
        <CardContent>
          <form className="flex flex-col gap-4" onSubmit={handleSubmit(onSubmit)}>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="email">E-mail</Label>
              <Input id="email" type="email" {...register('email')} />
              {errors.email && <p className="text-sm text-destructive">{errors.email.message}</p>}
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="senha">Senha</Label>
              <Input id="senha" type="password" {...register('senha')} />
              {errors.senha && <p className="text-sm text-destructive">{errors.senha.message}</p>}
            </div>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Entrando...' : 'Entrar'}
            </Button>
            <p className="text-center text-sm text-muted-foreground">
              Ainda não tem conta?{' '}
              <Link to="/cadastro/paciente" className="text-primary underline-offset-4 hover:underline">
                Cadastre-se como paciente
              </Link>{' '}
              ou{' '}
              <Link to="/cadastro/psicologo" className="text-primary underline-offset-4 hover:underline">
                como psicólogo
              </Link>
            </p>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
