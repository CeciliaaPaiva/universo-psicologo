import ReactMarkdown from 'react-markdown'

export function ChatMessage({ role, conteudo }) {
  const doUsuario = role === 'user'

  return (
    <div className={`flex ${doUsuario ? 'justify-end' : 'justify-start'}`}>
      <div
        className={`max-w-[80%] rounded-lg px-3 py-2 text-sm [&_ol]:list-decimal [&_ol]:pl-5 [&_p+p]:mt-2 [&_strong]:font-semibold [&_ul]:list-disc [&_ul]:pl-5 ${
          doUsuario ? 'bg-primary text-primary-foreground' : 'bg-muted text-foreground'
        }`}
      >
        <ReactMarkdown>{conteudo}</ReactMarkdown>
      </div>
    </div>
  )
}
