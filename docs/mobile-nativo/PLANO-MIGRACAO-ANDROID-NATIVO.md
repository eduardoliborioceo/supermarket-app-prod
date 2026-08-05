# Plano de Migração — App de Supermercado Android Nativo (Kotlin)

Documento de referência para transformar a lista de compras em um app Android
nativo. Ao contrário de um projeto que mantém desktop web + mobile nativo lado
a lado indefinidamente, aqui o destino final é **app nativo apenas** — o
PWA/Flask atual continua rodando só durante a transição (o usuário ainda faz
compras no mercado com ele hoje), até o app nativo ter paridade de feature e
poder ser publicado, ponto em que o PWA é aposentado. Ver seção 11.

A pasta do projeto Android Studio já foi criada em [`../../android`](../../android) —
este documento é o mapa de tudo o que falta construir dentro dela.

Escopo desta task: planejamento + esqueleto do projeto. As fases descritas
abaixo (2 em diante) são trabalho futuro, uma branch por fase, seguindo o
`CLAUDE.md`.

---

## 1. Estado atual do sistema (levantado linha a linha)

### 1.1 Backend (Flask, MVC)

| Camada | Onde vive | Quantidade |
|---|---|---|
| Models | N/A — schema definido em SQL puro (`init_db()` em `app/__init__.py`) | 3 tabelas: `usuarios`, `produtos`, `compras` |
| Repositories | `app/repositories/` | 4 arquivos (`produto`, `compra`, `supermercado`, `usuario`) |
| Services | `app/services/` | 4 arquivos (`produto`, `compra`, `supermercado`, `itens_padrao`) |
| Rotas de página (Jinja) | `app/routes/pages.py` | 4 rotas HTML (`/`, `/produtos`, `/selecionar-supermercado`, `/add`) |
| Rotas de API (JSON) | `app/routes/api.py` | 10 endpoints: seed-defaults, supermercados/buscar, supermercado/selecionar, produto/buscar, produto/adicionar, produto/excluir, produto/atualizar, carrinho/atualizar, carrinho/limpar, carrinho/gasto-previsto |
| Auth | `app/routes/auth.py` | `/login`, `/auth/google`, `/auth/google/callback`, `/logout` |

Autenticação atual: **sessão de cookie Flask** (`session["user_id"]`, sem
Flask-Login) + **Google OAuth via Authlib**, único provedor — não há login por
senha (ver `CLAUDE.md` > Auth). Isolamento de dados por `usuario_id` em todas
as tabelas.

### 1.2 Frontend web (o que o app nativo substitui)

`app/templates/` tem 6 templates. Todas as páginas reais (o projeto não tem
admin nem páginas legais):

| Página web | Rota | Módulo de domínio |
|---|---|---|
| Home (lista de compras / carrinho) | `/` | compras |
| Produtos (CRUD) | `/produtos` | produtos |
| Selecionar Supermercado | `/selecionar-supermercado` | supermercado |
| Login | `/login`, `/auth/google*` | autenticação |

Front-end atual é HTML/Jinja2 + CSS com design tokens em `:root` + JS vanilla
(`app/static/js/main.js`, `camera.js`). Esse código **não é reaproveitável em
Kotlin** — cada tela é reescrita do zero em Compose, mas a lógica de negócio
(normalização de nome/preço, validação, cálculo de carrinho) já vive nos
services do Flask, então o app nativo só precisa consumir a API JSON, não
reimplementar regra de negócio.

### 1.3 Sem serviço de API dedicado (diferença chave para projetos maiores)

Este projeto **não tem** um segundo backend separado para o app nativo (ao
contrário de projetos com dezenas de domínios, onde vale a pena construir uma
API nova do zero para autenticação JWT limpa). Aqui `app/routes/api.py` já é
pequeno (10 endpoints) e o próprio Flask vai ganhar suporte a JWT bearer
token na Fase 2, ao lado da sessão de cookie que o site continua usando — um
único serviço Railway, um único banco, dois métodos de autenticação
coexistindo no mesmo `login_required`.

Se o produto crescer muito (deixar de ser "mais simples"), reavaliar um
serviço irmão compartilhando o mesmo Postgres — não construir isso
antecipadamente sem necessidade real.

### 1.4 PWA existente (referência de branding, não de código)

`app/static/manifest.webmanifest`: nome "Supermercado", ícones em
`app/static/images/logos/`. Esses valores já foram portados para o projeto
Android (`ui/theme/Color.kt`, ícone do launcher, splash screen) — ver
`android/README.md`.

---

## 2. Decisão de arquitetura para o app nativo

