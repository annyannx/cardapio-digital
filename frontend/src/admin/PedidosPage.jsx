import { useCallback, useEffect, useRef, useState } from 'react'
import { STATUS } from '../utils'
import { alterarStatusPedido, listarPedidos, ouvirPedidos } from './adminApi'
import { useAdmin } from './AdminContext'
import PedidoCard from './PedidoCard'
import { prepararSom, tocarAviso } from './somNotificacao'

export default function PedidosPage() {
  const { sessao, tratarErro } = useAdmin()
  const token = sessao.token

  const [pedidos, setPedidos] = useState([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')
  const [filtro, setFiltro] = useState('')
  const [aoVivo, setAoVivo] = useState(false)
  const [somLigado, setSomLigado] = useState(false)
  const [destacados, setDestacados] = useState([])
  const [aviso, setAviso] = useState('')

  // O som é lido de dentro do canal de tempo real, por isso fica também numa "ref".
  const somLigadoRef = useRef(false)

  const carregar = useCallback(async () => {
    try {
      const lista = await listarPedidos(token)
      setPedidos(lista)
      setErro('')
    } catch (e) {
      setErro(tratarErro(e))
    } finally {
      setCarregando(false)
    }
  }, [token, tratarErro])

  // Carrega a lista ao abrir e, por garantia, atualiza a cada 20 segundos.
  useEffect(() => {
    carregar()
    const intervalo = setInterval(carregar, 20000)
    return () => clearInterval(intervalo)
  }, [carregar])

  // Tempo real (RF007): o servidor avisa na hora quando chega um pedido novo
  // ou quando um status muda.
  useEffect(() => {
    const controle = new AbortController()
    let ativo = true

    function tratarEvento(nome, pedido) {
      if (nome === 'pedido-novo') {
        setPedidos((atuais) =>
          atuais.some((p) => p.numero === pedido.numero) ? atuais : [pedido, ...atuais],
        )
        setDestacados((atuais) => [...atuais, pedido.numero])
        setAviso(`Novo pedido #${pedido.numero} de ${pedido.clienteNome}`)
        if (somLigadoRef.current) tocarAviso()
        setTimeout(() => {
          setDestacados((atuais) => atuais.filter((n) => n !== pedido.numero))
        }, 10000)
      } else if (nome === 'pedido-atualizado') {
        setPedidos((atuais) => atuais.map((p) => (p.numero === pedido.numero ? pedido : p)))
      }
    }

    async function ciclo() {
      while (ativo) {
        try {
          await ouvirPedidos(token, {
            sinal: controle.signal,
            aoConectar: () => {
              if (ativo) setAoVivo(true)
            },
            aoEvento: (nome, dados) => {
              if (ativo) tratarEvento(nome, dados)
            },
          })
        } catch (e) {
          if (!ativo) return
          if (e.status === 401 || e.status === 403) {
            tratarErro(e)
            return
          }
        }
        if (!ativo) return
        setAoVivo(false)
        // A conexão caiu: espera um pouco e tenta de novo.
        await new Promise((resolver) => setTimeout(resolver, 3000))
      }
    }

    ciclo()
    return () => {
      ativo = false
      controle.abort()
    }
  }, [token, tratarErro])

  function alternarSom() {
    if (!somLigado) {
      const suportado = prepararSom()
      if (!suportado) {
        setAviso('Este navegador não permite o aviso sonoro.')
        return
      }
      somLigadoRef.current = true
      setSomLigado(true)
      tocarAviso() // toque de teste
    } else {
      somLigadoRef.current = false
      setSomLigado(false)
    }
  }

  async function mudarStatus(numero, status) {
    try {
      const atualizado = await alterarStatusPedido(token, numero, status)
      setPedidos((atuais) => atuais.map((p) => (p.numero === numero ? atualizado : p)))
      setErro('')
    } catch (e) {
      setErro(tratarErro(e))
    }
  }

  const visiveis = filtro ? pedidos.filter((p) => p.status === filtro) : pedidos
  const contar = (codigo) => pedidos.filter((p) => p.status === codigo).length

  return (
    <section>
      <div className="admin__titulo">
        <h2>Pedidos</h2>
        <div className="admin__titulo-acoes">
          <span className={`ao-vivo ${aoVivo ? 'ao-vivo--ligado' : ''}`}>
            <span className="ao-vivo__ponto" aria-hidden="true" />
            {aoVivo ? 'Tempo real ativo' : 'Reconectando...'}
          </span>
          <button
            type="button"
            className={`botao ${somLigado ? 'botao--primario' : 'botao--secundario'}`}
            onClick={alternarSom}
          >
            {somLigado ? '🔔 Som ligado' : '🔕 Ativar som de novos pedidos'}
          </button>
        </div>
      </div>

      {aviso ? (
        <div className="faixa-aviso" role="status">
          <span>🔔 {aviso}</span>
          <button type="button" onClick={() => setAviso('')} aria-label="Fechar aviso">
            ✕
          </button>
        </div>
      ) : null}

      <div className="filtros" role="tablist" aria-label="Filtrar por status">
        <button
          type="button"
          className={`filtro ${filtro === '' ? 'filtro--ativo' : ''}`}
          onClick={() => setFiltro('')}
        >
          Todos ({pedidos.length})
        </button>
        {STATUS.map((s) => (
          <button
            key={s.codigo}
            type="button"
            className={`filtro ${filtro === s.codigo ? 'filtro--ativo' : ''}`}
            onClick={() => setFiltro(s.codigo)}
          >
            {s.rotulo} ({contar(s.codigo)})
          </button>
        ))}
      </div>

      {erro ? (
        <div className="alerta alerta--erro" role="alert">
          {erro}
        </div>
      ) : null}

      {carregando ? (
        <div className="estado">Carregando pedidos...</div>
      ) : visiveis.length === 0 ? (
        <div className="estado">Nenhum pedido {filtro ? 'com esse status' : 'ainda'}.</div>
      ) : (
        <div className="pedidos">
          {visiveis.map((pedido) => (
            <PedidoCard
              key={pedido.numero}
              pedido={pedido}
              destacado={destacados.includes(pedido.numero)}
              aoMudarStatus={mudarStatus}
            />
          ))}
        </div>
      )}
    </section>
  )
}
