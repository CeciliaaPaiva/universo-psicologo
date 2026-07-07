export function ChatMessage({ role, conteudo }) {
  const doUsuario = role === 'user'

  return (
    <div className={`flex ${doUsuario ? 'justify-end' : 'justify-start'}`}>
      <div
        className={`max-w-[80%] whitespace-pre-wrap rounded-lg px-3 py-2 text-sm ${
          doUsuario ? 'bg-primary text-primary-foreground' : 'bg-muted text-foreground'
        }`}
      >
        {conteudo}
      </div>
    </div>
  )
}
