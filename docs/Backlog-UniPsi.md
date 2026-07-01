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

**Como** paciente,  
**quero** atualizar meus dados cadastrais e minha faixa de renda declarada,  
**para que** o valor das sessões reflita minha situação atual.

**Critérios de aceitação:**
- [ ] Paciente pode editar todos os dados pessoais
- [ ] Atualização da faixa de renda recalcula o valor das próximas sessões (não afeta sessões já agendadas)
- [ ] Histórico de alterações de faixa de renda é registrado internamente

**Prioridade:** Média | **Pontos:** 2

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

**Como** paciente,  
**quero** buscar psicólogos disponíveis para terapia social com filtros por especialidade e disponibilidade,  
**para que** eu encontre um profissional adequado ao meu perfil e necessidade.

**Critérios de aceitação:**
- [ ] Listagem exibe psicólogos aprovados com sessões disponíveis
- [ ] Filtros disponíveis: especialidade, dias/horários disponíveis
- [ ] Card do psicólogo exibe: nome, especialidades, próximas disponibilidades e valor da sessão calculado para o perfil do paciente logado
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

**Como** paciente em situação de crise,  
**quero** que o chatbot reconheça meu estado e me ofereça orientações imediatas,  
**para que** eu tenha algum suporte enquanto aguardo atendimento profissional.

**Critérios de aceitação:**
- [ ] Chatbot identifica palavras-chave e contexto de crise (ansiedade severa, pensamentos de autolesão, pânico)
- [ ] Ao identificar crise: apresenta técnicas de suporte imediato (respiração 4-7-8, ancoragem 5-4-3-2-1, etc.)
- [ ] Após oferecer suporte imediato, aciona o fluxo de plantão (US-010)
- [ ] Se nenhum psicólogo estiver de plantão, chatbot informa e exibe contatos de emergência (CVV 188, SAMU 192)
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

## Resumo do Backlog

| Épico | Histórias | Pontos Totais |
|---|---|---|
| EP-01 Cadastro e Autenticação | US-001 a US-005 | 18 |
| EP-02 Gestão de Agenda | US-006 a US-008 | 16 |
| EP-03 Plantão | US-009 a US-010 | 6 |
| EP-04 Prontuário Eletrônico | US-011 a US-013 | 11 |
| EP-05 Marketplace e Terapia Social | US-014 a US-017 | 21 |
| EP-06 Chatbot de Triagem | US-018 a US-020 | 19 |
| EP-07 Cobranças e Pagamento | US-021 a US-023 | 8 |
| EP-08 Relatório Financeiro | US-024 | 5 |
| EP-09 Notificações | US-025 | 3 |
| EP-10 Administração | US-026 a US-027 | 10 |
| **Total** | **27 histórias** | **117 pontos** |

---

## Sugestão de Ordem de Desenvolvimento (MVP)

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
