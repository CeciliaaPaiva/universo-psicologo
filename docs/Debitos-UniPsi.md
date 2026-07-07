# Débitos Técnicos — Universo Psicólogo

**Última atualização:** 07/07/2026 (QA visual pré-demo, pós-Sprint 4)

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
| DT-10 | plataforma (testes) | Bugs de *lazy loading* do Hibernate fora do escopo de uma transação (ex.: entidade carregada por um service `@Transactional` e lida por outro fora dela) só aparecem em verificação manual contra o banco real — não há testes de integração (H2/Testcontainers) que peguem esse tipo de erro antes do runtime. Um caso real foi encontrado e corrigido na Sprint 4 (ver DT-R04). | v0.5.0 |
| DT-11 | chatbot | Sem `GEMINI_API_KEY` real nem infraestrutura de mock HTTP no projeto, a chamada real à API do Gemini nunca foi testada (nem em unit test nem em e2e) — só o caminho de fallback local foi exercitado. Mesma lacuna já registrada para o Google Calendar (DT-05). | v0.5.0 |
| DT-12 | chatbot | `CriseDetectorService` classifica a *resposta* do bot, não a mensagem do usuário (conforme o fluxo documentado em `Arquitetura-UniPsi.md`). Isso funciona porque o fallback local e o `system_prompt.txt` obrigam frases específicas ("psicólogo disponível agora", nomes das técnicas) a aparecerem em qualquer resposta de crise — mas se esse texto mudar no futuro sem atualizar as palavras-chave do detector, o acionamento de plantão pode parar de disparar silenciosamente. Ver `GeminiClientTest.gerarResposta_fallbackDeCrise_deveSerClassificadoComoCrisePeloDetector` como guarda de regressão. | v0.5.0 |
| DT-13 | chatbot | "Dados de contato" no e-mail de alerta de plantão dependem do visitante preencher voluntariamente um campo opcional no chat — não há coleta obrigatória. US-010 pressupõe "nome (ou codinome) do paciente", que não existe no fluxo anônimo do chatbot (simplificação deliberada, documentada em `v0.5.0-chatbot.md`). | v0.5.0 |
| DT-14 | chatbot | CVV/SAMU só são exibidos quando **não** há psicólogo de plantão (critério literal de US-019); quando o plantão é acionado com sucesso, eles somem da resposta — mesmo sendo um canal de ajuda imediata 24h enquanto o psicólogo não responde ao e-mail. | v0.5.0 |
| DT-15 | chatbot | US-020 ("sugerir filtros de busca com base na triagem") não foi implementado — o CTA de encerramento sempre aponta para `/cadastro/paciente` de forma genérica, sem extrair uma especialidade sugerida da conversa nem linkar direto para `/marketplace` (que exige login como paciente, inacessível ao visitante anônimo do chatbot). | v0.5.0 |

---

## Débitos resolvidos

| ID | Módulo | Descrição | Introduzido em | Resolvido em | Como |
|---|---|---|---|---|---|
| DT-R01 | agenda | Cancelamento de slot não notificava o paciente por e-mail — dependia do vínculo slot → sessão → paciente, que só passou a existir com o marketplace. | v0.2.0 (débito de US-008) | v0.3.0 | `AgendaService.cancelar` passou a localizar a `Sessao` `AGENDADA` vinculada ao slot, marcá-la `CANCELADA` e enviar e-mail ao paciente; o slot deixou de ser deletado quando há sessão vinculada (evita violar a FK). |
| DT-R02 | marketplace / prontuário | US-013 (Consultar Histórico de Prontuário) ficou fora do escopo da Sprint 2 por depender de um módulo de prontuário que ainda não existia. | v0.3.0 | v0.4.0 | Módulo `prontuario` implementado na Sprint 3; `GET /api/prontuario/{codinome}/anotacoes` cobre integralmente a história (histórico decrescente + busca por palavra-chave). |
| DT-R03 | plataforma (segurança) | Qualquer exceção não tratada (500) durante uma requisição virava um **403 opaco**: o dispatch interno para `/error` não estava coberto pelo `permitAll` do `SecurityConfig`, então o Spring Security barrava o forward por falta de autenticação, escondendo o status e a causa real do erro. Descoberto depurando um `LazyInitializationException` real durante a verificação da Sprint 4 (ver DT-R04). | v0.1.0 | v0.5.0 | Adicionado `.requestMatchers("/error").permitAll()` ao `SecurityConfig`. |
| DT-R04 | plantao / chatbot | `PlantaoService.buscarPsicologosDePlantaoHoje()` retornava `Psicologo` com `usuario` **lazy**; como o `ChatbotService` chama esse método fora de qualquer `@Transactional` (de propósito, para não segurar transação de banco durante a chamada HTTP ao Gemini), acessar `psicologo.getUsuario().getEmail()` lançava `LazyInitializationException` — mascarado como 403 pelo DT-R03 antes de ser corrigido. Encontrado só na verificação end-to-end contra o Postgres real; nenhum teste unitário (com mocks) pegaria esse tipo de erro. | v0.5.0 | v0.5.0 | `DisponibilidadePlantaoRepository.findByAtivoTrue()` passou a usar `JOIN FETCH` em `psicologo` e `psicologo.usuario`. |
| DT-R05 | plataforma (CORS) | **Todo o frontend estava quebrado quando acessado por um navegador real** de uma origem diferente de `unipsi-web.claudinha.local` (ex.: `http://localhost:8100`) — `SecurityConfig` aceitava só uma origem fixa em `FRONTEND_ORIGIN`, e o browser bloqueava a resposta CORS ("Invalid CORS request", 403) em todo POST/PUT autenticado. `curl` nunca pegou isso porque não aplica CORS. Descoberto na primeira verificação end-to-end via navegador real (Playwright) do projeto — todas as verificações anteriores tinham sido só via `curl`/testes automatizados. | v0.1.0 | QA pré-demo (07/07/2026) | `unipsi.frontend-origin` passou a aceitar lista separada por vírgula; `FRONTEND_ORIGIN` em `.env` agora inclui `http://localhost:8100` além do domínio Caddy. |
| DT-R06 | frontend (UI) | 4 componentes `Select` (`RegisterPacientePage`, `PerfilPacientePage`, `PlantaoPage`, `ModalidadeSelector`) exibiam o valor bruto do enum (`FAIXA_3`, `AVULSA`, `SEG`) em vez do rótulo formatado — `Select.Value` do Base UI não infere automaticamente o texto do `SelectItem` correspondente antes do popup ter sido aberto ao menos uma vez. Em `PerfilPacientePage` o problema era mais grave: o valor carregado nunca aparecia (ficava preso no placeholder "Selecione sua faixa de renda" para sempre), porque o Select virava "controlado" um render depois de montar como "não controlado" (`value` chega `undefined` antes do `reset()` do react-hook-form rodar). Só apareceu em verificação visual — nenhum teste teria pego, já que é puramente de renderização. | v0.1.0 (cadastro), v0.2.0 (plantão), v0.3.0 (perfil paciente/modalidade) | QA pré-demo (07/07/2026) | `SelectValue` passou a receber uma função `(valor) => rótulo` como children nos 4 locais; `PerfilPacientePage` também ganhou `key={faixaRenda ?? 'carregando'}` no `Select` para forçar remontagem controlada quando os dados chegam. |

---

## Fora do escopo deste documento

Decisões de produto ainda não tomadas (não são atalhos técnicos, são pendências de negócio) ficam
na tabela "Itens ainda a definir" do [`CLAUDE.md`](../CLAUDE.md) — ex.: gateway de pagamento real,
domínio de produção.
