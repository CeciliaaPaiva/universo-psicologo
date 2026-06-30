# Plano de Sprints — Universo Psicólogo (MVP)

**Versão:** 1.0  
**Data:** 30/06/2026  
**Referência:** Backlog-UniPsi.md, Arquitetura-UniPsi.md  
**Total de histórias:** 27 | **Total de pontos:** 117 | **Sprints:** 6 (Sprint 0 a Sprint 5)  
**Duração sugerida por sprint:** 2 semanas

---

## Definition of Ready (DoR)

Uma história está pronta para entrar em sprint quando:
- Critérios de aceitação definidos e sem ambiguidade
- Dependências de outras histórias resolvidas ou já entregues
- Sem bloqueadores técnicos conhecidos
- Estimativa de pontos atribuída

---

## Definition of Done (DoD)

Uma história está concluída quando:
- Código implementado e todos os critérios de aceitação atendidos
- Testes unitários escritos para a lógica de negócio do módulo
- Migration Flyway criada e aplicada (se houve alteração de banco)
- Endpoint testado manualmente no ambiente local
- PR revisado e mergeado na branch principal
- Release notes atualizadas em `releases/`

---

## Visão Geral

> **Hierarquia de valor:** o Marketplace é o core da plataforma — paciente encontra e agenda com psicólogo. Prontuário e Chatbot são as inovações diferenciais. A ordem dos sprints reflete isso.

| Sprint | Foco | Histórias | Pontos | Release |
|---|---|---|---|---|
| Pré-Sprint | Setup de infraestrutura e repositório | — | — | — |
| Sprint 0 | Fundação — cadastro, autenticação e painel admin | US-001, US-002, US-003, US-026 | 18 | `v0.1.0-fundacao` |
| Sprint 1 | Agenda e plantão — pré-requisitos do marketplace | US-004, US-006, US-007, US-008, US-009 | 22 | `v0.2.0-agenda` |
| Sprint 2 | **Marketplace — CORE da plataforma** | US-005, US-013, US-014, US-015, US-016 | 21 | `v0.3.0-marketplace` |
| Sprint 3 | Prontuário eletrônico — Inovação #1 | US-011, US-012 | 8 | `v0.4.0-prontuario` |
| Sprint 4 | Chatbot de triagem — Inovação #2 | US-018, US-019, US-020, US-010 | 22 | `v0.5.0-chatbot` |
| Sprint 5 | Financeiro e notificações | US-021, US-022, US-023, US-024, US-025 | 16 | `v0.6.0-financeiro` |
| Sprint 6 | Administração, QA e encerramento | US-017, US-027 + QA + LGPD | 10 | `v1.0.0-mvp` |
| **Total** | | **27 histórias** | **117** | |

> **Sprint 3 tem 8 pontos** intencionalmente — prontuário é o módulo mais sensível (LGPD, criptografia) e merece sprint dedicado sem concorrência com outras entregas.

---

## Pré-Sprint — Setup de Infraestrutura

Tarefas técnicas que devem ser concluídas **antes** do Sprint 0 começar. Não são histórias de usuário.

### Ambiente Docker

- [ ] Confirmar que `~/workspace/dev-environment` está rodando: PostgreSQL 17, Redis 7, MinIO e Caddy
- [ ] Criar `caddy/conf.d/unipsi.caddy` para rotear `unipsi.localhost:8000` → `api:8080` e `web:5173`
- [ ] Criar bucket `unipsi` no MinIO via console (`http://minio.localhost:8000`)

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

### Backend — Spring Boot

- [ ] Criar projeto via Spring Initializr com dependências: Spring Web, Spring Security, Spring Data JPA, Spring Data Redis, Flyway, Validation, Lombok
- [ ] Configurar `application.yml`: datasource PostgreSQL, Redis, porta 8080
- [ ] Criar estrutura de pacotes: `auth`, `usuario`, `agenda`, `plantao`, `prontuario`, `marketplace`, `chatbot`, `financeiro`, `notificacao`, `admin`, `config`
- [ ] Criar pasta `src/main/resources/db/migration/` para as migrations Flyway
- [ ] Criar `.env` e `.env.example` com as variáveis obrigatórias:

