# Backlog — Universo Psicólogo

**Versão:** 1.0  
**Data:** 30/06/2026  
**Metodologia:** Histórias de usuário com critérios de aceitação  
**Referência:** Doc-Requisitos-UniPsi.md

---

## Convenções

- **Prioridade:** Alta / Média / Baixa  
- **Tamanho (Story Points):** 1 (trivial) → 13 (muito complexo)  
- Histórias bloqueantes para o fluxo principal estão marcadas com 🔴  
- Histórias dependentes de outras estão referenciadas por ID

---

## Épicos

| ID | Épico |
|---|---|
| EP-01 | Cadastro e Autenticação |
| EP-02 | Gestão de Agenda |
| EP-03 | Plantão |
| EP-04 | Prontuário Eletrônico |
| EP-05 | Marketplace e Terapia Social |
| EP-06 | Chatbot de Triagem |
| EP-07 | Cobranças e Pagamento |
| EP-08 | Relatório Financeiro |
| EP-09 | Notificações |
| EP-10 | Administração da Plataforma |
| EP-11 | Anamnese e Perfil Clínico Básico |
| EP-12 | Mensagens Internas |

---

## EP-01 — Cadastro e Autenticação

### US-001 🔴 — Cadastro de Psicólogo

**Como** psicólogo,  
**quero** me cadastrar na plataforma informando meus dados profissionais, fazendo upload do meu currículo e descrevendo minha política de cancelamento,  
**para que** meu perfil seja avaliado pela plataforma e eu possa ser aprovado para atender.

**Critérios de aceitação:**
- [ ] Formulário contém: nome completo, e-mail, senha, número do CRP, estado do CRP, especialização(ões), foto de perfil (opcional)
- [ ] Campo de upload de currículo (PDF ou DOCX, máx. 5 MB)
- [ ] Campo de texto para descrição da política de cancelamento
- [ ] Após a submissão, o cadastro fica com status `pendente_aprovacao`
- [ ] O psicólogo recebe e-mail confirmando que o cadastro foi recebido e está em avaliação
- [ ] Campos obrigatórios ausentes impedem a submissão com mensagem de erro clara

**Prioridade:** Alta | **Pontos:** 5

---

### US-002 🔴 — Cadastro de Paciente

**Como** paciente,  
**quero** me cadastrar na plataforma com meus dados pessoais e informar minha faixa de renda,  
**para que** a plataforma calcule o valor das sessões compatível com minha realidade financeira.

**Critérios de aceitação:**
- [ ] Formulário contém: nome completo, e-mail, senha, data de nascimento, cidade/estado
- [ ] Campo de autodeclaração de **renda domiciliar per capita** com seleção por faixa (exibir apenas as 4 faixas elegíveis):
  - FAIXA_1: Até R$ 405,25 — BPC/LOAS (extrema pobreza)
  - FAIXA_2: R$ 405,26 – R$ 810,50 — CadÚnico / Bolsa Família
  - FAIXA_3: R$ 810,51 – R$ 1.621,00 — Classe E
  - FAIXA_4: R$ 1.621,01 – R$ 3.242,00 — Classe D
- [ ] Paciente que declare renda acima de R$ 3.242,00 per capita é informado de que não é elegível para a terapia social e é orientado a buscar atendimento particular — cadastro não é concluído
- [ ] Conta ativada imediatamente após o cadastro para pacientes elegíveis, sem necessidade de aprovação
- [ ] Paciente recebe e-mail de boas-vindas com orientações iniciais

**Prioridade:** Alta | **Pontos:** 3

---

### US-003 🔴 — Autenticação

**Como** psicólogo ou paciente,  
**quero** fazer login com e-mail e senha,  
**para que** eu acesse minha área segura na plataforma.

**Critérios de aceitação:**
- [ ] Login com e-mail e senha válidos gera sessão autenticada com token
- [ ] Sessão expira após inatividade configurável (padrão: 30 minutos)
- [ ] Tentativas inválidas exibem mensagem de erro sem revelar qual campo está incorreto
- [ ] Existe fluxo de recuperação de senha via e-mail
- [ ] Psicólogo com cadastro `pendente_aprovacao` ou `reprovado` não acessa funcionalidades operacionais

