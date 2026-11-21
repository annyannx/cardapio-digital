import { useCallback, useEffect, useRef, useState } from 'react'
import {
  atualizarAdicionalAdmin,
  criarAdicionalAdmin,
  listarAdicionaisAdmin,
  listarCategoriasAdmin,
  removerAdicionalAdmin,
} from './adminApi'
import { useAdmin } from './AdminContext'
import './opcionais.css'

/** 6 -> "6,00" (para mostrar no campo) */
function precoParaCampo(preco) {
  const numero = Number(preco)
  return Number.isFinite(numero) ? numero.toFixed(2).replace('.', ',') : ''
}

/** "6,50" -> 6.5 (NaN se não for um número válido) */
function campoParaNumero(texto) {
  const limpo = String(texto).trim().replace(',', '.')
  return limpo === '' ? Number.NaN : Number(limpo)
}

/** Resumo do que o usuário pode editar: serve para saber se a linha foi alterada. */
function assinatura(linha) {
  return JSON.stringify([
    linha.nome.trim(),
    campoParaNumero(linha.preco),
    linha.ativo,
    [...linha.categoriaIds].sort((a, b) => a - b),
  ])
}

function linhaDoServidor(dto) {
  const linha = {
    chave: String(dto.id),
    id: dto.id,
    nome: dto.nome,
    preco: precoParaCampo(dto.preco),
    ativo: dto.ativo,
    categoriaIds: dto.categoriaIds,
  }
  return { ...linha, original: assinatura(linha), salvando: false, erro: '' }
}

const alterada = (linha) => assinatura(linha) !== linha.original

