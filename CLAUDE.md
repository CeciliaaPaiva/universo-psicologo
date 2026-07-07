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

### Serviços externos
- **Google Gemini API** (`gemini-1.5-flash`) — chatbot de triagem; tier gratuito: 1.500 req/dia
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
- Se nenhum psicólogo estiver de plantão ativo no dia, informa o paciente e exibe contatos de emergência: CVV (188) e SAMU (192).

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

### Modalidades de atendimento
- **Avulsa:** sessão única; paciente agenda e paga por sessão.
- **Pacote mensal:** compromisso de 4 sessões/mês; cobrança única gerada ao confirmar o pacote; 5% de desconto sobre 4 avulsas.
- A modalidade é selecionada pelo paciente no momento do agendamento e registrada no campo `modalidade` da entidade `SESSAO` (enum `AVULSA` / `PACOTE_MENSAL`).
- Pacotes não são reembolsáveis — cancelamento de sessão individual dentro do pacote segue a política de cancelamento.

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
- Simulado no MVP (sem gateway real). `CobrancaService` gerencia os status: `PENDENTE → PAGO → CANCELADO`.

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
