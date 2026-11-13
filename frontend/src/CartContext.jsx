import { createContext, useContext, useEffect, useMemo, useReducer } from 'react'
import { somarPrecos } from './utils'

const CHAVE_STORAGE = 'cardapio:carrinho'
const QUANTIDADE_MAXIMA = 50

const CarrinhoContext = createContext(null)

/**
 * Uma "linha" do carrinho tem este formato:
 * {
 *   chave, itemId, itemNome, tamanhoId, tamanhoNome, precoTamanho,
 *   quantidade, adicionais: [{ id, nome, preco }], observacao, imagemUrl
 * }
 */

/** Preço de UMA unidade: tamanho + opcionais escolhidos. */
export function precoUnidade(linha) {
  return somarPrecos([linha.precoTamanho, ...linha.adicionais.map((a) => a.preco)])
}

/** Preço da linha inteira: preço da unidade x quantidade. */
export function precoLinha(linha) {
  return Math.round(precoUnidade(linha) * 100 * linha.quantidade) / 100
}

function gerarChave(linha) {
  const adicionais = linha.adicionais
    .map((a) => a.id)
    .sort((a, b) => a - b)
    .join(',')
  const observacao = (linha.observacao || '').trim().toLowerCase()
  return `${linha.tamanhoId}|${adicionais}|${observacao}`
}

function carregarLinhas() {
  try {
    const bruto = localStorage.getItem(CHAVE_STORAGE)
    const dados = bruto ? JSON.parse(bruto) : []
    return Array.isArray(dados) ? dados : []
  } catch {
    return []
  }
}

function reducer(estado, acao) {
  switch (acao.tipo) {
    case 'adicionar': {
      const nova = { ...acao.linha, chave: gerarChave(acao.linha) }
      const existente = estado.find((l) => l.chave === nova.chave)
      if (existente) {
        // Mesmo item, mesmo tamanho e mesmos opcionais: soma na linha que já existe.
        return estado.map((l) =>
          l.chave === nova.chave
            ? { ...l, quantidade: Math.min(QUANTIDADE_MAXIMA, l.quantidade + nova.quantidade) }
            : l,
        )
      }
      return [...estado, nova]
    }
    case 'quantidade':
      return estado.map((l) =>
        l.chave === acao.chave
          ? { ...l, quantidade: Math.max(1, Math.min(QUANTIDADE_MAXIMA, acao.quantidade)) }
          : l,
      )
    case 'remover':
      return estado.filter((l) => l.chave !== acao.chave)
    case 'limpar':
      return []
    default:
      return estado
  }
}

export function CarrinhoProvider({ children }) {
  const [linhas, dispatch] = useReducer(reducer, undefined, carregarLinhas)

  // Guarda o carrinho no navegador: se a página recarregar, o pedido continua lá.
  useEffect(() => {
    try {
      localStorage.setItem(CHAVE_STORAGE, JSON.stringify(linhas))
    } catch {
      // Sem armazenamento disponível: o carrinho segue só em memória.
    }
  }, [linhas])

  const valor = useMemo(
    () => ({
      linhas,
      quantidadeTotal: linhas.reduce((soma, l) => soma + l.quantidade, 0),
      valorTotal: somarPrecos(linhas.map(precoLinha)),
      adicionar: (linha) => dispatch({ tipo: 'adicionar', linha }),
      alterarQuantidade: (chave, quantidade) => dispatch({ tipo: 'quantidade', chave, quantidade }),
      remover: (chave) => dispatch({ tipo: 'remover', chave }),
      limpar: () => dispatch({ tipo: 'limpar' }),
    }),
    [linhas],
  )

  return <CarrinhoContext.Provider value={valor}>{children}</CarrinhoContext.Provider>
}

export function useCarrinho() {
  const contexto = useContext(CarrinhoContext)
  if (!contexto) {
    throw new Error('useCarrinho precisa ser usado dentro de <CarrinhoProvider>')
  }
  return contexto
}