| Camada | Escolha | Por quê |
|---|---|---|
| Linguagem | Kotlin | único caminho para "nativo" no Android; alinhado ao `CLAUDE.md` |
| UI | Jetpack Compose + Material 3 | padrão atual do Android; permite portar os design tokens do CSS quase 1:1 |
| Arquitetura | MVVM + Clean Architecture + Repository Pattern | mesmo vocabulário de camadas do `CLAUDE.md` (Model/Repository/Service) já usado no Flask |
| Injeção de dependência | Hilt | padrão Google/Android para DI, integra com ViewModel e WorkManager sem boilerplate manual |
| Rede | Retrofit + OkHttp + kotlinx.serialization | serialização Kotlin nativa, sem depender de Gson |
| Concorrência | Coroutines + Flow | padrão Kotlin, substitui callbacks |
| Persistência local | Room | fonte de verdade local — o web já usa LocalStorage para o carrinho funcionar offline durante a compra; Room é o equivalente nativo, mais robusto |
| Preferências leves | DataStore | token de sessão, preferências de UI |
| Background/sync | WorkManager | sincronizar o carrinho quando a conexão volta (mercados costumam ter sinal ruim) |
| Imagens | Coil 3 | carregamento de logo/ícones |
| Biometria | androidx.biometric | desbloqueio rápido sem repetir login toda vez, opcional (Fase 4) |
| Navegação | Navigation Compose | um grafo por feature, compostos no `SupermercadoNavHost` |

Push (FCM) e biometria ficam de fora do roadmap próximo — o app não tem hoje
nenhuma notificação (nem Web Push), então não há necessidade real de
antecipar essa infraestrutura; entram só se/quando surgir um caso de uso
concreto (ex.: lembrete de lista salva).

### Módulos Gradle

Fase 1 (esta task) entrega um único módulo `:app`. Este projeto é pequeno o
suficiente (3 domínios: home/carrinho, produtos, supermercado) para nunca
precisar de módulos Gradle separados por feature — não introduzir
modularização antecipada sem necessidade real.

---

## 3. Design system — paridade com o app web

Os tokens abaixo (já implementados em `android/app/src/main/java/app/supermercado/mobile/ui/theme/`)
são os mesmos definidos em `CLAUDE.md`:

| Token web | Valor | Uso no Compose |
|---|---|---|
| `--bg` | `#f1f5f9` | `SupermercadoColorTokens.background` |
| `--card` | `#ffffff` | `SupermercadoColorTokens.surface` |
| `--text` | `#1e293b` | `SupermercadoColorTokens.onSurface` |
| `--text-muted` | `#64748b` | `SupermercadoColorTokens.onSurfaceMuted` |
| `--border` | `rgba(15,23,42,0.09)` | `SupermercadoColorTokens.border` |
| `--primary` | `#0d6efd` | `SupermercadoColorTokens.primary` |
| `--success-color` | `#198754` | `SupermercadoColorTokens.success` |
| `--sidebar-bg` | `#0f172a` | `SupermercadoColorTokens.sidebarBackground` (tema escuro / splash) |

Raios de borda (`.card` 12px, `.stat-card`/`.shortcut-card` 14px, botões/forms
8px) viraram `SupermercadoShapes` em `Shape.kt`. Escala tipográfica virou
`SupermercadoTypography` em `Type.kt`.

**Regra de manutenção:** qualquer mudança de token no CSS (`:root` do web)
deve ser replicada em `Color.kt`/`Shape.kt`/`Type.kt`, e vice-versa. Os dois
frontends compartilham uma identidade visual, não duas — até o dia em que o
web for aposentado (ver seção 11).

---

## 4. Estratégia de API — transição, não big-bang

1. **Curto prazo (Fase 2):** o Flask existente ganha suporte a
   `Authorization: Bearer <JWT>` além da sessão de cookie que o site já usa
   — mesmo `login_required`, checando os dois métodos. Nenhum endpoint novo
   de domínio é necessário: `app/routes/api.py` já cobre produtos, carrinho e
   supermercado; só falta login/refresh emitindo o JWT.
2. **Contrato:** cada repository do app Android depende de uma *interface*
   Retrofit (`XxxApi`), nunca da implementação Retrofit diretamente.
3. **Autenticação:** JWT (access + refresh token), armazenados em
   `EncryptedSharedPreferences`/DataStore criptografado — nunca em
   SharedPreferences puro. Interceptor OkHttp injeta o `Authorization: Bearer`
   e dispara refresh automático em 401.
4. **Não duplicar regra de negócio.** Normalização de nome/preço, validação,
   cálculo de carrinho continuam só no backend (`app/services/`) — o app
   nativo é uma camada de apresentação + cache local.

---

## 5. Autenticação e sessão

- Único provedor: **Google OAuth** (igual ao web, `CLAUDE.md` > Auth — não
  existe login por senha neste projeto, não introduzir um).
- No Android, o fluxo abre uma Custom Tab (androidx.browser) para
  `/auth/google`, captura o retorno via deep link, e troca o resultado por um
  JWT emitido pelo Flask (endpoint novo, Fase 2) — não pela sessão de cookie
  usada pelo navegador.
