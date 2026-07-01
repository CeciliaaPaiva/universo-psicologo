# Plano de Arquitetura — Universo Psicólogo

**Versão:** 1.0  
**Data:** 30/06/2026  
**Status:** Em revisão  
**Referência:** Doc-Requisitos-UniPsi.md, Backlog-UniPsi.md

---

## Sumário

1. [Visão Geral](#1-visão-geral)
2. [Stack Tecnológica](#2-stack-tecnológica)
3. [Arquitetura do Sistema](#3-arquitetura-do-sistema)
4. [Estrutura do Backend](#4-estrutura-do-backend)
5. [Estrutura do Frontend](#5-estrutura-do-frontend)
6. [Modelo de Dados](#6-modelo-de-dados)
7. [Segurança e LGPD](#7-segurança-e-lgpd)
8. [Integrações Externas](#8-integrações-externas)
9. [Infraestrutura de Desenvolvimento](#9-infraestrutura-de-desenvolvimento)
10. [Fluxos Técnicos Críticos](#10-fluxos-técnicos-críticos)
11. [Decisões em Aberto](#11-decisões-em-aberto)

---

## 1. Visão Geral

O Universo Psicólogo é entregue no MVP como uma aplicação web composta por:

- **API REST** em Spring Boot (monolito modular)
- **SPA** em React + Vite
- **Banco de dados** PostgreSQL
- **Cache e sessões** Redis
- **Armazenamento de arquivos** MinIO (compatível com S3)
- **Proxy reverso** Caddy

A escolha por monolito modular no MVP reduz a complexidade operacional sem sacrificar a organização do código: cada domínio tem seu próprio pacote isolado com controller, service, repository e domínio. A separação em microsserviços pode ocorrer no futuro sem reescrita, pois os módulos já têm fronteiras claras.

---

## 2. Stack Tecnológica

### Backend

| Componente | Tecnologia | Justificativa |
|---|---|---|
| Framework | Spring Boot 3.x | Ecossistema maduro, suporte nativo a segurança, JPA e REST |
| Segurança | Spring Security + JWT | Controle de acesso por roles; tokens stateless |
| ORM | Spring Data JPA + Hibernate | Mapeamento objeto-relacional com PostgreSQL |
| Migração de banco | Flyway | Versionamento de schema com histórico rastreável |
| Cache / Sessões | Spring Data Redis | Armazenamento de refresh tokens e estado do chatbot |
| Storage de arquivos | MinIO Java SDK | Upload de currículos e documentos (S3-compatível) |
| Envio de e-mail | Resend API (HTTP) | Tier gratuito: 100 e-mails/dia — suficiente para o MVP |
| IA do chatbot | Google Gemini API | `gemini-1.5-flash`: 1.500 req/dia grátis, suporte a português |
| Agenda | Google Calendar API | Integração gratuita via OAuth 2.0 |
| Build | Maven | Padrão do ecossistema Spring |
| Testes | JUnit 5 + Mockito | Testes unitários e de integração |

### Frontend

| Componente | Tecnologia |
|---|---|
| Framework | React 18 |
| Build | Vite |
| Roteamento | React Router v6 |
| Estilização | Tailwind CSS |
| Requisições HTTP | Axios + React Query (TanStack Query) |
| Formulários | React Hook Form + Zod (validação) |
| Componentes de UI | shadcn/ui (baseado em Radix UI + Tailwind) |
| Estado global | Zustand (leve, sem boilerplate) |
| Chat (interface) | Componente próprio com streaming de resposta |

### Infraestrutura

| Componente | Tecnologia | Observação |
|---|---|---|
| Banco de dados | PostgreSQL 17 | Ambiente Docker já existente em `~/workspace/dev-environment` |
| Cache | Redis 7 | Idem — `localhost:6379` |
| Object Storage | MinIO | Idem — API S3 em `http://s3.localhost:8000` |
| Proxy reverso | Caddy | Idem — porta 8000, config em `caddy/conf.d/` |
| Containers | Docker + Docker Compose | Orquestração local |

---

## 3. Arquitetura do Sistema

```
┌─────────────────────────────────────────────────────────────────────┐
│                          USUÁRIOS                                    │
│          Psicólogo          Paciente          Administrador          │
└──────────────────┬──────────────────────────────────────────────────┘
                   │ HTTPS
                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        CADDY (Proxy Reverso)                        │
│   unipsi.localhost:8000  →  frontend :5173 / backend :8080          │
└──────────────┬─────────────────────────────┬───────────────────────┘
               │                             │
               ▼                             ▼
┌──────────────────────┐       ┌─────────────────────────────────────┐
│  FRONTEND            │       │  BACKEND (Spring Boot — Monolito)   │
│  React 18 + Vite     │◄─────►│                                     │
│  :5173               │  REST │  ┌─────────┐  ┌──────────────────┐ │
│                      │  JSON │  │  auth   │  │    marketplace   │ │
│  /marketplace        │       │  ├─────────┤  ├──────────────────┤ │
│  /chatbot            │       │  │ usuario │  │    agenda        │ │
│  /dashboard/psi      │       │  ├─────────┤  ├──────────────────┤ │
│  /dashboard/pac      │       │  │plantao  │  │    prontuario    │ │
│  /admin              │       │  ├─────────┤  ├──────────────────┤ │
└──────────────────────┘       │  │chatbot  │  │    financeiro    │ │
                               │  ├─────────┤  ├──────────────────┤ │
                               │  │notific. │  │    admin         │ │
                               │  └─────────┘  └──────────────────┘ │
                               │            :8080                     │
                               └─────┬──────────────┬────────────────┘
                                     │              │
               ┌─────────────────────┘              └──────────────┐
               │                                                    │
               ▼                                                    ▼
┌──────────────────────────┐              ┌──────────────────────────┐
│  PostgreSQL 17           │              │  Redis 7                 │
│  :5432                   │              │  :6379                   │
│                          │              │                          │
│  · tabelas de domínio    │              │  · refresh tokens (JWT)  │
│  · prontuário criptogr.  │              │  · estado do chatbot     │
│  · histórico financeiro  │              │  · cache de sessão       │
└──────────────────────────┘              └──────────────────────────┘

               ┌─────────────────────────────────┐
               │  MinIO (Object Storage)          │
               │  :9000 (API S3)                  │
               │                                  │
               │  · currículos (PDF/DOCX)         │
               │  · fotos de perfil               │
               └─────────────────────────────────┘

               ┌──────────────────────────────────────────────────────┐
               │  SERVIÇOS EXTERNOS                                    │
               │                                                       │
               │  Google Gemini API   → chatbot (módulo chatbot)      │
               │  Resend API          → e-mails (módulo notificacao)  │
               │  Google Calendar API → agenda (módulo agenda)        │
               └──────────────────────────────────────────────────────┘
```

---

## 4. Estrutura do Backend

### Organização de pacotes

```
src/main/java/br/com/unipsi/
│
├── config/                        # Configurações globais
│   ├── SecurityConfig.java        # Spring Security + JWT filter
│   ├── CorsConfig.java
│   ├── RedisConfig.java
│   ├── MinioConfig.java
│   └── GeminiConfig.java
│
├── auth/                          # Autenticação e tokens
│   ├── controller/AuthController.java
│   ├── service/AuthService.java
│   ├── service/JwtService.java
│   ├── service/RefreshTokenService.java   # armazenado no Redis
│   ├── dto/LoginRequest.java
│   ├── dto/TokenResponse.java
│   └── filter/JwtAuthFilter.java
│
├── usuario/                       # Usuários, perfis e roles
│   ├── controller/UsuarioController.java
│   ├── service/PsicologoService.java
│   ├── service/PacienteService.java
│   ├── repository/UsuarioRepository.java
│   ├── repository/PsicologoRepository.java
│   ├── repository/PacienteRepository.java
│   ├── domain/Usuario.java        # entidade base com role
│   ├── domain/Psicologo.java      # extends Usuario
│   ├── domain/Paciente.java       # extends Usuario
│   ├── domain/Role.java           # enum: PSICOLOGO, PACIENTE, ADMIN
│   └── dto/
│
├── agenda/                        # Slots, sessões e Google Calendar
│   ├── controller/AgendaController.java
│   ├── service/AgendaService.java
│   ├── service/GoogleCalendarService.java
│   ├── repository/SlotRepository.java
│   ├── repository/SessaoRepository.java
│   ├── domain/Slot.java           # horário disponível
│   ├── domain/Sessao.java         # sessão agendada
│   ├── domain/StatusSessao.java   # enum: AGENDADA, REALIZADA, CANCELADA
│   └── dto/
│
├── plantao/                       # Disponibilidade de plantão
│   ├── controller/PlantaoController.java
│   ├── service/PlantaoService.java
│   ├── repository/DisponibilidadePlantaoRepository.java
│   ├── domain/DisponibilidadePlantao.java
│   └── dto/
│
├── prontuario/                    # Prontuário eletrônico (dados sensíveis)
│   ├── controller/ProntuarioController.java
│   ├── service/ProntuarioService.java
│   ├── service/CriptografiaService.java   # AES-256 para as anotações
│   ├── repository/ProntuarioRepository.java
│   ├── repository/AnotacaoRepository.java
│   ├── domain/Prontuario.java     # vínculo psicólogo → codinome
│   ├── domain/Anotacao.java       # texto criptografado + timestamp
│   └── dto/
│
├── marketplace/                   # Busca de psicólogos e agendamento
│   ├── controller/MarketplaceController.java
│   ├── service/MarketplaceService.java
│   ├── service/PrecificacaoService.java   # calcula valor pela faixa de renda
│   └── dto/
│
├── chatbot/                       # Triagem via IA generativa
│   ├── controller/ChatbotController.java
│   ├── service/ChatbotService.java
│   ├── service/GeminiClient.java          # HTTP client para Gemini API
│   ├── service/ConversacaoStateService.java  # estado no Redis (TTL)
│   ├── service/CriseDetectorService.java  # analisa resposta do LLM
│   └── dto/
│
├── financeiro/                    # Cobranças e relatórios
│   ├── controller/FinanceiroController.java
│   ├── service/CobrancaService.java
│   ├── service/RelatorioService.java
│   ├── repository/CobrancaRepository.java
│   ├── domain/Cobranca.java
│   ├── domain/StatusCobranca.java # enum: PENDENTE, PAGO, CANCELADO
│   └── dto/
│
├── notificacao/                   # Envio de e-mails via Resend
│   ├── service/EmailService.java
│   ├── service/ResendClient.java
│   └── template/                  # templates HTML dos e-mails
│
└── admin/                         # Painel de administração
    ├── controller/AdminController.java
    ├── service/AprovacaoService.java
    ├── service/RevisaoPerfilService.java
    └── dto/
```

### Migrations Flyway

```
src/main/resources/db/migration/
├── V1__create_usuarios.sql
├── V2__create_psicologos.sql
├── V3__create_pacientes.sql
├── V4__create_slots_sessoes.sql
├── V5__create_plantao.sql
├── V6__create_prontuario_anotacoes.sql
├── V7__create_cobrancas.sql
└── V8__create_revisao_perfil.sql
```

### Endpoints principais

| Método | Endpoint | Módulo | Acesso |
|---|---|---|---|
| POST | `/api/auth/register/psicologo` | auth | Público |
| POST | `/api/auth/register/paciente` | auth | Público |
| POST | `/api/auth/login` | auth | Público |
| POST | `/api/auth/refresh` | auth | Público |
| GET | `/api/marketplace/psicologos` | marketplace | PACIENTE |
| POST | `/api/agenda/slots` | agenda | PSICOLOGO |
| GET | `/api/agenda/slots` | agenda | PSICOLOGO |
| POST | `/api/agenda/sessoes` | agenda | PACIENTE |
| DELETE | `/api/agenda/sessoes/{id}` | agenda | PSICOLOGO |
| POST | `/api/plantao/disponibilidade` | plantao | PSICOLOGO |
| GET | `/api/prontuario/{codinome}` | prontuario | PSICOLOGO (próprio) |
| POST | `/api/prontuario/{codinome}/anotacoes` | prontuario | PSICOLOGO (próprio) |
| POST | `/api/chatbot/message` | chatbot | Público |
| GET | `/api/financeiro/relatorio` | financeiro | PSICOLOGO |
| GET | `/api/admin/aprovacoes` | admin | ADMIN |
| PUT | `/api/admin/aprovacoes/{id}` | admin | ADMIN |
| POST | `/api/marketplace/revisao-perfil` | marketplace | PSICOLOGO |

---

## 5. Estrutura do Frontend

```
universo-psicologo-web/
├── src/
│   ├── main.jsx
│   ├── App.jsx                    # Definição de rotas
│   │
│   ├── pages/
│   │   ├── public/
│   │   │   ├── LandingPage.jsx
│   │   │   ├── MarketplacePage.jsx
│   │   │   ├── PsicologoPublicProfile.jsx
│   │   │   └── ChatbotPage.jsx
│   │   ├── auth/
│   │   │   ├── LoginPage.jsx
│   │   │   ├── RegisterPsicologoPage.jsx
│   │   │   └── RegisterPacientePage.jsx
│   │   ├── psicologo/
│   │   │   ├── DashboardPage.jsx
│   │   │   ├── AgendaPage.jsx
│   │   │   ├── ProntuarioPage.jsx
│   │   │   ├── ProntuarioDetalhePage.jsx
│   │   │   ├── PlantaoPage.jsx
│   │   │   ├── FinanceiroPage.jsx
│   │   │   └── PerfilPage.jsx
│   │   ├── paciente/
│   │   │   ├── DashboardPage.jsx
│   │   │   ├── AgendamentosPage.jsx
│   │   │   ├── CobrancasPage.jsx
│   │   │   └── PerfilPage.jsx
│   │   └── admin/
│   │       ├── DashboardPage.jsx
│   │       ├── AprovacoesPage.jsx
│   │       └── RevisoesPerfilPage.jsx
│   │
│   ├── components/
│   │   ├── layout/
│   │   │   ├── Navbar.jsx
│   │   │   ├── Sidebar.jsx
│   │   │   └── PrivateLayout.jsx
│   │   ├── agenda/
│   │   │   ├── CalendarioSemanal.jsx
│   │   │   └── SlotCard.jsx
│   │   ├── chatbot/
│   │   │   ├── ChatWindow.jsx
│   │   │   ├── ChatMessage.jsx
│   │   │   └── ChatInput.jsx
│   │   └── shared/
│   │       ├── ConfirmDialog.jsx
│   │       └── StatusBadge.jsx
│   │
│   ├── hooks/
│   │   ├── useAuth.js             # lê/escreve token, dados do usuário
│   │   ├── useAgenda.js
│   │   └── useChatbot.js
│   │
│   ├── services/                  # chamadas à API (Axios)
│   │   ├── api.js                 # instância Axios com interceptors de token
│   │   ├── authService.js
│   │   ├── agendaService.js
│   │   ├── prontuarioService.js
│   │   ├── marketplaceService.js
│   │   ├── chatbotService.js
│   │   └── financeiroService.js
│   │
│   ├── store/
│   │   └── authStore.js           # Zustand: usuário logado, token, role
│   │
│   └── router/
│       ├── AppRouter.jsx
│       └── PrivateRoute.jsx       # redireciona se não autenticado ou sem role
│
├── public/
├── index.html
├── vite.config.js
├── tailwind.config.js
└── package.json
```

### Proteção de rotas por role

```
/dashboard/psicologo/*  →  role: PSICOLOGO
/dashboard/paciente/*   →  role: PACIENTE
/admin/*                →  role: ADMIN
/marketplace            →  público
/chatbot                →  público
```

---

## 6. Modelo de Dados

> Diagrama ER completo em Mermaid: [`ER-UniPsi.md`](ER-UniPsi.md)

### Diagrama de entidades (simplificado)

```
┌──────────────────────┐          ┌──────────────────────────┐
│       usuario        │          │        psicologo          │
├──────────────────────┤ 1      1 ├──────────────────────────┤
│ id (UUID)            │◄────────►│ id (UUID)                │
│ nome                 │          │ usuario_id (FK)           │
│ email (unique)       │          │ crp                       │
│ senha_hash           │          │ estado_crp                │
│ role (enum)          │          │ especializacoes (array)   │
│ ativo                │          │ politica_cancelamento     │
│ criado_em            │          │ link_videochamada         │
└──────────────────────┘          │ status_aprovacao (enum)  │
                                  │ curriculo_path (MinIO)   │
                                  └──────────────────────────┘

┌──────────────────────┐          ┌──────────────────────────┐
│       paciente        │          │   disponibilidade_plantao│
├──────────────────────┤ 1      N ├──────────────────────────┤
│ id (UUID)            │          │ id (UUID)                │
│ usuario_id (FK)      │          │ psicologo_id (FK)        │
│ data_nascimento      │          │ dia_semana (enum)        │
│ cidade               │          │ data_especifica (nullable)│
│ estado               │          │ ativo                    │
│ faixa_renda (enum)   │          └──────────────────────────┘
└──────────────────────┘

┌──────────────────────┐          ┌──────────────────────────┐
│         slot          │          │          sessao           │
├──────────────────────┤ 1      1 ├──────────────────────────┤
│ id (UUID)            │◄────────►│ id (UUID)                │
│ psicologo_id (FK)    │          │ slot_id (FK)             │
│ inicio               │          │ paciente_id (FK)         │
│ fim                  │          │ psicologo_id (FK)        │
│ disponivel           │          │ modalidade (enum)        │ ← AVULSA | PACOTE_MENSAL
│ google_event_id      │          │ valor_sessao (decimal)   │
└──────────────────────┘          │ taxa_plataforma (decimal)│
                                  │ valor_liquido (decimal)  │
                                  │ status (enum)            │
                                  │ cancelado_em (nullable)  │
                                  │ criada_em                │
                                  └──────────────────────────┘

┌──────────────────────┐          ┌──────────────────────────┐
│       prontuario      │ 1      N │         anotacao          │
├──────────────────────┤◄────────►├──────────────────────────┤
│ id (UUID)            │          │ id (UUID)                │
│ psicologo_id (FK)    │          │ prontuario_id (FK)       │
│ codinome             │          │ conteudo_enc (texto)     │ ← AES-256
│ paciente_id (FK, opt)│          │ iv (bytes)               │ ← vetor de inicialização
│ criado_em            │          │ criada_em                │
└──────────────────────┘          └──────────────────────────┘

┌──────────────────────┐          ┌──────────────────────────┐
│        cobranca       │          │     revisao_perfil        │
├──────────────────────┤          ├──────────────────────────┤
│ id (UUID)            │          │ id (UUID)                │
│ sessao_id (FK)       │          │ paciente_id (FK)         │
│ paciente_id (FK)     │          │ psicologo_id (FK)        │
│ valor (decimal)      │          │ justificativa            │
│ status (enum)        │          │ status (enum)            │
│ pago_em (nullable)   │          │ decisao_admin (nullable) │
│ criada_em            │          │ criada_em                │
└──────────────────────┘          └──────────────────────────┘
```

### Enums relevantes

```sql
-- Role do usuário
CREATE TYPE role AS ENUM ('PSICOLOGO', 'PACIENTE', 'ADMIN');

-- Status de aprovação do psicólogo
CREATE TYPE status_aprovacao AS ENUM ('PENDENTE', 'APROVADO', 'REPROVADO', 'AGUARDANDO_COMPLEMENTACAO');

-- Faixa de renda do paciente (renda domiciliar per capita — SM 2026 = R$ 1.621,00)
-- Apenas pacientes de FAIXA_1 a FAIXA_4 são elegíveis para terapia social (até Classe D)
CREATE TYPE faixa_renda AS ENUM ('FAIXA_1', 'FAIXA_2', 'FAIXA_3', 'FAIXA_4');
-- Avulsa:  FAIXA_1→R$60 | FAIXA_2→R$65 | FAIXA_3→R$70 | FAIXA_4→R$75
-- Pacote:  FAIXA_1→R$228/mês | FAIXA_2→R$247/mês | FAIXA_3→R$266/mês | FAIXA_4→R$285/mês (4 sessões, 5% desconto)
-- Taxa plataforma: 20% sobre o valor por sessão em ambas as modalidades

-- Modalidade de atendimento
CREATE TYPE modalidade_sessao AS ENUM ('AVULSA', 'PACOTE_MENSAL');

-- Status de sessão
CREATE TYPE status_sessao AS ENUM ('AGENDADA', 'REALIZADA', 'CANCELADA');

-- Status de cobrança
CREATE TYPE status_cobranca AS ENUM ('PENDENTE', 'PAGO', 'CANCELADO');

-- Dias da semana (plantão)
CREATE TYPE dia_semana AS ENUM ('SEG', 'TER', 'QUA', 'QUI', 'SEX', 'SAB', 'DOM');
```

---

## 7. Segurança e LGPD

### Autenticação JWT

```
Login → Access Token (15 min) + Refresh Token (7 dias, guardado no Redis)

A cada requisição autenticada:
  Frontend → envia Authorization: Bearer <access_token>
  Backend  → JwtAuthFilter valida o token e popula o SecurityContext

Refresh:
  POST /api/auth/refresh com o refresh token
  Backend valida no Redis e emite novo par de tokens
  Refresh usado é deletado do Redis (rotação)
```

### Controle de acesso (Spring Security)

```java
// Prontuário: apenas o psicólogo autor pode acessar
@PreAuthorize("#psicologoId == authentication.principal.id")
public ProntuarioDto getProntuario(UUID psicologoId, String codinome) { ... }
```

Regras por módulo:
- `prontuario/**` → PSICOLOGO, verificação adicional de propriedade no serviço
- `admin/**` → ADMIN
- `agenda/slots` (POST) → PSICOLOGO
- `agenda/sessoes` (POST) → PACIENTE
- `marketplace/**` (GET) → PACIENTE
- `chatbot/**` → Público (sem autenticação)

### Criptografia do prontuário (LGPD)

As anotações clínicas são classificadas como **dados sensíveis de saúde** pela LGPD (Art. 11). A proteção ocorre em duas camadas:

```
Camada 1 — Transporte: TLS via Caddy (HTTPS)
Camada 2 — Repouso: AES-256-GCM no nível da aplicação

Fluxo de escrita:
  texto_plano → CriptografiaService.encrypt() → {conteudo_enc, iv} → salvo no banco

Fluxo de leitura:
  {conteudo_enc, iv} → CriptografiaService.decrypt() → texto_plano → retornado ao psicólogo

A chave AES é armazenada em variável de ambiente (nunca no banco ou no código).
```

### Outras medidas

- Senhas armazenadas com **BCrypt** (fator de custo 12)
- UUIDs em vez de IDs sequenciais para dificultar enumeração
- CORS configurado para aceitar apenas a origem do frontend
- Rate limiting no endpoint do chatbot para evitar abuso (Redis-based)
- Logs de acesso ao prontuário para auditoria (tabela `auditoria_prontuario`)

---

## 8. Integrações Externas

### Google Gemini API (Chatbot)

```
Modelo: gemini-1.5-flash
Limite gratuito: 1.500 requisições/dia, 1M tokens/min

Fluxo de uma mensagem:
1. POST /api/chatbot/message { sessionId, mensagem }
2. ChatbotService recupera histórico do Redis (TTL: 30 min)
3. Monta o prompt com:
   - System prompt com persona e restrições (sem diagnóstico)
   - Histórico da conversa
   - Mensagem atual do usuário
4. Chama Gemini API
5. CriseDetectorService analisa a resposta e classifica: NORMAL | CRISE
6. Se CRISE: aciona módulo de plantão
7. Salva nova mensagem no histórico (Redis)
8. Retorna resposta ao frontend

System prompt (resumido):
  "Você é um assistente de triagem psicológica da plataforma Universo Psicólogo.
   Sua função é acolher o usuário, entender seu estado emocional e encaminhá-lo
   a um profissional. Você NUNCA deve emitir diagnósticos clínicos ou sugerir
   medicamentos. Em situações de crise, ofereça técnicas de regulação imediata
   e acione o encaminhamento ao psicólogo de plantão..."
```

### Resend API (E-mail)

```
Tier gratuito: 100 e-mails/dia
Domínio: configurar SPF/DKIM para domínio próprio (obrigatório para produção)

Eventos que disparam e-mail:
  · Cadastro de psicólogo recebido
  · Psicólogo aprovado / reprovado / aguardando complementação
  · Boas-vindas ao paciente
  · Confirmação de agendamento (paciente + psicólogo)
  · Lembrete 24h antes da sessão (paciente + psicólogo)
  · Lembrete 1h antes da sessão (paciente + psicólogo)
  · Sessão cancelada
  · Cobrança gerada
  · Cobrança paga
  · Alerta de plantão ao psicólogo
  · Resultado de revisão de perfil financeiro
```

### Google Calendar API

```
Autenticação: OAuth 2.0 (psicólogo autoriza o acesso à sua agenda Google)
Escopo: https://www.googleapis.com/auth/calendar.events

Fluxo de integração:
1. Psicólogo clica em "Conectar Google Calendar" no painel
2. Frontend redireciona para OAuth consent screen do Google
3. Google retorna authorization code
4. Backend troca pelo access token + refresh token
5. Tokens do Google armazenados de forma segura no banco (criptografados)

Sincronização:
  · Slot criado na plataforma → evento criado no Google Calendar (google_event_id salvo)
  · Slot/sessão cancelado → evento deletado no Google Calendar
  · (MVP: sincronização unidirecional plataforma → Google)
```

---

## 9. Infraestrutura de Desenvolvimento

### Docker Compose do projeto

O projeto se conecta ao ambiente Docker existente em `~/workspace/dev-environment` via rede externa `dev-network`.

```yaml
# universo-psicologo/docker-compose.yml
services:
  api:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/dev
      - SPRING_DATASOURCE_USERNAME=dev
      - SPRING_DATASOURCE_PASSWORD=dev123
      - SPRING_DATA_REDIS_HOST=redis
      - MINIO_ENDPOINT=http://minio:9000
      - MINIO_ACCESS_KEY=minioadmin
      - MINIO_SECRET_KEY=minioadmin
      - GEMINI_API_KEY=${GEMINI_API_KEY}
      - RESEND_API_KEY=${RESEND_API_KEY}
      - JWT_SECRET=${JWT_SECRET}
      - CRIPTOGRAFIA_CHAVE=${CRIPTOGRAFIA_CHAVE}
    networks:
      - dev-network

  web:
    build: ./frontend
    ports:
      - "5173:5173"
    networks:
      - dev-network

networks:
  dev-network:
    external: true
```

### Caddy — configuração de roteamento local

```
# caddy/conf.d/unipsi.caddy
unipsi.localhost:8000 {
    handle /api/* {
        reverse_proxy api:8080
    }
    handle {
        reverse_proxy web:5173
    }
}
```

### Variáveis de ambiente (`.env`)

```env
GEMINI_API_KEY=...
RESEND_API_KEY=...
JWT_SECRET=...                    # mínimo 256 bits
CRIPTOGRAFIA_CHAVE=...            # chave AES-256 (32 bytes base64)
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
```

---

## 10. Fluxos Técnicos Críticos

### Fluxo de crise no chatbot

```
Paciente digita mensagem
        │
        ▼
ChatbotController.receberMensagem()
        │
        ▼
ConversacaoStateService.getHistorico(sessionId)  ← Redis
        │
        ▼
GeminiClient.chat(historico + mensagem)  ← Gemini API
        │
        ▼
CriseDetectorService.classificar(resposta)
        │
    ┌───┴───────────────────┐
  NORMAL                  CRISE
    │                       │
    ▼                       ▼
Salva no Redis     PlantaoService.buscarAtivos()
Retorna resposta          │
                          ▼
               Psicólogos com plantão ativo hoje?
                    │               │
                   SIM              NÃO
                    │               │
                    ▼               ▼
          EmailService        Retorna contatos
          .notificarPlantao() emergência (CVV/SAMU)
                    │
                    ▼
          Chatbot retorna dados
          de contato do psicólogo
```

### Fluxo de autenticação e acesso ao prontuário

```
1. Psicólogo faz login → recebe access_token + refresh_token
2. GET /api/prontuario/{codinome}/anotacoes
   → Header: Authorization: Bearer <access_token>
3. JwtAuthFilter.doFilter():
   - Valida assinatura e expiração do token
   - Extrai psicologoId e role do payload
   - Popula SecurityContext
4. ProntuarioController.getAnotacoes(codinome)
   → @PreAuthorize verifica que psicologoId == token.sub
5. ProntuarioService.getAnotacoes(psicologoId, codinome)
   → busca anotações no banco
   → para cada anotação: CriptografiaService.decrypt(conteudo_enc, iv)
   → retorna lista decriptografada
```

---

## 11. Decisões em Aberto

| Decisão | Status | Impacto |
|---|---|---|
| ~~Taxa da plataforma~~ | ✅ 20% por sessão — `TAXA_PLATAFORMA_PERCENTUAL=20` | `CobrancaService` |
| ~~Política de cancelamento~~ | ✅ 8h de antecedência; psicólogo decide em cancelamentos de última hora | Campo `cancelado_em` em `SESSAO` |
| ~~Modalidades de atendimento~~ | ✅ `AVULSA` e `PACOTE_MENSAL` (4 sessões, 5% desconto) | Enum `modalidade_sessao`; campo `modalidade` em `SESSAO` |
| Gateway de pagamento real (pós-MVP) | A definir para pós-MVP | Substituirá simulação em `CobrancaService` |
| Domínio de produção | A definir | Configuração Caddy e CORS |
| Sincronização bidirecional com Google Calendar | A avaliar | `GoogleCalendarService` (webhook do Google) |
| Domínio de produção e provedor de hospedagem | A definir | Configuração Caddy e CORS em produção |
| Política de retenção de logs de auditoria do prontuário | A definir com jurídico | Tabela auditoria_prontuario |
