import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent } from '@/components/ui/card'
import { listarContatos } from '@/services/mensagem'

export function MensagensListaPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['mensagens-contatos'],
    queryFn: () => listarContatos().then((res) => res.data),
  })

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6 p-6">
      <h1 className="text-xl font-medium">Mensagens</h1>

      {isLoading && <p className="text-muted-foreground">Carregando...</p>}
      {!isLoading && data?.length === 0 && (
        <p className="text-muted-foreground">
          O chat é liberado depois que uma sessão é agendada e paga. Você ainda não tem conversas
          disponíveis.
        </p>
      )}

      <div className="flex flex-col gap-3">
        {data?.map((contato) => (
          <Link key={contato.id} to={`/mensagens/${contato.id}`}>
            <Card className="transition-colors hover:bg-muted">
              <CardContent className="flex items-center justify-between pt-6">
                <span className="font-medium">{contato.nome}</span>
                {contato.naoLidas > 0 && <Badge>{contato.naoLidas}</Badge>}
              </CardContent>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  )
}
