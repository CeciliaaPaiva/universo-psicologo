import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { ativarDisponibilidade, criarDisponibilidade, statusPlantao } from '@/services/plantao'

const DIAS_SEMANA = [
  { value: 'SEG', label: 'Segunda-feira' },
  { value: 'TER', label: 'Terça-feira' },
  { value: 'QUA', label: 'Quarta-feira' },
  { value: 'QUI', label: 'Quinta-feira' },
  { value: 'SEX', label: 'Sexta-feira' },
  { value: 'SAB', label: 'Sábado' },
  { value: 'DOM', label: 'Domingo' },
]

const LABEL_DIA = Object.fromEntries(DIAS_SEMANA.map((d) => [d.value, d.label]))

export function PlantaoPage() {
  const queryClient = useQueryClient()
  const [diaSemana, setDiaSemana] = useState('')

  const { data, isLoading } = useQuery({
    queryKey: ['plantao-status'],
    queryFn: () => statusPlantao().then((res) => res.data),
  })

  const criarMutation = useMutation({
    mutationFn: () => criarDisponibilidade({ diaSemana, dataEspecifica: null }),
    onSuccess: () => {
      toast.success('Dia de plantão adicionado')
      queryClient.invalidateQueries({ queryKey: ['plantao-status'] })
      setDiaSemana('')
    },
    onError: (error) => toast.error(error.response?.data?.mensagem ?? 'Não foi possível adicionar'),
  })

  const ativarMutation = useMutation({
    mutationFn: ({ id, ativo }) => ativarDisponibilidade(id, ativo),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['plantao-status'] })
    },
    onError: (error) => toast.error(error.response?.data?.mensagem ?? 'Não foi possível atualizar'),
  })

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6 p-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-medium">Plantão de urgência</h1>
        {!isLoading && (
          <Badge variant={data?.plantaoAtivoHoje ? 'secondary' : 'outline'}>
            {data?.plantaoAtivoHoje ? 'Ativo hoje' : 'Inativo hoje'}
          </Badge>
        )}
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Adicionar dia de plantão</CardTitle>
        </CardHeader>
        <CardContent className="flex items-end gap-3">
          <Select value={diaSemana} onValueChange={setDiaSemana}>
            <SelectTrigger className="w-full">
              <SelectValue placeholder="Selecione o dia da semana" />
            </SelectTrigger>
            <SelectContent>
              {DIAS_SEMANA.map((dia) => (
                <SelectItem key={dia.value} value={dia.value}>
                  {dia.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Button disabled={!diaSemana || criarMutation.isPending} onClick={() => criarMutation.mutate()}>
            Adicionar
          </Button>
        </CardContent>
      </Card>

      <div className="flex flex-col gap-2">
        {isLoading && <p className="text-muted-foreground">Carregando...</p>}
        {!isLoading && data?.disponibilidades.length === 0 && (
          <p className="text-muted-foreground">Nenhum dia de plantão cadastrado.</p>
        )}
        {data?.disponibilidades.map((disponibilidade) => (
          <div key={disponibilidade.id} className="flex items-center justify-between rounded-md border p-3">
            <span className="text-sm">
              {disponibilidade.diaSemana ? LABEL_DIA[disponibilidade.diaSemana] : disponibilidade.dataEspecifica}
            </span>
            <div className="flex items-center gap-2">
              <Badge variant={disponibilidade.ativo ? 'secondary' : 'outline'}>
                {disponibilidade.ativo ? 'Ativo' : 'Inativo'}
              </Badge>
              <Button
                size="sm"
                variant="outline"
                disabled={ativarMutation.isPending}
                onClick={() =>
                  ativarMutation.mutate({ id: disponibilidade.id, ativo: !disponibilidade.ativo })
                }
              >
                {disponibilidade.ativo ? 'Desativar' : 'Ativar'}
              </Button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