- Sessão via **refresh token de longa duração**, armazenado criptografado no
  dispositivo. Ao reabrir o app, se o access token expirou mas o refresh
  token é válido, renovar silenciosamente — sem pedir login de novo.
- Biometria (Fase 4, opcional) como reautenticação leve ao voltar do
  background, em vez de logout completo.
- Logout completo (limpar refresh token) só em ação explícita do usuário ou
  expiração real do refresh token.

---

## 6. Offline-first e cache local (Room)

O web já resolve isso parcialmente com LocalStorage (carrinho sobrevive a
refresh de página, ver `app/static/js/main.js`). O app nativo formaliza isso
com Room como fonte única de verdade que a UI observa (`Flow<List<T>>`):

```
API (Retrofit) → Repository → Room (grava) → Flow → ViewModel → UI
```

Relevante especialmente para este app: dentro do mercado o sinal de internet
costuma ser ruim — marcar item como comprado, ajustar quantidade/preço no
carrinho precisa funcionar offline e sincronizar depois (WorkManager +
fila local), mesmo espírito de Optimistic UI (ex.: WhatsApp mostra enviado,
depois sincroniza).

---

## 7. Mapeamento tela a tela (paridade de feature)

| Tela nativa (Compose) | Endpoints principais hoje (`api.py` / `pages.py`) | Prioridade |
|---|---|---|
| Auth (login com Google) | `auth/google*` (Flask, adaptado para emitir JWT) | Fase 2 |
| Home (lista de compras / carrinho) | `GET /` (dados), `carrinho/atualizar`, `carrinho/limpar`, `carrinho/gasto-previsto` | Fase 2 |
| Produtos (CRUD + busca) | `produto/buscar`, `produto/adicionar`, `produto/atualizar`, `produto/excluir`, `seed-defaults` | Fase 3 |
| Selecionar Supermercado | `supermercados/buscar`, `supermercado/selecionar` | Fase 3 |

Cada linha vira, no futuro, um pacote `ui/screens/<dominio>/` +
`data/<dominio>/` (Repository + DTOs + Retrofit service).

---

## 8. Roadmap por fases

> Cada fase = uma branch dedicada, nunca misturada com outro assunto, seguindo
> o fluxo de trabalho do `CLAUDE.md` (branch a partir de `main`, PR ao final).

| Fase | Entrega | Depende de |
|---|---|---|
| **1 — concluída nesta task** | Pasta do projeto Android Studio criada e abrível (Gradle/Compose/Hilt configurados), tema com os tokens do design system, tela de Login e Home em placeholder, plano documentado | — |
| **2 — Fundação** | Flask emite/valida JWT (login Google + refresh) ao lado da sessão de cookie; sessão com refresh token no app; Home real (produtos + carrinho, dados de verdade, CRUD completo do carrinho) | endpoint de login JWT no Flask |
| **3 — Paridade de feature** | Produtos (CRUD + busca) e Selecionar Supermercado com dados reais, Room para leitura/escrita offline do carrinho | Fase 2 |
| **4 — Polimento e publicação** | Biometria (opcional), microinterações, dark mode, acessibilidade, assinatura de release, Play Console (internal testing → produção), CI Android (`.github/workflows/android.yml`) | Fase 3 completa |

---

## 9. Qualidade, testes e CI/CD

- **Testes unitários:** JUnit + MockK nos ViewModels e Repositories.
- **Testes de UI:** Compose UI Test (androidTest) para os fluxos críticos
  (login, adicionar produto ao carrinho, marcar item como comprado).
- **CI:** workflow do GitHub Actions dedicado (`.github/workflows/android.yml`)
  rodando `./gradlew test assembleDebug` a cada PR que toque `android/**` —
  não reaproveitar nenhum workflow existente do projeto.

---

## 10. Segredos e configuração

`google-services.json`, keystores de release e qualquer credencial usada pelo
app nativo seguem a mesma regra de **Repository Hygiene & Secret Protection**
do `CLAUDE.md` — nunca commitados, sempre fora do controle de versão.

---

## 11. Estado de transição e o que este documento explicitamente NÃO cobre

- **Este projeto não é "web + nativo para sempre"**: o destino final é
  **app nativo apenas**. O PWA/Flask atual (`app/templates/`) continua
  existindo só durante a transição — é usado de verdade hoje (compras no
  mercado) e será aposentado quando o app nativo atingir paridade de feature
  e for publicado na Play Store. Diferente de um projeto com desktop web
  permanente, aqui não existe caso de uso desktop: o PWA sempre foi pensado
  para celular.
- Se o volume de trabalho justificar, um segundo serviço Railway
  (compartilhando o mesmo Postgres) pode vir a existir dedicado ao app
  nativo — decisão de infraestrutura do usuário, fora do escopo desta task.
- **iOS** não está no escopo.
- Nenhuma implementação de tela real além do placeholder de Login/Home foi
  feita nesta task — implementar cada fase é trabalho futuro, uma branch por
  vez.
