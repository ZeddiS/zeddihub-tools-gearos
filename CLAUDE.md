# ZeddiHub GearOS — Claude kontext

> Sub-CLAUDE.md pro tento repo. Hlavní (parent) CLAUDE.md žije v `C:\Users\12voj\Documents\CLAUDE.md` a obsahuje kompletní mapu ZeddiHub rodiny — tento soubor jen doplňuje GearOS-specifika.

**Komunikace s uživatelem: česky.**

---

## 1. Co je GearOS

Wear OS aplikace pro Galaxy Watch 6 Classic, která rozšiřuje ZeddiHub na zápěstí. Hybrid arch — primárně companion ke `zeddihub_tools_mobile` přes Wearable Data Layer, fallback na `zeddihub.eu/api/` přes LTE/Wi-Fi.

**Stav (k 2026-05-08):** M0 — scaffolding. Jen "Hello GearOS" UI, žádný funkční feature. Plan.md popisuje M0–M5 roadmap.

## 2. Místo v ZeddiHub rodině

```
zeddihub-tools-website   ← web admin + API (zeddihub.eu) — verze 0.9.3
zeddihub_tools_mobile    ← Android phone (companion partner) — verze 0.9.3 / vCode 31
zeddihub_tools_desktop   ← Windows desktop — verze 1.7.6
zeddihub-tools-gearos    ← TENTO REPO — verze 0.1.0 / vCode 1 (vlastní startovní line)
```

Nezaměnit s `zeddihub_mobile_tools` (skeleton, ignorovat) ani `zeddihub_tools` (legacy).

## 3. Tech stack

- **Kotlin** + **Compose for Wear OS** (BOM 2024.06)
- **Hilt** DI, **Retrofit** + **Moshi**, **DataStore** + **EncryptedSharedPreferences**
- **play-services-wearable** (Data Layer), **Horologist** (bezel + UX helpers)
- **wear.tiles**, **wear.watchface.complications**, **wear.watchface**
- **firebase-messaging** (vlastní FCM token na watch, registruje jako `device_kind=watch`)
- **Health Services** + opt-in Samsung Health Sensor SDK (vyžaduje partner approval)

**Min SDK:** 30 (Wear OS 3+) **Target SDK:** 34
**Application ID:** `com.zeddihub.gearos`

## 4. Build

Bundled toolchain (stejný pattern jako mobile):

```
tools/
├── jdk17/             # Eclipse Temurin 17
├── android-sdk/       # Android SDK 34 + build-tools + platform-tools
└── gradle-cache/      # GRADLE_USER_HOME
```

**Build cmd** (PowerShell):

```powershell
$env:JAVA_HOME        = (Resolve-Path .\tools\jdk17).Path
$env:Path             = "$env:JAVA_HOME\bin;$env:Path"
$env:ANDROID_HOME     = (Resolve-Path .\tools\android-sdk).Path
$env:GRADLE_USER_HOME = (Resolve-Path .\tools\gradle-cache).Path
.\gradlew.bat clean :wear:assembleRelease --no-daemon
```

Výstup: `wear/build/outputs/apk/release/ZeddiHub-GearOS-{ver}.apk`

**Tool soubory NEJSOU v gitu** — uživatel musí jednorázově nakopírovat z `zeddihub_tools_mobile/tools/` (nebo stáhnout). Viz [tools/README.md](tools/README.md).

## 5. Klíčové konvence

- **Lock chování**: Stejně jako mobile v0.9.3+ — lock JEN při cold start, ne na backgrounding (uživatel si stěžoval, že 30s re-lock otravuje).
- **Auth tokens**: Krátký TTL (24h), refresh přes mobile (Data Layer). Standalone refresh fallback přes `/api/auth/refresh`.
- **IP storage**: Server-side jen hash (sha256(ZH_IP_SALT|ip)), watch sám raw IP nikdy neposílá.
- **Telemetry**: `device_kind=watch` flag v `telemetry_log.json`. Health data jen agg + opt-in.
- **Bezel**: Native input — `RotaryHandler` v `Horologist`. Vždy mappovat na scroll/cycle, ne na destruktivní akce.
- **Confirmations**: Destruktivní akce (restart, panic, kill switch) = long-press 1s + biometric (panic = 3s).

## 6. Cross-repo dependencies (NEZAPOMENOUT po M1+)

GearOS závisí na změnách v ostatních repech, které je potřeba implementovat **paralelně**:

### `zeddihub-tools-website`
- Nové API endpointy v `api/watch/`: `pair.php`, `profile.php`, `heartbeat.php`, `panic.php`.
- DB tabulka `zh_watch_tokens (id, user_id, device_label, jwt_hash, created_at, last_seen, revoked_at)` — schéma do `api/_db.php`.
- `tools/admin/clients.php` rozšířen o tab "Watches".
- `tools/admin/push.php` checkbox "Doručit na hodinky".
- `staged_releases.json` rozšířen o `target: "watch"|"phone"` field, `app_releases.php` filtr.

