# 📋 Ata de Reunião — Alinhamento do Sistema (Sprints 0 a 4)

**Data:** 07/07/2026
**Pauta:** Apresentação ao stakeholder do que foi entregue até a Sprint 4 + alinhamento de ajustes
**Status da reunião:** 🎉 Sucesso — sistema aprovado, com ajustes de refinamento

---

## 🚀 Foi um espetáculo!

Apresentamos ao vivo o fluxo completo da plataforma — do cadastro do paciente até o chatbot
identificando uma situação de crise e acionando um psicólogo de plantão em tempo real — e a
reunião foi muito bem recebida. O produto já conta a história completa que o projeto se propõe a
contar: **conectar terapia social acessível a quem precisa, com tecnologia séria por trás.**

Time, um aplauso pra gente. 👏 Agora é lapidar.

---

## ✅ O que já está rodando (Sprints 0 a 4)

- **Cadastro e aprovação** — psicólogos se cadastram com CRP e currículo, admin aprova; pacientes
  se cadastram com faixa de renda e entram na hora
- **Agenda e plantão** — psicólogo cadastra horários, sincroniza com Google Calendar, registra
  dias de plantão de urgência
- **Marketplace** — paciente busca psicólogo, vê o valor calculado pela própria faixa de renda,
  agenda e paga (simulado)
- **Prontuário eletrônico** — anotações clínicas criptografadas, paciente identificado só por
  codinome, nunca pelo nome real
- **Chatbot de triagem** — acolhe, nunca dá diagnóstico, identifica crise e aciona plantão
  automaticamente, com fallback seguro mesmo se a IA cair

---

## 🔧 Ajustes solicitados

### `/agendamentos`
- [ ] O valor do pacote mensal está confuso. Trocar por: **valor da sessão avulsa** + **valor
      total do pacote**, destacando quanto o paciente economiza escolhendo o pacote.

### `/perfil-paciente`
- [ ] Permitir **adicionar e editar foto de perfil**
- [ ] **Faixa de renda deixa de ser editável pelo paciente.** É definida uma única vez no
      cadastro — só o profissional pode reavaliar a situação financeira ao longo do
      acompanhamento (fluxo de revisão, já previsto no backlog)
- [ ] Adicionar campo de **idade**
- [ ] Adicionar **formulário de anamnese básica** (já fez terapia antes, motivo de buscar
      terapia agora, se toma medicação controlada, etc.) — isso garante que o psicólogo chegue
      na primeira sessão já sabendo o contexto do paciente, em vez de "perdido". Se o paciente
      for menor de idade, a primeira sessão precisa ser com o responsável — o formulário também
      serve pra sinalizar isso com antecedência.

### `/chatbot`
- [ ] Manter e reforçar o campo de **contato (e-mail/telefone) em caso de crise**: se a IA ou o
      sistema estiver instável, o paciente ainda consegue deixar um contato, e o sistema deve
      procurar profissionais disponíveis — **tanto quem está de plantão quanto quem tem
      disponibilidade mais próxima na agenda** — e retornar esse contato ao paciente.
- [ ] Na mensagem de instabilidade técnica, linkar o **CVV (188)** para
      [https://cvv.org.br/chat/](https://cvv.org.br/chat/) (chat) e também um link direto de
      ligação (`tel:188`).

### `/marketplace`
- [ ] O campo "Especialidade" está com o nome errado — especialidade é a formação/abordagem do
      psicólogo (ex.: "TCC e Esquemas"), que já aparece no card. O campo de busca é sobre
      **temas/situações que o psicólogo atende** (ansiedade, luto, terapia de casal...), pensado
      pra aparecer como tags abaixo do nome e da especialização.
      **Sugestão de nome: "Áreas de atuação"** (alternativas: "Temas atendidos", "Focos de
      atendimento") — a confirmar com o time.

---

## ✨ Novas funcionalidades e regras de negócio

- [ ] **Chat interno** entre psicólogo e paciente, liberado **depois que a sessão é agendada E
      paga**.
- [ ] **Terapia de casal custa o dobro** do valor da sessão convencional (individual) — se
      aplica em cima da tabela de faixas já existente.

---

## 🎯 Tarefas do time

| Quem | O quê |
|---|---|
| **Awana** | Rebranding do projeto + planejamento de conteúdo para o Instagram |
| **Victor** | Definir os valores da terapia de casal + definir as perguntas do formulário de anamnese |

---

## 📎 Adendo — anamnese e responsável (clarificação pós-reunião)

Duas regras importantes que ficaram mais claras depois da reunião:

1. **A anamnese é sempre do paciente, e o acesso do profissional é temporário.** Não é um dado
   público nem permanente pro psicólogo — ele só consegue ler as respostas **entre o momento em
   que o paciente agenda e paga a primeira sessão, e o momento em que essa sessão acontece.**
   Depois disso, o acesso é encerrado. A ideia é o psicólogo ler uma vez, se preparar, fazer as
   próprias anotações (essas sim ficam com ele, no prontuário) e planejar a primeira sessão já
   adaptado ao contexto — sem guardar o conteúdo bruto da anamnese indefinidamente.
   Em `/perfil-paciente`, mostrar um aviso nesse sentido:
   > *"Preencha seu perfil com sua anamnese. Essa informação não será pública — o profissional só
   > tem acesso quando você agendar e efetuar o pagamento, antes da primeira terapia. Depois
   > disso, ele não terá mais acesso."*

2. **O campo de contato do responsável só aparece se o paciente for menor de idade** — não é um
   campo genérico pra todo mundo. E não pode aparecer "seco": precisa vir com uma explicação de
   por que estamos pedindo isso, alinhada com a prática da psicologia (atendimento a menores exige
   presença/consentimento do responsável, especialmente na primeira sessão).

---

## 📌 Próximos passos

Todos os itens acima já foram registrados no backlog e na documentação técnica do projeto
(`docs/Backlog-UniPsi.md`, `docs/Doc-Requisitos-UniPsi.md`, `docs/ER-UniPsi.md`,
`docs/Sprints-UniPsi.md` e `CLAUDE.md`), prontos para entrar em desenvolvimento. Chat interno
depende do módulo financeiro (pagamento), então entra junto com essa frente.

Bora pra próxima. 🚀
