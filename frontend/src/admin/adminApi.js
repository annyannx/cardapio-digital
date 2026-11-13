import { ErroApi } from '../api'
import { API_URL } from '../config'

const CHAVE_SESSAO = 'cardapio:sessaoAdmin'

// ---------- sessão (RF005): o token fica guardado no navegador ----------

export function lerSessao() {
  try {
    const bruto = localStorage.getItem(CHAVE_SESSAO)
    const dados = bruto ? JSON.parse(bruto) : null
    return dados && dados.token ? dados : null
  } catch {
    return null
  }
}

export function salvarSessao(sessao) {
  try {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify(sessao))
  } catch {
    // sem armazenamento: a sessão vale só até recarregar a página
  }
}

export function apagarSessao() {
  try {
    localStorage.removeItem(CHAVE_SESSAO)
  } catch {
    // nada a fazer
  }
}

// ---------- requisições ----------

async function requisitar(caminho, { metodo = 'GET', corpo, token, formData } = {}) {
  const headers = { Accept: 'application/json' }
  if (token) headers.Authorization = `Bearer ${token}`

  const opcoes = { method: metodo, headers }
  if (formData) {
    // Upload de arquivo: o navegador define o Content-Type sozinho.
    opcoes.body = formData
  } else if (corpo !== undefined) {
    headers['Content-Type'] = 'application/json'
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

/** RF005: login. Devolve { token, nome, perfil, ... }. */
export function entrar(email, senha) {
  return requisitar('/api/auth/login', { metodo: 'POST', corpo: { email, senha } })
}

/** Confere se o token ainda vale. */
export function buscarMe(token) {
  return requisitar('/api/admin/me', { token })
}

// ---------- pedidos (RF007 e RF008) ----------

export function listarPedidos(token) {
  return requisitar('/api/admin/pedidos', { token })
}

export function alterarStatusPedido(token, numero, status) {
  return requisitar(`/api/admin/pedidos/${numero}/status`, {
    metodo: 'PATCH',
    token,
    corpo: { status },
  })
}

// ---------- cardápio (RF006) ----------

export function listarCategoriasAdmin(token) {
  return requisitar('/api/admin/categorias', { token })
}

export function listarItensAdmin(token) {
  return requisitar('/api/admin/itens', { token })
}

export function criarItemAdmin(token, dados) {
  return requisitar('/api/admin/itens', { metodo: 'POST', token, corpo: dados })
}

export function atualizarItemAdmin(token, id, dados) {
  return requisitar(`/api/admin/itens/${id}`, { metodo: 'PUT', token, corpo: dados })
}

export function removerItemAdmin(token, id) {
  return requisitar(`/api/admin/itens/${id}`, { metodo: 'DELETE', token })
}

export function enviarImagem(token, arquivo) {
  const formData = new FormData()
  formData.append('arquivo', arquivo)
  return requisitar('/api/admin/imagens', { metodo: 'POST', token, formData })
}

// ---------- tempo real (SSE com cabeçalho Authorization) ----------

/**
 * Abre o canal de tempo real do painel. O EventSource do navegador não consegue
 * enviar o token, então lemos o fluxo com fetch e interpretamos as mensagens aqui.
 * A função só termina quando a conexão cai (ou é cancelada pelo "sinal").
 */
export async function ouvirPedidos(token, { sinal, aoConectar, aoEvento }) {
  const resposta = await fetch(`${API_URL}/api/admin/pedidos/stream`, {
    headers: { Authorization: `Bearer ${token}`, Accept: 'text/event-stream' },
    signal: sinal,
  })

  if (!resposta.ok) {
    throw new ErroApi(`Erro ${resposta.status} ao abrir o tempo real`, resposta.status)
  }
  if (aoConectar) aoConectar()

  const leitor = resposta.body.getReader()
  const decodificador = new TextDecoder()
  const separador = /\r?\n\r?\n/
  let acumulado = ''

  for (;;) {
    const { value, done } = await leitor.read()
    if (done) break
    acumulado += decodificador.decode(value, { stream: true })

    let encontrado = separador.exec(acumulado)
    while (encontrado) {
      const bloco = acumulado.slice(0, encontrado.index)
      acumulado = acumulado.slice(encontrado.index + encontrado[0].length)
      tratarBloco(bloco, aoEvento)
      encontrado = separador.exec(acumulado)
    }
  }
}

/** Interpreta um bloco SSE: linhas "event: nome" e "data: {json}". */
function tratarBloco(bloco, aoEvento) {
  let nome = 'message'
  const dados = []

  for (const linha of bloco.split(/\r?\n/)) {
    if (linha.startsWith(':')) continue // comentário (batimento)
    if (linha.startsWith('event:')) nome = linha.slice(6).trim()
    else if (linha.startsWith('data:')) dados.push(linha.slice(5).replace(/^ /, ''))
  }

  if (dados.length === 0) return
  try {
    aoEvento(nome, JSON.parse(dados.join('\n')))
  } catch {
    // mensagem que não é JSON: ignora
  }
}
