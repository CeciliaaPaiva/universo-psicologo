import { useQuery } from '@tanstack/react-query'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { listarAgendamentos } from '@/services/agendamento'
import { formatarMoeda } from '@/lib/formatarMoeda'

const LABEL_STATUS = {
  AGENDADA: { label: 'Agendada', variant: 'secondary' },
  REALIZADA: { label: 'Realizada', variant: 'outline' },
  CANCELADA: { label: 'Cancelada', variant: 'destructive' },
}

const LABEL_MODALIDADE = {
  AVULSA: 'Avulsa',
  PACOTE_MENSAL: 'Pacote mensal',
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

export function AgendamentosPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['agendamentos'],
    queryFn: () => listarAgendamentos().then((res) => res.data),
  })

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6 p-6">
      <h1 className="text-xl font-medium">Meus agendamentos</h1>

      {isLoading && <p className="text-muted-foreground">Carregando...</p>}
      {!isLoading && data?.length === 0 && (
        <p className="text-muted-foreground">Você ainda não tem sessões agendadas.</p>
      )}

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
              <CardContent className="flex flex-col gap-1 text-sm">
                <span>Psicólogo: {sessao.nomePsicologo}</span>
                <span>Modalidade: {LABEL_MODALIDADE[sessao.modalidade]}</span>
                <span>Valor: {formatarMoeda(sessao.valorSessao)}</span>
                {sessao.linkVideochamada && sessao.status === 'AGENDADA' && (
                  <a
                    href={sessao.linkVideochamada}
                    target="_blank"
                    rel="noreferrer"
                    className="text-primary underline-offset-4 hover:underline"
                  >
                    Link da videochamada
                  </a>
                )}
              </CardContent>
            </Card>
          )
        })}
      </div>
    </div>
  )
}
