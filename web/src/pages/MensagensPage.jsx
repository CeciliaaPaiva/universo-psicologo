import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent } from '@/components/ui/card'
import { listarConversa, enviarMensagem } from '@/services/mensagem'
import { useAuthStore } from '@/store/authStore'
import { decodificarUsuarioId } from '@/lib/jwt'

function formatarHora(dataHoraIso) {
  return new Date(dataHoraIso).toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function MensagensPage() {
  const { outroId } = useParams()
  const [texto, setTexto] = useState('')
  const queryClient = useQueryClient()
  const accessToken = useAuthStore((s) => s.accessToken)
  const meuId = decodificarUsuarioId(accessToken)

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['mensagens-conversa', outroId],
    queryFn: () => listarConversa(outroId).then((res) => res.data),
    refetchInterval: 5000,
  })

  const enviarMutation = useMutation({
    mutationFn: (conteudo) => enviarMensagem(outroId, conteudo),
    onSuccess: () => {
      setTexto('')
      queryClient.invalidateQueries({ queryKey: ['mensagens-conversa', outroId] })
      queryClient.invalidateQueries({ queryKey: ['mensagens-contatos'] })
    },
    onError: (err) => toast.error(err.response?.data?.mensagem ?? 'Não foi possível enviar a mensagem'),
  })

  function handleEnviar(e) {
    e.preventDefault()
    if (!texto.trim()) return
    enviarMutation.mutate(texto.trim())
  }

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-4 p-6">
      <Link to="/mensagens" className="text-sm text-muted-foreground hover:underline">
        ← Voltar
      </Link>
      <h1 className="text-xl font-medium">Conversa</h1>

      {isError && (
        <p className="text-sm text-destructive">
          {error.response?.data?.mensagem ??
            'Este chat só é liberado depois que uma sessão entre vocês for agendada e paga.'}
        </p>
      )}

      {isLoading && <p className="text-muted-foreground">Carregando...</p>}

      {data && (
        <Card>
          <CardContent className="flex max-h-[60vh] flex-col gap-3 overflow-y-auto py-4">
            {data.length === 0 && (
              <p className="text-center text-sm text-muted-foreground">Nenhuma mensagem ainda.</p>
            )}
            {data.map((mensagem) => {
              const minha = mensagem.remetenteId === meuId
              return (
                <div key={mensagem.id} className={`flex ${minha ? 'justify-end' : 'justify-start'}`}>
                  <div
                    className={`flex max-w-[75%] flex-col gap-1 rounded-lg px-3 py-2 text-sm ${
                      minha ? 'bg-primary text-primary-foreground' : 'bg-muted text-foreground'
                    }`}
                  >
                    <span>{mensagem.conteudo}</span>
                    <span className="text-[10px] opacity-70">{formatarHora(mensagem.criadaEm)}</span>
                  </div>
                </div>
              )
            })}
          </CardContent>
        </Card>
      )}

      {data && (
        <form className="flex items-end gap-2" onSubmit={handleEnviar}>
          <Input
            placeholder="Digite sua mensagem..."
            value={texto}
            onChange={(e) => setTexto(e.target.value)}
            disabled={enviarMutation.isPending}
          />
          <Button type="submit" disabled={enviarMutation.isPending || !texto.trim()}>
            Enviar
          </Button>
        </form>
      )}
    </div>
  )
}
