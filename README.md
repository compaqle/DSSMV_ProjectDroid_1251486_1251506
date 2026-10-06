# DSSMV ProjectDroid — 1251486_1251506

App Android nativo em **Java** para a UC DSSMV (ISEP LETI), 2º ano / 1º semestre 2026/27.

**Equipa:** Tiago Santos (1251486) · Tomás Bastos (1251506) — Turma 2DD, equipa #8.

> Esqueleto do repositório: estrutura de projeto Gradle + config, **sem código de aplicação**.
> Não há `MainActivity` nem qualquer ficheiro `.java`. O código entra a partir daqui.

## Stack (decisões já verificadas — ver `projeto/design-features.md` da UC)

- Linguagem: **Java** (Android nativo), AGP 8.x, minSdk 26 / targetSdk 35.
- Persistência local: **Room** (`androidx.room` 2.8.5).
- Gráficos: **Canvas** nativo (sem lib de charts).
- API REST pública + sensores do dispositivo (GPS, luz/etc.) + notificações locais.
- Sem emulador na demo: corre num **dispositivo real** (OPPO Reno 11F 5G / CPH2603).

## Estrutura

```
app/src/main/
  AndroidManifest.xml
  java/com/compaqle/projectdroid/   # código da aplicação (vazio, de momento)
  res/                              # recursos (layouts, valores, drawables)
app/src/test/                       # testes unitários (vazio, de momento)
```

## Build

A build requer **Android SDK** + Gradle (wrapper ainda por gerar). Sem SDK nesta máquina de
desenvolvimento — correr nos laboratórios do ISEP ou com `sdkmanager` CLI.

```bash
./gradlew assembleDebug     # ainda não disponível (sem wrapper/SDK)
```

## Avisos de API e tema

- Tema/título e repositório: aprovados com `pbs@isep.ipp.pt` / `caf@isep.ipp.pt` (admin concedido).
- Todos os prazos às 23:59 do domingo da semana: relatório Moodle Semana 8 (até 8 nov), demo Semana 9.
