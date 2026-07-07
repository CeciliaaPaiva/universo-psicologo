import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { atualizarPerfilPaciente, buscarPerfilPaciente } from '@/services/paciente'

const FAIXAS = [
  { value: 'FAIXA_1', label: 'Até R$ 405,25 per capita (BPC/LOAS)' },
  { value: 'FAIXA_2', label: 'R$ 405,26 – R$ 810,50 per capita (CadÚnico/Bolsa Família)' },
  { value: 'FAIXA_3', label: 'R$ 810,51 – R$ 1.621,00 per capita (Classe E)' },
  { value: 'FAIXA_4', label: 'R$ 1.621,01 – R$ 3.242,00 per capita (Classe D)' },
  { value: 'FORA_DO_ESCOPO', label: 'Acima de R$ 3.242,00 per capita' },
]

const schema = z.object({
  faixaRenda: z.enum(['FAIXA_1', 'FAIXA_2', 'FAIXA_3', 'FAIXA_4', 'FORA_DO_ESCOPO'], {
    message: 'Selecione sua faixa de renda',
  }),
})

export function PerfilPacientePage() {
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['perfil-paciente'],
    queryFn: () => buscarPerfilPaciente().then((res) => res.data),
  })

  const {
    handleSubmit,
    watch,
    setValue,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schema) })

  useEffect(() => {
    if (data) {
      reset({ faixaRenda: data.faixaRenda })
    }
  }, [data, reset])

  const faixaRenda = watch('faixaRenda')
  const foraDoEscopo = faixaRenda === 'FORA_DO_ESCOPO'

  const salvarMutation = useMutation({
    mutationFn: (dados) => atualizarPerfilPaciente(dados),
    onSuccess: () => {
      toast.success('Perfil atualizado')
      queryClient.invalidateQueries({ queryKey: ['perfil-paciente'] })
    },
    onError: (error) => toast.error(error.response?.data?.mensagem ?? 'Não foi possível atualizar o perfil'),
  })

  if (isLoading) {
    return <p className="p-6 text-muted-foreground">Carregando...</p>
  }

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6 p-6">
      <Card>
        <CardHeader>
          <CardTitle>Meu perfil</CardTitle>
          <CardDescription>
            {data?.nome} · {data?.email}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form
            className="flex flex-col gap-4"
            onSubmit={handleSubmit((dados) => salvarMutation.mutate(dados))}
          >
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="faixaRenda">Faixa de renda per capita familiar</Label>
              <Select
                value={faixaRenda}
                onValueChange={(value) => setValue('faixaRenda', value, { shouldValidate: true })}
              >
                <SelectTrigger id="faixaRenda" className="w-full">
                  <SelectValue placeholder="Selecione sua faixa de renda" />
                </SelectTrigger>
                <SelectContent>
                  {FAIXAS.map((faixa) => (
                    <SelectItem key={faixa.value} value={faixa.value}>
                      {faixa.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.faixaRenda && <p className="text-sm text-destructive">{errors.faixaRenda.message}</p>}
            </div>

            {foraDoEscopo && (
              <Alert variant="destructive">
                <AlertTitle>Fora do escopo de atendimento</AlertTitle>
                <AlertDescription>
                  A plataforma atende exclusivamente famílias de baixa renda (até Classe D). Essa alteração não
                  pode ser salva.
                </AlertDescription>
              </Alert>
            )}

            <p className="text-xs text-muted-foreground">
              Alterar sua faixa de renda recalcula o valor das próximas sessões — sessões já agendadas mantêm o
              valor original.
            </p>

            <Button type="submit" disabled={isSubmitting || salvarMutation.isPending || foraDoEscopo}>
              Salvar
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