**Prioridade:** Alta | **Pontos:** 5

---

### US-004 — Edição de Perfil do Psicólogo

**Como** psicólogo aprovado,  
**quero** editar meus dados de perfil (foto, especialização, política de cancelamento, link de videochamada externa),  
**para que** as informações exibidas aos pacientes estejam sempre atualizadas.

**Critérios de aceitação:**
- [ ] Psicólogo pode atualizar todos os campos exceto CRP (alteração de CRP requer nova avaliação do admin)
- [ ] Alteração da política de cancelamento é registrada e reflete imediatamente no perfil público
- [ ] Campo opcional para inserir link de videochamada externa (Google Meet, Zoom)

**Prioridade:** Média | **Pontos:** 3

---

### US-005 — Edição de Perfil do Paciente

> **Revisado em 07/07/2026** (ver `atas/2026-07-07-alinhamento-sprint-4.md`) — a versão original desta
> história permitia ao paciente editar a própria faixa de renda; o stakeholder decidiu que isso não
> deve mais acontecer. O critério de recálculo automático de valor foi removido; a versão anterior
> já estava implementada em `PUT /api/usuarios/paciente/perfil` (Sprint 2) e precisa ser revertida.

**Como** paciente,  
**quero** atualizar meus dados cadastrais, foto de perfil e idade,  
**para que** meu perfil esteja sempre atualizado.

**Critérios de aceitação:**
- [ ] Paciente pode editar nome, foto de perfil e idade
- [ ] Paciente pode adicionar/trocar a foto de perfil (upload de imagem, mesmo padrão de arquivo usado no perfil do psicólogo)
- [ ] **Faixa de renda não é editável pelo paciente.** É autodeclarada uma única vez no cadastro (US-002) e só pode ser alterada pelo fluxo de revisão de perfil financeiro conduzido pelo psicólogo e decidido pelo admin (US-017/US-027)

**Prioridade:** Média | **Pontos:** 3

---

## EP-02 — Gestão de Agenda

### US-006 🔴 — Cadastrar Disponibilidade na Agenda

**Como** psicólogo aprovado,  
**quero** cadastrar os horários em que estou disponível para atendimento,  
**para que** os pacientes possam agendar sessões nesses horários.

**Critérios de aceitação:**
- [ ] Psicólogo seleciona data, horário de início e fim para criar um slot disponível
- [ ] É possível criar múltiplos slots em sequência (ex.: toda segunda das 08h às 12h)
- [ ] Slots ocupados (sessão agendada) não aparecem como disponíveis para novos agendamentos
- [ ] Sincronização com Google Calendar: evento criado na plataforma aparece no Google Calendar do psicólogo

**Prioridade:** Alta | **Pontos:** 8  
**Depende de:** US-003

---

### US-007 — Visualizar Agenda

**Como** psicólogo,  
**quero** visualizar minha agenda em formato semanal e mensal,  
**para que** eu tenha uma visão clara dos meus compromissos.

**Critérios de aceitação:**
- [ ] Visualização semanal mostra horários dia a dia com sessões agendadas e slots livres
- [ ] Visualização mensal exibe dias com ao menos uma sessão destacados
- [ ] Ao clicar em uma sessão, exibe dados do paciente (codinome) e horário
- [ ] Sessões canceladas são exibidas com status diferenciado

**Prioridade:** Alta | **Pontos:** 5  
**Depende de:** US-006

---

### US-008 — Cancelar Sessão Agendada

**Como** psicólogo,  
**quero** cancelar uma sessão agendada,  
**para que** o horário seja liberado e o paciente seja informado.

**Critérios de aceitação:**
- [ ] Psicólogo seleciona uma sessão e confirma o cancelamento com motivo (campo texto opcional)
- [ ] Sistema libera o slot e atualiza o Google Calendar
- [ ] Paciente recebe notificação por e-mail com o cancelamento e o motivo (se informado)
- [ ] Sessões canceladas com menos de X horas de antecedência são destacadas como fora da política (regra a definir)

**Prioridade:** Alta | **Pontos:** 3  
**Depende de:** US-006

---

## EP-03 — Plantão

### US-009 🔴 — Registrar Disponibilidade de Plantão

