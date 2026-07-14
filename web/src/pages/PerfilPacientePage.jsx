import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import {
  atualizarAnamnese,
  atualizarPerfilPaciente,
  buscarAnamnese,
  buscarPerfilPaciente,
} from '@/services/paciente'

const LABEL_FAIXA_RENDA = {
  FAIXA_1: 'Até R$ 405,25 per capita (BPC/LOAS)',
  FAIXA_2: 'R$ 405,26 – R$ 810,50 per capita (CadÚnico/Bolsa Família)',
  FAIXA_3: 'R$ 810,51 – R$ 1.621,00 per capita (Classe E)',
  FAIXA_4: 'R$ 1.621,01 – R$ 3.242,00 per capita (Classe D)',
  FORA_DO_ESCOPO: 'Acima de R$ 3.242,00 per capita',
}

const perfilSchema = z.object({
  nome: z.string().min(1, 'Informe seu nome'),
  idade: z.coerce.number({ message: 'Informe sua idade' }).int().min(0).max(120),
  foto: z.instanceof(FileList).optional(),
})

const anamneseSchema = z
  .object({
    jaFezTerapia: z.enum(['sim', 'nao'], { message: 'Informe se você já fez terapia antes' }),
    motivoBusca: z.string().optional(),
    medicacaoControlada: z.string().optional(),
    contatoResponsavel: z.string().optional(),
  })

function PerfilForm({ data }) {
  const queryClient = useQueryClient()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(perfilSchema) })

  useEffect(() => {
    if (data) {
      reset({ nome: data.nome, idade: data.idade ?? undefined })
    }
  }, [data, reset])

  const salvarMutation = useMutation({
    mutationFn: ({ foto, ...dados }) => atualizarPerfilPaciente(dados, foto?.[0]),
    onSuccess: () => {
      toast.success('Perfil atualizado')
      queryClient.invalidateQueries({ queryKey: ['perfil-paciente'] })
    },
    onError: (error) => toast.error(error.response?.data?.mensagem ?? 'Não foi possível atualizar o perfil'),
  })

  return (
    <Card>
      <CardHeader>
        <CardTitle>Meu perfil</CardTitle>
        <CardDescription>{data?.email}</CardDescription>
      </CardHeader>
      <CardContent>
        <form className="flex flex-col gap-4" onSubmit={handleSubmit((dados) => salvarMutation.mutate(dados))}>
          {data?.fotoUrl && (
            <img src={data.fotoUrl} alt="Foto de perfil" className="size-20 rounded-full object-cover" />
          )}
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="foto">Foto de perfil</Label>
            <Input id="foto" type="file" accept="image/png,image/jpeg,image/webp" {...register('foto')} />
          </div>

          <div className="flex flex-col gap-1.5">
            <Label htmlFor="nome">Nome</Label>
            <Input id="nome" {...register('nome')} />
            {errors.nome && <p className="text-sm text-destructive">{errors.nome.message}</p>}
          </div>

          <div className="flex flex-col gap-1.5">
            <Label htmlFor="idade">Idade</Label>
            <Input id="idade" type="number" min="0" max="120" {...register('idade')} />
            {errors.idade && <p className="text-sm text-destructive">{errors.idade.message}</p>}
          </div>

          <div className="flex flex-col gap-1.5">
            <Label>Faixa de renda per capita familiar</Label>
            <p className="rounded-md border bg-muted px-3 py-2 text-sm text-muted-foreground">
              {LABEL_FAIXA_RENDA[data?.faixaRenda] ?? '—'}
            </p>
            <p className="text-xs text-muted-foreground">
              A faixa de renda é autodeclarada uma única vez no cadastro e não pode ser editada por aqui. Se sua
              situação financeira mudou, converse com seu psicólogo para solicitar uma revisão de perfil.
            </p>
          </div>

          <Button type="submit" disabled={isSubmitting || salvarMutation.isPending}>
            Salvar
          </Button>
        </form>
      </CardContent>
    </Card>
  )
}

