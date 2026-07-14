# CLAUDE.md — Universo Psicólogo

Contexto persistente do projeto para o Claude Code. Leia este arquivo no início de cada sessão antes de qualquer implementação.

---

## O que é este projeto

Plataforma digital de saúde mental que conecta psicólogos comprometidos com terapia social a pacientes em situação de vulnerabilidade socioeconômica. Oferece ferramentas de gestão para o psicólogo (agenda, prontuário, financeiro) e acesso facilitado para o paciente (marketplace, chatbot de triagem).

Modelo de negócio: B2B2C — SaaS para psicólogos + marketplace social para pacientes.

---

## Documentação do projeto

Todos os documentos ficam em `/home/cecilia_paiva/universo-psicologo/`. Leia-os antes de implementar qualquer módulo novo.

| Arquivo | Conteúdo |
|---|---|
| `Doc-Visão-UniPsi.md` | Visão, problema, impacto social, escopo e stakeholders |
| `Doc-Requisitos-UniPsi.md` | 35 RF, 8 RNF e 11 casos de uso detalhados |
| `Backlog-UniPsi.md` | 27 histórias de usuário em 10 épicos, com critérios de aceitação e sugestão de sprints |
| `Arquitetura-UniPsi.md` | Stack, estrutura de pacotes, modelo de dados, fluxos técnicos críticos |
| `ER-UniPsi.md` | Diagrama Entidade-Relacionamento completo em Mermaid |
| `Sprints-UniPsi.md` | Plano de sprints do MVP — objetivos, entregas técnicas e critérios de aceite por sprint |
| `Testes-UniPsi.md` | Plano de cobertura de testes — estratégia, ferramentas, casos por módulo e thresholds de CI |
| `Debitos-UniPsi.md` | Índice vivo de débitos técnicos abertos e resolvidos — ver regra de atualização em "Convenção de releases" |
| `releases/` | Histórico de releases — um arquivo `.md` por versão entregue |
| `atas/` | Atas de reunião com o stakeholder — um arquivo `.md` por reunião, formatado para compartilhar com o time |

---

## Stack

### Backend
- **Java 21** com **Spring Boot 3.x**
- **Spring Security** + JWT (access token 15 min / refresh token 7 dias no Redis)
- **Spring Data JPA** + Hibernate — banco PostgreSQL 17
- **Flyway** — migrações de banco versionadas em `src/main/resources/db/migration/`
- **Spring Data Redis** — refresh tokens, estado do chatbot (TTL 30 min), rate limiting
- **MinIO Java SDK** — upload de currículos e fotos (bucket `unipsi`)
- **Maven** — build

### Frontend
- **React 18** + **Vite**
- **React Router v6** — rotas protegidas por role
- **Tailwind CSS** + **shadcn/ui** (Radix UI)
- **Axios** + **TanStack Query (React Query)** — requisições à API
- **React Hook Form** + **Zod** — formulários e validação
- **Zustand** — estado global de autenticação

### Infraestrutura (ambiente Docker existente em `~/workspace/dev-environment`)
- **PostgreSQL 17** — `localhost:5432`, user=`dev`, pass=`dev123`, db=`dev`
- **Redis 7** — `localhost:6379`, sem senha
- **MinIO** — API S3 em `http://s3.localhost:8000`, creds `minioadmin/minioadmin`
- **Caddy** — proxy reverso na porta 8000; adicionar `caddy/conf.d/unipsi.caddy` para expor o app

### Acesso ao ambiente de dev — sempre via domínio, nunca `localhost` direto

O Caddy do host já expõe o projeto em `http://unipsi-web.claudinha.local` (frontend) e
`http://unipsi-api.claudinha.local` (backend), provisionados via skill `dev-env`. **Sempre testar
e demonstrar por esses domínios**, não por `localhost:8100`/`localhost:8101` — é o que
`FRONTEND_ORIGIN`, `GOOGLE_REDIRECT_URI` e o restante do projeto assumem como ambiente real.

- Backend: `cd api && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8101`
- Frontend: **precisa de `--host 0.0.0.0`** — o Vite por padrão só escuta em `::1` (IPv6), e o
  `reverse_proxy` do Caddy aponta para `127.0.0.1` (IPv4); sem isso o Caddy responde 502 mesmo com
  o Vite rodando normalmente.
  ```bash
  cd web && VITE_API_PROXY_TARGET=http://localhost:8101 npm run dev -- --port 8100 --host 0.0.0.0
  ```