```env
JWT_SECRET=
CRIPTOGRAFIA_CHAVE=       # AES-256, 32 bytes em base64
GEMINI_API_KEY=
RESEND_API_KEY=
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
```

- [ ] Validar que a aplicação sobe e conecta ao banco e ao Redis

### Frontend — React + Vite

- [ ] Criar projeto: `npm create vite@latest universo-psicologo-web -- --template react`
- [ ] Instalar dependências: `react-router-dom`, `axios`, `@tanstack/react-query`, `react-hook-form`, `zod`, `zustand`, `tailwindcss`, `shadcn-ui`
- [ ] Configurar proxy no `vite.config.js`: `/api` → `http://localhost:8080`
- [ ] Criar estrutura de pastas: `pages/`, `components/`, `hooks/`, `services/`, `store/`, `router/`
- [ ] Criar instância base do Axios em `services/api.js` com interceptor para injetar `Authorization: Bearer <token>`

### Repositório Git

- [ ] Inicializar repositório: `git init`
- [ ] Criar `.gitignore` (ignorar `.env`, `target/`, `node_modules/`, `*.class`, `.idea/`)
- [ ] Commit inicial: `chore: estrutura base do projeto`

---

## Sprint 0 — Fundação

**Período:** Semanas 1–2  
**Objetivo:** Usuários conseguem se cadastrar e fazer login. O administrador consegue visualizar e avaliar cadastros de psicólogos. A plataforma tem identidade e acesso funcionais.  
**Pontos:** 18  
**Release:** `v0.1.0-fundacao`

### Histórias

| ID | História | Pontos | Prioridade |
|---|---|---|---|
| US-001 | Cadastro de Psicólogo | 5 | 🔴 Bloqueante |
| US-002 | Cadastro de Paciente | 3 | 🔴 Bloqueante |
| US-003 | Autenticação (login + refresh) | 5 | 🔴 Bloqueante |
| US-026 | Painel de Avaliação de Cadastros | 5 | 🔴 Bloqueante |

### Entregas técnicas

**Backend:**

| Arquivo | Descrição |
|---|---|
| `V1__create_usuarios.sql` | Tabela `usuario` com enum `role` |
| `V2__create_psicologos.sql` | Tabela `psicologo` com `status_aprovacao` |
| `V3__create_pacientes.sql` | Tabela `paciente` com enum `faixa_renda` |
| `AuthController` | POST `/api/auth/register/psicologo`, `/register/paciente`, `/login`, `/refresh` |
| `JwtService` + `JwtAuthFilter` | Geração e validação de JWT; injeção no SecurityContext |
| `RefreshTokenService` | Armazena/valida refresh token no Redis com TTL de 7 dias |
| `SecurityConfig` | Configuração de CORS, rotas públicas vs. protegidas |
| `MinioService` | Upload de currículo (PDF/DOCX ≤ 5 MB) para bucket `unipsi` |
| `AdminController` | GET `/api/admin/aprovacoes`, PUT `/api/admin/aprovacoes/{id}` |
| `EmailService` + `ResendClient` | E-mails: confirmação de cadastro, resultado de aprovação |
| `PacienteNaoElegivelException` | Lançada quando paciente tenta cadastro com renda acima de Classe D |

**Frontend:**

| Arquivo | Descrição |
|---|---|
| `RegisterPsicologoPage` | Formulário com CRP, especialização, upload de currículo e política de cancelamento |
| `RegisterPacientePage` | Formulário com seleção de faixa de renda (FAIXA_1 a FAIXA_4); bloqueia renda acima de Classe D |
| `LoginPage` | E-mail + senha; redireciona por role após login |
| `authStore.js` | Zustand: token, role, dados do usuário logado |
| `PrivateRoute.jsx` | Redireciona para `/login` se não autenticado; redireciona por role incorreta |
| `AprovacoesPage` | Lista de psicólogos pendentes; ações: Aprovar, Reprovar, Solicitar complementação |

### Critérios de aceite do sprint