function AnamneseForm({ menorDeIdade }) {
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['anamnese-paciente'],
    queryFn: () => buscarAnamnese().then((res) => res.data),
  })

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(anamneseSchema) })

  useEffect(() => {
    if (data) {
      reset({
        jaFezTerapia: data.jaFezTerapia == null ? undefined : data.jaFezTerapia ? 'sim' : 'nao',
        motivoBusca: data.motivoBusca ?? '',
        medicacaoControlada: data.medicacaoControlada ?? '',
        contatoResponsavel: data.contatoResponsavel ?? '',
      })
    }
  }, [data, reset])

  const jaFezTerapia = watch('jaFezTerapia')

  const salvarMutation = useMutation({
    mutationFn: (dados) =>
      atualizarAnamnese({
        jaFezTerapia: dados.jaFezTerapia === 'sim',
        motivoBusca: dados.motivoBusca || null,
        medicacaoControlada: dados.medicacaoControlada || null,
        contatoResponsavel: dados.contatoResponsavel || null,
      }),
    onSuccess: () => {
      toast.success('Anamnese salva')
      queryClient.invalidateQueries({ queryKey: ['anamnese-paciente'] })
    },
    onError: (error) => toast.error(error.response?.data?.mensagem ?? 'Não foi possível salvar sua anamnese'),
  })

  if (isLoading) {
    return null
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Anamnese</CardTitle>
        <CardDescription>
          Preencha seu perfil com sua anamnese. Essa informação não será pública — o profissional só tem acesso
          quando você agendar e efetuar o pagamento, antes da primeira terapia. Depois disso, ele não terá mais
          acesso.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form className="flex flex-col gap-4" onSubmit={handleSubmit((dados) => salvarMutation.mutate(dados))}>
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="jaFezTerapia">Você já fez terapia antes?</Label>
            <Select
              key={jaFezTerapia ?? 'carregando'}
              value={jaFezTerapia}
              onValueChange={(value) => setValue('jaFezTerapia', value, { shouldValidate: true })}
            >
              <SelectTrigger id="jaFezTerapia" className="w-full">
                <SelectValue placeholder="Selecione uma opção">
                  {(value) => (value === 'sim' ? 'Sim' : value === 'nao' ? 'Não' : undefined)}
                </SelectValue>
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="sim">Sim</SelectItem>
                <SelectItem value="nao">Não</SelectItem>
              </SelectContent>
            </Select>
            {errors.jaFezTerapia && <p className="text-sm text-destructive">{errors.jaFezTerapia.message}</p>}
          </div>

          <div className="flex flex-col gap-1.5">
            <Label htmlFor="motivoBusca">O que te motiva a buscar terapia agora?</Label>
            <Textarea id="motivoBusca" {...register('motivoBusca')} />
          </div>

          <div className="flex flex-col gap-1.5">
            <Label htmlFor="medicacaoControlada">Você toma alguma medicação controlada? Qual?</Label>
            <Textarea id="medicacaoControlada" {...register('medicacaoControlada')} />
          </div>

          {menorDeIdade && (
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="contatoResponsavel">Contato do responsável</Label>
              <p className="text-xs text-muted-foreground">
                Como você se identificou como menor de idade, pedimos o contato do seu responsável: o atendimento a
                menores exige a presença/consentimento do responsável, especialmente na primeira sessão.
              </p>
              <Input id="contatoResponsavel" placeholder="Telefone ou e-mail" {...register('contatoResponsavel')} />
            </div>
          )}

          <Button type="submit" disabled={isSubmitting || salvarMutation.isPending}>
            Salvar anamnese
          </Button>
        </form>
      </CardContent>
    </Card>
  )
}

export function PerfilPacientePage() {
  const { data, isLoading } = useQuery({
    queryKey: ['perfil-paciente'],
    queryFn: () => buscarPerfilPaciente().then((res) => res.data),
  })

  if (isLoading) {
    return <p className="p-6 text-muted-foreground">Carregando...</p>
  }

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6 p-6">
      <PerfilForm data={data} />

      {data?.menorDeIdade && (
        <Alert>
          <AlertTitle>Atendimento a menores de idade</AlertTitle>
          <AlertDescription>
            Sua primeira sessão precisa contar com a presença do seu responsável. É por isso que pedimos o contato
            dele na anamnese abaixo.
          </AlertDescription>
        </Alert>
      )}

      <AnamneseForm menorDeIdade={data?.menorDeIdade} />
    </div>
  )
}
