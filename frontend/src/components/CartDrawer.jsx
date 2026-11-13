import { useEffect, useState } from 'react'
import { criarPedido } from '../api'
import { precoLinha, useCarrinho } from '../CartContext'
import { formatarPreco } from '../utils'

const CHAVE_CLIENTE = 'cardapio:cliente'

function lerCliente() {
  try {
    const bruto = localStorage.getItem(CHAVE_CLIENTE)
    const dados = bruto ? JSON.parse(bruto) : {}
    return { nome: dados.nome || '', mesa: dados.mesa || '' }
  } catch {
    return { nome: '', mesa: '' }
  }
}

export default function CartDrawer({ aberto, aoFechar, aoPedidoEnviado }) {
  const { linhas, valorTotal, alterarQuantidade, remover, limpar } = useCarrinho()

  const [nome, setNome] = useState(() => lerCliente().nome)
  const [mesa, setMesa] = useState(() => lerCliente().mesa)
  const [observacao, setObservacao] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState('')
  const [detalhes, setDetalhes] = useState([])
  const [confirmado, setConfirmado] = useState(null)

  // Esc fecha o carrinho.
  useEffect(() => {
    if (!aberto) return undefined
    function aoTeclar(evento) {
      if (evento.key === 'Escape') fechar()
    }
    window.addEventListener('keydown', aoTeclar)
    return () => window.removeEventListener('keydown', aoTeclar)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [aberto, confirmado])

  function fechar() {
    // Depois de um pedido enviado, ao fechar o carrinho volta ao estado inicial.
    if (confirmado) setConfirmado(null)
    setErro('')
    setDetalhes([])
    aoFechar()
  }

  async function enviarPedido(evento) {
    evento.preventDefault()
    setErro('')
    setDetalhes([])

    if (linhas.length === 0) {
      setErro('Adicione pelo menos um item ao pedido.')
      return
    }
    if (!nome.trim()) {
      setErro('Informe o seu nome.')
      return
    }

    const pedido = {
      clienteNome: nome.trim(),
      mesa: mesa.trim() || null,
      observacao: observacao.trim() || null,
      itens: linhas.map((l) => ({
        tamanhoId: l.tamanhoId,
        quantidade: l.quantidade,
        observacao: l.observacao || null,
        adicionaisIds: l.adicionais.map((a) => a.id),
      })),
    }

    setEnviando(true)
    try {
      const resposta = await criarPedido(pedido)
      try {
        localStorage.setItem(CHAVE_CLIENTE, JSON.stringify({ nome: nome.trim(), mesa: mesa.trim() }))
      } catch {
        // sem armazenamento: não faz mal
      }
      setConfirmado(resposta)
      setObservacao('')
      limpar()
      aoPedidoEnviado(resposta.numero)
    } catch (e) {
      setErro(e.message)
      setDetalhes(e.detalhes || [])
    } finally {
      setEnviando(false)
    }
  }

  function acompanhar() {
    window.location.hash = `#/acompanhar/${confirmado.numero}`
    fechar()
  }

  if (!aberto) return null

  return (
    <div className="drawer-fundo" onClick={fechar} role="presentation">
      <aside
        className="drawer"
        role="dialog"
        aria-modal="true"
        aria-label="Seu pedido"
        onClick={(e) => e.stopPropagation()}
      >
        <header className="drawer__cabecalho">
          <h2>{confirmado ? 'Pedido enviado' : 'Seu pedido'}</h2>
          <button type="button" className="modal__fechar" onClick={fechar} aria-label="Fechar">
            ✕
          </button>
        </header>

        {confirmado ? (
          <div className="confirmacao">
            <div className="confirmacao__selo" aria-hidden="true">
              ✓
            </div>
            <p className="confirmacao__titulo">Pedido recebido com sucesso!</p>
            <p className="confirmacao__numero">
              Número do pedido: <strong>#{confirmado.numero}</strong>
            </p>
            <p>
              Status: <strong>Recebido</strong> · Total:{' '}
              <strong>{formatarPreco(confirmado.valorTotal)}</strong>
            </p>
            <p className="confirmacao__dica">
              Guarde o número. Você pode acompanhar o andamento do pedido em tempo real.
            </p>
            <div className="confirmacao__acoes">
              <button type="button" className="botao botao--primario botao--grande" onClick={acompanhar}>
                Acompanhar meu pedido
              </button>
              <button type="button" className="botao botao--secundario" onClick={fechar}>
                Fazer outro pedido
              </button>
            </div>
          </div>
        ) : linhas.length === 0 ? (
          <div className="estado">
            <p>Seu pedido está vazio.</p>
            <button type="button" className="botao botao--secundario" onClick={fechar}>
              Ver cardápio
            </button>
          </div>
        ) : (
          <form className="drawer__corpo" onSubmit={enviarPedido}>
            <ul className="linhas">
              {linhas.map((linha) => (
                <li key={linha.chave} className="linha">
                  <div className="linha__info">
                    <strong>{linha.itemNome}</strong>
                    <span className="linha__detalhe">
                      {linha.tamanhoNome !== 'Único' ? `${linha.tamanhoNome} · ` : ''}
                      {formatarPreco(linha.precoTamanho)}
                    </span>
                    {linha.adicionais.length > 0 ? (
                      <span className="linha__detalhe">
                        + {linha.adicionais.map((a) => a.nome).join(', ')}
                      </span>
                    ) : null}
                    {linha.observacao ? (
                      <span className="linha__detalhe">Obs.: {linha.observacao}</span>
                    ) : null}
                  </div>

                  <div className="linha__acoes">
                    <div className="seletor-quantidade seletor-quantidade--pequeno">
                      <button
                        type="button"
                        onClick={() => alterarQuantidade(linha.chave, linha.quantidade - 1)}
                        aria-label={`Diminuir quantidade de ${linha.itemNome}`}
                      >
                        −
                      </button>
                      <span>{linha.quantidade}</span>
                      <button
                        type="button"
                        onClick={() => alterarQuantidade(linha.chave, linha.quantidade + 1)}
                        aria-label={`Aumentar quantidade de ${linha.itemNome}`}
                      >
                        +
                      </button>
                    </div>
                    <strong>{formatarPreco(precoLinha(linha))}</strong>
                    <button
                      type="button"
                      className="linha__remover"
                      onClick={() => remover(linha.chave)}
                      aria-label={`Remover ${linha.itemNome}`}
                    >
                      Remover
                    </button>
                  </div>
                </li>
              ))}
            </ul>

            <div className="total">
              <span>Total</span>
              <strong>{formatarPreco(valorTotal)}</strong>
            </div>

            <div className="campos">
              <label className="campo">
                <span>Seu nome *</span>
                <input
                  type="text"
                  value={nome}
                  maxLength={100}
                  onChange={(e) => setNome(e.target.value)}
                  placeholder="Como podemos te chamar?"
                  required
                />
              </label>
              <label className="campo">
                <span>Mesa (opcional)</span>
                <input
                  type="text"
                  value={mesa}
                  maxLength={20}
                  onChange={(e) => setMesa(e.target.value)}
                  placeholder="Ex.: 5"
                />
              </label>
              <label className="campo">
                <span>Observação do pedido (opcional)</span>
                <textarea
                  rows={2}
                  maxLength={255}
                  value={observacao}
                  onChange={(e) => setObservacao(e.target.value)}
                />
              </label>
            </div>

            {erro ? (
              <div className="alerta alerta--erro" role="alert">
                <p>{erro}</p>
                {detalhes.length > 0 ? (
                  <ul>
                    {detalhes.map((d) => (
                      <li key={d}>{d}</li>
                    ))}
                  </ul>
                ) : null}
              </div>
            ) : null}

            <button type="submit" className="botao botao--primario botao--grande" disabled={enviando}>
              {enviando ? 'Enviando...' : `Enviar pedido · ${formatarPreco(valorTotal)}`}
            </button>
          </form>
        )}
      </aside>
    </div>
  )
}
