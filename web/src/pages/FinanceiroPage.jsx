import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { buscarRelatorio } from '@/services/financeiro'
import { formatarMoeda } from '@/lib/formatarMoeda'

function primeiroDiaDoMes() {
  const hoje = new Date()
  return new Date(hoje.getFullYear(), hoje.getMonth(), 1).toISOString().slice(0, 10)
}

function hojeIso() {
  return new Date().toISOString().slice(0, 10)
}

function formatarData(dataIso) {
  return new Date(dataIso).toLocaleDateString('pt-BR')
}

export function FinanceiroPage() {
  const [inicio, setInicio] = useState(primeiroDiaDoMes())
  const [fim, setFim] = useState(hojeIso())
  const [periodoConsultado, setPeriodoConsultado] = useState({ inicio, fim })

  const { data, isLoading } = useQuery({
    queryKey: ['relatorio-financeiro', periodoConsultado.inicio, periodoConsultado.fim],
    queryFn: () => buscarRelatorio(periodoConsultado.inicio, periodoConsultado.fim).then((res) => res.data),
  })

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6 p-6">
      <h1 className="text-xl font-medium">Relatório financeiro</h1>

      <Card>
        <CardContent className="flex flex-wrap items-end gap-3 pt-6">
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="inicio">De</Label>
            <Input id="inicio" type="date" value={inicio} onChange={(e) => setInicio(e.target.value)} />
          </div>
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="fim">Até</Label>
            <Input id="fim" type="date" value={fim} onChange={(e) => setFim(e.target.value)} />
          </div>
          <Button onClick={() => setPeriodoConsultado({ inicio, fim })}>Filtrar</Button>
        </CardContent>
      </Card>

      {isLoading && <p className="text-muted-foreground">Carregando...</p>}

      {data && (
        <>
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Totais do período</CardTitle>
            </CardHeader>
            <CardContent className="grid grid-cols-3 gap-4 text-sm">
              <div className="flex flex-col gap-1">
                <span className="text-muted-foreground">Bruto</span>
                <span className="text-lg font-medium">{formatarMoeda(data.totalBruto)}</span>
              </div>
              <div className="flex flex-col gap-1">
                <span className="text-muted-foreground">Taxa da plataforma</span>
                <span className="text-lg font-medium">{formatarMoeda(data.totalTaxa)}</span>
              </div>
              <div className="flex flex-col gap-1">
                <span className="text-muted-foreground">Líquido</span>
                <span className="text-lg font-medium text-primary">{formatarMoeda(data.totalLiquido)}</span>
              </div>
            </CardContent>
          </Card>

          <div className="flex flex-col gap-3">
            {data.cobrancas.length === 0 && (
              <p className="text-muted-foreground">Nenhuma cobrança paga nesse período.</p>
            )}
            {data.cobrancas.map((cobranca) => (
              <Card key={cobranca.id}>
                <CardContent className="flex items-center justify-between pt-6 text-sm">
                  <div className="flex flex-col gap-1">
                    <span className="font-medium">{cobranca.nomePaciente}</span>
                    <span className="text-muted-foreground">
                      Sessão em {formatarData(cobranca.dataSessao)} · Pago em {formatarData(cobranca.pagaEm)}
                    </span>
                  </div>
                  <span className="font-medium text-primary">{formatarMoeda(cobranca.valorLiquido)}</span>
                </CardContent>
              </Card>
            ))}
          </div>
        </>
      )}
    </div>
  )
}