**Como** psicólogo aprovado,  
**quero** informar os dias em que estou disponível para atendimento de urgência em plantão,  
**para que** a plataforma me acione quando um paciente em crise precisar de atendimento imediato.

**Critérios de aceitação:**
- [ ] Psicólogo acessa configuração de plantão e seleciona dias da semana e/ou datas específicas
- [ ] É possível ativar ou desativar o plantão a qualquer momento
- [ ] Sistema exibe status atual do plantão (ativo/inativo) de forma clara no painel
- [ ] Apenas psicólogos com plantão ativo no dia corrente recebem notificações de urgência

**Prioridade:** Alta | **Pontos:** 3  
**Depende de:** US-003

---

### US-010 — Receber Notificação de Plantão

**Como** psicólogo de plantão,  
**quero** receber uma notificação por e-mail quando um paciente em crise for encaminhado ao plantão,  
**para que** eu possa entrar em contato com o paciente o mais rapidamente possível.

**Critérios de aceitação:**
- [ ] E-mail contém: nome (ou codinome) do paciente, horário do acionamento e dados de contato
- [ ] Notificação é enviada apenas para psicólogos com plantão ativo no dia
- [ ] Se nenhum psicólogo estiver de plantão, o chatbot informa o paciente e sugere contato com o CVV (188) ou SAMU (192)

**Prioridade:** Alta | **Pontos:** 3  
**Depende de:** US-009, US-026 (chatbot)

---

## EP-04 — Prontuário Eletrônico

### US-011 🔴 — Criar Anotação de Prontuário

**Como** psicólogo,  
**quero** criar anotações clínicas em texto livre para um paciente identificado por codinome,  
**para que** eu mantenha um registro seguro do acompanhamento sem expor a identidade do paciente.

**Critérios de aceitação:**
- [ ] Psicólogo acessa o prontuário de um paciente a partir do painel de sessões
- [ ] O paciente é exibido apenas pelo codinome definido pelo psicólogo (nunca pelo nome real)
- [ ] Campo de texto livre sem limite de caracteres mínimo; com aviso de limite máximo razoável (ex.: 10.000 caracteres)
- [ ] Anotação registra data e hora automaticamente
- [ ] Anotação é salva com criptografia e só pode ser acessada pelo psicólogo autor mediante sessão autenticada ativa

**Prioridade:** Alta | **Pontos:** 5  
**Depende de:** US-003, US-006

---

### US-012 — Definir Codinome do Paciente

**Como** psicólogo,  
**quero** definir um codinome para identificar cada paciente no prontuário,  
**para que** as anotações clínicas não exponham a identidade real do paciente.

**Critérios de aceitação:**
- [ ] Ao vincular um novo paciente, o psicólogo define obrigatoriamente um codinome
- [ ] O codinome é único por psicólogo (não pode repetir no mesmo prontuário)
- [ ] O nome real do paciente nunca aparece nas telas de prontuário
- [ ] O psicólogo pode alterar o codinome; o histórico anterior é mantido com o novo codinome

**Prioridade:** Alta | **Pontos:** 3  
**Depende de:** US-011

---

### US-013 — Consultar Histórico de Prontuário

**Como** psicólogo,  
**quero** consultar o histórico de anotações de um paciente em ordem cronológica,  
**para que** eu possa acompanhar a evolução do atendimento.

**Critérios de aceitação:**
- [ ] Histórico exibe anotações em ordem cronológica decrescente (mais recente primeiro)
- [ ] Exibe data, hora e conteúdo de cada anotação
- [ ] Permite busca por palavra-chave dentro das anotações
- [ ] Acesso bloqueado imediatamente após expiração da sessão autenticada

**Prioridade:** Média | **Pontos:** 3  
**Depende de:** US-011

---

## EP-05 — Marketplace e Terapia Social

### US-014 🔴 — Buscar Psicólogos Disponíveis

> **Revisado em 07/07/2026** — o campo de busca por "especialidade" foi renomeado para não colidir
> conceitualmente com a especialização/abordagem do psicólogo (já exibida no card). Ver
> `atas/2026-07-07-alinhamento-sprint-4.md`. Nome sugerido: **"Áreas de atuação"** (a confirmar).

