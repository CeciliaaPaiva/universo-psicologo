import { useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { ModalidadeSelector } from '@/components/marketplace/ModalidadeSelector'
import { TipoAtendimentoSelector } from '@/components/marketplace/TipoAtendimentoSelector'
import { buscarPsicologoPorId } from '@/services/marketplace'
import { criarSessao } from '@/services/agendamento'
import { formatarMoeda } from '@/lib/formatarMoeda'

function formatarSlot(dataHoraIso) {
  return new Date(dataHoraIso).toLocaleString('pt-BR', {
    weekday: 'long',
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function PsicologoPublicProfilePage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [slotSelecionado, setSlotSelecionado] = useState(null)
  const [modalidade, setModalidade] = useState('AVULSA')
  const [tipoAtendimento, setTipoAtendimento] = useState('INDIVIDUAL')

  const { data, isLoading } = useQuery({
    queryKey: ['psicologo-perfil', id],
    queryFn: () => buscarPsicologoPorId(id).then((res) => res.data),
  })

  const agendarMutation = useMutation({
    mutationFn: () => criarSessao({ slotId: slotSelecionado.id, modalidade, tipoAtendimento }),
    onSuccess: () => {
      toast.success('Sessão agendada com sucesso')
      queryClient.invalidateQueries({ queryKey: ['psicologo-perfil', id] })
      setSlotSelecionado(null)
      navigate('/agendamentos')
    },
    onError: (error) => toast.error(error.response?.data?.mensagem ?? 'Não foi possível agendar a sessão'),
  })

  if (isLoading) {
    return <p className="p-6 text-muted-foreground">Carregando...</p>
  }

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6 p-6">
      <Link to="/marketplace" className="text-sm text-muted-foreground hover:underline">
        ← Voltar para a busca
      </Link>

      <Card>
        <CardHeader>
          <CardTitle>{data.nome}</CardTitle>
          {data.especializacao && <CardDescription>{data.especializacao}</CardDescription>}
          {data.areasAtuacao?.length > 0 && (
            <div className="flex flex-wrap gap-1.5 pt-1">
              {data.areasAtuacao.map((area) => (
                <Badge key={area} variant="secondary">
                  {area}
                </Badge>
              ))}
            </div>
          )}
        </CardHeader>
        <CardContent className="flex flex-col gap-3 text-sm">
          {data.politicaCancelamento && (
            <div>
              <p className="font-medium">Política de cancelamento</p>
              <p className="text-muted-foreground">{data.politicaCancelamento}</p>
            </div>
          )}
          {data.linkVideochamada && (
            <div>
              <p className="font-medium">Videochamada</p>
              <p className="text-muted-foreground">Link enviado por e-mail após a confirmação do agendamento</p>
            </div>
          )}
          <div className="flex gap-4">
            <Badge variant="secondary">Avulsa: {formatarMoeda(data.valorAvulsa)}</Badge>
            <Badge variant="secondary">Pacote: {formatarMoeda(data.valorPacotePorSessao)}/sessão</Badge>
          </div>
        </CardContent>
      </Card>

      <div className="flex flex-col gap-2">
        <h2 className="text-lg font-medium">Horários disponíveis</h2>
        {data.slotsDisponiveis.length === 0 && (
          <p className="text-muted-foreground">Nenhum horário disponível no momento.</p>
        )}
        {data.slotsDisponiveis.map((slot) => (
          <div key={slot.id} className="flex items-center justify-between rounded-md border p-3">
            <span className="text-sm capitalize">{formatarSlot(slot.inicio)}</span>
            <Button size="sm" onClick={() => setSlotSelecionado(slot)}>
              Agendar
            </Button>
          </div>
        ))}
      </div>

      <Dialog open={!!slotSelecionado} onOpenChange={(open) => !open && setSlotSelecionado(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Confirmar agendamento</DialogTitle>
            <DialogDescription>
              {slotSelecionado && `${data.nome} · ${formatarSlot(slotSelecionado.inicio)}`}
            </DialogDescription>
          </DialogHeader>
          <TipoAtendimentoSelector value={tipoAtendimento} onChange={setTipoAtendimento} />
          <ModalidadeSelector
            valorAvulsa={
              tipoAtendimento === 'CASAL' ? data.valorAvulsa * 2 : data.valorAvulsa
            }
            valorPacotePorSessao={
              tipoAtendimento === 'CASAL' ? data.valorPacotePorSessao * 2 : data.valorPacotePorSessao
            }
            value={modalidade}
            onChange={setModalidade}
          />
          <DialogFooter>
            <Button
              disabled={agendarMutation.isPending}
              onClick={() => agendarMutation.mutate()}
            >
              Confirmar
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
