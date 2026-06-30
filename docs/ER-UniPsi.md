# Diagrama Entidade-Relacionamento — Universo Psicólogo

**Versão:** 1.0  
**Data:** 30/06/2026  
**Referência:** Arquitetura-UniPsi.md — Seção 6 (Modelo de Dados)

---

```mermaid
erDiagram

    USUARIO {
        uuid    id          PK
        string  nome
        string  email
        string  senha_hash
        enum    role        "PSICOLOGO | PACIENTE | ADMIN"
        boolean ativo
        timestamp criado_em
    }

    PSICOLOGO {
        uuid    id                  PK
        uuid    usuario_id          FK
        string  crp
        string  estado_crp
        array   especializacoes
        text    politica_cancelamento
        string  link_videochamada
        enum    status_aprovacao    "PENDENTE | APROVADO | REPROVADO | AGUARDANDO_COMPLEMENTACAO"
        string  curriculo_path      "path no MinIO"
    }

    PACIENTE {
        uuid    id              PK
        uuid    usuario_id      FK
        date    data_nascimento
        string  cidade
        string  estado
        enum    faixa_renda     "FAIXA_1 | FAIXA_2 | FAIXA_3 | FAIXA_4"
    }

    SLOT {
        uuid      id              PK
        uuid      psicologo_id    FK
        timestamp inicio
        timestamp fim
        boolean   disponivel
        string    google_event_id "nullable"
    }

    SESSAO {
        uuid      id              PK
        uuid      slot_id         FK
        uuid      paciente_id     FK
        uuid      psicologo_id    FK
        decimal   valor_sessao
        decimal   taxa_plataforma
        decimal   valor_liquido
        enum      status          "AGENDADA | REALIZADA | CANCELADA"
        timestamp criada_em
    }

    DISPONIBILIDADE_PLANTAO {
        uuid    id               PK
        uuid    psicologo_id     FK
        enum    dia_semana       "SEG | TER | QUA | QUI | SEX | SAB | DOM"
        date    data_especifica  "nullable - para datas avulsas"
        boolean ativo
    }

    PRONTUARIO {
        uuid      id           PK
        uuid      psicologo_id FK
        uuid      paciente_id  FK  "nullable - psicólogo pode usar só codinome"
        string    codinome
        timestamp criado_em
    }

    ANOTACAO {
        uuid      id            PK
        uuid      prontuario_id FK
        text      conteudo_enc  "cifrado com AES-256-GCM"
        bytes     iv            "vetor de inicialização"
        timestamp criada_em
    }

    COBRANCA {
        uuid      id           PK
        uuid      sessao_id    FK
        uuid      paciente_id  FK
        decimal   valor
        enum      status       "PENDENTE | PAGO | CANCELADO"
        timestamp pago_em      "nullable"
        timestamp criada_em
    }

    REVISAO_PERFIL {
        uuid      id             PK
        uuid      paciente_id    FK
        uuid      psicologo_id   FK
        text      justificativa
        enum      status         "ABERTA | DECIDIDA"
        text      decisao_admin  "nullable"
        timestamp criada_em
    }

    AUDITORIA_PRONTUARIO {
        uuid      id            PK
        uuid      prontuario_id FK
        uuid      psicologo_id  FK
        enum      acao          "LEITURA | ESCRITA | EDICAO"
        timestamp criado_em
    }

    %% ─── Herança de usuário ───────────────────────────────────────
    USUARIO     ||--o|    PSICOLOGO             : "tem perfil"
    USUARIO     ||--o|    PACIENTE              : "tem perfil"

    %% ─── Agenda e sessões ─────────────────────────────────────────
    PSICOLOGO   ||--o{    SLOT                  : "disponibiliza"
    SLOT        ||--o|    SESSAO                : "origina"
    PACIENTE    ||--o{    SESSAO                : "agenda"
    PSICOLOGO   ||--o{    SESSAO                : "realiza"

    %% ─── Plantão ──────────────────────────────────────────────────
    PSICOLOGO   ||--o{    DISPONIBILIDADE_PLANTAO : "registra"

    %% ─── Prontuário e anotações ───────────────────────────────────
    PSICOLOGO   ||--o{    PRONTUARIO            : "mantém"
    PACIENTE    ||--o{    PRONTUARIO            : "referenciado em"
    PRONTUARIO  ||--|{    ANOTACAO              : "contém"
    PRONTUARIO  ||--o{    AUDITORIA_PRONTUARIO  : "registra acesso"

    %% ─── Financeiro ───────────────────────────────────────────────
    SESSAO      ||--||    COBRANCA              : "gera"
    PACIENTE    ||--o{    COBRANCA              : "recebe"

    %% ─── Revisão de perfil ────────────────────────────────────────
    PSICOLOGO   ||--o{    REVISAO_PERFIL        : "solicita"
    PACIENTE    ||--o{    REVISAO_PERFIL        : "é avaliado em"
```

---

## Legenda de cardinalidades

| Notação | Significado |
|---|---|
| `\|\|--\|\|` | Um para um (obrigatório dos dois lados) |
| `\|\|--o\|` | Um para zero ou um |
| `\|\|--o{` | Um para zero ou muitos |
| `\|\|--\|{` | Um para um ou muitos (obrigatório no lado N) |

---

## Notas do modelo

| Entidade | Observação |
|---|---|
| `USUARIO` | Tabela base para todos os perfis. O campo `role` determina qual tabela de perfil é consultada em seguida. |
| `PSICOLOGO` / `PACIENTE` | Herança por tabela associada — cada entidade tem sua própria tabela com FK para `USUARIO`. |
| `SLOT` | Representa um horário criado pelo psicólogo. Torna-se indisponível ao ser vinculado a uma `SESSAO`. |
| `SESSAO` | Guarda `psicologo_id` além do `slot_id` para facilitar consultas financeiras e de agenda sem join adicional. |
| `PRONTUARIO` | `paciente_id` é nullable — o psicólogo pode optar por não vincular o cadastro da plataforma, usando apenas o codinome. |
| `ANOTACAO` | `conteudo_enc` nunca trafega em texto claro fora do `CriptografiaService`. O `iv` é único por anotação. |
| `AUDITORIA_PRONTUARIO` | Registra todo acesso ao prontuário (leitura, escrita, edição) para conformidade com CFP e LGPD. |
| `REVISAO_PERFIL` | Iniciada pelo psicólogo, decidida pelo admin. Não bloqueia atendimentos em curso. |
| `COBRANCA` | Gerada automaticamente ao marcar uma `SESSAO` como `REALIZADA`. Fluxo de pagamento simulado no MVP. |