### `zeddihub_tools_mobile`
- `WearableListenerService` pro pairing handshake, status relay, push relay.
- Settings → "Connect Watch" sekce (generate pairing code).
- `MessageClient` action handlers pro server akce iniciované z hodinek.

## 7. Versioning

GearOS má **vlastní version line** od 0.1.0 (důvod: nový produkt, jiný release rytmus než mobile/web). Mobile/web jedou na 0.9.3 sjednoceně.

Po každém release:
1. Bumpni `versionCode/Name` v `wear/build.gradle.kts`.
2. Přidej entry do `staged_releases.json` (web repo) s `target: "watch"`.
3. Build APK, kopíruj do `zeddihub-tools-website/downloads/ZeddiHub-GearOS-{ver}.apk`, smaž starou.
4. `RELEASE_NOTES_GEAROS_{ver}.md` v rootu webu.
5. Admin → App releases → Test APK → Publikovat.

## 8. Pitfalls

- **GW6 Classic nemá kameru** → QR pairing nejde. Používáme 6-digit code + on-screen number pad.
- **Wear OS Compose ≠ phone Compose** — `androidx.wear.compose.*` má vlastní komponenty (`Chip`, `ScalingLazyColumn`, `TimeText`, `Vignette`). Nezaměnit s `androidx.compose.material3.*`.
- **Tiles a Complications** mají **separátní render proces** — žádný přístup k Hilt/DataStore z tile service bez explicitního worker bridge.
- **Watch face render budget** — AOD (Always-On Display) max 1Hz, full color jen v interactive mode.
- **Samsung Health Sensor SDK** — vyžaduje partner program approval (~3 týdny). Zatím použít androidx.health.services.client (Google's Wear Health Services), které pokrývá tep/kroky/workout state bez Samsung schválení.
- **FCM na watch** — vyžaduje `google-services.json` (zatím chybí). Musí se zaregistrovat samostatný FCM project nebo použít stejný jako mobile (preferuje se shared project, separate sender ID per platform).

## 9. Roadmap pointers

- **M0 (now)** — scaffolding ✅
- **M1** — pairing + companion sync MVP (nejvyšší priorita po scaffold review)
- **M2** — server controls + tiles + complications
- **M3** — push + ongoing activity + sleep-aware
- **M4** — TOTP + panic + biometric + geofence
- **M5** — wi-fi scan + find my desktop + voice + macro + health bridge + watch face

## 10. User preferences (zděděné z parent CLAUDE.md)

- Komunikace **česky**.
- Klikatelné otázky pro nejisté/víceznačné scénáře, dokud není 100% jistota.
- Build lokálně přes bundled toolchain, `--no-daemon`.
- APK deploy: kopírovat do `zeddihub-tools-website/downloads/` + smazat starou.
- Time format: `2026-05-08 14:23:11 (2h)` — full + relative.

---

## 11. Distribuce — install na hodinky přes mobile app

Od mobile v0.8.7 existuje `WatchInstallScreen` v zeddihub_tools_mobile (Účet → "Nainstalovat na hodinky"):

- **Detekuje paired watch** přes `Wearable.NodeClient.connectedNodes`.
- **3 install cesty:**
  1. **Wear Installer 2** (recommended) — Play Store deeplink (`market://search?q=wear+installer`).
  2. **Direct APK download** — otevře browser na `https://zeddihub.eu/downloads/ZeddiHub-GearOS-{ver}.apk`.
  3. **ADB sideload** — instrukce pro pokročilé.
- **Konstanta verze:** `GearOsVersion.LATEST` v `WatchInstallScreen.kt` — bumpnout při každém release.

**Cross-repo dependencies pro distribuci:**
- `zeddihub-tools-website/downloads/ZeddiHub-GearOS-{ver}.apk` — APK source-of-truth.
- `zeddihub-tools-website/tools/data/staged_releases.json` — entry s `platform: "watch"`, `target` filter v `app_releases.php` (M1 task).
- `zeddihub_tools_mobile/app/src/main/java/.../ui/watch/WatchInstallScreen.kt` — UI vstup.

**Keystore sharing:** GearOS i mobile podepsané stejným klíčem (`zeddihub_tools_mobile/keystore/zeddihub-release.jks`) přes Windows directory junction `gearos/keystore -> mobile/keystore`. Nezbytné pokud bychom v budoucnu zapnuli `wearApp` embed.

---

**Poslední update:** 2026-05-08 (v0.1.0 release + mobile install screen)
