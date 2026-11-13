import { useEffect, useState } from 'react'
import { buscarAcompanhamento, confirmarRecebimento, urlStreamPedido } from '../api'
import { STATUS, formatarDataHora, formatarPreco, rotuloStatus } from '../utils'
import './acompanhar.css'

export default function AcompanharPage({ numero }) {
  const [situacao, setSituacao] = useState(null)
  const [erro, setErro] = useState('')
  const [aoVivo, setAoVivo] = useState(false)
  const [confirmando, setConfirmando] = useState(false)
  const [erroConfirmacao, setErroConfirmacao] = useState('')

  useEffect(() => {
    let ativo = true
    setSituacao(null)
    setErro('')
    setAoVivo(false)
    setErroConfirmacao('')

    function consultar() {
      buscarAcompanhamento(numero)
        .then((dados) => {
          if (!ativo) return
          setSituacao(dados)
          setErro('')
        })
        .catch((e) => {
          if (ativo) setErro(e.message)
        })
    }

    consultar()

    // Tempo real: o servidor avisa assim que o status muda (SSE).
    const fonte = new EventSource(urlStreamPedido(numero))
    fonte.onopen = () => {
      if (ativo) setAoVivo(true)
    }
    fonte.onerror = () => {
      if (ativo) setAoVivo(false)
    }
    fonte.addEventListener('status', (evento) => {
      if (!ativo) return
      try {
        setSituacao(JSON.parse(evento.data))
        setErro('')
      } catch {
        // mensagem inesperada: ignora
      }
    })

    // Garantia extra: se a conexão em tempo real cair, consulta de tempos em tempos.
    const intervalo = setInterval(consultar, 15000)

    return () => {
      ativo = false
      fonte.close()
      clearInterval(intervalo)
    }
  }, [numero])

  async function confirmar() {
    setConfirmando(true)
    setErroConfirmacao('')
    try {
      const dados = await confirmarRecebimento(numero)
      setSituacao(dados)
    } catch (e) {
      setErroConfirmacao(e.message)
    } finally {
      setConfirmando(false)
    }
  }

  if (erro && !situacao) {
    return (
      <div className="estado estado--erro">
        <p>{erro}</p>
        <a className="botao botao--secundario" href="#/">
          Voltar ao cardápio
        </a>
      </div>
    )
  }

  if (!situacao) {
    return <div className="estado">Buscando o seu pedido...</div>
  }

  const indiceAtual = STATUS.findIndex((s) => s.codigo === situacao.status)
  const statusAtual = STATUS[indiceAtual]

  // Entregue pelo estabelecimento, mas o cliente ainda não confirmou que recebeu.
  const finalizado = Boolean(situacao.confirmadoEm)
  const podeConfirmar = situacao.status === 'ENTREGUE' && !finalizado

  // Etapas mostradas na lista: o histórico + a confirmação do cliente (se houver).
  const etapas = situacao.historico.map((e) => ({
    chave: `${e.status}-${e.dataHora}`,
    rotulo: rotuloStatus(e.status),
    dataHora: e.dataHora,
  }))
  if (finalizado) {
    etapas.push({
      chave: `confirmado-${situacao.confirmadoEm}`,
      rotulo: 'Recebimento confirmado por você',
      dataHora: situacao.confirmadoEm,
    })
  }

  return (
    <section className="cartao acompanhar" aria-live="polite">
      <p className="acompanhar__numero">Pedido #{situacao.numero}</p>
      <h2 className={`acompanhar__status ${finalizado ? 'acompanhar__status--finalizado' : ''}`}>
        {finalizado ? 'Pedido finalizado' : (statusAtual?.rotulo ?? situacao.status)}
      </h2>
      <p className="acompanhar__descricao">
        {finalizado
          ? 'Obrigado pela preferência! Bom apetite e volte sempre.'
          : statusAtual?.descricao}
      </p>

      <ol className="passos">
        {STATUS.map((passo, indice) => {
          let classe = 'passo'
          if (finalizado || indice < indiceAtual) classe = 'passo passo--feito'
          else if (indice === indiceAtual) classe = 'passo passo--atual'
          return (
            <li key={passo.codigo} className={classe}>
              <span className="passo__bolinha">{finalizado || indice < indiceAtual ? '✓' : indice + 1}</span>
              <span className="passo__rotulo">{passo.rotulo}</span>
            </li>
          )
        })}
      </ol>

      {podeConfirmar ? (
        <div className="confirmar">
          <p className="confirmar__titulo">🛎️ Seu pedido foi entregue!</p>
          <p>Já está com você? Toque no botão para confirmar que recebeu.</p>
          <button
            type="button"
            className="botao botao--primario botao--grande"
            onClick={confirmar}
            disabled={confirmando}
          >
            {confirmando ? 'Confirmando...' : 'Recebi meu pedido ✓'}
          </button>
          {erroConfirmacao ? (
            <p className="confirmar__erro" role="alert">
              {erroConfirmacao}
            </p>
          ) : null}
        </div>
      ) : null}

      {finalizado ? (
        <div className="finalizado" role="status">
          <strong>✓ Tudo certo!</strong>
          <span>Você confirmou o recebimento em {formatarDataHora(situacao.confirmadoEm)}.</span>
        </div>
      ) : (
        <p className={`ao-vivo ${aoVivo ? 'ao-vivo--ligado' : ''}`}>
          <span className="ao-vivo__ponto" aria-hidden="true" />
          {aoVivo ? 'Atualizando em tempo real' : 'Reconectando... (atualizamos a cada 15 s)'}
        </p>
      )}

      <dl className="resumo">
        <div>
          <dt>Total</dt>
          <dd>{formatarPreco(situacao.valorTotal)}</dd>
        </div>
        <div>
          <dt>Feito em</dt>
          <dd>{formatarDataHora(situacao.criadoEm)}</dd>
        </div>
      </dl>

      <h3 className="acompanhar__subtitulo">Etapas</h3>
      <ul className="historico">
        {etapas.map((etapa) => (
          <li key={etapa.chave}>
            <strong>{etapa.rotulo}</strong>
            <span>{formatarDataHora(etapa.dataHora)}</span>
          </li>
        ))}
      </ul>

      <a className="botao botao--secundario" href="#/">
        Voltar ao cardápio
      </a>
    </section>
  )
}
