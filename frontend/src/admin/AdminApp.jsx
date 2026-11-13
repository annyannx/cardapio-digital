import { useEffect, useState } from 'react'
import { NOME_ESTABELECIMENTO } from '../config'
import './admin.css'
import { buscarMe } from './adminApi'
import { AdminProvider, useAdmin } from './AdminContext'
import ItensPage from './ItensPage'
import LoginPage from './LoginPage'
import PedidosPage from './PedidosPage'

function AdminConteudo({ rota }) {
  const { sessao, sair } = useAdmin()
  const token = sessao?.token ?? null
  const [verificando, setVerificando] = useState(Boolean(token))

  // RF005: ao abrir o painel, confere com o servidor se o token ainda é válido.
  useEffect(() => {
    if (!token) {
      setVerificando(false)
      return undefined
    }

    let ativo = true
    setVerificando(true)
    buscarMe(token)
      .then(() => {
        if (ativo) setVerificando(false)
      })
      .catch((e) => {
        if (!ativo) return
        if (e.status === 401 || e.status === 403) {
          sair('Sua sessão expirou. Entre novamente.')
        }
        setVerificando(false)
      })

    return () => {
      ativo = false
    }
  }, [token, sair])

  // Sem login: tela de entrada (rotas /admin protegidas).
  if (!sessao) {
    return <LoginPage />
  }

  if (verificando) {
    return <div className="estado">Verificando sessão...</div>
  }

  const naAbaCardapio = rota.startsWith('/admin/itens')

  return (
    <div className="admin">
      <header className="admin__topo">
        <div className="admin__marca">
          <span aria-hidden="true">🍕</span>
          <div>
            <strong>{NOME_ESTABELECIMENTO}</strong>
            <small>Painel do administrador</small>
          </div>
        </div>

        <nav className="admin__nav" aria-label="Menu do painel">
          <a href="#/admin/pedidos" className={naAbaCardapio ? '' : 'admin__link--ativo'}>
            Pedidos
          </a>
          <a href="#/admin/itens" className={naAbaCardapio ? 'admin__link--ativo' : ''}>
            Cardápio
          </a>
        </nav>

        <div className="admin__usuario">
          <a href="#/" className="admin__ver-site">
            Ver cardápio
          </a>
          <span className="admin__nome">{sessao.nome}</span>
          <button type="button" className="botao botao--secundario" onClick={() => sair()}>
            Sair
          </button>
        </div>
      </header>

      <main className="admin__conteudo">{naAbaCardapio ? <ItensPage /> : <PedidosPage />}</main>
    </div>
  )
}

export default function AdminApp({ rota }) {
  return (
    <AdminProvider>
      <AdminConteudo rota={rota} />
    </AdminProvider>
  )
}