- `unipsi.frontend-origin` (env `FRONTEND_ORIGIN`) aceita lista separada por vírgula — mantenha
  `http://unipsi-web.claudinha.local` nela sempre; outras origens (ex.: `localhost`) podem ser
  adicionadas para depuração pontual, mas não removam a do Caddy.

### Serviços externos
- **Google Gemini API** (`gemini-flash-latest` — alias sempre válido para o modelo flash estável atual; `gemini-1.5-flash`/`gemini-2.0-flash` foram descontinuados pelo Google em 2026) — chatbot de triagem; tier gratuito
- **Resend API** — envio de e-mails; tier gratuito: 100 e-mails/dia
- **Google Calendar API** — sincronização de agenda via OAuth 2.0

---

## Arquitetura

Monolito modular. Um processo Spring Boot, módulos separados por domínio.

```
br.com.unipsi.
├── auth/         JWT, login, refresh token
├── usuario/      Entidades Usuario, Psicologo, Paciente; roles
├── agenda/       Slots, sessões, integração Google Calendar
├── plantao/      Disponibilidade de plantão; notificação de urgência
├── prontuario/   Anotações criptografadas (AES-256-GCM); codinome obrigatório
├── marketplace/  Busca de psicólogos; precificação dinâmica; revisão de perfil
├── chatbot/      Triagem via Gemini; detector de crise; estado no Redis
├── financeiro/   Cobranças (simuladas no MVP); relatório financeiro
├── notificacao/  EmailService via Resend; templates HTML
├── admin/        Aprovação de psicólogos; revisão de perfil financeiro
└── config/       SecurityConfig, CorsConfig, RedisConfig, MinioConfig, GeminiConfig
```

Cada módulo segue a estrutura: `controller/` → `service/` → `repository/` → `domain/` → `dto/`

---

## Hierarquia de valor do produto

Essencial para decisões de priorização, ordem de desenvolvimento e cortes de escopo.

| Camada | O que é | Por quê vem primeiro |
|---|---|---|
| **Core — o produto funciona aqui** | Marketplace/catálogo de psicólogos disponíveis para terapia social | Sem isso não há plataforma. Paciente precisa encontrar e agendar com um psicólogo. |
| **Inovação #1** | Prontuário eletrônico com criptografia e codinome | Diferencial técnico e legal. Não impede o marketplace de funcionar, mas eleva o valor para o psicólogo. |
| **Inovação #2** | Chatbot de triagem com detecção de crise | Diferencial de impacto social. Aumenta acesso e segurança, mas paciente pode agendar sem o chatbot. |

**Regra de priorização:** em caso de conflito de escopo ou atraso, preservar o marketplace. Prontuário e chatbot podem ser entregues em sprints subsequentes sem quebrar o produto.

---

## Regras de negócio críticas

### Prontuário eletrônico
- Anotações são criptografadas com **AES-256-GCM** antes de persistir. A chave fica em variável de ambiente (`CRIPTOGRAFIA_CHAVE`), nunca no banco.
- O paciente é identificado por **codinome** — o nome real jamais aparece nas telas de prontuário.
- Acesso restrito ao psicólogo autor, verificado no serviço com `@PreAuthorize`. Nenhum outro perfil, incluindo ADMIN, acessa prontuário.

