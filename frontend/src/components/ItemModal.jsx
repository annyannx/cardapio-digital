import { useEffect, useState } from 'react'
import { buscarItem } from '../api'
import { useCarrinho } from '../CartContext'
import { formatarPreco, iconeCategoria, somarPrecos } from '../utils'
import './item-modal.css'

export default function ItemModal({ itemId, aoFechar, aoAdicionado }) {
  const { adicionar } = useCarrinho()

  const [item, setItem] = useState(null)
  const [erro, setErro] = useState('')
  const [tamanhoId, setTamanhoId] = useState(null)
  const [adicionaisSelecionados, setAdicionaisSelecionados] = useState([])
  const [quantidade, setQuantidade] = useState(1)
  const [observacao, setObservacao] = useState('')

  // RF002: os detalhes vêm da API.
  useEffect(() => {
    let ativo = true
    buscarItem(itemId)
      .then((dados) => {
        if (!ativo) return
        setItem(dados)
        setTamanhoId(dados.tamanhos[0]?.id ?? null)
      })
      .catch((e) => {
        if (ativo) setErro(e.message)
      })
    return () => {
      ativo = false
    }
  }, [itemId])

  // Esc fecha a janela e o fundo da página não rola enquanto ela está aberta.
  useEffect(() => {
    function aoTeclar(evento) {
      if (evento.key === 'Escape') aoFechar()
    }
    window.addEventListener('keydown', aoTeclar)
    const overflowAnterior = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    return () => {
      window.removeEventListener('keydown', aoTeclar)
      document.body.style.overflow = overflowAnterior
    }
  }, [aoFechar])

  const tamanho = item?.tamanhos.find((t) => t.id === tamanhoId) ?? null
  const adicionaisEscolhidos = item
    ? item.adicionais.filter((a) => adicionaisSelecionados.includes(a.id))
    : []
  const precoUnidade = tamanho
    ? somarPrecos([tamanho.preco, ...adicionaisEscolhidos.map((a) => a.preco)])
    : 0
  const total = Math.round(precoUnidade * 100 * quantidade) / 100

  function alternarAdicional(id) {
    setAdicionaisSelecionados((atuais) =>
      atuais.includes(id) ? atuais.filter((x) => x !== id) : [...atuais, id],
    )
  }

  function adicionarAoPedido() {
    if (!item || !tamanho) return
    adicionar({
      itemId: item.id,
      itemNome: item.nome,
      tamanhoId: tamanho.id,
      tamanhoNome: tamanho.nome,
      precoTamanho: tamanho.preco,
      quantidade,
      adicionais: adicionaisEscolhidos.map((a) => ({ id: a.id, nome: a.nome, preco: a.preco })),
      observacao: observacao.trim(),
      imagemUrl: item.imagemUrl,
    })
    aoAdicionado(`${quantidade}x ${item.nome} adicionado ao pedido`)
    aoFechar()
  }

  const mostrarSeletorDeTamanho = item && item.tamanhos.length > 1

  return (
    <div className="modal-fundo" onClick={aoFechar} role="presentation">
      <div
        className="modal modal--item"
        role="dialog"
        aria-modal="true"
        aria-label="Detalhes do item"
        onClick={(e) => e.stopPropagation()}
      >
        <button type="button" className="modal__fechar" onClick={aoFechar} aria-label="Fechar">
          ✕
        </button>

        {erro ? (
          <div className="estado estado--erro">
            <p>{erro}</p>
            <button type="button" className="botao botao--secundario" onClick={aoFechar}>
              Fechar
            </button>
          </div>
        ) : !item ? (
          <div className="estado">Carregando...</div>
        ) : (
          <>
            {/* Tudo que rola fica aqui; o botão de adicionar fica fixo embaixo. */}
            <div className="modal__rolagem">
              {/* Foto com uma camada escura translúcida: o título fica legível em qualquer foto. */}
              <div className="modal__hero">
                {item.imagemUrl ? (
                  <img src={item.imagemUrl} alt={item.nome} />
                ) : (
                  <span className="item-card__icone" aria-hidden="true">
                    {iconeCategoria(item.categoriaNome)}
                  </span>
                )}
                <div className="modal__hero-texto">
                  <p className="modal__categoria">{item.categoriaNome}</p>
                  <h2 className="modal__titulo">
                    {item.codigo ? <span className="item-card__codigo">{item.codigo}</span> : null}
                    {item.nome}
                  </h2>
                </div>
              </div>

              <div className="modal__corpo">
                {item.descricao ? <p className="modal__texto">{item.descricao}</p> : null}
                {item.ingredientes && item.ingredientes !== item.descricao ? (
                  <p className="modal__texto">
                    <strong>Ingredientes:</strong> {item.ingredientes}
                  </p>
                ) : null}

                {mostrarSeletorDeTamanho ? (
                  <fieldset className="opcoes">
                    <legend>Tamanho</legend>
                    {item.tamanhos.map((t) => (
                      <label key={t.id} className="opcao">
                        <input
                          type="radio"
                          name="tamanho"
                          checked={t.id === tamanhoId}
                          onChange={() => setTamanhoId(t.id)}
                        />
                        <span className="opcao__nome">{t.nome}</span>
                        <span className="opcao__preco">{formatarPreco(t.preco)}</span>
                      </label>
                    ))}
                  </fieldset>
                ) : null}

                {item.adicionais.length > 0 ? (
                  <fieldset className="opcoes">
                    <legend>Opcionais</legend>
                    <div className="opcoes__grade">
                      {item.adicionais.map((a) => (
                        <label key={a.id} className="opcao">
                          <input
                            type="checkbox"
                            checked={adicionaisSelecionados.includes(a.id)}
                            onChange={() => alternarAdicional(a.id)}
                          />
                          <span className="opcao__nome">{a.nome}</span>
                          <span className="opcao__preco">+ {formatarPreco(a.preco)}</span>
                        </label>
                      ))}
                    </div>
                  </fieldset>
                ) : null}

                <label className="campo">
                  <span>Observações (opcional)</span>
                  <textarea
                    rows={2}
                    maxLength={255}
                    placeholder="Ex.: sem cebola, bem passada..."
                    value={observacao}
                    onChange={(e) => setObservacao(e.target.value)}
                  />
                </label>
              </div>
            </div>

            <div className="modal__rodape">
              <div className="seletor-quantidade">
                <button
                  type="button"
                  onClick={() => setQuantidade((q) => Math.max(1, q - 1))}
                  aria-label="Diminuir quantidade"
                >
                  −
                </button>
                <span aria-live="polite">{quantidade}</span>
                <button
                  type="button"
                  onClick={() => setQuantidade((q) => Math.min(50, q + 1))}
                  aria-label="Aumentar quantidade"
                >
                  +
                </button>
              </div>

              <button
                type="button"
                className="botao botao--primario botao--grande"
                onClick={adicionarAoPedido}
                disabled={!tamanho}
              >
                Adicionar · {formatarPreco(total)}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
