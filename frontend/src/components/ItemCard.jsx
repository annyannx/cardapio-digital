import { formatarPreco, iconeCategoria } from '../utils'

export default function ItemCard({ item, categoriaNome, aoAbrir }) {
  function aoTeclar(evento) {
    if (evento.key === 'Enter' || evento.key === ' ') {
      evento.preventDefault()
      aoAbrir(item)
    }
  }

  const varios = item.tamanhos.length > 1

  return (
    <article
      className="item-card"
      role="button"
      tabIndex={0}
      onClick={() => aoAbrir(item)}
      onKeyDown={aoTeclar}
      aria-label={`Ver detalhes de ${item.nome}`}
    >
      <div className="item-card__imagem">
        {item.imagemUrl ? (
          <img src={item.imagemUrl} alt={item.nome} loading="lazy" />
        ) : (
          <span className="item-card__icone" aria-hidden="true">
            {iconeCategoria(categoriaNome)}
          </span>
        )}
      </div>

      <div className="item-card__corpo">
        <h3 className="item-card__titulo">
          {item.codigo ? <span className="item-card__codigo">{item.codigo}</span> : null}
          {item.nome}
        </h3>

        {item.descricao ? <p className="item-card__descricao">{item.descricao}</p> : null}

        <div className="item-card__precos">
          {item.tamanhos.map((tamanho) => (
            <span key={tamanho.id} className="chip">
              {varios ? `${tamanho.nome} · ` : ''}
              {formatarPreco(tamanho.preco)}
            </span>
          ))}
        </div>
      </div>
    </article>
  )
}