**Como** paciente,  
**quero** buscar psicólogos disponíveis para terapia social com filtros por área de atuação e disponibilidade,  
**para que** eu encontre um profissional adequado ao meu perfil e necessidade.

**Critérios de aceitação:**
- [ ] Listagem exibe psicólogos aprovados com sessões disponíveis
- [ ] Filtros disponíveis: **áreas de atuação** (temas/situações atendidas, ex.: ansiedade, luto, terapia de casal — exibidas como tags no card, distintas da especialização), dias/horários disponíveis
- [ ] Card do psicólogo exibe: nome, especialização, tags de áreas de atuação, próximas disponibilidades e valor da sessão calculado para o perfil do paciente logado
- [ ] Paciente pode acessar o perfil completo do psicólogo (abordagem, política de cancelamento, link externo de videochamada)

**Prioridade:** Alta | **Pontos:** 8  
**Depende de:** US-002, US-006

---

### US-015 🔴 — Agendar Sessão pelo Marketplace

**Como** paciente,  
**quero** agendar uma sessão com um psicólogo diretamente pelo marketplace,  
**para que** eu inicie meu acompanhamento sem precisar passar pelo chatbot.

**Critérios de aceitação:**
- [ ] Paciente seleciona o psicólogo, visualiza os slots disponíveis e confirma o agendamento
- [ ] Sistema gera a sessão com valor calculado pelo perfil socioeconômico do paciente (faixa R$30–R$100)
- [ ] Confirmação por e-mail enviada para paciente e psicólogo com data, horário e link externo de videochamada (se configurado)
- [ ] Slot agendado é removido da disponibilidade visível para outros pacientes
- [ ] **(Ajuste 07/07/2026)** Em "Meus agendamentos", quando a modalidade for pacote mensal, exibir o **valor da sessão avulsa** e o **valor total do pacote**, destacando a economia — não só "valor por sessão", que gerava confusão (ver `atas/2026-07-07-alinhamento-sprint-4.md`)

**Prioridade:** Alta | **Pontos:** 5  
**Depende de:** US-014

---

### US-016 — Precificação Dinâmica e Modalidade de Atendimento

**Como** paciente,  
**quero** escolher entre sessão avulsa ou pacote mensal e ver o valor calculado automaticamente pelo meu perfil socioeconômico,  
**para que** o acesso à terapia seja proporcional à minha capacidade financeira e eu possa planejar meu investimento mensal.

**Critérios de aceitação:**

**Avulsa — valor por sessão:**

  | Faixa | Referência | Valor avulso |
  |---|---|---|
  | FAIXA_1 | BPC/LOAS (até ¼ SM) | R$ 60,00 |
  | FAIXA_2 | CadÚnico / Bolsa Família (½ SM) | R$ 65,00 |
  | FAIXA_3 | Classe E (1 SM) | R$ 70,00 |
  | FAIXA_4 | Classe D (2 SM) | R$ 75,00 |

**Pacote mensal — 4 sessões, 5% desconto:**

  | Faixa | Total/mês | Por sessão |
  |---|---|---|
  | FAIXA_1 | R$ 228,00 | R$ 57,00 |
  | FAIXA_2 | R$ 247,00 | R$ 61,75 |
  | FAIXA_3 | R$ 266,00 | R$ 66,50 |
  | FAIXA_4 | R$ 285,00 | R$ 71,25 |

- [ ] Paciente seleciona a modalidade (Avulsa / Pacote Mensal) antes de confirmar o agendamento
- [ ] Valor da modalidade selecionada exibido claramente antes da confirmação
- [ ] `PrecificacaoService(FaixaRenda, Modalidade)` retorna o valor correto para cada combinação
- [ ] `PrecificacaoService` lança `PacienteNaoElegivelException` para renda fora das 4 faixas
- [ ] Alteração de faixa de renda não altera valor de sessões já confirmadas
- [ ] Taxa de 20% calculada e registrada no `CobrancaService`; valor líquido exibido ao psicólogo no relatório financeiro
- [ ] Cancelamento dentro do pacote mensal segue a política de 8h (RF-21b)
- [ ] **(Ajuste 07/07/2026)** Paciente indica se o atendimento é individual ou **terapia de casal**; quando for casal, o valor cobrado é o **dobro** do valor individual da mesma faixa/modalidade — valores exatos a confirmar (tarefa do Victor, ver `atas/2026-07-07-alinhamento-sprint-4.md`)

