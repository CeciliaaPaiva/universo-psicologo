import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { atualizarCodinome, criarAnotacao, listarAnotacoes } from '@/services/prontuario'

const LIMITE_CARACTERES = 10000

const schemaAnotacao = z.object({
  conteudo: z.string().min(1, 'Escreva a anotação').max(LIMITE_CARACTERES, 'Limite de 10.000 caracteres'),
})

function formatarDataHora(dataHoraIso) {
  return new Date(dataHoraIso).toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function ProntuarioDetalhePage() {
  const { codinome } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [busca, setBusca] = useState('')
  const [editandoCodinome, setEditandoCodinome] = useState(false)
  const [novoCodinome, setNovoCodinome] = useState(codinome)

  const { data, isLoading } = useQuery({
    queryKey: ['anotacoes', codinome, busca],
    queryFn: () => listarAnotacoes(codinome, busca).then((res) => res.data),
  })

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schemaAnotacao), defaultValues: { conteudo: '' } })

  const conteudoAtual = watch('conteudo') ?? ''

  const criarAnotacaoMutation = useMutation({
    mutationFn: (dados) => criarAnotacao(codinome, dados.conteudo),
    onSuccess: () => {
      toast.success('Anotação salva')
      queryClient.invalidateQueries({ queryKey: ['anotacoes', codinome] })
      reset()
    },
    onError: (error) => toast.error(error.response?.data?.mensagem ?? 'Não foi possível salvar a anotação'),
  })

  const renomearMutation = useMutation({
    mutationFn: (novo) => atualizarCodinome(codinome, novo),
    onSuccess: (res) => {
      toast.success('Codinome atualizado')
      queryClient.invalidateQueries({ queryKey: ['prontuarios'] })
      navigate(`/prontuario/${encodeURIComponent(res.data.codinome)}`, { replace: true })
    },
    onError: (error) => toast.error(error.response?.data?.mensagem ?? 'Não foi possível atualizar o codinome'),
  })

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6 p-6">
      <Link to="/prontuario" className="text-sm text-muted-foreground hover:underline">
        ← Meus pacientes
      </Link>

      <div className="flex items-center gap-3">
        {!editandoCodinome && (
          <>
            <h1 className="text-xl font-medium">{codinome}</h1>
            <Button size="sm" variant="ghost" onClick={() => setEditandoCodinome(true)}>
              Renomear
            </Button>
          </>
        )}
        {editandoCodinome && (
          <div className="flex items-center gap-2">
            <Input value={novoCodinome} onChange={(e) => setNovoCodinome(e.target.value)} className="w-48" />
            <Button
              size="sm"
              disabled={renomearMutation.isPending || !novoCodinome.trim()}
              onClick={() => renomearMutation.mutate(novoCodinome.trim())}
            >
              Salvar
            </Button>
            <Button size="sm" variant="ghost" onClick={() => setEditandoCodinome(false)}>
              Cancelar
            </Button>
          </div>
        )}
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Nova anotação</CardTitle>
        </CardHeader>
        <CardContent>
          <form
            className="flex flex-col gap-2"
            onSubmit={handleSubmit((dados) => criarAnotacaoMutation.mutate(dados))}
          >
            <Textarea
              rows={5}
              placeholder="Registro da sessão..."
              {...register('conteudo')}
            />
            <div className="flex items-center justify-between">
              <span className="text-xs text-muted-foreground">
                {conteudoAtual.length} / {LIMITE_CARACTERES} caracteres
              </span>
              <Button type="submit" size="sm" disabled={isSubmitting || criarAnotacaoMutation.isPending}>
                Salvar anotação
              </Button>
            </div>
            {errors.conteudo && <p className="text-sm text-destructive">{errors.conteudo.message}</p>}
          </form>
        </CardContent>
      </Card>

      <div className="flex flex-col gap-3">
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="busca">Buscar no histórico</Label>
          <Input
            id="busca"
            placeholder="Palavra-chave..."
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
          />
        </div>

        {isLoading && <p className="text-muted-foreground">Carregando...</p>}
        {!isLoading && data?.length === 0 && (
          <p className="text-muted-foreground">Nenhuma anotação encontrada.</p>
        )}
        {data?.map((anotacao) => (
          <Card key={anotacao.id}>
            <CardContent className="flex flex-col gap-1 py-4">
              <span className="text-xs text-muted-foreground">{formatarDataHora(anotacao.criadaEm)}</span>
              <p className="whitespace-pre-wrap text-sm">{anotacao.conteudo}</p>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
}
