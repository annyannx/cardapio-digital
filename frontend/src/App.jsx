import { useEffect, useRef, useState } from 'react'
import AdminApp from './admin/AdminApp'
import { useCarrinho } from './CartContext'
import CartDrawer from './components/CartDrawer'
import { NOME_ESTABELECIMENTO, SUBTITULO_ESTABELECIMENTO } from './config'
import AcompanharPage from './pages/AcompanharPage'
import CardapioPage from './pages/CardapioPage'
import { formatarPreco } from './utils'

const CHAVE_ULTIMO_PEDIDO = 'cardapio:ultimoPedido'

function lerUltimoPedido() {
  try {
    return localStorage.getItem(CHAVE_ULTIMO_PEDIDO) || ''
  } catch {
    return ''
  }
}

/**
 * Rotas simples pelo "#" do endereço:
 *   "#/"                  cardápio do cliente
 *   "#/acompanhar/12"     acompanhar o pedido 12
 *   "#/admin"             painel do administrador (pedidos)
 *   "#/admin/itens"       painel do administrador (cardápio)
 */
function useRota() {
  const [hash, setHash] = useState(window.location.hash)
  useEffect(() => {
    const aoMudar = () => setHash(window.location.hash)
    window.addEventListener('hashchange', aoMudar)
    return () => window.removeEventListener('hashchange', aoMudar)
  }, [])
  return hash.replace(/^#/, '') || '/'
}

export default function App() {
  const rota = useRota()

  // O painel do administrador tem o seu próprio layout.
  if (rota === '/admin' || rota.startsWith('/admin/')) {
    return <AdminApp rota={rota} />
  }

  return <AppCliente rota={rota} />
}

function AppCliente({ rota }) {
  const { quantidadeTotal, valorTotal } = useCarrinho()

  const [carrinhoAberto, setCarrinhoAberto] = useState(false)
  const [aviso, setAviso] = useState('')
  const [ultimoPedido, setUltimoPedido] = useState(lerUltimoPedido)
  const temporizadorAviso = useRef(null)

  const correspondencia = rota.match(/^\/acompanhar\/(\d+)$/)
  const numeroAcompanhado = correspondencia ? correspondencia[1] : null

  function mostrarAviso(mensagem) {
    setAviso(mensagem)
    clearTimeout(temporizadorAviso.current)
    temporizadorAviso.current = setTimeout(() => setAviso(''), 2500)
  }

  function aoPedidoEnviado(numero) {
    const texto = String(numero)
    setUltimoPedido(texto)
    try {
      localStorage.setItem(CHAVE_ULTIMO_PEDIDO, texto)
    } catch {
      // sem armazenamento: não faz mal
    }
  }

  // Limpa o temporizador do aviso ao sair.
  useEffect(() => () => clearTimeout(temporizadorAviso.current), [])

  return (
    <div className="app">
      <header className="topo">
        <a className="topo__marca" href="#/" aria-label={`${NOME_ESTABELECIMENTO} - voltar ao cardápio`}>
          <span className="topo__icone" aria-hidden="true">
            🍕
          </span>
          <span>
            <strong>{NOME_ESTABELECIMENTO}</strong>
            <small>{SUBTITULO_ESTABELECIMENTO}</small>
          </span>
        </a>

        <div className="topo__acoes">
          {ultimoPedido ? (
            <a className="link-pedido" href={`#/acompanhar/${ultimoPedido}`}>
              Meu pedido #{ultimoPedido}
            </a>
          ) : null}

          <button
            type="button"
            className="botao-carrinho"
            onClick={() => setCarrinhoAberto(true)}
            aria-label={`Abrir o pedido. ${quantidadeTotal} itens`}
          >
            <span aria-hidden="true">🛒</span>
            <span className="botao-carrinho__texto">Pedido</span>
            {quantidadeTotal > 0 ? (
              <span key={quantidadeTotal} className="selo">
                {quantidadeTotal}
              </span>
            ) : null}
          </button>
        </div>
      </header>

      <main className="conteudo">
        {numeroAcompanhado ? (
          <AcompanharPage numero={numeroAcompanhado} />
        ) : (
          <CardapioPage aoAdicionado={mostrarAviso} />
        )}
      </main>

      <footer className="rodape">
        Cardápio Digital · Prática Extensionista VII ·{' '}
        <a href="#/admin" className="rodape__admin">
          Área do administrador
        </a>
      </footer>

      {quantidadeTotal > 0 && !carrinhoAberto && !numeroAcompanhado ? (
        <button type="button" className="barra-carrinho" onClick={() => setCarrinhoAberto(true)}>
          <span>
            Ver pedido · {quantidadeTotal} {quantidadeTotal === 1 ? 'item' : 'itens'}
          </span>
          <strong>{formatarPreco(valorTotal)}</strong>
        </button>
      ) : null}

      <CartDrawer
        aberto={carrinhoAberto}
        aoFechar={() => setCarrinhoAberto(false)}
        aoPedidoEnviado={aoPedidoEnviado}
      />

      {aviso ? (
        <div className="aviso" role="status">
          {aviso}
        </div>
      ) : null}
    </div>
  )
}
