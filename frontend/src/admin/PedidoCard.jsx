import { useState } from 'react'
import { STATUS, formatarDataHora, formatarPreco, rotuloStatus } from '../utils'
import './pedido-card.css'

// Próximo passo natural de cada status (botão principal do cartão).
const PROXIMO = {
  RECEBIDO: { status: 'EM_PREPARO', texto: 'Iniciar preparo' },
  EM_PREPARO: { status: 'PRONTO', texto: 'Marcar como pronto' },
  PRONTO: { status: 'ENTREGUE', texto: 'Marcar como entregue' },
}

export default function PedidoCard({ pedido, destacado, aoMudarStatus }) {
  const [salvando, setSalvando] = useState(false)
  const proximo = PROXIMO[pedido.status]

  async function mudar(status) {
    setSalvando(true)
    try {
      await aoMudarStatus(pedido.numero, status)
    } finally {
      setSalvando(false)
    }
  }

  return (
    <article className={`pedido ${destacado ? 'pedido--novo' : ''}`}>
      <header className="pedido__cabecalho">
        <div>
          <h3 className="pedido__numero">Pedido #{pedido.numero}</h3>
          <p className="pedido__meta">
            {formatarDataHora(pedido.criadoEm)} · <strong>{pedido.clienteNome}</strong>
            {pedido.mesa ? ` · Mesa ${pedido.mesa}` : ''}
          </p>
        </div>
        <span className={`badge badge--${pedido.status}`}>{rotuloStatus(pedido.status)}</span>
      </header>

      <ul className="pedido__itens">
        {pedido.itens.map((item, indice) => (
          <li key={`${item.nome}-${item.tamanho}-${indice}`}>
            <div>
              <strong>
                {item.quantidade}x {item.nome}
              </strong>
              {item.tamanho !== 'Único' ? <span> ({item.tamanho})</span> : null}
              {item.adicionais.length > 0 ? (
                <div className="pedido__detalhe">+ {item.adicionais.map((a) => a.nome).join(', ')}</div>
              ) : null}
              {item.observacao ? <div className="pedido__detalhe">Obs.: {item.observacao}</div> : null}
            </div>
            <span>{formatarPreco(item.subtotal)}</span>
          </li>
        ))}
      </ul>

      {pedido.observacao ? <p className="pedido__obs">Obs. do pedido: {pedido.observacao}</p> : null}

      <div className="pedido__total">
        <span>Total</span>
        <strong>{formatarPreco(pedido.valorTotal)}</strong>
      </div>

      {/* Feedback do cliente para o administrador: o cliente confirmou que recebeu? */}
      {pedido.status === 'ENTREGUE' ? (
        pedido.confirmadoEm ? (
          <p className="pedido__confirmacao pedido__confirmacao--ok">
            ✓ Cliente confirmou o recebimento em {formatarDataHora(pedido.confirmadoEm)}
          </p>
        ) : (
          <p className="pedido__confirmacao">⏳ Aguardando o cliente confirmar o recebimento</p>
        )
      ) : null}

      <div className="pedido__acoes">
        {proximo ? (
          <button
            type="button"
            className="botao botao--primario"
            disabled={salvando}
            onClick={() => mudar(proximo.status)}
          >
            {salvando ? 'Salvando...' : proximo.texto}
          </button>
        ) : (
          <span className="pedido__finalizado">Pedido finalizado ✓</span>
        )}

        <label className="pedido__trocar">
          <span>Mudar para</span>
          <select
            value=""
            disabled={salvando}
            onChange={(e) => e.target.value && mudar(e.target.value)}
          >
            <option value="">Escolher...</option>
            {STATUS.filter((s) => s.codigo !== pedido.status).map((s) => (
              <option key={s.codigo} value={s.codigo}>
                {s.rotulo}
              </option>
            ))}
          </select>
        </label>
      </div>

      <details className="pedido__historico">
        <summary>Histórico de alterações ({pedido.historico.length})</summary>
        <ul>
          {pedido.historico.map((h, indice) => (
            <li key={`${h.statusNovo}-${h.dataHora}-${indice}`}>
              <strong>{rotuloStatus(h.statusNovo)}</strong>
              <span>
                {formatarDataHora(h.dataHora)} · {h.usuario}
              </span>
            </li>
          ))}
          {pedido.confirmadoEm ? (
            <li>
              <strong>Recebimento confirmado</strong>
              <span>{formatarDataHora(pedido.confirmadoEm)} · Cliente</span>
            </li>
          ) : null}
        </ul>
      </details>
    </article>
  )
}
