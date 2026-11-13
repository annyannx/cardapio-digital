import { useCallback, useEffect, useRef, useState } from 'react'
import { formatarPreco, iconeCategoria } from '../utils'
import {
  atualizarItemAdmin,
  criarItemAdmin,
  listarCategoriasAdmin,
  listarItensAdmin,
  removerItemAdmin,
} from './adminApi'
import { useAdmin } from './AdminContext'
import ItemFormModal from './ItemFormModal'

/** Monta o corpo do PUT a partir de um item que já existe (para alterações rápidas). */
function montarDados(item, alteracoes = {}) {
  return {
    categoriaId: item.categoriaId,
    codigo: item.codigo,
    nome: item.nome,
    descricao: item.descricao,
    ingredientes: item.ingredientes,
    imagemUrl: item.imagemUrl,
    disponivel: item.disponivel,
    tamanhos: item.tamanhos.map((t) => ({ nome: t.nome, preco: t.preco })),
    ...alteracoes,
  }
}

export default function ItensPage() {
  const { sessao, tratarErro } = useAdmin()
  const token = sessao.token

  const [categorias, setCategorias] = useState([])
  const [itens, setItens] = useState([])
  const [filtro, setFiltro] = useState('')
  const [busca, setBusca] = useState('')
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState('')
  const [formAberto, setFormAberto] = useState(false)
  const [itemEmEdicao, setItemEmEdicao] = useState(null)
  const [ocupadoId, setOcupadoId] = useState(null)
  const temporizadorAviso = useRef(null)

  const carregar = useCallback(async () => {
    try {
      const [listaCategorias, listaItens] = await Promise.all([
        listarCategoriasAdmin(token),
        listarItensAdmin(token),
      ])
      setCategorias(listaCategorias)
      setItens(listaItens)
      setErro('')
    } catch (e) {
      setErro(tratarErro(e))
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

  const fecharFormulario = useCallback(() => {
    setFormAberto(false)
    setItemEmEdicao(null)
  }, [])

  function abrirNovo() {
    setItemEmEdicao(null)
    setFormAberto(true)
  }

  function abrirEdicao(item) {
    setItemEmEdicao(item)
    setFormAberto(true)
  }

  // Chamado pelo formulário. Se a API recusar, o erro volta para o formulário mostrar.
  async function salvar(dados, id) {
    if (id) {
      await atualizarItemAdmin(token, id, dados)
    } else {
      await criarItemAdmin(token, dados)
    }
    fecharFormulario()
    await carregar() // o cardápio é atualizado automaticamente após cada operação
    mostrarAviso(id ? 'Item atualizado.' : 'Item cadastrado.')
  }

  async function alternarDisponivel(item) {
    setOcupadoId(item.id)
    try {
      await atualizarItemAdmin(token, item.id, montarDados(item, { disponivel: !item.disponivel }))
      await carregar()
      mostrarAviso(item.disponivel ? `"${item.nome}" escondido do cardápio.` : `"${item.nome}" voltou ao cardápio.`)
    } catch (e) {
      setErro(tratarErro(e))
    } finally {
      setOcupadoId(null)
    }
  }

  async function remover(item) {
    const confirmado = window.confirm(
      `Remover "${item.nome}" do cardápio?\n\nEsta ação é permanente e não pode ser desfeita.`,
    )
    if (!confirmado) return

    setOcupadoId(item.id)
    try {
      await removerItemAdmin(token, item.id)
      await carregar()
      mostrarAviso(`"${item.nome}" removido.`)
    } catch (e) {
      setErro(tratarErro(e))
    } finally {
      setOcupadoId(null)
    }
  }

  const termo = busca.trim().toLowerCase()
  const visiveis = itens.filter((item) => {
    if (filtro && String(item.categoriaId) !== filtro) return false
    if (!termo) return true
    return (
      item.nome.toLowerCase().includes(termo) || (item.codigo ?? '').toLowerCase().includes(termo)
    )
  })

  const semFoto = itens.filter((i) => !i.imagemUrl).length

  return (
    <section>
      <div className="admin__titulo">
        <h2>Cardápio</h2>
        <button type="button" className="botao botao--primario" onClick={abrirNovo}>
          + Novo item
        </button>
      </div>

      {aviso ? (
        <div className="alerta alerta--ok" role="status">
          {aviso}
        </div>
      ) : null}
      {erro ? (
        <div className="alerta alerta--erro" role="alert">
          {erro}
        </div>
      ) : null}

      <div className="filtros" role="tablist" aria-label="Filtrar por categoria">
        <button
          type="button"
          className={`filtro ${filtro === '' ? 'filtro--ativo' : ''}`}
          onClick={() => setFiltro('')}
        >
          Todas ({itens.length})
        </button>
        {categorias.map((c) => (
          <button
            key={c.id}
            type="button"
            className={`filtro ${filtro === String(c.id) ? 'filtro--ativo' : ''}`}
            onClick={() => setFiltro(String(c.id))}
          >
            {c.nome} ({itens.filter((i) => i.categoriaId === c.id).length})
          </button>
        ))}
      </div>

      <div className="busca">
        <input
          type="search"
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
          placeholder="Buscar por nome ou código..."
          aria-label="Buscar item"
        />
        {semFoto > 0 ? <small>{semFoto} itens ainda sem foto</small> : <small>Todos os itens têm foto 🎉</small>}
      </div>

      {carregando ? (
        <div className="estado">Carregando o cardápio...</div>
      ) : visiveis.length === 0 ? (
        <div className="estado">Nenhum item encontrado.</div>
      ) : (
        <ul className="itens-admin">
          {visiveis.map((item) => (
            <li key={item.id} className={`item-admin ${item.disponivel ? '' : 'item-admin--oculto'}`}>
              <div className="item-admin__foto">
                {item.imagemUrl ? (
                  <img src={item.imagemUrl} alt={item.nome} loading="lazy" />
                ) : (
                  <span aria-hidden="true">{iconeCategoria(item.categoriaNome)}</span>
                )}
              </div>

              <div className="item-admin__info">
                <strong>
                  {item.codigo ? <span className="item-card__codigo">{item.codigo}</span> : null}
                  {item.nome}
                </strong>
                <span className="item-admin__categoria">
                  {item.categoriaNome}
                  {item.disponivel ? '' : ' · escondido do cardápio'}
                </span>
                <div className="item-card__precos">
                  {item.tamanhos.map((t) => (
                    <span key={t.id} className="chip">
                      {item.tamanhos.length > 1 ? `${t.nome} · ` : ''}
                      {formatarPreco(t.preco)}
                    </span>
                  ))}
                </div>
              </div>

              <div className="item-admin__acoes">
                <button
                  type="button"
                  className="botao botao--secundario"
                  onClick={() => abrirEdicao(item)}
                  disabled={ocupadoId === item.id}
                >
                  {item.imagemUrl ? 'Editar' : 'Editar / foto'}
                </button>
                <button
                  type="button"
                  className="botao botao--secundario"
                  onClick={() => alternarDisponivel(item)}
                  disabled={ocupadoId === item.id}
                >
                  {item.disponivel ? 'Esconder' : 'Mostrar'}
                </button>
                <button
                  type="button"
                  className="linha__remover"
                  onClick={() => remover(item)}
                  disabled={ocupadoId === item.id}
                >
                  Remover
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}

      {formAberto ? (
        <ItemFormModal
          key={itemEmEdicao?.id ?? 'novo'}
          item={itemEmEdicao}
          categorias={categorias}
          categoriaInicialId={filtro ? Number(filtro) : undefined}
          aoFechar={fecharFormulario}
          aoSalvar={salvar}
        />
      ) : null}
    </section>
  )
}
