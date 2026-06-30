# Plano de Cobertura de Testes — Universo Psicólogo

**Versão:** 1.0  
**Data:** 30/06/2026  
**Referência:** Arquitetura-UniPsi.md, Backlog-UniPsi.md, Sprints-UniPsi.md

---

## Sumário

1. [Estratégia e Pirâmide de Testes](#1-estratégia-e-pirâmide-de-testes)
2. [Ferramentas](#2-ferramentas)
3. [Metas de Cobertura](#3-metas-de-cobertura)
4. [Testes de Backend — Por Módulo](#4-testes-de-backend--por-módulo)
5. [Testes de Frontend](#5-testes-de-frontend)
6. [Testes E2E — Fluxos Críticos](#6-testes-e2e--fluxos-críticos)
7. [Testes de Segurança e LGPD](#7-testes-de-segurança-e-lgpd)
8. [Organização dos Arquivos](#8-organização-dos-arquivos)
9. [Integração com CI](#9-integração-com-ci)

---

## 1. Estratégia e Pirâmide de Testes

```
          /\
         /  \
        / E2E \         ← Poucos, lentos, cobrem fluxos completos
       /--------\
      / Integração\     ← Controllers + Repository + banco real (H2/Testcontainers)
     /------------\
    /   Unitários   \   ← Muitos, rápidos, cobrem lógica de negócio isolada
   /________________\
```

| Camada | Foco | Quantidade esperada | Velocidade |
|---|---|---|---|
| **Unitários** | Services, regras de negócio, cálculos, criptografia | ~70% dos testes | Milissegundos |
| **Integração** | Controllers + banco + Redis (sem mocks) | ~25% dos testes | Segundos |
| **E2E** | Fluxos completos happy path + crise | ~5% dos testes | Dezenas de segundos |

**Princípios:**
- Módulo `prontuario` tem cobertura prioritária por ser dado sensível de saúde (LGPD).
- `PrecificacaoService` e `CriseDetectorService` devem ter 100% de cobertura de branches.
- Nunca mockar o banco nos testes de integração — usar banco real (H2 em memória ou Testcontainers com PostgreSQL).
- Nunca testar o que o framework já testa (ex.: validação de anotações `@NotNull` do Spring).

---

## 2. Ferramentas

### Backend

| Ferramenta | Uso |
|---|---|
| **JUnit 5** | Framework de testes unitários e de integração |
| **Mockito** | Mock de dependências em testes unitários |
| **Spring Boot Test** (`@SpringBootTest`) | Testes de integração com contexto completo |
| **MockMvc** | Testes de controllers sem servidor real |
| **H2** (in-memory) | Banco de dados para testes de integração rápidos |
| **Testcontainers** (opcional) | PostgreSQL real para testes de integração críticos (prontuário, criptografia) |
| **AssertJ** | Asserções fluentes e legíveis |
| **JaCoCo** | Relatório de cobertura de código |

### Frontend

| Ferramenta | Uso |
|---|---|
| **Vitest** | Runner de testes (integrado ao Vite, rápido) |
| **React Testing Library** | Testes de componentes orientados ao comportamento do usuário |
| **MSW (Mock Service Worker)** | Mock de requisições HTTP nos testes de componentes |
| **Playwright** | Testes E2E no browser real |

---

## 3. Metas de Cobertura

### Backend

| Módulo | Meta de cobertura (linhas) | Justificativa |
|---|---|---|
| `prontuario` | **100%** | Dado sensível de saúde; criptografia não pode ter caminho não testado |
| `auth` | **95%** | Segurança; JWT, refresh token e BCrypt são críticos |
| `financeiro` | **90%** | Cálculos monetários; erros geram impacto financeiro direto |
| `marketplace` / `PrecificacaoService` | **100% de branches** | Cada faixa e a exception de inelegibilidade devem ser exercitadas |
| `chatbot` / `CriseDetectorService` | **100% de branches** | Classificação incorreta pode colocar paciente em risco |
| `agenda` | **85%** | Conflito de slots e integração Google Calendar |
| `plantao` | **85%** | Lógica de busca de plantão ativo no dia |
| `usuario` | **85%** | Validações de elegibilidade do paciente |
| `notificacao` | **80%** | Mock do ResendClient; testar disparo e templates |
| `admin` | **80%** | Fluxo de aprovação/reprovação |

### Frontend

| Tipo | Meta |
|---|---|
| Componentes críticos (formulários de cadastro, chatbot, prontuário) | 80% de branches |
| Páginas de dashboard | 70% |
| Serviços de API (`services/`) | 90% |

---

## 4. Testes de Backend — Por Módulo

### 4.1 Módulo `auth`

**Classes a testar:** `JwtService`, `AuthService`, `RefreshTokenService`, `JwtAuthFilter`

```
JwtServiceTest
  ✓ gerarToken_deveConterSubjectERoleNoPayload
  ✓ validarToken_tokenValido_deveRetornarTrue
  ✓ validarToken_tokenExpirado_deveRetornarFalse
  ✓ validarToken_assinaturaInvalida_deveRetornarFalse
  ✓ extrairRole_deveRetornarRoleCorreta

AuthServiceTest
  ✓ registrarPsicologo_dadosValidos_deveSalvarComStatusPendente
  ✓ registrarPsicologo_emailDuplicado_deveLancarException
  ✓ registrarPaciente_faixaElegivel_deveSalvarEAtivar
  ✓ registrarPaciente_faixaInelegivel_deveLancarPacienteNaoElegivelException
  ✓ login_credenciaisValidas_deveRetornarTokens
  ✓ login_senhaIncorreta_deveLancarException
  ✓ login_psicologoPendente_deveLancarException

RefreshTokenServiceTest
  ✓ salvarRefreshToken_devePersistirNoRedisComTTL
  ✓ validarRefreshToken_tokenExistente_deveRetornarTrue
  ✓ validarRefreshToken_tokenInexistente_deveRetornarFalse
  ✓ rotacionarRefreshToken_deveInvalidarAntigoEEmitirNovo
```

**Teste de integração:**
```
AuthControllerIntegrationTest  (@SpringBootTest + MockMvc)
  ✓ POST /api/auth/register/psicologo → 201 + status PENDENTE
  ✓ POST /api/auth/register/paciente (FAIXA_1) → 201 + conta ativa
  ✓ POST /api/auth/register/paciente (renda inelegível) → 422
  ✓ POST /api/auth/login → 200 + access_token + refresh_token
  ✓ POST /api/auth/refresh → 200 + novos tokens
  ✓ POST /api/auth/login (senha errada) → 401
```

---

### 4.2 Módulo `prontuario` ⭐ Prioritário

**Classes a testar:** `CriptografiaService`, `ProntuarioService`, `AuditoriaProntuarioService`

```
CriptografiaServiceTest
  ✓ encrypt_textoSimples_deveRetornarConteudoCifradoEIv
  ✓ decrypt_dadosCifradosValidos_deveRetornarTextoOriginal
  ✓ encrypt_textosDiferentes_devemGerarIvsDiferentes
  ✓ encrypt_mesmoTexto_duasVezes_devemGerarCifradosDiferentes (IV aleatório)
  ✓ decrypt_ivInvalido_deveLancarException
  ✓ decrypt_conteudoCorrompido_deveLancarException
  ✓ encrypt_textoVazio_deveLancarException

ProntuarioServiceTest
  ✓ criarProntuario_codinomeUnicoPorPsicologo_deveSalvar
  ✓ criarProntuario_codinomeDuplicadoPorMesmoPsicologo_deveLancarException
  ✓ criarProntuario_codinomeDuplicadoPorPsicologoDiferente_devePermitir
  ✓ criarAnotacao_deveArmazenarConteudoCifrado
  ✓ buscarAnotacoes_deveDecifrarConteudoAntesDeRetornar
  ✓ buscarAnotacoes_psicologoNaoAutor_deveLancarException (acesso negado)
  ✓ buscarAnotacoes_deveRegistrarAuditoria

AuditoriaProntuarioServiceTest
  ✓ registrarAcesso_leitura_devePersistirComTimestamp
  ✓ registrarAcesso_escrita_devePersistirComTimestamp
```

**Teste de integração:**
```
ProntuarioControllerIntegrationTest
  ✓ GET /api/prontuario/{codinome}/anotacoes (psicólogo autor) → 200 + conteúdo decifrado
  ✓ GET /api/prontuario/{codinome}/anotacoes (outro psicólogo) → 403
  ✓ GET /api/prontuario/{codinome}/anotacoes (admin) → 403
  ✓ GET /api/prontuario/{codinome}/anotacoes (sem token) → 401
  ✓ POST /api/prontuario/{codinome}/anotacoes → 201 + conteúdo cifrado no banco (verificar diretamente)
  ✓ Confirmar que nome real do paciente NÃO aparece em nenhuma resposta
```

---

### 4.3 Módulo `marketplace` — `PrecificacaoService` ⭐ 100% de branches

```
PrecificacaoServiceTest
  ✓ calcularValor_FAIXA_1_deveRetornar30reais
  ✓ calcularValor_FAIXA_2_deveRetornar45reais
  ✓ calcularValor_FAIXA_3_deveRetornar65reais
  ✓ calcularValor_FAIXA_4_deveRetornar80reais
  ✓ calcularValor_faixaNula_deveLancarPacienteNaoElegivelException
  ✓ calcularValor_resultadoNuncaDeveSerMenorQue30
  ✓ calcularValor_resultadoNuncaDeveSerMaiorQue80
```

---

### 4.4 Módulo `chatbot` — `CriseDetectorService` ⭐ 100% de branches

```
CriseDetectorServiceTest
  ✓ classificar_respostaComIndicativoDeAutolesao_deveRetornarCRISE
  ✓ classificar_respostaComPanicoSevero_deveRetornarCRISE
  ✓ classificar_respostaComAnsiedadeAlta_deveRetornarCRISE
  ✓ classificar_respostaComDesabafoComum_deveRetornarNORMAL
  ✓ classificar_respostaVazia_deveRetornarNORMAL
  ✓ classificar_respostaNula_deveLancarException

ChatbotServiceTest  (com mock do GeminiClient)
  ✓ processarMensagem_statusNormal_deveRetornarRespostaSemAcionarPlantao
  ✓ processarMensagem_statusCrise_deveAcionarPlantaoERetornarContato
  ✓ processarMensagem_criseSemPlantaoAtivo_deveRetornarContatosEmergencia
  ✓ processarMensagem_deveAtualizarHistoricoNoRedis
  ✓ processarMensagem_historicoExpirado_deveIniciarNovaConversa
  ✓ respostaChatbot_nuncaDeveConterPalavrasDiagnostico (smoke test no system prompt)
```

---

### 4.5 Módulo `agenda`

```
AgendaServiceTest
  ✓ criarSlot_horarioLivre_deveSalvar
  ✓ criarSlot_conflito_deveLancarException
  ✓ agendarSessao_slotDisponivel_deveCriarSessaoEMarcarSlotIndisponivel
  ✓ agendarSessao_slotOcupado_deveLancarException
  ✓ cancelarSessao_deveLibertarSlot
  ✓ cancelarSessao_deveNotificarPaciente (verificar chamada ao EmailService)
  ✓ buscarSlotsPorPsicologo_deveRetornarApenasDisponíveis
```

---

### 4.6 Módulo `plantao`

```
PlantaoServiceTest
  ✓ buscarPsicologosAtivos_diaComPlantao_deveRetornarLista
  ✓ buscarPsicologosAtivos_diaSemPlantao_deveRetornarListaVazia
  ✓ buscarPsicologosAtivos_plantaoDesativado_naoDeveIncluirNaLista
  ✓ registrarDisponibilidade_diaSemana_deveSalvar
  ✓ registrarDisponibilidade_dataEspecifica_deveSalvar
  ✓ alternarStatus_ativo_deveDesativar
  ✓ alternarStatus_inativo_deveAtivar
```

---

### 4.7 Módulo `financeiro`

```
CobrancaServiceTest
  ✓ gerarCobranca_sessaoRealizada_deveCriarComStatusPendente
  ✓ gerarCobranca_deveAplicarValorDaSessao
  ✓ confirmarPagamento_deveAplicarTaxaECalcularLiquido
  ✓ confirmarPagamento_deveAlterarStatusParaPago
  ✓ confirmarPagamento_deveNotificarPsicologo
  ✓ cancelarCobranca_deveAlterarStatusParaCancelado
  ✓ cancelarCobranca_deveNotificarPaciente
  ✓ gerarRelatorio_deveFiltrarApenasSessoesComStatusPago
  ✓ gerarRelatorio_deveCalcularTotaisCorretos
  ✓ gerarCobranca_sessaoJaCobrada_deveLancarException (idempotência)
```

---

### 4.8 Módulo `usuario` — Elegibilidade do Paciente

```
PacienteServiceTest
  ✓ registrar_FAIXA_1_deveSalvarComFaixaCorreta
  ✓ registrar_FAIXA_2_deveSalvarComFaixaCorreta
  ✓ registrar_FAIXA_3_deveSalvarComFaixaCorreta
  ✓ registrar_FAIXA_4_deveSalvarComFaixaCorreta
  ✓ registrar_faixaInelegivel_deveLancarPacienteNaoElegivelException
  ✓ atualizarFaixa_novaSessoesSaoRecalculadas
  ✓ atualizarFaixa_sessoesAntigasMantemValorOriginal
```

---

### 4.9 Módulo `admin`

```
AprovacaoServiceTest
  ✓ aprovarPsicologo_pendente_deveAlterarStatusParaAprovado
  ✓ aprovarPsicologo_deveNotificarPorEmail
  ✓ reprovarPsicologo_deveAlterarStatusParaReprovado
  ✓ reprovarPsicologo_deveSalvarMotivo
  ✓ solicitarComplementacao_deveAlterarStatusParaAguardandoComplementacao
  ✓ aprovarPsicologo_jaAprovado_deveLancarException

RevisaoPerfilServiceTest
  ✓ abrirRevisao_deveSalvarComStatusAberta
  ✓ abrirRevisao_deveNotificarAdmin
  ✓ decidirRevisao_manter_naoDeveAlterarFaixa
  ✓ decidirRevisao_atualizar_deveAlterarFaixaDoPaciente
  ✓ decidirRevisao_deveNotificarPsicologoEPaciente
  ✓ decidirRevisao_atendimentosEmCursoNaoDevemSerAfetados
```

---

## 5. Testes de Frontend

### 5.1 Ferramentas e configuração

```javascript
// vite.config.js — adicionar configuração de teste
export default defineConfig({
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.js'],
    coverage: {
      reporter: ['text', 'html'],
      exclude: ['src/test/**', 'src/main.jsx']
    }
  }
})
```

### 5.2 Serviços de API (`services/`)

```javascript
// authService.test.js
✓ login_credenciaisValidas_deveRetornarTokens
✓ login_erro401_deveLancarExceptionComMensagem
✓ register_dadosValidos_deveChamarEndpointCorreto

// precificacaoService (lógica client-side de exibição)
✓ formatarValorSessao_FAIXA_1_deveExibir30reais
✓ formatarValorSessao_FAIXA_4_deveExibir80reais
```

### 5.3 Componentes críticos

**`RegisterPacientePage`**
```javascript
✓ renderiza_4opcoesDeFaixa_semOpcaoInelegivel
✓ submit_faixaValida_chamaCadastroComFaixaCorreta
✓ exibe_mensagemDeInelegibilidade_quandoRendaAcimaClasseD
✓ campos_obrigatorios_impedemSubmit
```

**`RegisterPsicologoPage`**
```javascript
✓ upload_curriculo_aceitaPDFeDocx
✓ upload_curriculo_rejeitaArquivoAcima5MB
✓ submit_semCurriculo_impedeSubmit
✓ submit_valido_exibeConfirmacaoPendente
```

**`ChatWindow` (Chatbot)**
```javascript
✓ renderiza_mensagemInicial_semLogin
✓ envia_mensagem_exibeRespostaDoBotNaTela
✓ exibe_indicadorDeCarregamento_duranteRequisicao
✓ exibe_contatosEmergencia_quandoRespostaContemCVV
✓ bloqueia_input_duranteCarregamento
```

**`ProntuarioDetalhePage`**
```javascript
✓ renderiza_codinome_naoNomeReal
✓ salvar_anotacao_chamEndpointCorreto
✓ historico_exibeAnotacoesEmOrdemDecrescente
✓ busca_filtraAnotacoesPorPalavraChave
✓ sessaoExpirada_redirecionaParaLogin
```

**`AprovacoesPage` (Admin)**
```javascript
✓ renderiza_listaDePendentes
✓ clicar_aprovar_chamaChamadaDeAprovacao
✓ clicar_reprovar_exibeCampoDeMotivo
✓ reprovar_semMotivo_impedeSubmit
```

---

## 6. Testes E2E — Fluxos Críticos

Implementados com **Playwright** cobrindo os happy paths e o fluxo de crise.

### Fluxo 1 — Cadastro e aprovação de psicólogo

```
1. Psicólogo acessa /register/psicologo
2. Preenche todos os campos e faz upload de currículo
3. Submete → vê tela de "aguardando aprovação"
4. Admin faz login em /admin/aprovacoes
5. Aprova o psicólogo
6. Psicólogo faz login e acessa o dashboard
```

### Fluxo 2 — Agendamento de sessão pelo paciente

```
1. Paciente (FAIXA_2) faz login
2. Acessa /marketplace
3. Filtra por especialidade
4. Clica em psicólogo, visualiza valor: R$ 45,00
5. Seleciona slot disponível e confirma agendamento
6. Vê sessão listada em /dashboard/paciente/agendamentos
```

### Fluxo 3 — Crise no chatbot e acionamento de plantão ⭐

```
1. Usuário anônimo acessa /chatbot
2. Descreve sintomas de crise (ansiedade severa)
3. Chatbot responde com técnica de respiração
4. Chatbot exibe dados de contato do psicólogo de plantão
   (ou CVV/SAMU se não houver plantão ativo)
5. Nenhuma mensagem do chatbot contém a palavra "diagnóstico"
   nem sugestão de medicamento
```

### Fluxo 4 — Ciclo financeiro completo

```
1. Psicólogo marca sessão como REALIZADA
2. Paciente acessa /dashboard/paciente/cobrancas e vê cobrança PENDENTE
3. Paciente confirma pagamento simulado
4. Psicólogo acessa /dashboard/psicologo/financeiro
5. Relatório exibe sessão com valor bruto, taxa e líquido corretos
```

---

## 7. Testes de Segurança e LGPD

Estes testes validam diretamente os requisitos de RNF-01 a RNF-04.

### 7.1 Isolamento do prontuário

```java
ProntarioAcessoIsolamentoTest
  ✓ psicologoA_naoConsegueAcessar_prontuarioDePsicologoB      → 403
  ✓ paciente_naoConsegueAcessar_prontuarioProprio              → 403
  ✓ admin_naoConsegueAcessar_prontuarioDeNenhumPsicologo       → 403
  ✓ semToken_naoConsegueAcessar_prontuario                     → 401
  ✓ tokenExpirado_naoConsegueAcessar_prontuario                → 401
```

### 7.2 Criptografia em repouso

```java
CriptografiaEmRepousoTest
  ✓ anotacaoSalva_conteudoNoBancoNaoEhTextoClaro
  ✓ anotacaoSalva_ivSalvoJuntoCom_conteudoCifrado
  ✓ duasAnotacoesIguais_geramCifradosDiferentes (IV aleatório)
  ✓ anotacaoRecuperada_conteudoDecifradoIgualAoOriginal
```

### 7.3 Dados sensíveis não vazam nas respostas

```java
RespostaApiSemDadosSensiveisTest
  ✓ GET /api/prontuario/**_respostaNaoContem_senhaHash
  ✓ GET /api/marketplace/psicologos_respostaNaoContem_senhaHash
  ✓ GET /api/prontuario/**_respostaNaoContem_nomeRealDoPaciente
  ✓ qualquerEndpoint_respostaNaoContem_chaveAES
  ✓ qualquerEndpoint_respostaNaoContem_refreshToken
```

### 7.4 Rate limiting do chatbot

```java
RateLimitingChatbotTest
  ✓ maisde20MensagensPorMinuto_mesmoPIP_retorna429
  ✓ apos1Minuto_bloqueioELiberado
  ✓ ipsDistintos_naoCompartilhamLimite
```

### 7.5 Auditoria de acesso

```java
AuditoriaProntuarioTest
  ✓ leitura_gera_registroEmAuditoriaProntuario
  ✓ escrita_gera_registroEmAuditoriaProntuario
  ✓ edicao_gera_registroEmAuditoriaProntuario
  ✓ tentativaDeAcessoNegado_naoGera_registroDeAuditoria
```

---

## 8. Organização dos Arquivos

### Backend

```
src/test/java/br/com/unipsi/
│
├── auth/
│   ├── JwtServiceTest.java
│   ├── AuthServiceTest.java
│   ├── RefreshTokenServiceTest.java
│   └── integration/
│       └── AuthControllerIntegrationTest.java
│
├── prontuario/
│   ├── CriptografiaServiceTest.java
│   ├── ProntuarioServiceTest.java
│   ├── AuditoriaProntuarioServiceTest.java
│   └── integration/
│       ├── ProntuarioControllerIntegrationTest.java
│       ├── CriptografiaEmRepousoTest.java
│       └── ProntuarioAcessoIsolamentoTest.java
│
├── marketplace/
│   ├── PrecificacaoServiceTest.java
│   └── integration/
│       └── MarketplaceControllerIntegrationTest.java
│
├── chatbot/
│   ├── CriseDetectorServiceTest.java
│   └── ChatbotServiceTest.java
│
├── agenda/
│   ├── AgendaServiceTest.java
│   └── integration/
│       └── AgendaControllerIntegrationTest.java
│
├── plantao/
│   └── PlantaoServiceTest.java
│
├── financeiro/
│   ├── CobrancaServiceTest.java
│   └── integration/
│       └── FinanceiroControllerIntegrationTest.java
│
├── usuario/
│   └── PacienteServiceTest.java
│
├── admin/
│   ├── AprovacaoServiceTest.java
│   └── RevisaoPerfilServiceTest.java
│
└── security/
    ├── RespostaApiSemDadosSensiveisTest.java
    └── RateLimitingChatbotTest.java
```

### Frontend

```
src/test/
├── setup.js                              # configuração global (MSW, jest-dom)
├── services/
│   ├── authService.test.js
│   └── precificacaoService.test.js
├── pages/
│   ├── auth/
│   │   ├── RegisterPacientePage.test.jsx
│   │   └── RegisterPsicologoPage.test.jsx
│   ├── psicologo/
│   │   └── ProntuarioDetalhePage.test.jsx
│   └── admin/
│       └── AprovacoesPage.test.jsx
└── components/
    └── chatbot/
        └── ChatWindow.test.jsx
```

### E2E (Playwright)

```
e2e/
├── fluxo-cadastro-aprovacao.spec.ts
├── fluxo-agendamento.spec.ts
├── fluxo-chatbot-crise.spec.ts
└── fluxo-financeiro-completo.spec.ts
```

---

## 9. Integração com CI

Executar na seguinte ordem no pipeline:

```yaml
# .github/workflows/ci.yml (ou equivalente)

jobs:
  backend:
    steps:
      - name: Testes unitários
        run: mvn test -Dgroups="unitario"

      - name: Testes de integração
        run: mvn test -Dgroups="integracao"
        # requer banco H2 em memória (configurado no application-test.yml)

      - name: Cobertura JaCoCo
        run: mvn jacoco:report
        # falha o build se cobertura global < 80%

  frontend:
    steps:
      - name: Testes de componentes
        run: npm run test

      - name: Cobertura
        run: npm run test:coverage

  e2e:
    needs: [backend, frontend]
    steps:
      - name: Subir ambiente local
        run: docker compose up -d

      - name: Testes E2E
        run: npx playwright test
```

### Thresholds que quebram o build

| Métrica | Threshold |
|---|---|
| Cobertura global de linhas (backend) | ≥ 80% |
| Cobertura do módulo `prontuario` | ≥ 100% |
| Cobertura de branches do `PrecificacaoService` | ≥ 100% |
| Cobertura de branches do `CriseDetectorService` | ≥ 100% |
| Todos os testes passando | Obrigatório |

---

## Resumo de Prioridade de Implementação

Implementar os testes nesta ordem, alinhado com as sprints:

| Sprint | Testes a implementar junto com o código |
|---|---|
| Sprint 0 | `AuthServiceTest`, `PacienteServiceTest`, `AuthControllerIntegrationTest`, `RegisterPacientePage.test.jsx` |
| Sprint 1 | `AgendaServiceTest`, `PlantaoServiceTest` |
| Sprint 2 | `PrecificacaoServiceTest` (100% branches), `MarketplaceControllerIntegrationTest`, E2E fluxo de agendamento |
| Sprint 3 | `CriptografiaServiceTest`, `ProntuarioServiceTest`, `AuditoriaProntuarioServiceTest`, `ProntuarioControllerIntegrationTest`, `CriptografiaEmRepousoTest`, `ProntuarioAcessoIsolamentoTest` |
| Sprint 4 | `CriseDetectorServiceTest` (100% branches), `ChatbotServiceTest`, `RateLimitingChatbotTest`, `ChatWindow.test.jsx`, E2E fluxo de crise |
| Sprint 5 | `CobrancaServiceTest`, `FinanceiroControllerIntegrationTest`, E2E fluxo financeiro |
| Sprint 6 | `RespostaApiSemDadosSensiveisTest`, E2E todos os fluxos, revisão geral de cobertura |