- [ ] Psicólogo preenche cadastro, faz upload de currículo e recebe e-mail de confirmação com status `PENDENTE`
- [ ] Admin visualiza lista de cadastros pendentes, aprova e o psicólogo recebe e-mail de aprovação
- [ ] Admin reprova com motivo e o psicólogo recebe e-mail com o motivo
- [ ] Paciente com FAIXA_1 a FAIXA_4 se cadastra normalmente
- [ ] Paciente com renda acima de R$ 3.242,00 (Classe D+) é bloqueado com mensagem orientando atendimento particular
- [ ] Login retorna access token (15 min) + refresh token (7 dias, no Redis)
- [ ] Rotas protegidas redirecionam para `/login` sem token válido
- [ ] Psicólogo com status `PENDENTE` ou `REPROVADO` não acessa funcionalidades operacionais

---

## Sprint 1 — Agenda e Plantão

**Período:** Semanas 3–4  
**Objetivo:** Psicólogo aprovado cadastra seus horários disponíveis e configura plantão. Estes são pré-requisitos diretos do marketplace — sem slots não há o que agendar.  
**Pontos:** 22  
**Release:** `v0.2.0-agenda`

### Histórias

| ID | História | Pontos | Prioridade |
|---|---|---|---|
| US-004 | Edição de Perfil do Psicólogo | 3 | Normal |
| US-006 | Cadastrar Disponibilidade na Agenda | 8 | 🔴 Bloqueante |
| US-007 | Visualizar Agenda | 5 | Normal |
| US-008 | Cancelar Sessão Agendada | 3 | Normal |
| US-009 | Registrar Disponibilidade de Plantão | 3 | 🔴 Bloqueante |

### Entregas técnicas

**Backend:**

| Arquivo | Descrição |
|---|---|
| `V4__create_slots.sql` | Tabela `slot` com `google_event_id` nullable |
| `V5__create_plantao.sql` | Tabela `disponibilidade_plantao` com enum `dia_semana` |
| `AgendaController` | POST/GET `/api/agenda/slots`, DELETE `/api/agenda/slots/{id}` |
| `GoogleCalendarService` | OAuth 2.0; criar/deletar evento no Google Calendar do psicólogo |
| `PlantaoController` | POST `/api/plantao/disponibilidade`, GET `/api/plantao/status`, PATCH `/api/plantao/{id}/ativar` |
| `UsuarioController` | PUT `/api/usuarios/psicologo/perfil` (foto, especialização, link videochamada) |

**Frontend:**

| Arquivo | Descrição |
|---|---|
| `AgendaPage` | Calendário semanal e mensal; criação de slots por data/hora |
| `PlantaoPage` | Seleção de dias de plantão; toggle ativo/inativo com status visível |
| `PerfilPage` (psicólogo) | Edição de foto, especialização, política de cancelamento, link de videochamada |
| Botão OAuth Google | Fluxo de autorização para conectar Google Calendar |

### Critérios de aceite do sprint

- [ ] Psicólogo cria slot; evento aparece no Google Calendar vinculado
- [ ] Slots criados ficam visíveis no marketplace para agendamento (validar na Sprint 2)
- [ ] Cancelamento de sessão libera o slot e atualiza o Google Calendar
- [ ] Psicólogo seleciona dias de plantão; status ativo/inativo visível no painel

---

## Sprint 2 — Marketplace — CORE da Plataforma

**Período:** Semanas 5–6  
**Objetivo:** A plataforma entra em operação. Paciente encontra psicólogos disponíveis para terapia social, visualiza o valor calculado pelo seu perfil e agenda uma sessão. Este é o sprint mais crítico do MVP.  
**Pontos:** 21  
**Release:** `v0.3.0-marketplace`

### Histórias

| ID | História | Pontos | Prioridade |
|---|---|---|---|
| US-005 | Edição de Perfil do Paciente | 2 | Normal |
| US-014 | Buscar Psicólogos Disponíveis | 8 | 🔴 Bloqueante |
| US-015 | Agendar Sessão pelo Marketplace | 5 | 🔴 Bloqueante |
| US-016 | Precificação Dinâmica da Sessão | 3 | 🔴 Bloqueante |
| US-013 | Consultar Histórico de Prontuário (psicólogo) | 3 | Normal |

