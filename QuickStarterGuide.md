# Quick Starter Guide

Zero to a running OPFIS Desktop build in a few minutes. This is the
action-only path — for what OPFIS is and why, see
[README.md](README.md); for a full guided tour, see [DEMO.md](DEMO.md).

## 1. Prerequisites

- **JDK 21+** (verified against JDK 21.0.11).
- **Android SDK** with `platform;android-36` — only required if you
  intend to build the Android target. Not needed to run Desktop.

## 2. Clone and verify the toolchain

```sh
git clone <this-repository-url>
cd Offline-Personal-Financial-Intelligence-System-OPFIS-
./gradlew ktlintCheck detekt allTests assemble
```

This runs the project's full verification gate (lint, static
analysis, all tests, both Android and Desktop compile). If it's
green, your toolchain is correctly set up.

## 3. Run it

```sh
./gradlew :composeApp:run
```

Desktop app launches in place. On first run the database is empty —
that's expected, OPFIS starts with zero data by design. See
[DEMO.md](DEMO.md) for a scripted walkthrough of every screen with no
seed data required.

To build an Android debug APK instead:

```sh
./gradlew :composeApp:assembleDebug
```

## 4. Where to go next

- [DEMO.md](DEMO.md) — full ~10-minute scripted walkthrough of all
  seven screens, entirely offline.
- [README.md](README.md) — architecture, feature list, packaging, and
  full repository layout.
- [`project-memory-bank/`](project-memory-bank/) — the engineering
  memory bank; start with `05-current-state.md` for current status.
- [`documents/`](documents/) — engineering thesis, architecture
  document, ADR collection, and other deeper-dive material.
