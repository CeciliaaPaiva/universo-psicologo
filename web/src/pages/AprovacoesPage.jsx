import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useAuthStore } from '@/store/authStore'
import { decidirAprovacao, listarAprovacoesPendentes } from '@/services/auth'

function PsicologoCard({ psicologo }) {
  const queryClient = useQueryClient()
  const [acaoComMotivo, setAcaoComMotivo] = useState(null)
  const [motivo, setMotivo] = useState('')

  const mutation = useMutation({
    mutationFn: ({ decisao, motivo }) => decidirAprovacao(psicologo.id, decisao, motivo),
    onSuccess: () => {
      toast.success('Decisão registrada')
      queryClient.invalidateQueries({ queryKey: ['aprovacoes-pendentes'] })
    },
    onError: (error) => {
      toast.error(error.response?.data?.mensagem ?? 'Não foi possível registrar a decisão')
    },
  })

  function confirmarComMotivo() {
    if (!motivo.trim()) return
    mutation.mutate({ decisao: acaoComMotivo, motivo })
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>{psicologo.nome}</CardTitle>
        <CardDescription>
          {psicologo.email} · CRP {psicologo.crp}
          {psicologo.especializacao ? ` · ${psicologo.especializacao}` : ''}
        </CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-3">
        <p className="text-sm">
          <span className="font-medium">Política de cancelamento: </span>
          {psicologo.politicaCancelamento}
        </p>
        <a
          href={psicologo.curriculoUrl}
          target="_blank"
          rel="noreferrer"
          className="text-sm text-primary underline-offset-4 hover:underline w-fit"
        >
          Ver currículo
        </a>

        {acaoComMotivo ? (
          <div className="flex flex-col gap-2">
            <Textarea
              placeholder={
                acaoComMotivo === 'REPROVAR' ? 'Motivo da reprovação' : 'O que precisa ser complementado'
              }
              value={motivo}
              onChange={(e) => setMotivo(e.target.value)}
            />
            <div className="flex gap-2">
              <Button
                size="sm"
                variant="destructive"
                disabled={mutation.isPending}
                onClick={confirmarComMotivo}
              >
                Confirmar
              </Button>
              <Button
                size="sm"
                variant="ghost"
                onClick={() => {
                  setAcaoComMotivo(null)
                  setMotivo('')
                }}
              >
                Cancelar
              </Button>
            </div>
          </div>
        ) : (
          <div className="flex gap-2">
            <Button size="sm" disabled={mutation.isPending} onClick={() => mutation.mutate({ decisao: 'APROVAR' })}>
              Aprovar
            </Button>
            <Button size="sm" variant="destructive" onClick={() => setAcaoComMotivo('REPROVAR')}>
              Reprovar
            </Button>
            <Button size="sm" variant="outline" onClick={() => setAcaoComMotivo('SOLICITAR_COMPLEMENTACAO')}>
              Solicitar complementação
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  )
}

export function AprovacoesPage() {
  const usuario = useAuthStore((s) => s.usuario)
  const { data, isLoading } = useQuery({
    queryKey: ['aprovacoes-pendentes'],
    queryFn: () => listarAprovacoesPendentes().then((res) => res.data),
  })

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-4 p-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-medium">Cadastros pendentes de aprovação</h1>
        <Badge variant="secondary">{usuario?.nome}</Badge>
      </div>

      {isLoading && <p className="text-muted-foreground">Carregando...</p>}
      {!isLoading && data?.length === 0 && (
        <p className="text-muted-foreground">Nenhum cadastro pendente no momento.</p>
      )}

      <div className="flex flex-col gap-4">
        {data?.map((psicologo) => (
          <PsicologoCard key={psicologo.id} psicologo={psicologo} />
        ))}
      </div>
    </div>
  )
}