### Entregas técnicas

**Backend:**

| Arquivo | Descrição |
|---|---|
| `V6__create_sessoes.sql` | Tabela `sessao` com status e valores financeiros |
| `MarketplaceController` | GET `/api/marketplace/psicologos` (com filtros), GET `/api/marketplace/psicologos/{id}` |
| `PrecificacaoService` | Recebe `FaixaRenda` → retorna `BigDecimal`; lança `PacienteNaoElegivelException` fora do escopo |
| `AgendaController` (paciente) | POST `/api/agenda/sessoes` — registra agendamento e marca slot como indisponível |
| `UsuarioController` | PUT `/api/usuarios/paciente/perfil`; histórico de alterações de `faixa_renda` registrado |
| E-mail | Confirmação de agendamento para paciente e psicólogo |

**Tabela de precificação implementada em `PrecificacaoService`:**

| FaixaRenda | Valor retornado |
|---|---|
| FAIXA_1 | R$ 30,00 |
| FAIXA_2 | R$ 45,00 |
| FAIXA_3 | R$ 65,00 |
| FAIXA_4 | R$ 80,00 |
| fora do escopo | `PacienteNaoElegivelException` |

**Frontend:**

| Arquivo | Descrição |
|---|---|
| `MarketplacePage` | Cards de psicólogos com filtros por especialidade e disponibilidade |
| `PsicologoPublicProfile` | Perfil público: especialidades, política de cancelamento, link de videochamada, slots disponíveis |
| `AgendamentosPage` (paciente) | Lista de sessões agendadas com status |
| `PerfilPage` (paciente) | Edição de dados pessoais e faixa de renda |

### Critérios de aceite do sprint

- [ ] **A plataforma funciona:** paciente encontra psicólogo, vê valor da sessão e confirma agendamento
- [ ] Listagem exibe apenas psicólogos aprovados com slots disponíveis
- [ ] Card do psicólogo exibe o valor calculado para o perfil do paciente logado
- [ ] FAIXA_1 → R$30 / FAIXA_2 → R$45 / FAIXA_3 → R$65 / FAIXA_4 → R$80
- [ ] Agendamento confirmado remove o slot da disponibilidade pública
- [ ] E-mail de confirmação enviado para paciente e psicólogo
- [ ] Alteração de faixa de renda não altera valor de sessões já confirmadas

---

## Sprint 3 — Prontuário Eletrônico — Inovação #1

**Período:** Semanas 7–8  
**Objetivo:** Psicólogo registra anotações clínicas seguras com criptografia e codinome. Sprint dedicado e intencionalmente leve (8 pts) por tratar do módulo mais sensível da plataforma — dados de saúde protegidos pela LGPD.  
**Pontos:** 8  
**Release:** `v0.4.0-prontuario`

### Histórias

| ID | História | Pontos | Prioridade |
|---|---|---|---|
| US-011 | Criar Anotação de Prontuário | 5 | 🔴 Bloqueante |
| US-012 | Definir Codinome do Paciente | 3 | 🔴 Bloqueante |

### Entregas técnicas

**Backend:**

| Arquivo | Descrição |
|---|---|
| `V7__create_prontuario_anotacoes.sql` | Tabelas `prontuario` e `anotacao` com campos de criptografia; tabela `auditoria_prontuario` |
| `ProntuarioController` | POST `/api/prontuario/pacientes`, POST/GET `/api/prontuario/{codinome}/anotacoes` |
| `CriptografiaService` | `encrypt(texto)` → `{conteudo_enc, iv}`; `decrypt(conteudo_enc, iv)` → texto (AES-256-GCM) |
| `AuditoriaProntuarioService` | Registra leitura, escrita e edição em `auditoria_prontuario` |

**Frontend:**

| Arquivo | Descrição |
|---|---|
| `ProntuarioPage` | Lista de pacientes por codinome |
| `ProntuarioDetalhePage` | Editor de texto livre; data/hora automáticos; histórico decrescente |