export default function OpcionaisPage() {
  const { sessao, tratarErro } = useAdmin()
  const token = sessao.token

  const [linhas, setLinhas] = useState([])
  const [categorias, setCategorias] = useState([])
  const [carregando, setCarregando] = useState(true)
  const [erroGeral, setErroGeral] = useState('')
  const [aviso, setAviso] = useState('')
  const [salvandoTodas, setSalvandoTodas] = useState(false)
  const contadorNovo = useRef(0)
  const temporizadorAviso = useRef(null)

  const carregar = useCallback(async () => {
    try {
      const [dtos, cats] = await Promise.all([listarAdicionaisAdmin(token), listarCategoriasAdmin(token)])
      setCategorias(cats)
      setLinhas(dtos.map(linhaDoServidor))
      setErroGeral('')
    } catch (e) {
      setErroGeral(tratarErro(e))
    } finally {
      setCarregando(false)
    }
  }, [token, tratarErro])

  useEffect(() => {
    carregar()
  }, [carregar])

  useEffect(() => () => clearTimeout(temporizadorAviso.current), [])

  function mostrarAviso(mensagem) {
    setAviso(mensagem)
    clearTimeout(temporizadorAviso.current)
    temporizadorAviso.current = setTimeout(() => setAviso(''), 3000)
  }

  function alterar(chave, campo, valor) {
    setLinhas((atuais) => atuais.map((l) => (l.chave === chave ? { ...l, [campo]: valor, erro: '' } : l)))
  }

  function alternarCategoria(chave, categoriaId) {
    setLinhas((atuais) =>
      atuais.map((l) => {
        if (l.chave !== chave) return l
        const tem = l.categoriaIds.includes(categoriaId)
        return {
          ...l,
          erro: '',
          categoriaIds: tem ? l.categoriaIds.filter((c) => c !== categoriaId) : [...l.categoriaIds, categoriaId],
        }
      }),
    )
  }

  function adicionarNovo() {
    contadorNovo.current += 1
    const nova = {
      chave: `novo-${contadorNovo.current}`,
      id: null,
      nome: '',
      preco: '',
      ativo: true,
      categoriaIds: categorias.slice(0, 2).map((c) => c.id),
      original: '',
      salvando: false,
      erro: '',
    }
    setLinhas((atuais) => [nova, ...atuais])
  }

  /** Salva uma linha. Devolve true se deu certo. */
  async function salvar(linha) {
    const nome = linha.nome.trim()
    const preco = campoParaNumero(linha.preco)

    if (!nome) {
      alterar(linha.chave, 'erro', 'Informe o nome do opcional.')
      return false
    }
    if (!Number.isFinite(preco) || preco < 0) {
      alterar(linha.chave, 'erro', 'Informe um preço válido (ex.: 6,50).')
      return false
    }

    const corpo = { nome, preco, ativo: linha.ativo, categoriaIds: linha.categoriaIds }

    setLinhas((atuais) => atuais.map((l) => (l.chave === linha.chave ? { ...l, salvando: true, erro: '' } : l)))
    try {
      const dto = linha.id
        ? await atualizarAdicionalAdmin(token, linha.id, corpo)
        : await criarAdicionalAdmin(token, corpo)
      setLinhas((atuais) => atuais.map((l) => (l.chave === linha.chave ? linhaDoServidor(dto) : l)))
      return true
    } catch (e) {
      const mensagem = [tratarErro(e), ...(e.detalhes || [])].join(' ')
      setLinhas((atuais) =>
        atuais.map((l) => (l.chave === linha.chave ? { ...l, salvando: false, erro: mensagem } : l)),
      )
      return false
    }
  }

  async function salvarTodas() {
    const pendentes = linhas.filter(alterada)
    if (pendentes.length === 0) return

    setSalvandoTodas(true)
    let certas = 0
    for (const linha of pendentes) {
      // Um de cada vez, para o banco não receber tudo junto.
      if (await salvar(linha)) certas += 1
    }
    setSalvandoTodas(false)

    if (certas === pendentes.length) {
      mostrarAviso(`${certas} ${certas === 1 ? 'opcional salvo' : 'opcionais salvos'}.`)
    } else {
      setErroGeral('Alguns opcionais não foram salvos. Veja a mensagem em vermelho em cada um.')
    }
  }

  async function remover(linha) {
    if (!linha.id) {
      setLinhas((atuais) => atuais.filter((l) => l.chave !== linha.chave))
      return
    }
    const confirmado = window.confirm(
      `Remover o opcional "${linha.nome}"?\n\nEle deixa de aparecer para os clientes. Pedidos antigos não são afetados.`,
    )
    if (!confirmado) return

    try {
      await removerAdicionalAdmin(token, linha.id)
      setLinhas((atuais) => atuais.filter((l) => l.chave !== linha.chave))
      mostrarAviso(`"${linha.nome}" removido.`)
    } catch (e) {
      setErroGeral(tratarErro(e))
    }
  }

  const pendentes = linhas.filter(alterada).length

  return (
    <section>
      <div className="admin__titulo">
        <h2>Opcionais</h2>
        <div className="admin__titulo-acoes">
          <button type="button" className="botao botao--secundario" onClick={adicionarNovo}>
            + Novo opcional
          </button>
          <button
            type="button"
            className="botao botao--primario"
            onClick={salvarTodas}
            disabled={pendentes === 0 || salvandoTodas}
          >
            {salvandoTodas ? 'Salvando...' : `Salvar todas as alterações (${pendentes})`}
          </button>
        </div>
      </div>

      <p className="opcionais__ajuda">
        Altere o nome ou o preço e clique em <strong>Salvar</strong> na linha (ou em{' '}
        <strong>Salvar todas as alterações</strong>). Marque as categorias em que cada opcional deve
        aparecer para o cliente.
      </p>

      {aviso ? (
        <div className="alerta alerta--ok" role="status">
          {aviso}
        </div>
      ) : null}
      {erroGeral ? (
        <div className="alerta alerta--erro" role="alert">
          {erroGeral}
        </div>
      ) : null}

      {carregando ? (
        <div className="estado">Carregando os opcionais...</div>
      ) : linhas.length === 0 ? (
        <div className="estado">Nenhum opcional cadastrado.</div>
      ) : (
        <ul className="opcionais">
          {linhas.map((linha) => {
            const mudou = alterada(linha)
            return (
              <li
                key={linha.chave}
                className={`opc ${mudou ? 'opc--alterado' : ''} ${linha.ativo ? '' : 'opc--inativo'}`}
              >
                <div className="opc__linha">
                  <label className="campo opc__nome">
                    <span>Nome</span>
                    <input
                      type="text"
                      value={linha.nome}
                      maxLength={80}
                      onChange={(e) => alterar(linha.chave, 'nome', e.target.value)}
                    />
                  </label>

                  <label className="campo opc__preco">
                    <span>Preço (R$)</span>
                    <input
                      type="text"
                      inputMode="decimal"
                      value={linha.preco}
                      onChange={(e) => alterar(linha.chave, 'preco', e.target.value)}
                      placeholder="0,00"
                    />
                  </label>
                </div>

                <div className="opc__linha opc__linha--rodape">
                  <div className="opc__categorias">
                    <span className="opc__rotulo">Aparece em:</span>
                    {categorias.map((categoria) => (
                      <label key={categoria.id} className="opcao">
                        <input
                          type="checkbox"
                          checked={linha.categoriaIds.includes(categoria.id)}
                          onChange={() => alternarCategoria(linha.chave, categoria.id)}
                        />
                        <span>{categoria.nome}</span>
                      </label>
                    ))}
                  </div>

                  <div className="opc__acoes">
                    <label className="opcao">
                      <input
                        type="checkbox"
                        checked={linha.ativo}
                        onChange={(e) => alterar(linha.chave, 'ativo', e.target.checked)}
                      />
                      <span>Ativo</span>
                    </label>

                    <button
                      type="button"
                      className="botao botao--primario"
                      onClick={() => salvar(linha)}
                      disabled={!mudou || linha.salvando || salvandoTodas}
                    >
                      {linha.salvando ? 'Salvando...' : 'Salvar'}
                    </button>
                    <button type="button" className="linha__remover" onClick={() => remover(linha)}>
                      Remover
                    </button>
                  </div>
                </div>

                {linha.erro ? (
                  <div className="alerta alerta--erro" role="alert">
                    {linha.erro}
                  </div>
                ) : null}
              </li>
            )
          })}
        </ul>
      )}
    </section>
  )
}
