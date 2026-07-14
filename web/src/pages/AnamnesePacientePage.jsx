import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { buscarAnamnesePaciente } from '@/services/agenda'

const LABEL_JA_FEZ_TERAPIA = { true: 'Sim', false: 'Não' }

export function AnamnesePacientePage() {
  const { pacienteId } = useParams()

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['anamnese-paciente', pacienteId],
    queryFn: () => buscarAnamnesePaciente(pacienteId).then((res) => res.data),
    retry: false,
  })

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6 p-6">
      <Link to="/sessoes" className="text-sm text-muted-foreground hover:underline">
        ← Voltar
      </Link>
      <h1 className="text-xl font-medium">Anamnese do paciente</h1>

      {isLoading && <p className="text-muted-foreground">Carregando...</p>}

      {isError && (
        <Alert variant="destructive">
          <AlertTitle>Acesso não disponível</AlertTitle>
          <AlertDescription>
            {error.response?.data?.mensagem ??
              'Você só pode ver a anamnese enquanto houver uma sessão agendada com este paciente.'}
          </AlertDescription>
        </Alert>
      )}

      {data && !data.preenchida && (
        <p className="text-muted-foreground">Este paciente ainda não preencheu a anamnese.</p>
      )}

      {data && data.preenchida && (
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Respostas</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col gap-3 text-sm">
            <div>
              <span className="font-medium">Já fez terapia antes: </span>
              {LABEL_JA_FEZ_TERAPIA[data.jaFezTerapia] ?? 'Não informado'}
            </div>
            <div>
              <span className="font-medium">Motivo de buscar terapia agora: </span>
              {data.motivoBusca || 'Não informado'}
            </div>
            <div>
              <span className="font-medium">Toma medicação controlada: </span>
              {data.medicacaoControlada || 'Não informado'}
            </div>
            {data.menorDeIdade && (
              <div>
                <span className="font-medium">Contato do responsável: </span>
                {data.contatoResponsavel || 'Não informado'}
              </div>
            )}
          </CardContent>
        </Card>
      )}

      <p className="text-xs text-muted-foreground">
        Este acesso é temporário — válido só enquanto a sessão com este paciente ainda não foi
        realizada. Cada leitura fica registrada em auditoria.
      </p>
    </div>
  )
}
