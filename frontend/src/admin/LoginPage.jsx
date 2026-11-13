import { useState } from 'react'
import { NOME_ESTABELECIMENTO } from '../config'
import { entrar } from './adminApi'
import { useAdmin } from './AdminContext'

export default function LoginPage() {
  const { iniciarSessao, avisoLogin } = useAdmin()

  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState('')

  async function enviar(evento) {
    evento.preventDefault()
    setErro('')

    if (!email.trim() || !senha) {
      setErro('Informe o e-mail e a senha.')
      return
    }

    setEnviando(true)
    try {
      const resposta = await entrar(email.trim(), senha)
      iniciarSessao({ token: resposta.token, nome: resposta.nome, perfil: resposta.perfil })
    } catch (e) {
      setErro(e.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="login-tela">
      <form className="login cartao" onSubmit={enviar}>
        <div className="login__marca" aria-hidden="true">
          🍕
        </div>
        <h1 className="login__titulo">{NOME_ESTABELECIMENTO}</h1>
        <p className="login__subtitulo">Painel do administrador</p>

        {avisoLogin ? <div className="alerta alerta--info">{avisoLogin}</div> : null}

        <label className="campo">
          <span>E-mail</span>
          <input
            type="email"
            autoComplete="username"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="admin@cardapio.com"
            required
          />
        </label>

        <label className="campo">
          <span>Senha</span>
          <input
            type="password"
            autoComplete="current-password"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            required
          />
        </label>

        {erro ? (
          <div className="alerta alerta--erro" role="alert">
            {erro}
          </div>
        ) : null}

        <button type="submit" className="botao botao--primario botao--grande" disabled={enviando}>
          {enviando ? 'Entrando...' : 'Entrar'}
        </button>

        <a className="login__voltar" href="#/">
          ← Voltar ao cardápio
        </a>
      </form>
    </div>
  )
}
