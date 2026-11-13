const formatadorMoeda = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
})

/** 74.9 -> "R$ 74,90" */
export function formatarPreco(valor) {
  return formatadorMoeda.format(Number(valor) || 0)
}

/** Etapas do pedido, na ordem em que acontecem. */
export const STATUS = [
  { codigo: 'RECEBIDO', rotulo: 'Recebido', descricao: 'Recebemos o seu pedido.' },
  { codigo: 'EM_PREPARO', rotulo: 'Em preparo', descricao: 'Estamos preparando o seu pedido.' },
  { codigo: 'PRONTO', rotulo: 'Pronto', descricao: 'Seu pedido está pronto!' },
  { codigo: 'ENTREGUE', rotulo: 'Entregue', descricao: 'Pedido entregue. Bom apetite!' },
]

export function rotuloStatus(codigo) {
  return STATUS.find((s) => s.codigo === codigo)?.rotulo ?? codigo
}

/** "2026-11-10T16:48:20-03:00" -> "10/11/2026 16:48" */
export function formatarDataHora(iso) {
  if (!iso) return ''
  const data = new Date(iso)
  if (Number.isNaN(data.getTime())) return ''
  return data.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
}

/** Emoji usado quando o item ainda não tem foto. */
export function iconeCategoria(nome = '') {
  const n = nome.toLowerCase()
  if (n.includes('pizza')) return '🍕'
  if (n.includes('esfi')) return '🥟'
  if (n.includes('refri') || n.includes('bebida')) return '🥤'
  if (n.includes('suco')) return '🧃'
  return '🍽️'
}

/** Soma valores em centavos para evitar erros de arredondamento (0.1 + 0.2). */
export function somarPrecos(valores) {
  const centavos = valores.reduce((soma, v) => soma + Math.round((Number(v) || 0) * 100), 0)
  return centavos / 100
}
