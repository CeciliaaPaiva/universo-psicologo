import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { obterUrlGoogleCalendar } from '@/services/agenda'
import { atualizarPerfilPsicologo, buscarPerfilPsicologo } from '@/services/usuario'

const schema = z.object({
  especializacao: z.string().optional(),
  politicaCancelamento: z.string().min(1, 'Descreva sua política de cancelamento'),
  linkVideochamada: z.string().optional(),
  foto: z.instanceof(FileList).optional(),
})

export function PerfilPsicologoPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['perfil-psicologo'],
    queryFn: () => buscarPerfilPsicologo().then((res) => res.data),
  })

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schema) })

  useEffect(() => {
    if (data) {
      reset({
        especializacao: data.especializacao ?? '',
        politicaCancelamento: data.politicaCancelamento ?? '',
        linkVideochamada: data.linkVideochamada ?? '',
      })
    }
  }, [data, reset])

  const salvarMutation = useMutation({
    mutationFn: ({ foto, ...dados }) => atualizarPerfilPsicologo(dados, foto?.[0]),
    onSuccess: () => toast.success('Perfil atualizado'),
    onError: (error) => toast.error(error.response?.data?.mensagem ?? 'Não foi possível atualizar o perfil'),
  })

  const conectarGoogleMutation = useMutation({
    mutationFn: () => obterUrlGoogleCalendar().then((res) => res.data),
    onSuccess: (dados) => {
      window.location.href = dados.url
    },
    onError: () => toast.error('Não foi possível iniciar a conexão com o Google Calendar'),
  })

  if (isLoading) {
    return <p className="p-6 text-muted-foreground">Carregando...</p>
  }

  return (
    <div className="mx-auto flex max-w-xl flex-col gap-6 p-6">
      <Card>
        <CardHeader>
          <CardTitle>Meu perfil</CardTitle>
          <CardDescription>
            {data?.nome} · CRP {data?.crp} (não editável — alterar o CRP exige nova avaliação do admin)
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form className="flex flex-col gap-4" onSubmit={handleSubmit((dados) => salvarMutation.mutate(dados))}>
            {data?.fotoUrl && (
              <img src={data.fotoUrl} alt="Foto de perfil" className="size-20 rounded-full object-cover" />
            )}
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="foto">Foto de perfil</Label>
              <Input id="foto" type="file" accept="image/png,image/jpeg,image/webp" {...register('foto')} />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="especializacao">Especialização</Label>
              <Input id="especializacao" {...register('especializacao')} />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="politicaCancelamento">Política de cancelamento</Label>
              <Textarea id="politicaCancelamento" {...register('politicaCancelamento')} />
              {errors.politicaCancelamento && (
                <p className="text-sm text-destructive">{errors.politicaCancelamento.message}</p>
              )}
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="linkVideochamada">Link de videochamada (Google Meet, Zoom)</Label>
              <Input id="linkVideochamada" placeholder="https://" {...register('linkVideochamada')} />
            </div>
            <Button type="submit" disabled={isSubmitting || salvarMutation.isPending}>
              Salvar
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Google Calendar</CardTitle>
          <CardDescription>Sincronize os horários da sua agenda com o Google Calendar</CardDescription>
        </CardHeader>
        <CardContent className="flex items-center gap-3">
          <Badge variant={data?.googleCalendarConectado ? 'secondary' : 'outline'}>
            {data?.googleCalendarConectado ? 'Conectado' : 'Não conectado'}
          </Badge>
          <Button
            variant="outline"
            disabled={conectarGoogleMutation.isPending}
            onClick={() => conectarGoogleMutation.mutate()}
          >
            {data?.googleCalendarConectado ? 'Reconectar' : 'Conectar Google Calendar'}
          </Button>
        </CardContent>
      </Card>
    </div>
  )
}
