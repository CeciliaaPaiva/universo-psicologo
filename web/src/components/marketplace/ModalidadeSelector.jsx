import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { formatarMoeda } from '@/lib/formatarMoeda'

export function ModalidadeSelector({ valorAvulsa, valorPacotePorSessao, value, onChange }) {
  return (
    <Select value={value} onValueChange={onChange}>
      <SelectTrigger className="w-full">
        <SelectValue placeholder="Selecione a modalidade" />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="AVULSA">Avulsa — {formatarMoeda(valorAvulsa)} por sessão</SelectItem>
        <SelectItem value="PACOTE_MENSAL">
          Pacote mensal (4 sessões) — {formatarMoeda(valorPacotePorSessao)} por sessão
        </SelectItem>
      </SelectContent>
    </Select>
  )
}
