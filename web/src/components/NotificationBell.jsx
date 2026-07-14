import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Bell } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  contarNaoLidas,
  listarNotificacoes,
  marcarNotificacaoLida,
  marcarTodasLidas,
} from '@/services/notificacao'

function formatarDataHora(dataHoraIso) {
  return new Date(dataHoraIso).toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function NotificationBell() {
  const [aberto, setAberto] = useState(false)
  const queryClient = useQueryClient()

  const { data: contador } = useQuery({
    queryKey: ['notificacoes-nao-lidas'],
    queryFn: () => contarNaoLidas().then((res) => res.data.naoLidas),
    refetchInterval: 30_000,
  })

  const { data: notificacoes } = useQuery({
    queryKey: ['notificacoes'],
    queryFn: () => listarNotificacoes().then((res) => res.data),
    enabled: aberto,
  })

  const marcarLidaMutation = useMutation({
    mutationFn: (id) => marcarNotificacaoLida(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notificacoes'] })
      queryClient.invalidateQueries({ queryKey: ['notificacoes-nao-lidas'] })
    },
  })

  const marcarTodasMutation = useMutation({
    mutationFn: () => marcarTodasLidas(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notificacoes'] })
      queryClient.invalidateQueries({ queryKey: ['notificacoes-nao-lidas'] })
    },
  })

  return (
    <div className="relative">
      <Button size="sm" variant="ghost" className="relative" onClick={() => setAberto((v) => !v)}>
        <Bell className="h-4 w-4" />
        {contador > 0 && (
          <span className="absolute -right-1 -top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-destructive px-1 text-[10px] font-medium text-destructive-foreground">
            {contador}
          </span>
        )}
      </Button>

      {aberto && (
        <>
          <button
            type="button"
            aria-label="Fechar notificações"
            className="fixed inset-0 z-40 cursor-default"
            onClick={() => setAberto(false)}
          />
          <div className="absolute right-0 z-50 mt-2 flex max-h-96 w-80 flex-col overflow-hidden rounded-md border bg-popover shadow-md">
            <div className="flex items-center justify-between border-b px-3 py-2">
              <span className="text-sm font-medium">Notificações</span>
              {notificacoes?.some((n) => !n.lida) && (
                <button
                  type="button"
                  className="text-xs text-primary hover:underline"
                  onClick={() => marcarTodasMutation.mutate()}
                >
                  Marcar todas como lidas
                </button>
              )}
            </div>
            <div className="flex flex-col overflow-y-auto">
              {notificacoes?.length === 0 && (
                <p className="px-3 py-4 text-center text-sm text-muted-foreground">
                  Nenhuma notificação ainda.
                </p>
              )}
              {notificacoes?.map((notificacao) => (
                <button
                  key={notificacao.id}
                  type="button"
                  className={`flex flex-col gap-0.5 border-b px-3 py-2 text-left text-sm last:border-b-0 hover:bg-muted ${
                    notificacao.lida ? 'text-muted-foreground' : 'font-medium'
                  }`}
                  onClick={() => !notificacao.lida && marcarLidaMutation.mutate(notificacao.id)}
                >
                  <span>{notificacao.mensagem}</span>
                  <span className="text-xs text-muted-foreground">
                    {formatarDataHora(notificacao.criadaEm)}
                  </span>
                </button>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  )
}
