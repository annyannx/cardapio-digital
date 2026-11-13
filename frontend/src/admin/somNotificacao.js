// Aviso sonoro de pedido novo (RF007), gerado pelo próprio navegador.
// Os navegadores só liberam o som depois de um clique do usuário,
// por isso o painel tem um botão "Ativar som" que chama prepararSom().

let contexto = null

/** Libera o áudio (chame dentro de um clique). Devolve true se o navegador suporta som. */
export function prepararSom() {
  try {
    if (!contexto) {
      const Contexto = window.AudioContext || window.webkitAudioContext
      if (Contexto) contexto = new Contexto()
    }
    if (contexto && contexto.state === 'suspended') {
      contexto.resume()
    }
  } catch {
    contexto = null
  }
  return contexto !== null
}

/** Toca dois toques curtos ("ding-dong"). */
export function tocarAviso() {
  if (!contexto) return
  try {
    const inicio = contexto.currentTime
    const notas = [880, 1175]

    notas.forEach((frequencia, indice) => {
      const comeco = inicio + indice * 0.2
      const oscilador = contexto.createOscillator()
      const volume = contexto.createGain()

      oscilador.type = 'sine'
      oscilador.frequency.value = frequencia

      volume.gain.setValueAtTime(0.0001, comeco)
      volume.gain.exponentialRampToValueAtTime(0.35, comeco + 0.02)
      volume.gain.exponentialRampToValueAtTime(0.0001, comeco + 0.18)

      oscilador.connect(volume)
      volume.connect(contexto.destination)
      oscilador.start(comeco)
      oscilador.stop(comeco + 0.2)
    })
  } catch {
    // sem som: o aviso visual continua funcionando
  }
}
