import { Link } from 'react-router-dom'
import { ChatWindow } from '@/components/chatbot/ChatWindow'

export function ChatbotPage() {
  return (
    <div className="mx-auto flex min-h-screen max-w-2xl flex-col gap-6 p-6">
      <div className="flex flex-col gap-1.5">
        <Link to="/login" className="text-sm text-muted-foreground hover:underline">
          ← Voltar
        </Link>
        <div className="flex items-center gap-3">
          <img src="/unipsi-icon.png" alt="Universo Psicólogo" className="h-10 w-10" />
          <h1 className="text-xl font-medium">Converse com nosso assistente de triagem</h1>
        </div>
        <p className="text-sm text-muted-foreground">
          Este espaço é anônimo e não exige cadastro. Nosso assistente nunca emite diagnósticos —
          ele está aqui para te ouvir e te encaminhar a um profissional quando fizer sentido.
        </p>
      </div>

      <ChatWindow />
    </div>
  )
}
