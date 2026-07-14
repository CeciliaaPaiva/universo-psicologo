import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { listarCobrancas, pagarCobranca } from '@/services/financeiro'
import { formatarMoeda } from '@/lib/formatarMoeda'

const LABEL_STATUS = {
  PENDENTE: { label: 'Pendente', variant: 'secondary' },
  PAGO: { label: 'Pago', variant: 'default' },
  CANCELADO: { label: 'Cancelado', variant: 'destructive' },
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

export function CobrancasPage() {
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['cobrancas'],
    queryFn: () => listarCobrancas().then((res) => res.data),
  })

  const pagarMutation = useMutation({
    mutationFn: (id) => pagarCobranca(id),
    onSuccess: () => {
      toast.success('Pagamento confirmado')
      queryClient.invalidateQueries({ queryKey: ['cobrancas'] })
    },
    onError: (error) => toast.error(error.response?.data?.mensagem ?? 'Não foi possível confirmar o pagamento'),
  })

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6 p-6">
      <h1 className="text-xl font-medium">Minhas cobranças</h1>

      {isLoading && <p className="text-muted-foreground">Carregando...</p>}
      {!isLoading && data?.length === 0 && (
        <p className="text-muted-foreground">Você ainda não tem cobranças.</p>
      )}

      <div className="flex flex-col gap-4">
        {data?.map((cobranca) => {
          const status = LABEL_STATUS[cobranca.status]
          return (
            <Card key={cobranca.id}>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle className="text-base capitalize">{formatarDataHora(cobranca.dataSessao)}</CardTitle>
                  <Badge variant={status.variant}>{status.label}</Badge>
                </div>
              </CardHeader>
              <CardContent className="flex flex-col gap-2 text-sm">
                <span>Psicólogo: {cobranca.nomePsicologo}</span>
                <span>Valor: {formatarMoeda(cobranca.valorBruto)}</span>
                {cobranca.status === 'PENDENTE' && (
                  <Button
                    size="sm"
                    className="mt-1 w-fit"
                    disabled={pagarMutation.isPending}
                    onClick={() => pagarMutation.mutate(cobranca.id)}
                  >
                    Confirmar pagamento
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
