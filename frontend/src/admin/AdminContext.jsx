import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import { apagarSessao, lerSessao, salvarSessao } from './adminApi'

const AdminContext = createContext(null)

export function AdminProvider({ children }) {
  const [sessao, setSessao] = useState(lerSessao)
  const [avisoLogin, setAvisoLogin] = useState('')

  const iniciarSessao = useCallback((novaSessao) => {
    salvarSessao(novaSessao)
    setSessao(novaSessao)
    setAvisoLogin('')
  }, [])

  const sair = useCallback((mensagem = '') => {
    apagarSessao()
    setSessao(null)
    setAvisoLogin(mensagem)
  }, [])

  /**
   * Transforma um erro da API em texto para a tela.
   * Se o token expirou (401), encerra a sessão e volta para o login.
   */
  const tratarErro = useCallback(
    (erro) => {
      if (erro?.status === 401) {
        sair('Sua sessão expirou. Entre novamente.')
        return 'Sessão expirada.'
      }
      if (erro?.status === 403) {
        return 'Você não tem permissão para fazer isso.'
      }
      return erro?.message || 'Ocorreu um erro inesperado.'
    },
    [sair],
  )

  const valor = useMemo(
    () => ({ sessao, avisoLogin, iniciarSessao, sair, tratarErro }),
    [sessao, avisoLogin, iniciarSessao, sair, tratarErro],
  )

  return <AdminContext.Provider value={valor}>{children}</AdminContext.Provider>
}

export function useAdmin() {
  const contexto = useContext(AdminContext)
  if (!contexto) {
    throw new Error('useAdmin precisa ser usado dentro de <AdminProvider>')
  }
  return contexto
}
