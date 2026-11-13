import { API_URL } from './config'

/** Erro devolvido pela API (ou falha de conexão, com status 0). */
export class ErroApi extends Error {
  constructor(mensagem, status, detalhes = []) {
    super(mensagem)
    this.status = status
    this.detalhes = detalhes
  }
}

async function requisitar(caminho, { metodo = 'GET', corpo } = {}) {
  const opcoes = { method: metodo, headers: { Accept: 'application/json' } }
  if (corpo !== undefined) {
    opcoes.headers['Content-Type'] = 'application/json'
    opcoes.body = JSON.stringify(corpo)
  }

  let resposta
  try {
    resposta = await fetch(API_URL + caminho, opcoes)
  } catch {
    throw new ErroApi(
      'Não foi possível conectar ao servidor. Verifique se o sistema está ligado.',
      0,
    )
  }

  let dados = null
  const texto = await resposta.text()
  if (texto) {
    try {
      dados = JSON.parse(texto)
    } catch {
      dados = null
    }
  }

  if (!resposta.ok) {
    throw new ErroApi(
      dados?.mensagem || `Erro ${resposta.status} ao falar com o servidor`,
      resposta.status,
      dados?.detalhes || [],
    )
  }
  return dados
}

/** RF001: cardápio completo, organizado por categorias. */
export function buscarCardapio() {
  return requisitar('/api/cardapio')
}

/** RF002: detalhes de um item. */
export function buscarItem(id) {
  return requisitar(`/api/cardapio/itens/${id}`)
}

/** RF004: envia o pedido. */
export function criarPedido(pedido) {
  return requisitar('/api/pedidos', { metodo: 'POST', corpo: pedido })
}

/** Situação atual do pedido (status e etapas). */
export function buscarAcompanhamento(numero) {
  return requisitar(`/api/pedidos/${numero}`)
}

/** O cliente confirma que recebeu o pedido (só vale depois de ENTREGUE). */
export function confirmarRecebimento(numero) {
  return requisitar(`/api/pedidos/${numero}/confirmar-recebimento`, { metodo: 'POST' })
}

/** Endereço do canal de tempo real (SSE) de um pedido. */
export function urlStreamPedido(numero) {
  return `${API_URL}/api/pedidos/${numero}/stream`
}
