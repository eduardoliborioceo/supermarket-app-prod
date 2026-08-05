# Supermercado — App Android Nativo (Kotlin)

Projeto Android Studio nativo do app de lista de compras. Não é um wrapper do
PWA — é um app Kotlin/Jetpack Compose de verdade, construído para consumir a
mesma API JSON que hoje serve o app web (Flask, `app/routes/api.py`).

O plano completo de migração — decisões de arquitetura, mapeamento de cada
tela/rota atual para uma tela nativa e o roadmap por fases — está em
[`../docs/mobile-nativo/PLANO-MIGRACAO-ANDROID-NATIVO.md`](../docs/mobile-nativo/PLANO-MIGRACAO-ANDROID-NATIVO.md).
Leia aquele documento antes de implementar qualquer feature nova aqui.

## Como abrir

1. Abra o Android Studio.
2. **File > Open** e selecione esta pasta (`android/`) — não use "New Project".
3. Aguarde o Gradle sync (primeira vez baixa a distribuição do Gradle 9.6.1 e
   as dependências; precisa de internet).
4. Rode a configuração `app` num emulador ou dispositivo físico com Android 8.0+ (API 26).

Pré-requisitos: Android Studio e o Android SDK (`platform android-36`). Se o
sync pedir para instalar algum componente do SDK, aceite — são atualizações
normais.

## Estrutura

```
android/
├─ app/
│  ├─ build.gradle.kts        # applicationId app.supermercado.mobile, Compose, Hilt, KSP
│  ├─ src/main/java/app/supermercado/mobile/
│  │  ├─ SupermercadoApplication.kt
│  │  ├─ MainActivity.kt
│  │  ├─ core/
│  │  │  ├─ network/          # Retrofit/OkHttp — camada de API
│  │  │  ├─ data/             # repositories (auth, sessão, produtos) — a preencher por fase
│  │  │  └─ di/                # módulos Hilt — a preencher por fase
│  │  └─ ui/
│  │     ├─ theme/            # Color/Type/Shape/Theme — tokens do design system web
│  │     ├─ navigation/       # NavHost
│  │     └─ screens/          # um pacote por feature (auth, home, produtos, ...)
│  └─ src/test, src/androidTest
├─ gradle/libs.versions.toml   # catálogo de versões
└─ settings.gradle.kts
```

Arquitetura: **MVVM + Clean Architecture + Repository Pattern**, um pacote de
feature por domínio (home/carrinho, produtos, selecionar supermercado),
espelhando a separação Model/Repository/Service que o `CLAUDE.md` já exige no
backend Flask — a ideia é manter o mesmo vocabulário de camadas dos dois lados.

## Design system

As cores/tipografia/espaçamento em `ui/theme/` são os mesmos tokens definidos
em `CLAUDE.md` (`--bg`, `--card`, `--text`, `--primary`, `--sidebar-bg` etc).
Qualquer ajuste de cor/token feito no app web deve ser replicado aqui, e
vice-versa — os dois frontends compartilham uma identidade visual, não duas.

O ícone do launcher usa `app/static/images/logos/icon-logo-mobile.png` como
placeholder de foreground — regenere via **Image Asset Studio** (botão
direito em `res` > New > Image Asset) para obter todas as densidades e o
recorte correto dentro da safe zone do ícone adaptativo.

## Rede

`BuildConfig.API_BASE_URL` aponta para o Flask/Railway que já serve o site e
o PWA hoje (não há ambiente de desenvolvimento separado — só produção, ver
CLAUDE.md). Quando a autenticação JWT estiver pronta no Flask (Fase 2 do
plano), essa constante continua a mesma; só muda se um serviço Railway
dedicado ao app nativo vier a existir.

## O que ainda não existe aqui (de propósito)

Este é o esqueleto do projeto (Fase 1 do plano de migração), não o app
completo. Autenticação real, as telas de Produtos/Carrinho/Selecionar
Supermercado, Room para uso offline, etc. entram nas fases seguintes descritas
no plano — implementar tudo de uma vez não é o objetivo desta task.
