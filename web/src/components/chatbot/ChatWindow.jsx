import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Card, CardContent } from '@/components/ui/card'
import { ChatMessage } from './ChatMessage'
import { ChatInput } from './ChatInput'
import { enviarMensagem } from '@/services/chatbot'

const MENSAGEM_INICIAL = {
  role: 'model',
  conteudo:
    'Oi! Eu sou o assistente de triagem da Universo Psicólogo. Estou aqui para te ouvir, sem julgamentos. ' +
    'Como você está se sentindo hoje?',
}

export function ChatWindow() {
  const [mensagens, setMensagens] = useState([MENSAGEM_INICIAL])
  const [sessionId, setSessionId] = useState(null)
  const [contato, setContato] = useState('')
  const [ultimaResposta, setUltimaResposta] = useState(null)

  const enviarMutation = useMutation({
    mutationFn: (mensagem) => enviarMensagem({ sessionId, mensagem, contato: contato || undefined }),
    onSuccess: ({ data }) => {
      setSessionId(data.sessionId)
      setUltimaResposta(data)
      setMensagens((atual) => [...atual, { role: 'model', conteudo: data.resposta }])
    },
    onError: (error) => {
      if (error.response?.status === 429) {
        toast.error('Muitas mensagens em pouco tempo. Aguarde um minuto e tente novamente.')
      } else {
        toast.error('Não foi possível enviar sua mensagem. Tente novamente.')
      }
    },
  })

  function handleEnviar(texto) {
    setMensagens((atual) => [...atual, { role: 'user', conteudo: texto }])
    enviarMutation.mutate(texto)
  }

  return (
    <div className="flex flex-col gap-4">
      <Card>
        <CardContent className="flex max-h-[60vh] flex-col gap-3 overflow-y-auto py-4">
          {mensagens.map((mensagem, indice) => (
            <ChatMessage key={indice} role={mensagem.role} conteudo={mensagem.conteudo} />
          ))}
          {enviarMutation.isPending && <p className="text-xs text-muted-foreground">Digitando...</p>}
        </CardContent>
      </Card>

      {ultimaResposta?.crise && ultimaResposta.plantaoAcionado && (
        <Alert>
          <AlertTitle>Um psicólogo de plantão foi avisado</AlertTitle>
          <AlertDescription>
            Encontramos um profissional disponível agora e ele foi notificado para entrar em
            contato o quanto antes. Se você deixou um contato, fique de olho nele.
          </AlertDescription>
        </Alert>
      )}

      {ultimaResposta?.crise && !ultimaResposta.plantaoAcionado && (
        <Alert variant="destructive">
          <AlertTitle>Não há psicólogo de plantão no momento</AlertTitle>
          <AlertDescription>
            Se você está em perigo imediato, ligue agora:
            <div className="mt-2 flex flex-col gap-1">
              {ultimaResposta.contatosEmergencia.map((contatoEmergencia) => (
                <span key={contatoEmergencia} className="font-medium">
                  {contatoEmergencia}
                </span>
              ))}
            </div>
          </AlertDescription>
        </Alert>
      )}

      {ultimaResposta && !ultimaResposta.crise && ultimaResposta.sugerirMarketplace && (
        <Alert>
          <AlertTitle>Quer encontrar um psicólogo agora?</AlertTitle>
          <AlertDescription className="flex items-center justify-between gap-3">
            <span>Você pode buscar um profissional para terapia social a qualquer momento.</span>
            <Button size="sm" nativeButton={false} render={<Link to="/cadastro/paciente" />}>
              Buscar psicólogo
            </Button>
          </AlertDescription>
        </Alert>
      )}

      <div className="flex flex-col gap-1.5">
        <Label htmlFor="contato" className="text-xs text-muted-foreground">
          Contato para retorno em caso de crise (opcional)
        </Label>
        <Input
          id="contato"
          placeholder="Telefone ou e-mail"
          value={contato}
          onChange={(e) => setContato(e.target.value)}
        />
      </div>

      <ChatInput disabled={enviarMutation.isPending} onEnviar={handleEnviar} />
    </div>
  )
}