### Critérios de aceite do sprint

- [ ] Criação de prontuário exige codinome único por psicólogo; nome real do paciente nunca aparece na tela
- [ ] Anotação salva com `conteudo_enc` cifrado + `iv` único — verificar diretamente no banco que não está em texto claro
- [ ] Anotação acessível apenas pelo psicólogo autor com sessão JWT ativa
- [ ] Admin, outros psicólogos e o próprio paciente recebem 403 ao tentar acessar
- [ ] Todo acesso (leitura e escrita) registrado em `auditoria_prontuario`
- [ ] Token expirado bloqueia acesso imediatamente

---

## Sprint 4 — Chatbot de Triagem — Inovação #2

**Período:** Semanas 9–10  
**Objetivo:** Chatbot de triagem com IA generativa está no ar — identifica crise, oferece suporte imediato, notifica psicólogo de plantão e encaminha para o marketplace quando não há urgência.  
**Pontos:** 22  
**Release:** `v0.5.0-chatbot`

### Histórias

| ID | História | Pontos | Prioridade |
|---|---|---|---|
| US-018 | Iniciar Conversa de Triagem | 8 | 🔴 Bloqueante |
| US-019 | Identificar Crise e Oferecer Suporte Imediato | 8 | 🔴 Bloqueante |
| US-020 | Encaminhar ao Marketplace após Triagem | 3 | Normal |
| US-010 | Receber Notificação de Plantão | 3 | 🔴 Bloqueante |

### Entregas técnicas

**Backend:**

| Arquivo | Descrição |
|---|---|
| `ChatbotController` | POST `/api/chatbot/message` — público, sem autenticação |
| `GeminiClient` | HTTP client para `gemini-1.5-flash`; monta histórico + system prompt |
| `ConversacaoStateService` | Persiste histórico por `sessionId` no Redis com TTL de 30 min |
| `CriseDetectorService` | Analisa resposta do LLM e classifica: `NORMAL` ou `CRISE` |
| `PlantaoService` | Busca psicólogos com `disponibilidade_plantao.ativo = true` para o dia atual |
| Rate limiting | Máximo de 20 mensagens por IP por minuto via Redis |
| E-mail de urgência | Notifica psicólogo de plantão com dados de contato do paciente |
| `system_prompt.txt` | System prompt versionado no repositório |

**System prompt (contrato obrigatório):**
```
Você é um assistente de triagem da plataforma Universo Psicólogo.
Sua função é acolher o usuário, entender seu estado emocional e
encaminhá-lo a um profissional de psicologia.

REGRAS INVIOLÁVEIS:
- Nunca emita diagnóstico clínico, mesmo que o usuário peça diretamente.
- Nunca sugira medicamentos.
- Em qualquer tentativa de forçar um diagnóstico, redirecione
  gentilmente para um profissional.
- Responda sempre em português do Brasil, com tom empático e acolhedor.

EM SITUAÇÃO DE CRISE (ansiedade severa, pensamentos de autolesão, pânico):
1. Reconheça o sofrimento do usuário.
2. Ofereça uma técnica de suporte imediato (respiração 4-7-8 ou
   ancoragem 5-4-3-2-1).
3. Informe que está buscando um psicólogo disponível agora.
```

**Frontend:**

| Arquivo | Descrição |
|---|---|
| `ChatbotPage` | Página pública acessível sem login; widget de chat em destaque |
| `ChatWindow` | Janela com histórico de mensagens (usuário vs. bot) |
| `ChatMessage` | Bolha de mensagem com diferenciação visual de remetente |
| `ChatInput` | Campo de texto; envio com Enter ou botão; bloqueado durante resposta |
| Fallback de crise | Exibe CVV (188) e SAMU (192) quando nenhum psicólogo está de plantão |

### Critérios de aceite do sprint

