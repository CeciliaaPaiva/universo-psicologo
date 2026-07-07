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
| 4 a 6 | Chatbot, financeiro, administração/QA | 📋 Planejadas |

Veja o changelog detalhado de cada entrega em [`releases/`](releases).

## Rodando localmente

Pré-requisitos: Java 21, Node 20+, e uma stack de PostgreSQL/Redis/MinIO acessível (local ou via
Docker). As variáveis de ambiente ficam em `api/.env` (veja `api/.env.example` como referência —
nunca commitar o `.env` real).

```bash
# Backend (porta padrão 8080)
cd api
./mvnw spring-boot:run

# Frontend (porta padrão 5173, proxy de /api para localhost:8080)
cd web
npm install
npm run dev
```

Se você estiver usando o ambiente de dev provisionado via Caddy (domínios `*.claudinha.local`),
suba o backend na porta correspondente ao domínio da API e o frontend com
`VITE_API_PROXY_TARGET` apontando para essa porta — veja "Pontos de atenção" em
[`releases/v0.2.0-agenda.md`](releases/v0.2.0-agenda.md).

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
