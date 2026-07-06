import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { useSearchParams } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { cancelarSlot, criarSlots, listarSlots } from '@/services/agenda'

const schema = z
  .object({
    data: z.string().min(1, 'Informe a data'),
    horaInicio: z.string().min(1, 'Informe o horário de início'),
    horaFim: z.string().min(1, 'Informe o horário de fim'),
  })
  .refine((dados) => dados.horaFim > dados.horaInicio, {
    message: 'O horário de fim deve ser depois do início',
    path: ['horaFim'],
  })

function agruparPorDia(slots) {
  const grupos = new Map()
  for (const slot of slots) {
    const dia = slot.inicio.slice(0, 10)
    if (!grupos.has(dia)) grupos.set(dia, [])
    grupos.get(dia).push(slot)
  }
  return [...grupos.entries()].sort(([a], [b]) => a.localeCompare(b))
}

function formatarHora(dataHoraIso) {
  return dataHoraIso.slice(11, 16)
}

function formatarDia(dia) {
  return new Date(dia + 'T00:00:00').toLocaleDateString('pt-BR', {
    weekday: 'long',
    day: '2-digit',
    month: '2-digit',
  })
}

export function AgendaPage() {
  const [searchParams] = useSearchParams()
  const queryClient = useQueryClient()
  const [cancelandoId, setCancelandoId] = useState(null)
  const [motivo, setMotivo] = useState('')

  const { data, isLoading } = useQuery({
    queryKey: ['agenda-slots'],
    queryFn: () => listarSlots().then((res) => res.data),
  })

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schema) })

  const criarMutation = useMutation({
    mutationFn: (pedido) =>
      criarSlots([
        {
          inicio: `${pedido.data}T${pedido.horaInicio}:00`,
          fim: `${pedido.data}T${pedido.horaFim}:00`,
        },
      ]),
    onSuccess: () => {
      toast.success('Slot criado com sucesso')
      queryClient.invalidateQueries({ queryKey: ['agenda-slots'] })
      reset()
    },
    onError: (error) => toast.error(error.response?.data?.mensagem ?? 'Não foi possível criar o slot'),
  })

  const cancelarMutation = useMutation({
    mutationFn: ({ id, motivo }) => cancelarSlot(id, motivo),
    onSuccess: () => {
      toast.success('Slot cancelado')
      queryClient.invalidateQueries({ queryKey: ['agenda-slots'] })
      setCancelandoId(null)
      setMotivo('')
    },
    onError: (error) => toast.error(error.response?.data?.mensagem ?? 'Não foi possível cancelar o slot'),
  })

  const grupos = agruparPorDia(data ?? [])

  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-6 p-6">
      {searchParams.get('google') === 'conectado' && (
        <Badge className="w-fit" variant="secondary">
          Google Calendar conectado com sucesso
        </Badge>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Novo horário disponível</CardTitle>
        </CardHeader>
        <CardContent>
          <form
            className="flex flex-wrap items-end gap-3"
            onSubmit={handleSubmit((dados) => criarMutation.mutate(dados))}
          >
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="data">Data</Label>
              <Input id="data" type="date" {...register('data')} />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="horaInicio">Início</Label>
              <Input id="horaInicio" type="time" {...register('horaInicio')} />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="horaFim">Fim</Label>
              <Input id="horaFim" type="time" {...register('horaFim')} />
            </div>
            <Button type="submit" disabled={isSubmitting || criarMutation.isPending}>
              Adicionar
            </Button>
          </form>
          {(errors.data || errors.horaInicio || errors.horaFim) && (
            <p className="mt-2 text-sm text-destructive">
              {errors.data?.message || errors.horaInicio?.message || errors.horaFim?.message}
            </p>
          )}
        </CardContent>
      </Card>

      <div className="flex flex-col gap-4">
        <h1 className="text-xl font-medium">Minha agenda</h1>
        {isLoading && <p className="text-muted-foreground">Carregando...</p>}
        {!isLoading && grupos.length === 0 && (
          <p className="text-muted-foreground">Nenhum horário cadastrado ainda.</p>
        )}
        {grupos.map(([dia, slots]) => (
          <Card key={dia}>
            <CardHeader>
              <CardTitle className="text-base capitalize">{formatarDia(dia)}</CardTitle>
            </CardHeader>
            <CardContent className="flex flex-col gap-2">
              {slots.map((slot) => (
                <div key={slot.id} className="flex flex-col gap-2 rounded-md border p-2">
                  <div className="flex items-center justify-between">
                    <span className="text-sm">
                      {formatarHora(slot.inicio)} – {formatarHora(slot.fim)}
                    </span>
                    <div className="flex items-center gap-2">
                      <Badge variant={slot.disponivel ? 'secondary' : 'default'}>
                        {slot.disponivel ? 'Disponível' : 'Ocupado'}
                      </Badge>
                      {slot.sincronizadoGoogleCalendar && <Badge variant="outline">Google Calendar</Badge>}
                      {cancelandoId !== slot.id && (
                        <Button size="sm" variant="destructive" onClick={() => setCancelandoId(slot.id)}>
                          Cancelar
                        </Button>
                      )}
                    </div>
                  </div>
                  {cancelandoId === slot.id && (
                    <div className="flex items-center gap-2">
                      <Input
                        placeholder="Motivo (opcional)"
                        value={motivo}
                        onChange={(e) => setMotivo(e.target.value)}
                      />
                      <Button
                        size="sm"
                        disabled={cancelarMutation.isPending}
                        onClick={() => cancelarMutation.mutate({ id: slot.id, motivo })}
                      >
                        Confirmar
                      </Button>
                      <Button size="sm" variant="ghost" onClick={() => setCancelandoId(null)}>
                        Voltar
                      </Button>
                    </div>
                  )}
                </div>
              ))}
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
}
