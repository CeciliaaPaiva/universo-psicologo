# Documento de Requisitos — Universo Psicólogo

**Versão:** 1.0  
**Data:** 30/06/2026  
**Status:** Em revisão

---

## Sumário

1. [Introdução](#1-introdução)
2. [Atores do Sistema](#2-atores-do-sistema)
3. [Requisitos Funcionais](#3-requisitos-funcionais)
4. [Requisitos Não Funcionais](#4-requisitos-não-funcionais)
5. [Casos de Uso](#5-casos-de-uso)
6. [Restrições e Premissas](#6-restrições-e-premissas)
7. [Pontos em Aberto](#7-pontos-em-aberto)

---

## 1. Introdução

Este documento especifica os requisitos funcionais e não funcionais do **Universo Psicólogo**, plataforma digital voltada à democratização do acesso à saúde mental. O sistema conecta psicólogos comprometidos com a terapia social a pacientes em situação de vulnerabilidade socioeconômica, oferecendo ferramentas de gestão administrativa para os profissionais e um sistema de triagem via chatbot para os pacientes.

O escopo deste documento cobre o **MVP (versão inicial)** da plataforma, conforme definido no Documento de Visão.

---

## 2. Atores do Sistema

| Ator | Descrição |
|---|---|
| **Psicólogo** | Profissional com registro ativo no CRP, aprovado pela plataforma após avaliação de currículo e perfil. Gerencia agenda, prontuário e cobranças. |
| **Paciente** | Pessoa que busca atendimento psicológico. Pode acessar o marketplace diretamente ou ser direcionada via chatbot. |
| **Administrador** | Equipe interna da plataforma. Aprova ou reprova cadastros de psicólogos, avalia políticas de cancelamento e decide revisões de perfil financeiro de pacientes. |
| **Chatbot** | Ator sistêmico. Realiza triagem, oferece suporte imediato em crises e encaminha o paciente a um psicólogo de plantão. |
| **Sistema** | Ator sistêmico. Dispara notificações automáticas (lembretes, cobranças, alertas de plantão). |

---

## 3. Requisitos Funcionais

### 3.1 Cadastro e Autenticação

| ID | Requisito | Ator |
|---|---|---|
| RF-01 | O sistema deve permitir o cadastro de psicólogos com: dados pessoais, número do CRP, especialização, upload de currículo e política de cancelamento configurada pelo profissional. | Psicólogo |
| RF-02 | O sistema deve manter o cadastro do psicólogo em estado **pendente de aprovação** até que o administrador o avalie e aprove ou reprove. | Admin |
| RF-03 | O administrador deve ser capaz de aprovar, reprovar ou solicitar complementação de cadastro de psicólogos. O profissional deve ser notificado por e-mail em qualquer um dos casos. | Admin |
| RF-04 | O sistema deve permitir o cadastro de pacientes com dados pessoais e autodeclaração de perfil socioeconômico (renda domiciliar per capita). A conta é ativada imediatamente após o cadastro. Somente pacientes enquadrados em `FAIXA_1` a `FAIXA_4` (até 2 SM per capita) são elegíveis para a terapia social. Pacientes que declarem renda acima desse limite devem ser informados da inelegibilidade e orientados a buscar atendimento particular. | Paciente |
| RF-05 | O sistema deve prover autenticação segura para psicólogos e pacientes, com sessão protegida por token e logout automático por inatividade. | Todos |
| RF-06 | O sistema deve permitir que psicólogos e pacientes editem seus perfis após o cadastro. | Todos |

### 3.2 Gestão de Agenda

| ID | Requisito | Ator |
|---|---|---|
| RF-07 | O psicólogo deve poder cadastrar horários disponíveis para atendimento (data, horário de início e fim). | Psicólogo |
| RF-08 | O psicólogo deve poder visualizar sua agenda em formato semanal e mensal. | Psicólogo |
| RF-09 | O psicólogo deve poder cancelar uma sessão agendada. O paciente deve ser notificado por e-mail. | Psicólogo |
| RF-10 | O sistema deve integrar a agenda com o **Google Calendar**, permitindo sincronização bidirecional de eventos. (Avaliar viabilidade de Calendly como alternativa.) | Sistema |

### 3.3 Plantão

| ID | Requisito | Ator |
|---|---|---|
| RF-11 | O psicólogo deve poder registrar sua disponibilidade para plantão, selecionando os dias em que está ativo. | Psicólogo |
| RF-12 | Quando o chatbot identificar um paciente em situação de crise, o sistema deve buscar psicólogos com plantão ativo no dia e notificá-los por e-mail. | Sistema |
| RF-13 | O sistema deve apresentar ao paciente os dados de contato do psicólogo de plantão notificado para que o atendimento possa ocorrer. | Sistema |

### 3.4 Prontuário Eletrônico

| ID | Requisito | Ator |
|---|---|---|
| RF-14 | O psicólogo deve poder criar anotações clínicas em **texto livre** para cada paciente sob sua responsabilidade. | Psicólogo |
| RF-15 | O paciente deve ser identificado no prontuário por um **codinome** definido pelo psicólogo — nunca pelo nome real. | Psicólogo |
| RF-16 | As anotações de prontuário devem ser visíveis e editáveis **exclusivamente** pelo psicólogo que as criou, mediante autenticação. Nenhum outro usuário, inclusive o administrador, deve ter acesso. | Sistema |
| RF-17 | O psicólogo deve poder consultar o histórico de anotações de um paciente a qualquer momento. | Psicólogo |

### 3.5 Marketplace e Terapia Social

| ID | Requisito | Ator |
|---|---|---|
| RF-18 | O paciente deve poder buscar psicólogos disponíveis para terapia social, com filtros por especialidade e disponibilidade. | Paciente |
| RF-19 | O sistema deve exibir o perfil público do psicólogo (especialidades, abordagem, disponibilidade e política de cancelamento). | Paciente |
| RF-20 | O paciente deve poder agendar uma sessão diretamente pelo marketplace, sem obrigatoriedade de passar pelo chatbot. | Paciente |
| RF-21 | O valor da sessão deve ser calculado automaticamente com base na **renda domiciliar per capita** autodeclarada pelo paciente e na **modalidade de atendimento** escolhida (avulsa ou pacote mensal). | Sistema |

**Tabela de precificação — Terapia Social**

> Referência: Salário Mínimo 2026 = **R$ 1.621,00** — alinhada com critérios oficiais do governo federal (CadÚnico, Bolsa Família, BPC/LOAS).

**Sessão avulsa:**

| Faixa | Referência oficial | Renda domiciliar per capita/mês | Valor avulso | Taxa plataforma (20%) | Psicólogo recebe |
|---|---|---|---|---|---|
| `FAIXA_1` | BPC/LOAS — extrema pobreza | Até R$ 405,25 (¼ SM) | **R$ 60,00** | R$ 12,00 | R$ 48,00 |
| `FAIXA_2` | CadÚnico / Bolsa Família | R$ 405,26 – R$ 810,50 (½ SM) | **R$ 65,00** | R$ 13,00 | R$ 52,00 |
| `FAIXA_3` | Classe E — baixa renda | R$ 810,51 – R$ 1.621,00 (1 SM) | **R$ 70,00** | R$ 14,00 | R$ 56,00 |
| `FAIXA_4` | Classe D — renda baixa-média | R$ 1.621,01 – R$ 3.242,00 (2 SM) | **R$ 75,00** | R$ 15,00 | R$ 60,00 |

**Pacote mensal (4 sessões, 5% de desconto sobre 4 × avulsa):**

| Faixa | Total/mês | Por sessão | Taxa plataforma (20% por sessão) | Psicólogo recebe/sessão |
|---|---|---|---|---|
| `FAIXA_1` | **R$ 228,00** | R$ 57,00 | R$ 11,40 | R$ 45,60 |
| `FAIXA_2` | **R$ 247,00** | R$ 61,75 | R$ 12,35 | R$ 49,40 |
| `FAIXA_3` | **R$ 266,00** | R$ 66,50 | R$ 13,30 | R$ 53,20 |
| `FAIXA_4` | **R$ 285,00** | R$ 71,25 | R$ 14,25 | R$ 57,00 |

> Pacientes com renda per capita acima de R$ 3.242,00 **não são elegíveis** para a terapia social. O sistema deve impedir o cadastro nessa condição e orientar o usuário a buscar atendimento particular.

**Regras de aplicação:**
- Renda domiciliar per capita autodeclarada no cadastro (total ÷ número de moradores).
- Valor exibido ao paciente antes da confirmação, conforme modalidade selecionada.
- Alterações de faixa afetam apenas novos agendamentos; sessões já confirmadas mantêm o valor original.
- Cálculo centralizado em `PrecificacaoService(FaixaRenda, Modalidade)` → `BigDecimal`. Para renda fora do escopo, lança `PacienteNaoElegivelException`.
- Taxa da plataforma: **20% por sessão**, aplicado pelo `CobrancaService` via `TAXA_PLATAFORMA_PERCENTUAL`. |
| RF-21a | O paciente deve poder escolher a modalidade de atendimento no ato do agendamento: **Avulsa** (sessão única) ou **Pacote Mensal** (4 sessões com 5% de desconto). | Paciente |
| RF-21b | A plataforma deve impor a **política de cancelamento**: cancelamento livre até 8h antes do atendimento. Cancelamentos com menos de 8h ficam registrados e o psicólogo decide — sem penalidade automática da plataforma — entre cobrar a sessão ou realocar para outra data. A regra se aplica igualmente a sessões avulsas e a sessões individuais dentro de um pacote. | Sistema / Psicólogo |
| RF-22 | O psicólogo deve poder solicitar revisão do perfil financeiro de um paciente quando identificar inconsistência. A plataforma avalia e decide sem prazo definido e sem suspender os atendimentos em curso. | Psicólogo / Admin |

### 3.6 Chatbot de Triagem

| ID | Requisito | Ator |
|---|---|---|
| RF-23 | O chatbot deve iniciar uma conversa de triagem com o paciente, coletando informações sobre seu estado emocional e contexto. | Chatbot |
| RF-24 | Diante de sinais de crise (ansiedade severa, risco de autolesão, etc.), o chatbot deve oferecer orientações imediatas de suporte (técnicas de respiração, ancoragem sensorial) antes de encaminhar ao profissional. | Chatbot |
| RF-25 | O chatbot deve acionar o fluxo de plantão (RF-12) quando identificar situação de urgência. | Chatbot |
| RF-26 | O chatbot **não deve emitir diagnóstico clínico** em hipótese alguma. Suas respostas devem ser restritas a suporte imediato e encaminhamento. | Chatbot |
| RF-27 | O chatbot deve ser implementado com **IA generativa** (recomendado: Google Gemini API — tier gratuito `gemini-1.5-flash` — ou Groq com LLaMA 3). | Sistema |

### 3.7 Cobranças e Pagamento (Simulado no MVP)

| ID | Requisito | Ator |
|---|---|---|
| RF-28 | Após a realização de uma sessão, o sistema deve gerar uma cobrança para o paciente com o valor calculado conforme o perfil socioeconômico. | Sistema |
| RF-29 | O sistema deve simular o fluxo de pagamento no MVP (sem gateway real). O status da cobrança deve poder ser alterado para: pendente, pago ou cancelado. | Paciente |
| RF-30 | O sistema deve aplicar **20% de taxa da plataforma** sobre o valor pago pelo paciente por sessão (avulsa ou por-sessão no pacote) e registrar o valor líquido a ser repassado ao psicólogo. | Sistema |

### 3.8 Relatório Financeiro

| ID | Requisito | Ator |
|---|---|---|
| RF-31 | O psicólogo deve ter acesso a um relatório financeiro com: sessões realizadas, valor bruto, taxa da plataforma e valor líquido por período. | Psicólogo |

### 3.9 Notificações

| ID | Requisito | Ator |
|---|---|---|
| RF-32 | O sistema deve enviar lembrete de sessão por **e-mail** para paciente e psicólogo com antecedência configurável. | Sistema |
| RF-33 | O sistema deve notificar o paciente por e-mail quando uma cobrança for gerada. | Sistema |
| RF-34 | O sistema deve notificar o psicólogo por e-mail quando um pagamento for registrado. | Sistema |
| RF-35 | O sistema deve notificar o psicólogo de plantão por e-mail quando um paciente em crise precisar de atendimento urgente. | Sistema |

> **Tecnologia recomendada para e-mail no MVP:** Resend ou SendGrid (até 100 e-mails/dia no tier gratuito de ambos).

---

## 4. Requisitos Não Funcionais

| ID | Categoria | Requisito |
|---|---|---|
| RNF-01 | Segurança | Dados de prontuário e perfil socioeconômico devem ser classificados como dados sensíveis, com criptografia em repouso e em trânsito (TLS). |
| RNF-02 | Privacidade | O sistema deve estar em conformidade com a **LGPD** no armazenamento e tratamento de dados sensíveis de saúde. |
| RNF-03 | Privacidade | O sistema deve estar em conformidade com as **resoluções do CFP** sobre sigilo clínico e prontuário eletrônico. |
| RNF-04 | Controle de Acesso | O prontuário de um paciente deve ser acessível exclusivamente pelo psicólogo que o criou, protegido por autenticação. Nenhum outro perfil de usuário tem acesso, incluindo o administrador da plataforma. |
| RNF-05 | Disponibilidade | O fluxo de triagem e encaminhamento de crise do chatbot deve ter prioridade de disponibilidade na infraestrutura. |
| RNF-06 | Plataforma | A entrega do MVP será como **aplicação web responsiva**. Aplicativo móvel nativo (iOS/Android) está fora do escopo desta versão. |
| RNF-07 | Integrações | Videochamada não é provida pela plataforma no MVP. O agendamento pode incluir um link externo (Google Meet, Zoom) fornecido pelo psicólogo. |
| RNF-08 | Custo | A stack tecnológica do MVP deve priorizar serviços gratuitos ou de baixo custo. |

---

## 5. Casos de Uso

### UC-01 — Cadastrar Psicólogo

| Campo | Descrição |
|---|---|
| **Ator principal** | Psicólogo |
| **Pré-condição** | Nenhuma |
| **Fluxo principal** | 1. Psicólogo acessa a plataforma e seleciona "Cadastrar como psicólogo". 2. Preenche dados pessoais, número do CRP e especialização. 3. Faz upload do currículo. 4. Configura e descreve sua política de cancelamento. 5. Submete o cadastro. 6. Sistema registra o perfil com status **pendente de aprovação** e notifica o psicólogo por e-mail. |
| **Fluxo alternativo** | Dados obrigatórios ausentes: sistema exibe mensagem de erro e impede a submissão. |
| **Pós-condição** | Cadastro registrado e aguardando avaliação do administrador. |

---

### UC-02 — Avaliar Cadastro de Psicólogo

| Campo | Descrição |
|---|---|
| **Ator principal** | Administrador |
| **Pré-condição** | Existe ao menos um cadastro de psicólogo com status pendente. |
| **Fluxo principal** | 1. Administrador acessa painel de avaliações pendentes. 2. Visualiza currículo, dados do CRP, especialização e política de cancelamento. 3. Aprova o cadastro. 4. Sistema ativa o perfil e notifica o psicólogo por e-mail. |
| **Fluxo alternativo A** | Administrador reprova: sistema arquiva o cadastro e notifica o psicólogo com o motivo. |
| **Fluxo alternativo B** | Administrador solicita complementação: sistema notifica o psicólogo, que pode editar e reenviar. |
| **Pós-condição** | Psicólogo aprovado com acesso ativo à plataforma, ou cadastro reprovado/suspenso para correção. |

---

### UC-03 — Cadastrar Paciente

| Campo | Descrição |
|---|---|
| **Ator principal** | Paciente |
| **Pré-condição** | Nenhuma |
| **Fluxo principal** | 1. Paciente acessa a plataforma e seleciona "Cadastrar como paciente". 2. Preenche dados pessoais. 3. Preenche autodeclaração de perfil socioeconômico (faixa de renda). 4. Confirma cadastro. 5. Sistema ativa a conta imediatamente. |
| **Pós-condição** | Conta de paciente ativa. Valor das sessões calculado com base no perfil declarado. |

---

### UC-04 — Gerenciar Agenda

| Campo | Descrição |
|---|---|
| **Ator principal** | Psicólogo |
| **Pré-condição** | Psicólogo com cadastro aprovado. |
| **Fluxo principal** | 1. Psicólogo acessa sua agenda. 2. Seleciona uma data e cadastra horários disponíveis para atendimento. 3. Sistema sincroniza com o Google Calendar. 4. Psicólogo pode visualizar agenda em formato semanal ou mensal. |
| **Fluxo alternativo** | Psicólogo cancela sessão agendada: sistema registra o cancelamento e envia notificação por e-mail ao paciente. |
| **Pós-condição** | Disponibilidade atualizada e visível para pacientes no marketplace. |

---

### UC-05 — Registrar Disponibilidade de Plantão

| Campo | Descrição |
|---|---|
| **Ator principal** | Psicólogo |
| **Pré-condição** | Psicólogo com cadastro aprovado. |
| **Fluxo principal** | 1. Psicólogo acessa configurações de plantão. 2. Seleciona os dias em que estará disponível para atendimento urgente. 3. Sistema registra a disponibilidade. |
| **Pós-condição** | Psicólogo elegível para ser notificado quando um paciente em crise precisar de atendimento no dia marcado. |

---

### UC-06 — Buscar e Agendar Psicólogo

| Campo | Descrição |
|---|---|
| **Ator principal** | Paciente |
| **Pré-condição** | Paciente com conta ativa. |
| **Fluxo principal** | 1. Paciente acessa o marketplace. 2. Aplica filtros (especialidade, disponibilidade). 3. Visualiza perfil público do psicólogo (abordagem, especialidades, política de cancelamento). 4. Seleciona horário disponível e confirma o agendamento. 5. Sistema registra a sessão e envia confirmação por e-mail para paciente e psicólogo. |
| **Pós-condição** | Sessão agendada. Cobrança gerada com valor calculado pelo perfil socioeconômico do paciente. |

---

### UC-07 — Triagem via Chatbot

| Campo | Descrição |
|---|---|
| **Ator principal** | Paciente |
| **Ator secundário** | Chatbot, Sistema |
| **Pré-condição** | Nenhuma (acesso público). |
| **Fluxo principal** | 1. Paciente inicia conversa com o chatbot. 2. Chatbot realiza triagem com perguntas sobre estado emocional e contexto. 3. Chatbot identifica necessidade de suporte não urgente e encaminha o paciente para o marketplace, sugerindo psicólogos compatíveis. |
| **Fluxo alternativo — Crise** | 3a. Chatbot identifica sinais de crise. 4a. Chatbot oferece orientações imediatas (respiração, ancoragem sensorial). 5a. Sistema busca psicólogos com plantão ativo no dia e os notifica por e-mail. 6a. Chatbot apresenta ao paciente os dados de contato do profissional. |
| **Regra de negócio** | O chatbot não deve, em nenhuma circunstância, emitir diagnóstico clínico ou sugerir medicação. |
| **Pós-condição** | Paciente encaminhado a um psicólogo (via marketplace ou plantão). |

---

### UC-08 — Gerenciar Prontuário Eletrônico

| Campo | Descrição |
|---|---|
| **Ator principal** | Psicólogo |
| **Pré-condição** | Psicólogo autenticado com ao menos um paciente vinculado. |
| **Fluxo principal** | 1. Psicólogo acessa o prontuário pelo painel de sessões. 2. Seleciona o paciente (identificado pelo codinome). 3. Cria ou edita anotações em campo de texto livre. 4. Sistema salva as anotações com criptografia. |
| **Regra de negócio** | O codinome é definido pelo psicólogo; o nome real do paciente não pode aparecer nas anotações. O acesso exige autenticação ativa. |
| **Pós-condição** | Anotações salvas e acessíveis apenas pelo psicólogo autor. |

---

### UC-09 — Solicitar Revisão de Perfil Financeiro

| Campo | Descrição |
|---|---|
| **Ator principal** | Psicólogo |
| **Ator secundário** | Administrador |
| **Pré-condição** | Psicólogo com sessões em andamento com o paciente em questão. |
| **Fluxo principal** | 1. Psicólogo identifica inconsistência no perfil socioeconômico do paciente. 2. Abre solicitação de revisão via painel, descrevendo a suspeita. 3. Sistema registra a solicitação e notifica o administrador. 4. Administrador avalia e toma uma decisão (manter ou atualizar o perfil do paciente). 5. Psicólogo é notificado do resultado por e-mail. |
| **Regra de negócio** | O atendimento não é suspenso durante a revisão. Não há prazo definido para a decisão. |
| **Pós-condição** | Perfil financeiro do paciente mantido ou atualizado. |

---

### UC-10 — Realizar Pagamento (Simulado)

| Campo | Descrição |
|---|---|
| **Ator principal** | Paciente |
| **Pré-condição** | Sessão realizada e cobrança gerada pelo sistema. |
| **Fluxo principal** | 1. Paciente acessa área financeira e visualiza cobranças pendentes. 2. Seleciona a cobrança e confirma o pagamento (simulado). 3. Sistema registra o status como **pago**, aplica a taxa da plataforma e calcula o valor líquido para o psicólogo. 4. Psicólogo é notificado por e-mail. |
| **Pós-condição** | Cobrança marcada como paga. Valor líquido registrado no relatório financeiro do psicólogo. |

---

### UC-11 — Visualizar Relatório Financeiro

| Campo | Descrição |
|---|---|
| **Ator principal** | Psicólogo |
| **Pré-condição** | Psicólogo com ao menos uma sessão realizada e paga. |
| **Fluxo principal** | 1. Psicólogo acessa o painel financeiro. 2. Seleciona período de referência. 3. Sistema exibe: sessões realizadas, valor bruto total, taxa da plataforma e valor líquido a receber. |
| **Pós-condição** | Nenhuma (consulta apenas). |

---

## 6. Restrições e Premissas

| Tipo | Descrição |
|---|---|
| **Restrição** | O chatbot não substitui diagnóstico ou acompanhamento clínico profissional. |
| **Restrição** | O armazenamento de prontuários deve estar em conformidade com a LGPD e com as resoluções do CFP sobre sigilo e prontuário eletrônico. |
| **Restrição** | Não haverá videochamada própria no MVP. |
| **Restrição** | Não haverá aplicativo móvel nativo no MVP. |
| **Restrição** | Não haverá integração com planos de saúde, convênios ou emissão de notas fiscais no MVP. |
| **Premissa** | Os psicólogos cadastrados possuem registro ativo no CRP. |
| **Premissa** | Os pacientes aceitam compartilhar o perfil socioeconômico de forma autodeclarada. |
| **Premissa** | Existe demanda latente de psicólogos dispostos a oferecer atendimentos sociais em troca das ferramentas de gestão. |

---

## 7. Pontos em Aberto

| Item | Detalhe |
|---|---|
| ~~Faixas de precificação por perfil socioeconômico~~ | ✅ Decidido — ver tabela em RF-21. Avulsa R$60–R$75; pacote mensal R$228–R$285. |
| ~~Percentual retido pela plataforma por sessão~~ | ✅ Decidido — 20% por sessão sobre o valor pago pelo paciente. Ver RF-30. |
| ~~Regras da política de cancelamento~~ | ✅ Decidido — 8h de antecedência; psicólogo decide em cancelamentos de última hora. Ver RF-21b. |
| ~~Modalidades de atendimento~~ | ✅ Decidido — Avulsa e Pacote Mensal (4 sessões, 5% desconto). Ver RF-21a. |
| Gateway de pagamento | A definir para versão pós-MVP. |
| Critérios formais de aprovação/reprovação de psicólogos | A documentar em processo interno do time de administração. |
| Integração de agenda | Google Calendar API — definido na arquitetura. |
