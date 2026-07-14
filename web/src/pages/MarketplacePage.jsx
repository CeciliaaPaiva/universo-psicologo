import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { buscarPsicologos } from '@/services/marketplace'
import { formatarMoeda } from '@/lib/formatarMoeda'

function formatarDisponibilidade(dataHoraIso) {
  return new Date(dataHoraIso).toLocaleString('pt-BR', {
    weekday: 'short',
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function MarketplacePage() {
  const [areaAtuacao, setAreaAtuacao] = useState('')

  const { data, isLoading } = useQuery({
    queryKey: ['marketplace-psicologos', areaAtuacao],
    queryFn: () => buscarPsicologos(areaAtuacao).then((res) => res.data),
  })

  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-6 p-6">
      <div className="flex flex-col gap-1.5">
        <h1 className="text-xl font-medium">Encontre um psicólogo</h1>
        <p className="text-sm text-muted-foreground">
          Profissionais aprovados com horários disponíveis para terapia social
        </p>
      </div>

      <div className="flex flex-col gap-1.5">
        <Label htmlFor="areaAtuacao">Áreas de atuação</Label>
        <Input
          id="areaAtuacao"
          placeholder="Ex.: ansiedade, luto, terapia de casal..."
          value={areaAtuacao}
          onChange={(e) => setAreaAtuacao(e.target.value)}
        />
      </div>

      <div className="flex flex-col gap-4">
        {isLoading && <p className="text-muted-foreground">Carregando...</p>}
        {!isLoading && data?.length === 0 && (
          <p className="text-muted-foreground">Nenhum psicólogo disponível com esse filtro no momento.</p>
        )}
        {data?.map((psicologo) => (
          <Card key={psicologo.id}>
            <CardHeader>
              <CardTitle>{psicologo.nome}</CardTitle>
              {psicologo.especializacao && <CardDescription>{psicologo.especializacao}</CardDescription>}
              {psicologo.areasAtuacao?.length > 0 && (
                <div className="flex flex-wrap gap-1.5 pt-1">
                  {psicologo.areasAtuacao.map((area) => (
                    <Badge key={area} variant="secondary">
                      {area}
                    </Badge>
                  ))}
                </div>
              )}
            </CardHeader>
            <CardContent className="flex flex-col gap-3">
              <div className="flex flex-wrap gap-2">
                {psicologo.proximasDisponibilidades.map((disponibilidade) => (
                  <Badge key={disponibilidade} variant="outline">
                    {formatarDisponibilidade(disponibilidade)}
                  </Badge>
                ))}
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">
                  Avulsa: {formatarMoeda(psicologo.valorAvulsa)} · Pacote:{' '}
                  {formatarMoeda(psicologo.valorPacotePorSessao)}/sessão
                </span>
                <Button size="sm" nativeButton={false} render={<Link to={`/marketplace/${psicologo.id}`} />}>
                  Ver perfil
                </Button>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
}
