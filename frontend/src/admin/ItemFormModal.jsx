import { useEffect, useState } from 'react'
import { enviarImagem } from './adminApi'
import { useAdmin } from './AdminContext'

// Atalhos para preencher os tamanhos mais comuns.
const MODELOS_TAMANHOS = [
  { rotulo: 'Pizza (Grande / Broto)', nomes: ['Grande', 'Broto'] },
  { rotulo: 'Refri (Lata / 600 ml / 1 Litro / 2 Litros)', nomes: ['Lata', '600 ml', '1 Litro', '2 Litros'] },
  { rotulo: 'Preço único', nomes: ['Único'] },
]

/** "74.9" -> "74,90" (para mostrar no campo) */
function paraCampo(preco) {
  const numero = Number(preco)
  return Number.isFinite(numero) ? numero.toFixed(2).replace('.', ',') : ''
}

/** "74,90" -> 74.9 (para enviar à API). Devolve NaN se não for um número válido. */
function paraNumero(texto) {
  const limpo = String(texto).trim().replace(',', '.')
  if (limpo === '') return Number.NaN
  return Number(limpo)
}

export default function ItemFormModal({ item, categorias, categoriaInicialId, aoFechar, aoSalvar }) {
  const { sessao, tratarErro } = useAdmin()
  const editando = Boolean(item)

  const [categoriaId, setCategoriaId] = useState(
    String(item?.categoriaId ?? categoriaInicialId ?? categorias[0]?.id ?? ''),
  )
  const [codigo, setCodigo] = useState(item?.codigo ?? '')
  const [nome, setNome] = useState(item?.nome ?? '')
  const [descricao, setDescricao] = useState(item?.descricao ?? '')
  const [ingredientes, setIngredientes] = useState(item?.ingredientes ?? '')
  const [imagemUrl, setImagemUrl] = useState(item?.imagemUrl ?? '')
  const [disponivel, setDisponivel] = useState(item?.disponivel ?? true)
  const [tamanhos, setTamanhos] = useState(
    item?.tamanhos?.length
      ? item.tamanhos.map((t) => ({ nome: t.nome, preco: paraCampo(t.preco) }))
      : [{ nome: 'Único', preco: '' }],
  )

  const [enviandoImagem, setEnviandoImagem] = useState(false)
  const [salvando, setSalvando] = useState(false)
  const [erro, setErro] = useState('')
  const [detalhes, setDetalhes] = useState([])

  // Esc fecha a janela e o fundo da página não rola.
  useEffect(() => {
    function aoTeclar(evento) {
      if (evento.key === 'Escape') aoFechar()
    }
    window.addEventListener('keydown', aoTeclar)
    const anterior = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    return () => {
      window.removeEventListener('keydown', aoTeclar)
      document.body.style.overflow = anterior
    }
  }, [aoFechar])

  function alterarTamanho(indice, campo, valor) {
    setTamanhos((atuais) => atuais.map((t, i) => (i === indice ? { ...t, [campo]: valor } : t)))
  }

  function adicionarTamanho() {
    setTamanhos((atuais) => [...atuais, { nome: '', preco: '' }])
  }

  function removerTamanho(indice) {
    setTamanhos((atuais) => atuais.filter((_, i) => i !== indice))
  }

  function aplicarModelo(nomes) {
    // Mantém os preços já digitados, na ordem, e completa o resto vazio.
    setTamanhos(nomes.map((n, i) => ({ nome: n, preco: tamanhos[i]?.preco ?? '' })))
  }

  async function escolherImagem(evento) {
    const arquivo = evento.target.files?.[0]
    evento.target.value = '' // permite escolher o mesmo arquivo de novo
    if (!arquivo) return

    setErro('')
    setDetalhes([])
    setEnviandoImagem(true)
    try {
      const resposta = await enviarImagem(sessao.token, arquivo)
      setImagemUrl(resposta.url)
    } catch (e) {
      setErro(tratarErro(e))
    } finally {
      setEnviandoImagem(false)
    }
  }

  async function enviar(evento) {
    evento.preventDefault()
    setErro('')
    setDetalhes([])

    // Validação dos campos obrigatórios antes de enviar (RF006).
    if (!categoriaId) {
      setErro('Escolha a categoria.')
      return
    }
    if (!nome.trim()) {
      setErro('Informe o nome do item.')
      return
    }
    if (tamanhos.length === 0) {
      setErro('Informe pelo menos um tamanho com preço.')
      return
    }

    const tamanhosEnviar = []
    for (const t of tamanhos) {
      const preco = paraNumero(t.preco)
      if (!t.nome.trim()) {
        setErro('Todo tamanho precisa de um nome (ex.: Grande).')
        return
      }
      if (!Number.isFinite(preco) || preco < 0) {
        setErro(`Informe um preço válido para "${t.nome.trim()}" (ex.: 49,90).`)
        return
      }
      tamanhosEnviar.push({ nome: t.nome.trim(), preco })
    }

    const dados = {
      categoriaId: Number(categoriaId),
      codigo: codigo.trim() || null,
      nome: nome.trim(),
      descricao: descricao.trim() || null,
      ingredientes: ingredientes.trim() || null,
      imagemUrl: imagemUrl.trim() || null,
      disponivel,
      tamanhos: tamanhosEnviar,
    }

    setSalvando(true)
    try {
      await aoSalvar(dados, item?.id)
    } catch (e) {
      setErro(tratarErro(e))
      setDetalhes(e.detalhes || [])
      setSalvando(false)
    }
  }

  return (
    <div className="modal-fundo" role="presentation">
      <div className="modal modal--formulario" role="dialog" aria-modal="true" aria-label="Item do cardápio">
        <header className="drawer__cabecalho">
          <h2>{editando ? 'Editar item' : 'Novo item'}</h2>
          <button type="button" className="modal__fechar" onClick={aoFechar} aria-label="Fechar">
            ✕
          </button>
        </header>

        <form className="formulario" onSubmit={enviar}>
          <div className="formulario__corpo">
            <div className="formulario__grade">
              <label className="campo">
                <span>Categoria *</span>
                <select value={categoriaId} onChange={(e) => setCategoriaId(e.target.value)} required>
                  {categorias.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.nome}
                    </option>
                  ))}
                </select>
              </label>

              <label className="campo">
                <span>Código (opcional)</span>
                <input
                  type="text"
                  value={codigo}
                  maxLength={10}
                  onChange={(e) => setCodigo(e.target.value)}
                  placeholder="Ex.: 221"
                />
              </label>
            </div>

            <label className="campo">
              <span>Nome *</span>
              <input
                type="text"
                value={nome}
                maxLength={100}
                onChange={(e) => setNome(e.target.value)}
                required
              />
            </label>

            <label className="campo">
              <span>Descrição resumida</span>
              <input
                type="text"
                value={descricao}
                maxLength={255}
                onChange={(e) => setDescricao(e.target.value)}
                placeholder="Aparece na lista do cardápio"
              />
            </label>

            <label className="campo">
              <span>Ingredientes</span>
              <textarea
                rows={2}
                maxLength={2000}
                value={ingredientes}
                onChange={(e) => setIngredientes(e.target.value)}
                placeholder="Aparece nos detalhes do item"
              />
            </label>

            <fieldset className="opcoes">
              <legend>Tamanhos e preços *</legend>

              <div className="modelos">
                {MODELOS_TAMANHOS.map((m) => (
                  <button
                    key={m.rotulo}
                    type="button"
                    className="filtro"
                    onClick={() => aplicarModelo(m.nomes)}
                  >
                    {m.rotulo}
                  </button>
                ))}
              </div>

              {tamanhos.map((t, indice) => (
                <div key={indice} className="tamanho-linha">
                  <input
                    type="text"
                    value={t.nome}
                    maxLength={40}
                    onChange={(e) => alterarTamanho(indice, 'nome', e.target.value)}
                    placeholder="Nome (ex.: Grande)"
                    aria-label={`Nome do tamanho ${indice + 1}`}
                  />
                  <input
                    type="text"
                    inputMode="decimal"
                    value={t.preco}
                    onChange={(e) => alterarTamanho(indice, 'preco', e.target.value)}
                    placeholder="Preço (ex.: 49,90)"
                    aria-label={`Preço do tamanho ${indice + 1}`}
                  />
                  <button
                    type="button"
                    className="linha__remover"
                    onClick={() => removerTamanho(indice)}
                    aria-label={`Remover tamanho ${indice + 1}`}
                  >
                    Remover
                  </button>
                </div>
              ))}

              <button type="button" className="botao botao--secundario" onClick={adicionarTamanho}>
                + Adicionar tamanho
              </button>
            </fieldset>

            <fieldset className="opcoes">
              <legend>Foto do item</legend>
              <div className="foto">
                <div className="foto__previa">
                  {imagemUrl ? <img src={imagemUrl} alt="Prévia da foto" /> : <span aria-hidden="true">🖼️</span>}
                </div>
                <div className="foto__acoes">
                  <label className="botao botao--secundario">
                    {enviandoImagem ? 'Enviando...' : imagemUrl ? 'Trocar foto' : 'Escolher foto'}
                    <input
                      type="file"
                      accept="image/jpeg,image/png,image/webp"
                      onChange={escolherImagem}
                      disabled={enviandoImagem}
                      hidden
                    />
                  </label>
                  {imagemUrl ? (
                    <button type="button" className="linha__remover" onClick={() => setImagemUrl('')}>
                      Remover foto
                    </button>
                  ) : null}
                  <small>JPG, PNG ou WEBP, até 5 MB.</small>
                </div>
              </div>
            </fieldset>

            <label className="opcao">
              <input type="checkbox" checked={disponivel} onChange={(e) => setDisponivel(e.target.checked)} />
              <span className="opcao__nome">Disponível no cardápio dos clientes</span>
            </label>

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
          </div>

          <footer className="modal__rodape">
            <button type="button" className="botao botao--secundario" onClick={aoFechar}>
              Cancelar
            </button>
            <button
              type="submit"
              className="botao botao--primario botao--grande"
              disabled={salvando || enviandoImagem}
            >
              {salvando ? 'Salvando...' : editando ? 'Salvar alterações' : 'Cadastrar item'}
            </button>
          </footer>
        </form>
      </div>
    </div>
  )
}
