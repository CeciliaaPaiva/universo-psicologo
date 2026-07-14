import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'

const ROTULOS = {
  INDIVIDUAL: 'Individual',
  CASAL: 'Terapia de casal (dobro do valor individual)',
}

export function TipoAtendimentoSelector({ value, onChange }) {
  return (
    <Select value={value} onValueChange={onChange}>
      <SelectTrigger className="w-full">
        <SelectValue placeholder="Selecione o tipo de atendimento">{(valorAtual) => ROTULOS[valorAtual]}</SelectValue>
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="INDIVIDUAL">{ROTULOS.INDIVIDUAL}</SelectItem>
        <SelectItem value="CASAL">{ROTULOS.CASAL}</SelectItem>
      </SelectContent>
    </Select>
  )
}
