# Débitos Técnicos — Universo Psicólogo

**Última atualização:** 07/07/2026 (Sprint 3 — `v0.4.0-prontuario`)

Índice vivo de atalhos, lacunas e decisões técnicas conscientes tomadas durante o desenvolvimento.
Cada release já registra seus próprios débitos na seção "Débitos técnicos" do arquivo em
[`releases/`](../releases) — isso continua sendo o registro histórico, ponto-no-tempo, do que se
sabia naquele momento. Este arquivo é o **agregado atualizado**: mostra o que está aberto *agora*,
independente de em qual release foi introduzido, e para onde foi quando resolvido.

> **Regra:** todo débito técnico novo (ou resolvido) entra ou sai daqui na mesma sessão em que a
> release correspondente é fechada. Ver `CLAUDE.md` → "Convenção de releases".

---

## Débitos abertos

| ID | Módulo | Descrição | Introduzido em |
|---|---|---|---|
| DT-01 | Plataforma (segurança) | Requisição sem token de autenticação retorna **403**, não 401. `SecurityConfig` não tem `AuthenticationEntryPoint` customizado — o `AnonymousAuthenticationFilter` do Spring Security autentica a requisição como anônima antes do `@PreAuthorize` barrar por falta de role, então "sem token" e "sem permissão" ficam indistinguíveis por status HTTP. Afeta todos os módulos protegidos (confirmado em `agenda`, `plantao`, `prontuario`). | v0.1.0 (comportamento existe desde então; identificado explicitamente em v0.4.0) |
| DT-02 | auth / frontend | Refresh token não é renovado automaticamente pelo Axios antes do access token expirar (endpoint `/api/auth/refresh` existe, mas não há interceptor de renovação silenciosa). | v0.1.0 |
| DT-03 | admin / frontend | Psicólogo com status `PENDENTE_APROVACAO`/`REPROVADO` é bloqueado no login (403), mas não há tela dedicada explicando o status — só a mensagem de erro genérica. | v0.1.0 |
| DT-04 | agenda / frontend | "Visualização semanal e mensal" (US-007) foi entregue como lista agrupada por dia — não há grade visual de calendário. | v0.2.0 |
| DT-05 | agenda | Integração real com Google Calendar nunca testada ponta a ponta com `GOOGLE_CLIENT_ID`/`SECRET` reais — só o caminho de degradação graciosa (sem credenciais) foi validado. | v0.2.0 |
| DT-06 | agenda / marketplace | Concorrência no agendamento: `SessaoService.agendar` faz check-then-act simples (sem lock pessimista). A constraint `UNIQUE` em `sessao.slot_id` evita double-booking na gravação, mas a violação cai no handler 500 genérico do Spring, sem tradução para um erro amigável. | v0.3.0 |
| DT-07 | marketplace | Filtro de especialidade em `GET /api/marketplace/psicologos` é um `contains` simples em memória, sem paginação — ok na escala atual (dezenas de psicólogos). | v0.3.0 |
| DT-08 | prontuario | `Prontuario.paciente_id` (nullable) não é vinculado a nenhum paciente real do marketplace — o psicólogo cria o codinome diretamente, sem partir de uma sessão existente. | v0.4.0 |
| DT-09 | prontuario | Enum `AcaoAuditoria.EDICAO` existe (par com o modelo ER) mas nenhum fluxo o dispara ainda — não há endpoint de editar/apagar anotação (fora do escopo de US-011/012/013). | v0.4.0 |

---

## Débitos resolvidos

| ID | Módulo | Descrição | Introduzido em | Resolvido em | Como |
|---|---|---|---|---|---|
| DT-R01 | agenda | Cancelamento de slot não notificava o paciente por e-mail — dependia do vínculo slot → sessão → paciente, que só passou a existir com o marketplace. | v0.2.0 (débito de US-008) | v0.3.0 | `AgendaService.cancelar` passou a localizar a `Sessao` `AGENDADA` vinculada ao slot, marcá-la `CANCELADA` e enviar e-mail ao paciente; o slot deixou de ser deletado quando há sessão vinculada (evita violar a FK). |
| DT-R02 | marketplace / prontuário | US-013 (Consultar Histórico de Prontuário) ficou fora do escopo da Sprint 2 por depender de um módulo de prontuário que ainda não existia. | v0.3.0 | v0.4.0 | Módulo `prontuario` implementado na Sprint 3; `GET /api/prontuario/{codinome}/anotacoes` cobre integralmente a história (histórico decrescente + busca por palavra-chave). |

---

## Fora do escopo deste documento

Decisões de produto ainda não tomadas (não são atalhos técnicos, são pendências de negócio) ficam
na tabela "Itens ainda a definir" do [`CLAUDE.md`](../CLAUDE.md) — ex.: gateway de pagamento real,
domínio de produção.