**Prioridade:** Alta | **Pontos:** 5  
**Depende de:** US-002, US-015

---

### US-017 — Solicitar Revisão de Perfil Financeiro do Paciente

**Como** psicólogo,  
**quero** solicitar a revisão do perfil financeiro de um paciente quando identificar inconsistência,  
**para que** a plataforma avalie e garanta a integridade do programa de terapia social.

**Critérios de aceitação:**
- [ ] Psicólogo acessa o perfil do paciente e abre solicitação de revisão com campo de justificativa
- [ ] Sistema registra a solicitação e notifica o administrador
- [ ] Atendimentos em curso não são suspensos durante a revisão
- [ ] Psicólogo e paciente são notificados por e-mail quando a plataforma toma uma decisão
- [ ] Não há prazo definido para a decisão da plataforma

**Prioridade:** Média | **Pontos:** 5  
**Depende de:** US-014

---

## EP-06 — Chatbot de Triagem

### US-018 🔴 — Iniciar Conversa de Triagem

**Como** visitante ou paciente,  
**quero** iniciar uma conversa com o chatbot para descrever como estou me sentindo,  
**para que** ele me ajude a entender meu estado emocional e me indique o próximo passo.

**Critérios de aceitação:**
- [ ] Chatbot acessível na página inicial, sem necessidade de login
- [ ] Conversa iniciada com mensagem de apresentação e primeiras perguntas abertas
- [ ] Chatbot usa IA generativa (Google Gemini API `gemini-1.5-flash` ou Groq/LLaMA 3)
- [ ] Respostas em português do Brasil, tom empático e acolhedor
- [ ] Chatbot não emite diagnóstico clínico em nenhuma resposta

**Prioridade:** Alta | **Pontos:** 8

---

### US-019 🔴 — Identificar Situação de Crise e Oferecer Suporte Imediato

> **Ajuste 07/07/2026** (ver ata) — a busca de profissional deixa de se limitar a quem está de
> plantão hoje: passa a incluir também psicólogos com a próxima disponibilidade mais próxima na
> agenda, retornando o contato ao paciente. Link do CVV também mudou: chat em
> `https://cvv.org.br/chat/` + ligação `tel:188`, não só o número.

**Como** paciente em situação de crise,  
**quero** que o chatbot reconheça meu estado e me ofereça orientações imediatas,  
**para que** eu tenha algum suporte enquanto aguardo atendimento profissional.

