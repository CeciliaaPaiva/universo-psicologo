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
    mutationFn: (mensagem) => enviarMensagem({ sessionId, mensagem, contato }),
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

  const contatoValido = contato.trim().length > 0

  function handleEnviar(texto) {
    if (!contatoValido) {
      toast.error('Informe um telefone ou e-mail de contato antes de conversar.')
      return
    }
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

      {ultimaResposta?.crise && ultimaResposta.profissionalAcionado && (
        <Alert>
          <AlertTitle>Um profissional foi avisado</AlertTitle>
          <AlertDescription>
            Encontramos um psicólogo e ele foi notificado agora mesmo. <strong>Aguarde alguns minutos</strong> —
            ele vai entrar em contato o quanto antes, seja pelo contato que você deixou ou por aqui mesmo.
            Enquanto isso, continue respirando com calma; você não está sozinho(a).
          </AlertDescription>
        </Alert>
      )}

      {ultimaResposta?.crise && !ultimaResposta.profissionalAcionado && (
        <Alert variant="destructive">
          <AlertTitle>Não encontramos um profissional disponível agora</AlertTitle>
          <AlertDescription className="flex flex-col gap-3">
            <span>
              No momento não há nenhum psicólogo de plantão nem horário próximo disponível. Recomendamos que
              você{' '}
              <Link to="/cadastro/paciente" className="font-medium underline underline-offset-4">
                agende uma sessão
              </Link>{' '}
              o quanto antes para ter acompanhamento profissional.
            </span>
            <span>
              Se a situação for muito urgente e você precisar de ajuda imediata, entre em contato com o CVV:
            </span>
            <div className="flex flex-col gap-1">
              {ultimaResposta.contatosEmergencia.map((contatoEmergencia) => (
                <a
                  key={contatoEmergencia.url}
                  href={contatoEmergencia.url}
                  target={contatoEmergencia.url.startsWith('tel:') ? undefined : '_blank'}
                  rel="noreferrer"
                  className="font-medium underline underline-offset-4"
                >
                  {contatoEmergencia.label}
                </a>
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
          Telefone ou e-mail de contato <span className="text-destructive">*</span>
        </Label>
        <Input
          id="contato"
          placeholder="Telefone ou e-mail"
          value={contato}
          onChange={(e) => setContato(e.target.value)}
          required
        />
        <p className="text-xs text-muted-foreground">
          Precisamos desse contato para que um psicólogo possa falar com você em caso de crise.
        </p>
      </div>

      <ChatInput disabled={enviarMutation.isPending || !contatoValido} onEnviar={handleEnviar} />

      <p className="text-center text-xs text-muted-foreground">
        Precisa de ajuda imediata? CVV —{' '}
        <a
          href="https://cvv.org.br/chat/"
          target="_blank"
          rel="noreferrer"
          className="font-medium underline underline-offset-4"
        >
          chat online
        </a>{' '}
        ou{' '}
        <a href="tel:188" className="font-medium underline underline-offset-4">
          ligue 188
        </a>{' '}
        (24h, gratuito)
      </p>
    </div>
  )
}
