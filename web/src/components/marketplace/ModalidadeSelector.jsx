import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { formatarMoeda } from '@/lib/formatarMoeda'

export function ModalidadeSelector({ valorAvulsa, valorPacotePorSessao, value, onChange }) {
  const rotulos = {
    AVULSA: `Avulsa — ${formatarMoeda(valorAvulsa)} por sessão`,
    PACOTE_MENSAL: `Pacote mensal (4 sessões) — ${formatarMoeda(valorPacotePorSessao)} por sessão`,
  }

  return (
    <Select value={value} onValueChange={onChange}>
      <SelectTrigger className="w-full">
        <SelectValue placeholder="Selecione a modalidade">{(valorAtual) => rotulos[valorAtual]}</SelectValue>
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="AVULSA">{rotulos.AVULSA}</SelectItem>
        <SelectItem value="PACOTE_MENSAL">{rotulos.PACOTE_MENSAL}</SelectItem>
      </SelectContent>
    </Select>
  )
}