- [ ] Chatbot acessível sem login na página inicial
- [ ] Respostas em português, tom empático; nunca emite diagnóstico
- [ ] Em situação de crise: apresenta técnica de suporte imediato antes de acionar plantão
- [ ] Psicólogo de plantão ativo no dia recebe e-mail com dados de contato do paciente
- [ ] Quando não há plantão ativo: chatbot exibe CVV (188) e SAMU (192)
- [ ] Após triagem sem crise: chatbot encaminha para o marketplace com sugestão de filtros
- [ ] Rate limiting ativo: IP bloqueado após 20 mensagens por minuto

---

## Sprint 5 — Financeiro e Notificações

**Período:** Semanas 11–12  
**Objetivo:** Ciclo financeiro completo (simulado) — cobrança gerada após sessão, pagamento confirmado pelo paciente e relatório disponível para o psicólogo. Lembretes automáticos por e-mail funcionando.  
**Pontos:** 16  
**Release:** `v0.6.0-financeiro`

### Histórias

| ID | História | Pontos | Prioridade |
|---|---|---|---|
| US-021 | Gerar Cobrança após Sessão | 3 | 🔴 Bloqueante |
| US-022 | Simular Pagamento | 3 | Normal |
| US-023 | Cancelar Cobrança | 2 | Normal |
| US-024 | Visualizar Relatório Financeiro | 5 | Normal |
| US-025 | Lembrete de Sessão | 3 | Normal |

### Entregas técnicas

**Backend:**

| Arquivo | Descrição |
|---|---|
| `V8__create_cobrancas.sql` | Tabela `cobranca` com status e timestamps |
| `CobrancaService` | Gera cobrança ao marcar sessão como `REALIZADA`; aplica taxa da plataforma (percentual configurável via env); calcula valor líquido |
| `FinanceiroController` | GET `/api/financeiro/cobrancas` (paciente), POST `/api/financeiro/cobrancas/{id}/pagar`, GET `/api/financeiro/relatorio?inicio=&fim=` (psicólogo) |
| `LembreteSessaoScheduler` | `@Scheduled` — verifica sessões nas próximas 24h e 1h; dispara e-mail para paciente e psicólogo |
| Templates de e-mail | Cobrança gerada, cobrança paga, lembrete 24h, lembrete 1h |

> `TAXA_PLATAFORMA_PERCENTUAL` deve ser uma variável de ambiente com valor placeholder até a decisão formal.

**Frontend:**

| Arquivo | Descrição |
|---|---|
| `CobrancasPage` (paciente) | Lista de cobranças com status e botão "Confirmar pagamento" (simulado) |
| `FinanceiroPage` (psicólogo) | Filtro de período; lista de sessões; totais: bruto, taxa, líquido |

### Critérios de aceite do sprint

- [ ] Ao marcar sessão como `REALIZADA`, cobrança é gerada automaticamente com status `PENDENTE`
- [ ] Paciente visualiza cobranças pendentes e confirma pagamento simulado; status muda para `PAGO`
- [ ] Psicólogo é notificado por e-mail com o valor líquido após pagamento
- [ ] Cancelamento de sessão cancela a cobrança automaticamente; paciente é notificado
- [ ] Relatório financeiro filtra por período e exibe bruto, taxa e líquido consolidados
- [ ] Apenas sessões com status `PAGO` são contabilizadas nos totais do relatório
- [ ] Lembretes enviados 24h e 1h antes para paciente e psicólogo; não disparados para sessões canceladas

---

## Sprint 6 — Administração e Encerramento do MVP

**Período:** Semanas 13–14  
**Objetivo:** Fluxo de revisão de perfil financeiro completo; QA geral do MVP; validação de conformidade LGPD/CFP; entrega da versão `v1.0.0-mvp`.  
**Pontos:** 10 (histórias) + tarefas técnicas de fechamento  
**Release:** `v1.0.0-mvp`

### Histórias

| ID | História | Pontos | Prioridade |
|---|---|---|---|
| US-017 | Solicitar Revisão de Perfil Financeiro (Psicólogo) | 5 | Normal |
| US-027 | Gerenciar Solicitações de Revisão (Admin) | 5 | Normal |

### Entregas técnicas

**Backend:**

