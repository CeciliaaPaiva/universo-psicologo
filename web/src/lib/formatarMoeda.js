const FORMATADOR = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

export function formatarMoeda(valor) {
  return FORMATADOR.format(Number(valor))
}
