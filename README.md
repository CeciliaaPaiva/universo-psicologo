# Universo Psicólogo

Plataforma digital de saúde mental que conecta psicólogos comprometidos com terapia social a
pacientes em situação de vulnerabilidade socioeconômica. Oferece ferramentas de gestão para o
psicólogo (agenda, prontuário, financeiro) e acesso facilitado para o paciente (marketplace,
chatbot de triagem).

Modelo de negócio: B2B2C — SaaS para psicólogos + marketplace social para pacientes.

## Documentação

Toda a documentação de produto e arquitetura vive em [`docs/`](docs):

| Arquivo | Conteúdo |
|---|---|
| [`Doc-Visão-UniPsi.md`](docs/Doc-Visão-UniPsi.md) | Visão, problema, impacto social, escopo e stakeholders |
| [`Doc-Requisitos-UniPsi.md`](docs/Doc-Requisitos-UniPsi.md) | Requisitos funcionais/não funcionais e casos de uso |
| [`Backlog-UniPsi.md`](docs/Backlog-UniPsi.md) | Histórias de usuário por épico, com critérios de aceitação |
| [`Sprints-UniPsi.md`](docs/Sprints-UniPsi.md) | Plano de sprints do MVP |
| [`Arquitetura-UniPsi.md`](docs/Arquitetura-UniPsi.md) | Stack, estrutura de pacotes, fluxos técnicos |
| [`ER-UniPsi.md`](docs/ER-UniPsi.md) | Diagrama entidade-relacionamento |
| [`Testes-UniPsi.md`](docs/Testes-UniPsi.md) | Estratégia de cobertura de testes |
| [`Debitos-UniPsi.md`](docs/Debitos-UniPsi.md) | Índice vivo de débitos técnicos — o que está aberto agora e o que já foi resolvido |
| [`releases/`](releases) | Changelog por versão entregue |
| [`atas/`](atas) | Atas de reunião com o stakeholder — uma por reunião |

Contexto de convenções de código e regras de negócio para desenvolvimento assistido está em
[`CLAUDE.md`](CLAUDE.md).

## Stack

- **Backend:** Java 21, Spring Boot 3, Spring Security + JWT, Spring Data JPA, Flyway, Redis, MinIO
- **Frontend:** React 18, Vite, React Router, Tailwind CSS + shadcn/ui, TanStack Query, React Hook Form + Zod, Zustand
- **Infraestrutura local:** PostgreSQL 17, Redis 7, MinIO, Caddy (reverse proxy)
- **Serviços externos:** Google Gemini (chatbot), Resend (e-mail), Google Calendar API (agenda)

## Progresso

| Sprint | Foco | Status |
|---|---|---|
| 0 — Fundação | Cadastro, autenticação, painel de aprovação | ✅ Entregue (`v0.1.0-fundacao`) |
| 1 — Agenda e Plantão | Slots, Google Calendar, plantão de urgência, perfil do psicólogo | ✅ Entregue (`v0.2.0-agenda`) |
| 2 — Marketplace | Busca, agendamento, precificação dinâmica | ✅ Entregue (`v0.3.0-marketplace`) |
| 3 — Prontuário | Anotações criptografadas, codinome, auditoria de acesso | ✅ Entregue (`v0.4.0-prontuario`) |
| 4 — Chatbot | Triagem via IA generativa, detecção de crise, plantão de urgência | ✅ Entregue (`v0.5.0-chatbot`) |
| 5 e 6 | Financeiro, administração/QA | 📋 Planejadas |

Veja o changelog detalhado de cada entrega em [`releases/`](releases).

## Rodando localmente

Pré-requisitos: Java 21, Node 20+, e uma stack de PostgreSQL/Redis/MinIO acessível (local ou via
Docker). As variáveis de ambiente ficam em `api/.env` (veja `api/.env.example` como referência —
nunca commitar o `.env` real).

**Acesse sempre pelos domínios do Caddy — `http://unipsi-web.claudinha.local` (frontend) e
`http://unipsi-api.claudinha.local` (backend) — nunca por `localhost` direto.** É o que
`FRONTEND_ORIGIN` e `GOOGLE_REDIRECT_URI` em `api/.env` assumem como origem real; acessar via
`localhost` pode disparar erro de CORS dependendo do que estiver configurado ali.

```bash
# Backend — porta 8101, casando com o domínio unipsi-api.claudinha.local já provisionado
cd api
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8101

# Frontend — porta 8100, casando com unipsi-web.claudinha.local
# --host 0.0.0.0 é obrigatório: o Vite por padrão só escuta em ::1 (IPv6), e o
# reverse_proxy do Caddy aponta para 127.0.0.1 (IPv4) — sem isso o Caddy responde 502
# mesmo com o Vite rodando normalmente.
cd web
npm install
VITE_API_PROXY_TARGET=http://localhost:8101 npm run dev -- --port 8100 --host 0.0.0.0
```

Sem o ambiente Caddy provisionado (ex.: fora deste projeto), backend e frontend caem nas portas
padrão do Spring Boot/Vite (8080/5173) e falam entre si via `VITE_API_PROXY_TARGET` — mas isso é
o caso excepcional, não o fluxo padrão de desenvolvimento aqui.

## Credenciais de teste (ambiente local)

> Válidas apenas no ambiente de desenvolvimento local. O usuário ADMIN é criado automaticamente
> na primeira subida do backend a partir de `ADMIN_EMAIL`/`ADMIN_PASSWORD` em `api/.env`
> (veja `AdminSeeder`).

| Papel | E-mail | Senha | Observação |
|---|---|---|---|
| Admin | `admin@unipsi.local` | `UnipsiAdmin123!` | Acessa `/admin/aprovacoes` para aprovar cadastros de psicólogos |
| Psicólogo (aprovado) | `psi.sprint1.1783357196@teste.com` | `SenhaForte123` | Criado durante os testes da Sprint 1; acessa `/agenda`, `/plantao`, `/perfil` |

Para testar o marketplace como paciente, cadastre-se em `/cadastro/paciente` (login liberado
imediatamente). O psicólogo acima já tem ao menos um slot futuro disponível (criado nos testes da
Sprint 1) e deve aparecer em `/marketplace`; para testar o fluxo de agendamento por completo, crie
outro slot futuro em `/agenda` logado como esse psicólogo.

Para criar novos usuários de teste:
- **Psicólogo:** cadastre-se em `/cadastro/psicologo` e aprove o cadastro com a conta ADMIN em `/admin/aprovacoes`
- **Paciente:** cadastre-se em `/cadastro/paciente` (login liberado imediatamente, sem aprovação)

O chatbot de triagem (`/chatbot`) é público — não exige login nem cadastro. Sem `GEMINI_API_KEY`
configurada, ele responde com um fallback local (ver `releases/v0.5.0-chatbot.md`), que ainda assim
detecta crise e aciona o plantão normalmente.