| Arquivo | Descrição |
|---|---|
| `V9__create_revisao_perfil.sql` | Tabela `revisao_perfil` com status e decisão do admin |
| `RevisaoPerfilController` | POST `/api/marketplace/revisao-perfil`, GET `/api/admin/revisoes`, PUT `/api/admin/revisoes/{id}` |
| E-mails | Notificação ao admin (revisão aberta); ao psicólogo e paciente (resultado da decisão) |

**Frontend:**

| Arquivo | Descrição |
|---|---|
| Botão no painel do psicólogo | "Solicitar revisão de perfil" com campo de justificativa |
| `RevisoesPerfilPage` (admin) | Lista de solicitações abertas com ação de manter ou atualizar faixa |

### Tarefas técnicas de fechamento

| Tarefa | Critério de conclusão |
|---|---|
| Teste do fluxo completo (happy path) | Cadastro → aprovação → agenda → agendamento → pagamento → relatório |
| Teste do fluxo de crise | Chatbot → crise → plantão → e-mail de urgência → CVV quando sem plantão |
| Auditoria de acesso ao prontuário | Confirmar que cada leitura e escrita gera registro em `auditoria_prontuario` |
| Teste de isolamento do prontuário | Admin, outro psicólogo e paciente não conseguem acessar prontuário alheio |
| Validação da criptografia | Dado salvo no banco está cifrado; `CriptografiaService` decifra corretamente |
| Revisão de CORS | Apenas a origem do frontend é aceita; testar com origem diferente |
| Revisão de rate limiting | Endpoint `/api/chatbot/message` bloqueia IPs abusivos |
| Criar `.env.example` | Todas as variáveis documentadas com descrição e exemplo de formato |
| Documentar release `v1.0.0-mvp` | Changelog completo em `releases/v1.0.0-mvp.md` |

### Critérios de aceite do sprint

- [ ] Psicólogo solicita revisão com justificativa; admin recebe notificação por e-mail
- [ ] Admin decide (manter ou atualizar faixa); psicólogo e paciente são notificados
- [ ] Atendimentos não são suspensos durante revisão em andamento
- [ ] Nenhum usuário (incluindo admin) consegue acessar prontuário de outro psicólogo
- [ ] Fluxo completo do MVP testado e sem regressões
- [ ] `.env.example` criado e documentado no repositório

---

## Mapa de Dependências

```
US-001 (Cadastro Psicólogo)
  └── US-026 (Admin Aprova)
        └── US-006 (Agenda — só aprovados) ──► US-007, US-008
              └── US-014 (Marketplace) ──► US-015 ──► US-016
                                                 └── US-021 ──► US-022 ──► US-024
                                                 └── US-025 (Lembretes)

US-002 (Cadastro Paciente)
  └── US-014 (Marketplace)

US-003 (Auth)
  ├── US-006 (Agenda)
  ├── US-009 (Plantão) ──► US-010 ◄── US-019 (Crise chatbot)
  └── US-011 (Prontuário) ──► US-012 ──► US-013

US-018 (Chatbot) ──► US-019 ──► US-020
US-021 (Cobrança) ──► US-023 (Cancelar cobrança) ◄── US-008 (Cancelar sessão)
US-017 (Solicitar revisão) ──► US-027 (Admin decide)
```

---

## Velocidade e Distribuição de Pontos

| Sprint | Foco | Pontos | Risco |
|---|---|---|---|
| Sprint 0 | Fundação | 18 | Baixo |
| Sprint 1 | Agenda e Plantão | 22 | Baixo |
| Sprint 2 | **Marketplace — CORE** | 21 | Médio |
| Sprint 3 | Prontuário — Inovação #1 | 8 | Baixo (sprint dedicado) |
| Sprint 4 | Chatbot — Inovação #2 | 22 | Médio |
| Sprint 5 | Financeiro e Notificações | 16 | Baixo |
| Sprint 6 | Admin, QA e Encerramento | 10 | Baixo |
| **Total** | | **117** | |

> A reestruturação eliminou o Sprint 1 sobrecarregado (30 pts → 22 pts) e garante que a plataforma já funciona ao final do Sprint 2 — antes de qualquer inovação.