**Critérios de aceitação:**
- [ ] Chatbot identifica palavras-chave e contexto de crise (ansiedade severa, pensamentos de autolesão, pânico)
- [ ] Ao identificar crise: apresenta técnicas de suporte imediato (respiração 4-7-8, ancoragem 5-4-3-2-1, etc.)
- [ ] Paciente pode deixar um contato (e-mail/telefone) de retorno, especialmente útil se a IA/sistema estiver indisponível
- [ ] Após oferecer suporte imediato, aciona o fluxo de plantão (US-010) **e também busca psicólogos com a próxima disponibilidade mais próxima na agenda** (não só quem está de plantão hoje) — retorna esse contato ao paciente
- [ ] Se ninguém for encontrado (nem plantão, nem disponibilidade próxima), chatbot informa e exibe contatos de emergência: **CVV** — chat [https://cvv.org.br/chat/](https://cvv.org.br/chat/) e ligação `tel:188` — e **SAMU** (192)
- [ ] Chatbot **nunca simula diagnóstico** — qualquer tentativa do usuário de forçar isso é respondida com redirecionamento ao profissional

**Prioridade:** Alta | **Pontos:** 8  
**Depende de:** US-018, US-009

---

### US-020 — Encaminhar ao Marketplace após Triagem

**Como** paciente sem urgência imediata,  
**quero** que o chatbot me encaminhe ao marketplace após a triagem,  
**para que** eu encontre um psicólogo adequado ao meu perfil.

**Critérios de aceitação:**
- [ ] Ao concluir triagem sem identificar crise, chatbot apresenta link para o marketplace
- [ ] Chatbot pode sugerir filtros de busca com base nas informações coletadas na triagem (ex.: especialidade identificada)
- [ ] Transição é suave: chatbot finaliza a conversa com mensagem de encerramento antes do redirecionamento

**Prioridade:** Média | **Pontos:** 3  
**Depende de:** US-018, US-014

---

## EP-07 — Cobranças e Pagamento

### US-021 🔴 — Gerar Cobrança após Sessão

**Como** sistema,  
**quero** gerar automaticamente uma cobrança ao paciente após a realização de uma sessão,  
**para que** o processo financeiro seja iniciado sem intervenção manual.

**Critérios de aceitação:**
- [ ] Cobrança gerada com: valor da sessão, data, nome do psicólogo e status inicial `pendente`
- [ ] Paciente é notificado por e-mail com os detalhes da cobrança
- [ ] Cobrança visível na área financeira do paciente e do psicólogo

**Prioridade:** Alta | **Pontos:** 3  
**Depende de:** US-015

---

### US-022 — Simular Pagamento

**Como** paciente,  
**quero** confirmar o pagamento de uma cobrança na plataforma,  
**para que** o ciclo financeiro seja registrado (simulado no MVP, sem gateway real).

**Critérios de aceitação:**
- [ ] Paciente acessa cobranças pendentes e clica em "Confirmar pagamento"
- [ ] Sistema altera status para `pago` e registra data/hora do pagamento
- [ ] Sistema aplica percentual de taxa da plataforma e calcula valor líquido para o psicólogo
- [ ] Psicólogo é notificado por e-mail com o valor líquido

**Prioridade:** Alta | **Pontos:** 3  
**Depende de:** US-021

---

### US-023 — Cancelar Cobrança

**Como** sistema ou administrador,  
**quero** cancelar uma cobrança quando uma sessão não foi realizada,  
**para que** o paciente não seja cobrado indevidamente.

**Critérios de aceitação:**
- [ ] Cobrança pode ser cancelada manualmente pelo administrador
- [ ] Cobrança é cancelada automaticamente quando a sessão vinculada é cancelada
- [ ] Paciente é notificado por e-mail do cancelamento

**Prioridade:** Média | **Pontos:** 2  
**Depende de:** US-021, US-008

---

## EP-08 — Relatório Financeiro

### US-024 — Visualizar Relatório Financeiro

**Como** psicólogo,  
**quero** visualizar um relatório financeiro das minhas sessões por período,  
**para que** eu acompanhe minha renda na plataforma.

**Critérios de aceitação:**
- [ ] Psicólogo seleciona período (mês/ano ou intervalo de datas)
- [ ] Relatório exibe: lista de sessões realizadas, valor bruto de cada sessão, taxa da plataforma e valor líquido
- [ ] Exibe totais consolidados: bruto total, total de taxas, líquido total
- [ ] Apenas sessões com status `pago` são contabilizadas nos totais

**Prioridade:** Média | **Pontos:** 5  
**Depende de:** US-022

---

## EP-09 — Notificações

### US-025 — Lembrete de Sessão

**Como** paciente ou psicólogo,  
**quero** receber um lembrete por e-mail antes da minha sessão agendada,  
**para que** eu não esqueça o compromisso.

**Critérios de aceitação:**
- [ ] Lembrete enviado por e-mail para paciente e psicólogo com antecedência de 24h e 1h
- [ ] E-mail contém: data, horário, nome do profissional/paciente e link de videochamada (se configurado)
- [ ] Lembretes não são enviados para sessões canceladas
- [ ] Envio via Resend ou SendGrid (tier gratuito)

**Prioridade:** Alta | **Pontos:** 3  
**Depende de:** US-015

---

## EP-10 — Administração da Plataforma

### US-026 🔴 — Painel de Avaliação de Cadastros

**Como** administrador,  
**quero** visualizar e avaliar os cadastros de psicólogos pendentes de aprovação,  
**para que** apenas profissionais qualificados e alinhados ao projeto acessem a plataforma.

**Critérios de aceitação:**
- [ ] Painel lista todos os cadastros com status `pendente_aprovacao` em ordem cronológica
- [ ] Administrador visualiza: dados pessoais, CRP, especialização, currículo (download) e política de cancelamento
- [ ] Ações disponíveis: Aprovar, Reprovar (com campo de motivo), Solicitar complementação
- [ ] Em todos os casos, psicólogo é notificado por e-mail com o resultado
- [ ] Cadastros aprovados tornam o psicólogo ativo imediatamente

**Prioridade:** Alta | **Pontos:** 5  
**Depende de:** US-001

---

### US-027 — Gerenciar Solicitações de Revisão de Perfil Financeiro

**Como** administrador,  
**quero** visualizar e decidir sobre as solicitações de revisão de perfil financeiro de pacientes,  
**para que** a integridade do programa de terapia social seja preservada.

**Critérios de aceitação:**
- [ ] Painel lista solicitações abertas com: nome do paciente, codinome no prontuário, psicólogo solicitante, justificativa e data
- [ ] Administrador pode manter o perfil atual ou atualizar a faixa de renda do paciente
- [ ] Após decisão, psicólogo e paciente são notificados por e-mail
- [ ] Histórico de revisões é mantido para auditoria

**Prioridade:** Média | **Pontos:** 5  
**Depende de:** US-017

---

## EP-11 — Anamnese e Perfil Clínico Básico

> **Novo épico — reunião de 07/07/2026.** Ver `atas/2026-07-07-alinhamento-sprint-4.md`.

### US-028 — Preencher Anamnese Básica

> **Refinado em 07/07/2026** (adendo à ata) — a anamnese é sempre do paciente: nunca pública, e o
> psicólogo só tem acesso temporário (ver US-030). Critérios de acesso do psicólogo foram
> desmembrados para uma história própria.

**Como** paciente,  
**quero** preencher um formulário básico de anamnese antes da minha primeira sessão,  
**para que** o psicólogo já chegue preparado no primeiro atendimento, sem depender só da conversa inicial.

**Critérios de aceitação:**
- [ ] Formulário inclui, entre outras: se já fez terapia antes, motivo de buscar terapia agora, se toma medicação controlada (lista final de perguntas é tarefa do Victor — ver ata)
- [ ] Formulário é preenchido uma vez após o cadastro, em `/perfil-paciente` (não por psicólogo/sessão específica)
- [ ] `/perfil-paciente` exibe aviso explicando o uso e o acesso temporário, copy sugerida: *"Preencha seu perfil com sua anamnese. Essa informação não será pública — o profissional só tem acesso quando você agendar e efetuar o pagamento, antes da primeira terapia. Depois disso, ele não terá mais acesso."*
- [ ] Campo de contato do responsável só aparece **se o paciente se identificar como menor de idade**, acompanhado de uma explicação de por que é solicitado (prática clínica: atendimento a menores exige presença/consentimento do responsável, sobretudo na primeira sessão) — nunca exibido para pacientes maiores de idade

**Prioridade:** Alta | **Pontos:** 5  
**Depende de:** US-002, US-005

---

### US-030 — Acesso Temporário do Psicólogo à Anamnese

> **Nova história — adendo de 07/07/2026.** A anamnese segue o mesmo padrão de proteção do
> prontuário (criptografia + auditoria), mas com controle de acesso por **janela de tempo**, não
> por autoria fixa — o psicólogo não é "dono" da anamnese como é das próprias anotações.

**Como** psicólogo,  
**quero** acessar a anamnese do paciente apenas entre o pagamento confirmado da primeira sessão e a
realização dela,  
**para que** eu me prepare para o primeiro atendimento sem reter esse dado sensível depois.

**Critérios de aceitação:**
- [ ] Psicólogo só visualiza a anamnese de um paciente se existir uma `Sessao` entre os dois com `Cobranca` paga **e** essa sessão ainda não foi marcada como `REALIZADA`
- [ ] Assim que a sessão é marcada `REALIZADA`, o acesso à anamnese é revogado para esse psicólogo
- [ ] Se o paciente agendar depois com outro psicólogo, esse novo profissional recebe sua própria janela de acesso (independente, não compartilhada)
- [ ] Anamnese é armazenada cifrada (mesmo padrão AES-256-GCM do prontuário, via `CriptografiaService`) — conteúdo nunca em texto claro no banco
- [ ] Toda leitura da anamnese por um psicólogo é registrada em auditoria (mesmo espírito de `AuditoriaProntuarioService`)
- [ ] Tentativa de leitura fora da janela de acesso é bloqueada (mesmo padrão de `AcessoProntuarioNegadoException` → 403)

**Prioridade:** Alta | **Pontos:** 5  
**Depende de:** US-028, US-015, US-021, US-022 *(cobrança e pagamento — módulo financeiro, Sprint 5)*

---

## EP-12 — Mensagens Internas

> **Novo épico — reunião de 07/07/2026.** Ver `atas/2026-07-07-alinhamento-sprint-4.md`.

### US-029 — Trocar Mensagens na Plataforma Após Pagamento

**Como** paciente ou psicólogo,  
**quero** trocar mensagens diretamente na plataforma depois que a sessão for agendada e paga,  
**para que** eu possa combinar detalhes do atendimento sem precisar de outro canal externo.

**Critérios de aceitação:**
- [ ] Chat entre paciente e psicólogo só é liberado quando a `Sessao` correspondente tem `Cobranca` com status `PAGO`
- [ ] Antes do pagamento confirmado, não há canal de mensagens entre as partes
- [ ] Mensagens ficam associadas à sessão/relação psicólogo-paciente
- [ ] Notificação (e-mail ou in-app) quando há mensagem nova

**Prioridade:** Média | **Pontos:** 8  
**Depende de:** US-015, US-021, US-022 *(cobrança e pagamento — módulo financeiro, Sprint 5)*

---

## Resumo do Backlog

| Épico | Histórias | Pontos Totais |
|---|---|---|
| EP-01 Cadastro e Autenticação | US-001 a US-005 | 19 |
| EP-02 Gestão de Agenda | US-006 a US-008 | 16 |
| EP-03 Plantão | US-009 a US-010 | 6 |
| EP-04 Prontuário Eletrônico | US-011 a US-013 | 11 |
| EP-05 Marketplace e Terapia Social | US-014 a US-017 | 21 |
| EP-06 Chatbot de Triagem | US-018 a US-020 | 19 |
| EP-07 Cobranças e Pagamento | US-021 a US-023 | 8 |
| EP-08 Relatório Financeiro | US-024 | 5 |
| EP-09 Notificações | US-025 | 3 |
| EP-10 Administração | US-026 a US-027 | 10 |
| EP-11 Anamnese e Perfil Clínico Básico *(novo, 07/07/2026)* | US-028, US-030 | 10 |
| EP-12 Mensagens Internas *(novo, 07/07/2026)* | US-029 | 8 |
| **Total** | **30 histórias** | **136 pontos** |

> A pontuação de EP-01 subiu de 18→19 porque US-005 foi revisada em 07/07/2026 (removeu edição de
> faixa de renda pelo paciente, adicionou foto de perfil e idade). Ver
> `atas/2026-07-07-alinhamento-sprint-4.md`.

---

## Sugestão de Ordem de Desenvolvimento (MVP)

> ⚠️ Esta seção ficou **defasada** em relação à ordem real seguida pelo projeto — ver
> `Sprints-UniPsi.md`, que é o plano efetivamente executado (Sprint 3 = Prontuário, Sprint 4 =
> Chatbot, não como sugerido abaixo). Mantida aqui só como registro histórico do planejamento
> inicial. As histórias novas desta reunião (US-028, US-029) e os ajustes marcados "07/07/2026"
> ainda não têm sprint definida — ver `Sprints-UniPsi.md`.

### Sprint 0 — Fundação
US-001, US-002, US-003, US-026

### Sprint 1 — Gestão do Psicólogo
US-004, US-006, US-007, US-008, US-009, US-011, US-012

### Sprint 2 — Marketplace
US-005, US-013, US-014, US-015, US-016

### Sprint 3 — Chatbot e Plantão
US-018, US-019, US-020, US-010

### Sprint 4 — Financeiro e Notificações
US-021, US-022, US-023, US-024, US-025

### Sprint 5 — Administração e Fechamento
US-017, US-027, testes de integração, ajustes de LGPD
