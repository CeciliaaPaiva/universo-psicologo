import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { registrarPaciente } from '@/services/auth'

const FAIXAS = [
  { value: 'FAIXA_1', label: 'Até R$ 405,25 per capita (BPC/LOAS)' },
  { value: 'FAIXA_2', label: 'R$ 405,26 – R$ 810,50 per capita (CadÚnico/Bolsa Família)' },
  { value: 'FAIXA_3', label: 'R$ 810,51 – R$ 1.621,00 per capita (Classe E)' },
  { value: 'FAIXA_4', label: 'R$ 1.621,01 – R$ 3.242,00 per capita (Classe D)' },
  { value: 'FORA_DO_ESCOPO', label: 'Acima de R$ 3.242,00 per capita' },
]

const schema = z.object({
  nome: z.string().min(1, 'Informe seu nome'),
  email: z.string().email('E-mail inválido'),
  senha: z.string().min(8, 'A senha deve ter ao menos 8 caracteres'),
  faixaRenda: z.enum(['FAIXA_1', 'FAIXA_2', 'FAIXA_3', 'FAIXA_4', 'FORA_DO_ESCOPO'], {
    message: 'Selecione sua faixa de renda',
  }),
})

export function RegisterPacientePage() {
  const navigate = useNavigate()
  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schema) })

  const faixaRenda = watch('faixaRenda')
  const foraDoEscopo = faixaRenda === 'FORA_DO_ESCOPO'

  async function onSubmit(dados) {
    try {
      await registrarPaciente(dados)
      toast.success('Cadastro realizado! Você já pode entrar.')
      navigate('/login')
    } catch (error) {
      toast.error(error.response?.data?.mensagem ?? 'Não foi possível concluir o cadastro')
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center p-4">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <CardTitle>Cadastro de paciente</CardTitle>
          <CardDescription>Atendimento social exclusivo para famílias de baixa renda</CardDescription>
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
              <Label htmlFor="faixaRenda">Faixa de renda per capita familiar</Label>
              <Select onValueChange={(value) => setValue('faixaRenda', value, { shouldValidate: true })}>
                <SelectTrigger id="faixaRenda" className="w-full">
                  <SelectValue placeholder="Selecione sua faixa de renda">
                    {(value) => FAIXAS.find((faixa) => faixa.value === value)?.label}
                  </SelectValue>
                </SelectTrigger>
                <SelectContent>
                  {FAIXAS.map((faixa) => (
                    <SelectItem key={faixa.value} value={faixa.value}>
                      {faixa.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.faixaRenda && (
                <p className="text-sm text-destructive">{errors.faixaRenda.message}</p>
              )}
            </div>

            {foraDoEscopo && (
              <Alert variant="destructive">
                <AlertTitle>Fora do escopo de atendimento</AlertTitle>
                <AlertDescription>
                  A plataforma atende exclusivamente famílias de baixa renda (até Classe D).
                  Procure atendimento particular.
                </AlertDescription>
              </Alert>
            )}

            <Button type="submit" disabled={isSubmitting || foraDoEscopo}>
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
