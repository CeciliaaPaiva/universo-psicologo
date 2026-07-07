import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

export function ChatInput({ disabled, onEnviar }) {
  const [texto, setTexto] = useState('')

  function enviar() {
    if (!texto.trim() || disabled) return
    onEnviar(texto.trim())
    setTexto('')
  }

  return (
    <div className="flex items-end gap-2">
      <Input
        placeholder="Digite sua mensagem..."
        value={texto}
        disabled={disabled}
        onChange={(e) => setTexto(e.target.value)}
        onKeyDown={(e) => {
          if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault()
            enviar()
          }
        }}
      />
      <Button onClick={enviar} disabled={disabled || !texto.trim()}>
        Enviar
      </Button>
    </div>
  )
}