### Chatbot
- Nunca emite diagnóstico clínico. O system prompt deve impor essa restrição explicitamente.
- Em situações de crise, primeiro oferece técnicas de suporte imediato (respiração, ancoragem sensorial), depois aciona o plantão.
- Se nenhum psicólogo estiver de plantão ativo no dia, informa o paciente e exibe contatos de emergência: **CVV** — chat em [https://cvv.org.br/chat/](https://cvv.org.br/chat/) e ligação via `tel:188` — e **SAMU** (192). *(Ajuste da reunião de 07/07/2026 — ver `atas/2026-07-07-alinhamento-sprint-4.md`; antes só exibia os números.)*
- **Busca ampliada de profissional (ajuste 07/07/2026, implementado em v0.5.1):** quando o paciente deixa um contato de retorno em situação de crise, o sistema procura tanto psicólogos **de plantão no dia** (`PlantaoService.buscarPsicologosDePlantaoHoje`) quanto, se ninguém estiver de plantão, o psicólogo aprovado com a **próxima disponibilidade mais próxima na agenda** (`AgendaService.buscarPsicologoComProximaDisponibilidade`). CVV/SAMU só aparecem se nenhum dos dois encontrar alguém.
- **Contato obrigatório no chatbot (ajuste 14/07/2026, implementado em v0.6.0):** o campo de contato deixou de ser opcional — o input de mensagem fica bloqueado no frontend até o paciente preencher telefone ou e-mail, e o backend valida com `@NotBlank` em `ChatMessageRequest.contato`. Motivo: sem contato, o psicólogo acionado numa crise não tem como retornar ao paciente.
- **Retry ao Gemini (v0.6.0):** `GeminiClient` tenta até 3 vezes (backoff 500ms/1000ms) antes de cair no texto fixo de fallback — reduz a frequência de respostas idênticas quando a Gemini API (tier gratuito) retorna `503` por sobrecarga. A repetição ainda pode ocorrer ocasionalmente (ver DT-22 em `docs/Debitos-UniPsi.md`).

### Terapia social e precificação
- A plataforma atende **exclusivamente** pacientes de baixa renda (até Classe D). Pacientes acima desse limite são inelegíveis.
- Métrica: **renda domiciliar per capita** (SM 2026 = R$ 1.621,00), alinhada com critérios oficiais do governo federal.

**Sessão avulsa** — valor por sessão única:

| Enum `FaixaRenda` | Referência | Renda per capita/mês | Valor avulso | Taxa 20% | Psicólogo recebe |
|---|---|---|---|---|---|
| `FAIXA_1` | BPC/LOAS | Até R$ 405,25 (¼ SM) | R$ 60,00 | R$ 12,00 | R$ 48,00 |
| `FAIXA_2` | CadÚnico / Bolsa Família | R$ 405,26 – R$ 810,50 (½ SM) | R$ 65,00 | R$ 13,00 | R$ 52,00 |
| `FAIXA_3` | Classe E | R$ 810,51 – R$ 1.621,00 (1 SM) | R$ 70,00 | R$ 14,00 | R$ 56,00 |
| `FAIXA_4` | Classe D | R$ 1.621,01 – R$ 3.242,00 (2 SM) | R$ 75,00 | R$ 15,00 | R$ 60,00 |

**Pacote mensal** — 4 sessões com 5% de desconto sobre o total avulso:

| Enum `FaixaRenda` | Total/mês | Por sessão |
|---|---|---|
| `FAIXA_1` | R$ 228,00 | R$ 57,00 |
| `FAIXA_2` | R$ 247,00 | R$ 61,75 |
| `FAIXA_3` | R$ 266,00 | R$ 66,50 |
| `FAIXA_4` | R$ 285,00 | R$ 71,25 |

- Cálculo centralizado em `PrecificacaoService`. Para renda fora do escopo, lançar `PacienteNaoElegivelException`.
- Taxa da plataforma: **20% por sessão** sobre o valor pago pelo paciente (avulsa ou per-sessão no pacote). Variável de ambiente: `TAXA_PLATAFORMA_PERCENTUAL=20`.
- Pacote: a cobrança mensal é gerada no ato do agendamento e cobre as 4 sessões do mês.
- **Exibição do pacote mensal (ajuste 07/07/2026, implementado em v0.5.1):** em `/agendamentos`, `SessaoResponse` mostra `valorSessaoAvulsa` + `valorPacoteTotal` + `economiaPacote`, destacando quanto o paciente economiza no pacote.
- **Terapia de casal (ajuste 07/07/2026, implementado em v0.5.1):** custa o **dobro** do valor da sessão individual/convencional na mesma faixa e modalidade (ex.: `FAIXA_1` avulsa individual R$ 60,00 → casal R$ 120,00). `PrecificacaoService.calcularValorSessao` recebe `FaixaRenda` + `Modalidade` + `TipoAtendimento` (`INDIVIDUAL`/`CASAL`, enum novo em `agenda.domain`). Valores exatos ainda dependem de confirmação formal do Victor — a regra "dobro" já estava definida na ata e foi o que se implementou.

### Marketplace — Áreas de atuação *(ajuste 07/07/2026, implementado em v0.5.1)*
- O campo de busca "Especialidade" virou dois conceitos distintos: **especialização** (`Psicologo.especializacao`) continua sendo a formação/abordagem do psicólogo, exibida no card; o campo de busca agora é **"Áreas de atuação"** (`Psicologo.areasAtuacao`, `@ElementCollection` de tags — ex.: ansiedade, luto, terapia de casal), exibido como tags abaixo do nome+especialização. `GET /api/marketplace/psicologos` filtra por `areaAtuacao` em vez de `especialidade`.
- Psicólogos aprovados antes desta mudança ficam com `areasAtuacao` vazio até se atualizarem em `/perfil-psicologo` — não há backfill automático (ver DT-19 em `docs/Debitos-UniPsi.md`).

### Modalidades de atendimento
- **Avulsa:** sessão única; paciente agenda e paga por sessão.
- **Pacote mensal:** compromisso de 4 sessões/mês; cobrança única gerada ao confirmar o pacote; 5% de desconto sobre 4 avulsas.
- A modalidade é selecionada pelo paciente no momento do agendamento e registrada no campo `modalidade` da entidade `SESSAO` (enum `AVULSA` / `PACOTE_MENSAL`).
- Pacotes não são reembolsáveis — cancelamento de sessão individual dentro do pacote segue a política de cancelamento.

### Perfil do paciente e anamnese *(ajuste 07/07/2026 — preenchimento implementado em v0.5.1; acesso temporário do psicólogo ainda não implementado, ver DT-16)*
- **Faixa de renda deixou de ser editável pelo paciente** (implementado). É autodeclarada uma única vez no cadastro (`RegisterPacientePage`); depois disso, só o psicólogo pode reavaliar a situação financeira ao longo do acompanhamento, via "Solicitar Revisão de Perfil Financeiro" (US-017/US-027). `PUT /api/usuarios/paciente/perfil` não aceita mais `faixaRenda`.
- Paciente pode adicionar/editar **foto de perfil** (implementado, multipart, reaproveita `MinioService`).
- Cadastro/perfil do paciente tem **idade** (implementado, `Paciente.idade`).
- **Anamnese básica (implementado em v0.5.1 — só o preenchimento pelo paciente):** já fez terapia antes, motivo de buscar terapia agora, se toma medicação controlada, contato do responsável (condicional a menor de idade). Cifrada com `CriptografiaService` (AES-256-GCM), módulo `usuario.domain.Anamnese`. **O acesso temporário do psicólogo (janela entre pagamento da 1ª sessão e sua realização) ainda não foi implementado** — depende de `Cobranca` (agora existe, desde v0.6.0), corretamente alocado para a Sprint 5.5. Ver DT-16.
  - **A anamnese é sempre do paciente — nunca pública, nunca permanente para o psicólogo.** Diferente do prontuário (acesso permanente ao autor), o acesso do psicólogo à anamnese é **temporário**: só existe na janela entre a **primeira sessão com aquele paciente ser agendada e paga** e essa sessão **ser realizada**. Depois disso, o acesso é revogado — o psicólogo já deve ter registrado o que precisa nas próprias anotações de prontuário.
  - Se o paciente agendar depois com outro psicólogo, esse novo profissional ganha sua própria janela de acesso (primeira sessão *com ele*), independente — não é um acesso permanente nem compartilhado entre profissionais.
  - Segue o mesmo padrão de proteção do prontuário: conteúdo cifrado com `CriptografiaService` (AES-256-GCM) e toda leitura registrada em auditoria (mesmo espírito de `AuditoriaProntuarioService`) — a diferença é o controle de acesso ser por **janela de tempo/status da sessão**, não por autoria fixa.
  - Aviso obrigatório em `/perfil-paciente`, copy sugerida pelo stakeholder: *"Preencha seu perfil com sua anamnese. Essa informação não será pública — o profissional só tem acesso quando você agendar e efetuar o pagamento, antes da primeira terapia. Depois disso, ele não terá mais acesso."*
- **Paciente menor de idade (refinado 07/07/2026):** o campo de contato do responsável só aparece **condicionalmente**, quando o paciente se identifica como menor de idade — não é um campo genérico exibido pra todo mundo. Deve vir acompanhado de uma explicação de por que é pedido (prática clínica: atendimento a menores exige presença/consentimento do responsável, especialmente na primeira sessão). A primeira sessão de um paciente menor deve ser com o responsável.

### Política de cancelamento
- **Prazo livre:** cancelamento permitido até **8 horas antes** do horário agendado, sem custo.
- **Cancelamento de última hora:** menos de 8h de antecedência — o psicólogo avalia o motivo e decide entre:
  - Cobrar a sessão normalmente, ou
  - Realocar o atendimento para outra data/horário.
- A decisão é exclusivamente entre psicólogo e paciente; a plataforma registra o cancelamento mas não impõe penalidade automática.
- O fluxo é **idêntico para avulsa e pacote mensal**.

### Aprovação de psicólogos
- Cadastro criado com status `PENDENTE_APROVACAO`. O ADMIN avalia currículo, CRP e política de cancelamento informada pelo psicólogo.
- Psicólogo com status `PENDENTE_APROVACAO` ou `REPROVADO` não acessa funcionalidades operacionais.

### Pagamento
- Simulado no MVP (sem gateway real). Implementado em v0.6.0 (Sprint 5): `CobrancaService` gerencia os status `PENDENTE → PAGO → CANCELADO`. Cobrança é gerada automaticamente quando o psicólogo marca a sessão como `REALIZADA` (`POST /api/agenda/sessoes/{id}/realizar`), copiando os valores já calculados em `Sessao` (`valorSessao`/`taxaPlataforma`/`valorLiquido`) — não recalcula. `LembreteSessaoScheduler` dispara e-mail + notificação in-app 24h e 1h antes da sessão.

### Mensagens internas *(nova feature, ajuste 07/07/2026 — ainda não implementada)*
- Chat interno entre psicólogo e paciente, na própria plataforma.
- **Liberado somente depois que a sessão está agendada E paga** (`Sessao` com `Cobranca.status = PAGO`) — antes disso não há canal de mensagens entre as partes.
- `Cobranca`/status de pagamento já existe desde v0.6.0 (Sprint 5) — a dependência que bloqueava esta história está resolvida; segue alocada para a Sprint 5.5.

---

## Segurança (não negociável)

- **LGPD**: dados de prontuário e perfil socioeconômico são dados sensíveis. Sempre criptografar em repouso.
- **TLS**: Caddy cuida do HTTPS. Nunca expor a API sem proxy.
- **Senhas**: BCrypt com fator 12.
- **IDs**: sempre UUID, nunca inteiro sequencial.
- **CORS**: configurado para aceitar apenas a origem do frontend.
- **Rate limiting**: endpoint `/api/chatbot/message` limitado via Redis para evitar abuso.
- **Logs de auditoria**: todo acesso e escrita em prontuário deve ser registrado na tabela `auditoria_prontuario`.

---

## Variáveis de ambiente obrigatórias

```env
GEMINI_API_KEY=
RESEND_API_KEY=
JWT_SECRET=                      # mínimo 256 bits
CRIPTOGRAFIA_CHAVE=              # chave AES-256 (32 bytes, base64)
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
TAXA_PLATAFORMA_PERCENTUAL=20    # 20% sobre o valor pago pelo paciente por sessão
```

---

## Convenção de releases

A cada entrega implementada, criar um arquivo em `releases/` seguindo o padrão:

```
releases/
├── v0.1.0-cadastro-auth.md
├── v0.2.0-agenda.md
├── v0.3.0-prontuario.md
└── ...
```

**Formato do arquivo de release** (ver template em `releases/TEMPLATE.md`):
- Versão e data
- Histórias entregues (ID + título)
- Endpoints adicionados ou alterados
- Migrações de banco executadas
- Variáveis de ambiente novas
- Pontos de atenção / débitos técnicos

**Regra obrigatória — `docs/Debitos-UniPsi.md`:** toda vez que a seção "Débitos técnicos" de uma
release ganhar um item novo, ou um débito de release anterior for corrigido, atualizar também
`docs/Debitos-UniPsi.md` na mesma sessão: adicionar a linha em "Débitos abertos" (ou mover de
"Débitos abertos" para "Débitos resolvidos", preenchendo Resolvido em / Como). O arquivo de release
é o registro histórico ponto-no-tempo; `Debitos-UniPsi.md` é o agregado sempre atualizado — nunca
deixar um débito existir só em um dos dois.

---

## Convenção de commits

```
feat(modulo): descrição curta
fix(modulo): descrição curta
refactor(modulo): descrição curta
docs: descrição curta
test(modulo): descrição curta
```

Exemplos:
```
feat(auth): implementa login e geração de JWT
feat(prontuario): adiciona criptografia AES-256 nas anotações
fix(chatbot): corrige detecção de crise em mensagens curtas
```

---

## Itens ainda a definir (não implementar sem decisão)

| Item | Impacto no código |
|---|---|
| Gateway de pagamento real | Substitui simulação em `CobrancaService` (pós-MVP) |
| Domínio de produção | Configuração Caddy e CORS |
| Valor exato da terapia de casal (tarefa do Victor) | Regra já definida como "dobro do individual" — falta só confirmar se o dobro se aplica igual em todas as faixas/modalidades. Ver `atas/2026-07-07-alinhamento-sprint-4.md` |
| Perguntas do formulário de anamnese básica (tarefa do Victor) | Já implementado um subconjunto inicial (já fez terapia, motivo da busca, medicação controlada); lista final ainda não confirmada — `Anamnese` guarda um blob JSON cifrado, então novas perguntas não exigem migração |
