import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { listarSessoesPsicologo, marcarSessaoRealizada } from '@/services/agenda'
import { formatarMoeda } from '@/lib/formatarMoeda'

const LABEL_STATUS = {
  AGENDADA: { label: 'Agendada', variant: 'secondary' },
  REALIZADA: { label: 'Realizada', variant: 'outline' },
  CANCELADA: { label: 'Cancelada', variant: 'destructive' },
}

function formatarDataHora(dataHoraIso) {
  return new Date(dataHoraIso).toLocaleString('pt-BR', {
    weekday: 'short',
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function SessoesPage() {
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['sessoes-psicologo'],
    queryFn: () => listarSessoesPsicologo().then((res) => res.data),
  })

  const realizarMutation = useMutation({
    mutationFn: (id) => marcarSessaoRealizada(id),
    onSuccess: () => {
      toast.success('Sessão marcada como realizada — cobrança gerada')
      queryClient.invalidateQueries({ queryKey: ['sessoes-psicologo'] })
    },
    onError: (error) => toast.error(error.response?.data?.mensagem ?? 'Não foi possível marcar a sessão'),
  })

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6 p-6">
      <h1 className="text-xl font-medium">Minhas sessões</h1>

      {isLoading && <p className="text-muted-foreground">Carregando...</p>}
      {!isLoading && data?.length === 0 && <p className="text-muted-foreground">Nenhuma sessão ainda.</p>}

      <div className="flex flex-col gap-4">
        {data?.map((sessao) => {
          const status = LABEL_STATUS[sessao.status]
          return (
            <Card key={sessao.id}>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle className="text-base capitalize">{formatarDataHora(sessao.inicio)}</CardTitle>
                  <Badge variant={status.variant}>{status.label}</Badge>
                </div>
              </CardHeader>
              <CardContent className="flex flex-col gap-2 text-sm">
                <span>Paciente: {sessao.nomePaciente}</span>
                <span>Valor: {formatarMoeda(sessao.valorSessao)}</span>
                {sessao.status === 'AGENDADA' && (
                  <Button
                    size="sm"
                    className="mt-1 w-fit"
                    disabled={realizarMutation.isPending}
                    onClick={() => realizarMutation.mutate(sessao.id)}
                  >
                    Marcar como realizada
                  </Button>
                )}
              </CardContent>
            </Card>
          )
        })}
      </div>
    </div>
  )
}
