import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { registrarPsicologo } from '@/services/auth'

const TIPOS_ACEITOS = [
  'application/pdf',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
]
const TAMANHO_MAXIMO = 5 * 1024 * 1024

const schema = z.object({
  nome: z.string().min(1, 'Informe seu nome'),
  email: z.string().email('E-mail inválido'),
  senha: z.string().min(8, 'A senha deve ter ao menos 8 caracteres'),
  crp: z.string().min(1, 'Informe seu CRP'),
  especializacao: z.string().optional(),
  politicaCancelamento: z.string().min(1, 'Descreva sua política de cancelamento'),
  curriculo: z
    .instanceof(FileList)
    .refine((files) => files.length === 1, 'Anexe seu currículo')
    .refine((files) => files[0] && TIPOS_ACEITOS.includes(files[0].type), 'Currículo deve ser PDF ou DOCX')
    .refine((files) => files[0] && files[0].size <= TAMANHO_MAXIMO, 'Currículo deve ter no máximo 5 MB'),
})

export function RegisterPsicologoPage() {
  const navigate = useNavigate()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schema) })

  async function onSubmit(dados) {
    try {
      const { curriculo, ...resto } = dados
      await registrarPsicologo(resto, curriculo[0])
      toast.success('Cadastro enviado! Você receberá um e-mail quando for avaliado.')
      navigate('/login')
    } catch (error) {
      toast.error(error.response?.data?.mensagem ?? 'Não foi possível concluir o cadastro')
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center p-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>Cadastro de psicólogo</CardTitle>
          <CardDescription>Seu cadastro passará por avaliação da nossa equipe</CardDescription>
        </CardHeader>
        <CardContent>
          <form className="flex flex-col gap-4" onSubmit={handleSubmit(onSubmit)}>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="nome">Nome completo</Label>
              <Input id="nome" {...register('nome')} />
              {errors.nome && <p className="text-sm text-destructive">{errors.nome.message}</p>}
            </div>
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
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="crp">CRP</Label>
              <Input id="crp" placeholder="06/12345" {...register('crp')} />
              {errors.crp && <p className="text-sm text-destructive">{errors.crp.message}</p>}
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="especializacao">Especialização</Label>
              <Input id="especializacao" {...register('especializacao')} />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="politicaCancelamento">Política de cancelamento</Label>
              <Textarea
                id="politicaCancelamento"
                placeholder="Descreva como funciona o cancelamento de sessões com você"
                {...register('politicaCancelamento')}
              />
              {errors.politicaCancelamento && (
                <p className="text-sm text-destructive">{errors.politicaCancelamento.message}</p>
              )}
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="curriculo">Currículo (PDF ou DOCX, até 5 MB)</Label>
              <Input id="curriculo" type="file" accept=".pdf,.docx" {...register('curriculo')} />
              {errors.curriculo && (
                <p className="text-sm text-destructive">{errors.curriculo.message}</p>
              )}
            </div>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Enviando...' : 'Cadastrar'}
            </Button>
            <p className="text-center text-sm text-muted-foreground">
              Já tem conta?{' '}
              <Link to="/login" className="text-primary underline-offset-4 hover:underline">
                Entrar
              </Link>
            </p>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
