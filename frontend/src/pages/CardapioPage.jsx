import { useEffect, useState } from 'react'
import { buscarCardapio } from '../api'
import ItemCard from '../components/ItemCard'
import ItemModal from '../components/ItemModal'
import { iconeCategoria } from '../utils'

export default function CardapioPage({ aoAdicionado }) {
  const [categorias, setCategorias] = useState([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')
  const [tentativa, setTentativa] = useState(0)
  const [categoriaAtivaId, setCategoriaAtivaId] = useState(null)
  const [itemAbertoId, setItemAbertoId] = useState(null)

  // RF001: o cardápio vem da API. Trocar de categoria não recarrega a página:
  // os dados já estão aqui e só mudamos o que é exibido.
  useEffect(() => {
    let ativo = true
    setCarregando(true)
    setErro('')
    buscarCardapio()
      .then((dados) => {
        if (!ativo) return
        setCategorias(dados)
        setCategoriaAtivaId((atual) => atual ?? dados[0]?.id ?? null)
      })
      .catch((e) => {
        if (ativo) setErro(e.message)
      })
      .finally(() => {
        if (ativo) setCarregando(false)
      })
    return () => {
      ativo = false
    }
  }, [tentativa])

  const categoriaAtiva = categorias.find((c) => c.id === categoriaAtivaId) ?? categorias[0] ?? null

  if (carregando) {
    return <div className="estado">Carregando o cardápio...</div>
  }

  if (erro) {
    return (
      <div className="estado estado--erro">
        <p>{erro}</p>
        <button
          type="button"
          className="botao botao--secundario"
          onClick={() => setTentativa((t) => t + 1)}
        >
          Tentar novamente
        </button>
      </div>
    )
  }

  if (categorias.length === 0) {
    return <div className="estado">O cardápio ainda está vazio.</div>
  }

  return (
    <>
      <nav className="abas" aria-label="Categorias do cardápio">
        {categorias.map((categoria) => (
          <button
            key={categoria.id}
            type="button"
            className={`aba ${categoria.id === categoriaAtiva?.id ? 'aba--ativa' : ''}`}
            onClick={() => setCategoriaAtivaId(categoria.id)}
            aria-current={categoria.id === categoriaAtiva?.id ? 'true' : undefined}
          >
            <span aria-hidden="true">{iconeCategoria(categoria.nome)}</span> {categoria.nome}
          </button>
        ))}
      </nav>

      <section aria-label={categoriaAtiva?.nome}>
        <h2 className="titulo-categoria">{categoriaAtiva?.nome}</h2>

        {categoriaAtiva && categoriaAtiva.itens.length === 0 ? (
          <div className="estado">Nenhum item disponível nesta categoria.</div>
        ) : (
          <div className="grade">
            {categoriaAtiva?.itens.map((item) => (
              <ItemCard
                key={item.id}
                item={item}
                categoriaNome={categoriaAtiva.nome}
                aoAbrir={(i) => setItemAbertoId(i.id)}
              />
            ))}
          </div>
        )}
      </section>

      {itemAbertoId !== null ? (
        <ItemModal
          itemId={itemAbertoId}
          aoFechar={() => setItemAbertoId(null)}
          aoAdicionado={aoAdicionado}
        />
      ) : null}
    </>
  )
}
