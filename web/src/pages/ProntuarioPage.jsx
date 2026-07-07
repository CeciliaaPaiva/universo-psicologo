import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { criarProntuario, listarProntuarios } from '@/services/prontuario'

const schema = z.object({
  codinome: z.string().min(1, 'Informe um codinome'),
})

export function ProntuarioPage() {
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['prontuarios'],
    queryFn: () => listarProntuarios().then((res) => res.data),
  })

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schema) })

  const criarMutation = useMutation({
    mutationFn: (dados) => criarProntuario(dados.codinome),
    onSuccess: () => {
      toast.success('Prontuário criado')
      queryClient.invalidateQueries({ queryKey: ['prontuarios'] })
      reset()
    },
    onError: (error) => toast.error(error.response?.data?.mensagem ?? 'Não foi possível criar o prontuário'),
  })

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6 p-6">
      <Card>
        <CardHeader>
          <CardTitle>Novo paciente no prontuário</CardTitle>
        </CardHeader>
        <CardContent>
          <form
            className="flex flex-wrap items-end gap-3"
            onSubmit={handleSubmit((dados) => criarMutation.mutate(dados))}
          >
            <div className="flex flex-1 flex-col gap-1.5">
              <Label htmlFor="codinome">Codinome</Label>
              <Input id="codinome" placeholder="Ex.: Girassol" {...register('codinome')} />
              {errors.codinome && <p className="text-sm text-destructive">{errors.codinome.message}</p>}
            </div>
            <Button type="submit" disabled={isSubmitting || criarMutation.isPending}>
              Criar
            </Button>
          </form>
          <p className="mt-2 text-xs text-muted-foreground">
            O nome real do paciente nunca é usado no prontuário — apenas o codinome que você definir aqui.
          </p>
        </CardContent>
      </Card>

      <div className="flex flex-col gap-2">
        <h1 className="text-xl font-medium">Meus pacientes</h1>
        {isLoading && <p className="text-muted-foreground">Carregando...</p>}
        {!isLoading && data?.length === 0 && (
          <p className="text-muted-foreground">Nenhum paciente cadastrado no prontuário ainda.</p>
        )}
        {data?.map((prontuario) => (
          <Link key={prontuario.id} to={`/prontuario/${encodeURIComponent(prontuario.codinome)}`}>
            <Card className="transition-colors hover:bg-muted/50">
              <CardContent className="flex items-center justify-between py-4">
                <span className="font-medium">{prontuario.codinome}</span>
                <span className="text-sm text-muted-foreground">
                  {prontuario.totalAnotacoes}{' '}
                  {prontuario.totalAnotacoes === 1 ? 'anotação' : 'anotações'}
                </span>
              </CardContent>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  )
}
