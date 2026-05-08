# ZeddiHub GearOS — Plan

> Wear OS aplikace pro Samsung Galaxy Watch 6 Classic, integrovaná do ZeddiHub ekosystému.
> Sdílí auth, telemetrii, push a release-gating s `zeddihub_tools_mobile` (companion) a `zeddihub-tools-website` (admin/API).

**Repo:** `zeddihub-tools-gearos` (public, MIT)
**Application ID:** `com.zeddihub.gearos`
**Cílové zařízení:** Samsung Galaxy Watch 6 Classic (Wear OS 4 / Android 13, One UI Watch 5)
**Min SDK:** 30 (Wear OS 3) — zajistí kompatibilitu i s GW4 / GW5 / GW6
**Target SDK:** 34
**Verze startu:** `0.1.0` / `versionCode 1`

---

## 1. Cíle

GearOS je **rozšíření ZeddiHub na zápěstí**. Hodinky doplňují, co je na telefonu/desktopu nepraktické:

- Rychlé akce: restart serveru, ack alertu, panic kill switch — bez vytahování telefonu.
- Konsolidované notifikace: push z FCM se actionable replies přímo na watch.
- Bezpečnostní vrstva: TOTP 2FA generátor, sleep-aware push routing, geofence auto-mode.
- Telemetry bridge: Samsung Health → ZeddiHub dashboard (osobní device-fleet view).
- Native UX: rotating bezel pro navigaci, tiles + complications, custom watch face.

## 2. Architektura — Hybrid (companion + standalone fallback)

```
┌────────────────────────┐    Wearable Data Layer    ┌────────────────────────┐
│  zeddihub_tools_mobile │ ◄────────────────────────► │ zeddihub-tools-gearos  │
│  (telefon, paired)     │   (BT, low-latency)        │  (watch, standalone-   │
│                        │                            │   capable)             │
└──────────┬─────────────┘                            └───────────┬────────────┘
           │                                                      │
           │  HTTPS (Retrofit)                                    │  HTTPS (Retrofit)
           │  primární auth gate                                  │  fallback když BT
           │                                                      │  není v dosahu
           ▼                                                      ▼
       ┌─────────────────────────────────────────────────────────────┐
       │           zeddihub-tools-website (zeddihub.eu/api/)         │
       │      auth, push queue, telemetry, app_releases, …           │
       └─────────────────────────────────────────────────────────────┘
```

### 2.1 Datový tok

| Operace | Primární cesta | Fallback |
|---|---|---|
| Login (poprvé) | 6-digit code z mobile/web → watch zadá → vyměna za token | — (vyžaduje aspoň první ručeí spárování) |
| Token refresh | Wearable Data Layer (telefon má refresh token) | Standalone refresh přes `/api/auth/refresh` (watch má vlastní krátkodobý token) |
| Server status (poll) | `MessageClient` request → mobile vrací cached snapshot | `GET /api/servers` přes LTE/Wi-Fi |
| Server akce (restart) | `MessageClient` → mobile provede call → ACK | Direct `POST /api/servers/{id}/restart` |
| Push příjem | FCM → mobile → Data Layer relay → watch ongoing activity | FCM přímo na watch (registrovat samostatný FCM token) |
| Telemetry log | Data Layer → mobile batchne → upload | Batch + upload sám každých N min |

### 2.2 Connectivity awareness

`ConnectivityClient` v `data/sync/` sleduje `NodeClient` z Wearable API. Stav:
- `CONNECTED` — telefon online, vše proudí Data Layer (battery friendly).
- `DISCONNECTED` — fallback na vlastní HTTPS přes LTE/Wi-Fi (vyšší spotřeba).
- `AUTH_MISSING` — watch nemá token, ukáže pairing screen.

## 3. Feature breakdown (M0 → M5)

### M0 — Scaffolding (tato session)
- [x] Repo struktura, gradle, manifest, theme, navigace, Hilt DI.
- [x] Plan + dokumentace.
- [ ] Žádný funkční feature — jen "Hello GearOS" screen.

### M1 — Pairing + Companion sync
- [ ] **6-digit pairing screen** (rotating bezel pro výběr číslic, on-screen confirm).
  - Mobile/web vygeneruje kód v `admin/clients.php` → tab "Watches".
  - API endpoint `POST /api/watch/pair` (kód → JWT).
  - Token uložen v EncryptedSharedPreferences (Android Keystore).
- [ ] **Wearable Data Layer kostra** (`MessageClient`, `DataClient`, `NodeClient`).
- [ ] **Home screen** s `ConnectionState` chip + `AccountInfo` row.
- [ ] **Web admin tab** `clients.php?tab=watches` — seznam párovaných hodinek, revoke akce.

### M2 — Server quick controls + Tiles + Complications
- [ ] **HomeScreen server cards** (Compose for Wear, ScalingLazyColumn) — bezel scrolluje seznam.
- [ ] **ServerDetailScreen** — ping/start/stop/restart akce s confirm gate (long-press 1s).
- [ ] **ServerStatusTileService** — swipe vlevo z watch face = tile s top 3 servery.
- [ ] **PingComplicationService** — short/long text complication s ping latency.
- [ ] **AlertCountComplicationService** — count nepřečtených pushů.

### M3 — Push notifications + Ongoing activity
- [ ] **ZeddiHubFirebaseMessagingService** (watch má vlastní FCM token, registruje do `push_queue.json` jako `device_kind=watch`).
- [ ] **Actionable notifications** — Schválit/Zamítnout/Restart now/Ack, mapování na `MessageClient` actions.
- [ ] **Ongoing Activity (CSAH)** — během dlouhé akce (restart serveru) banner s progress + cancel.
- [ ] **Sleep-aware routing** — `Sleep API` (Health Services) detekuje spánek, server-side push gating přes `device_state` flag.

### M4 — Bezpečnost a "panic" UX
- [ ] **TOTP screen** — 6-digit kód, 30s rotace, seedy synced z mobile (encrypted).
- [ ] **Panic button tile** — long-press 3s = call `POST /api/auth/panic` → invalidate all sessions, kick from servers, flag account.
- [ ] **Biometric unlock pro citlivé akce** (`BiometricPrompt` na Wear).
- [ ] **Geofence auto-mode** — opt-in, `FusedLocationProviderClient`. Doma = relax, mimo = vigilant.

### M5 — Power user features
- [ ] **Wi-Fi map quick scan** — jeden tap = scan + upload (využívá existující `/api/wifi-map/`).
- [ ] **Find My Desktop** — `POST /api/desktop/locate` → desktop ZeddiHubTools přehraje sound + flash.
- [ ] **Voice commands** — Wear OS Assistant intent: "OK Google, restart Minecraft" → ZeddiHub action.
- [ ] **Macro trigger** — list desktopových maker, tap = spustí přes API.
- [ ] **Health → telemetry bridge** (opt-in) — Samsung Health Sensor SDK, agg only (ne raw bio data).
- [ ] **Custom watch face** — `WatchFaceService` (canvas), live server status dots, accent z `panel_settings.json`.

## 4. Mobile companion changes (separátní session, repo `zeddihub_tools_mobile`)

Po M1 musí mobile aplikace:
- Přidat **"Connect Watch"** sekci v Settings.
- Implementovat `WearableListenerService` pro:
  - Pairing handshake (vrátí JWT odvozený z mobile master tokenu).
  - Server status snapshot (relay z mobile cache).
  - Push relay (forward FCM data payloads na watch s actionable buttons).
- Push akce z hodinek: mobile dostane `MessageClient` event → provede call → ACK zpět.

## 5. Web/admin changes (separátní session, repo `zeddihub-tools-website`)

- **`tools/admin/clients.php`** — přidat tab "Watches" (vedle Users/Codes/Roles):
  - Seznam párovaných hodinek (per user): model, last_seen, paired_at, BT MAC hash.
  - Generate pairing code (POST `pair-code/start.php`).
  - Revoke watch session (vymaže JWT v `zh_watch_tokens`).
- **`api/watch/`** nové endpointy:
  - `POST /api/watch/pair` — kód → JWT.
  - `GET /api/watch/profile` — minimal user info pro home screen.
  - `POST /api/watch/heartbeat` — last_seen + battery + connectivity.
  - `POST /api/watch/panic` — kill switch (cascade na auth/sessions/servers).
- **DB schéma** — nová tabulka `zh_watch_tokens (id, user_id, device_label, jwt_hash, created_at, last_seen, revoked_at)`.
- **`admin/push.php`** — checkbox "Doručit na hodinky" v targetingu.

## 6. Tech stack

| Vrstva | Knihovna | Verze |
|---|---|---|
| Jazyk | Kotlin | 1.9.24+ |
| UI | Compose for Wear OS | 1.4.x (BOM 2024.06) |
| Material | wear-compose-material3 | latest |
| Navigation | wear-compose-navigation | 1.4.x |
| Wear UX helpers | Horologist (Google) | 0.6.x — bezel handling, rotating input, scaling lists |
| DI | Hilt | 2.51+ |
| HTTP | Retrofit + OkHttp + Moshi | 2.11 / 4.12 / 1.15 |
| Storage | DataStore + EncryptedSharedPreferences | androidx.datastore 1.1, security-crypto 1.1 |
| Wearable | play-services-wearable | 18.2.0 |
| Tiles | androidx.wear.tiles | 1.4.x |
| Complications | androidx.wear.watchface.complications | 1.2.x |
| Watch face | androidx.wear.watchface | 1.2.x |
| Health | androidx.health.services.client | 1.0.x |
| Auth | androidx.biometric | 1.2.x |
| Push | firebase-messaging | 24.x |
| Logging | Timber | 5.0+ |

## 7. Toolchain (bundled jako u mobile repu)

GearOS replikuje pattern ze `zeddihub_tools_mobile`:

```
tools/
├── jdk17/             # Eclipse Temurin 17 (bundled JDK)
├── android-sdk/       # Android SDK 34 + build-tools + platform-tools
├── gradle-cache/      # GRADLE_USER_HOME pro reprodukovatelné buildy
└── README.md          # bootstrap instrukce
```

**První setup** (manuálně, jednorázově):
```powershell
# z rootu repu
mkdir tools
# zkopíruj z zeddihub_tools_mobile/tools/* do tools/* (jdk17, android-sdk, gradle-cache prázdná)
# nebo stáhni Temurin 17 + AGP-compatible Android SDK
```

**Build**:
```powershell
$env:JAVA_HOME       = (Resolve-Path .\tools\jdk17).Path
$env:Path            = "$env:JAVA_HOME\bin;$env:Path"
$env:ANDROID_HOME    = (Resolve-Path .\tools\android-sdk).Path
$env:GRADLE_USER_HOME = (Resolve-Path .\tools\gradle-cache).Path
.\gradlew.bat clean :wear:assembleRelease --no-daemon
```

Výstup: `wear/build/outputs/apk/release/ZeddiHub-GearOS-{ver}.apk`

## 8. Distribuce

Watch APK doručen třemi cestami:

1. **Companion auto-install** (M1+) — mobile app obsahuje watch APK jako embedded asset (Wear App Bundle), Play Services automaticky installne na párované hodinky.
2. **Direct sideload** (developer/early access) — `apk_url` v `tools/data/staged_releases.json` rozšířený o `target=watch`. Wear-side update flow přes `app_releases.php`.
3. **Public download** — `zeddihub.eu/downloads/ZeddiHub-GearOS-{ver}.apk`. ADB sideload na watch (`adb -s <serial> install`).

## 9. Versioning

Sjednoceno s `staged_releases.json` schématem v `zeddihub-tools-website`. Nové pole `target`:

```json
{
  "version_code": 1,
  "version_name": "0.1.0",
  "target": "watch",
  "min_sdk": 30,
  "apk_url": "https://zeddihub.eu/downloads/ZeddiHub-GearOS-0.1.0.apk",
  "release_notes_cs": "První public alfa GearOS.",
  "release_notes_en": "First public alpha of GearOS.",
  "published_at": null,
  "live": false
}
```

`app_releases.php` rozšířen o filter `target=watch` (vedle `phone`).

## 10. Bezpečnostní rizika a mitigace

| Riziko | Mitigace |
|---|---|
| 6-digit pairing kód brute-force | Rate limit 5 pokusů / 5 min, lockout 1h, rotace každých 5 min |
| JWT na hodinkách (krádež watch) | Krátký TTL (24h), refresh přes mobile, panic button revokuje |
| Panic button false-positive | Long-press 3s + biometric confirm, undo grace 30s |
| Health data leak | Opt-in, anonymize na klientu, agg-only upload, retention 30d |
| Voice command misuse | Confirmation gate na destruktivní akce (restart/kill) |
| Geofence battery drain | Passive location only, 50m fence radius, 5min coalescing |
| Sleep detection abuse | Server-side max push lag 8h, override flag pro critical alerts |

## 11. Testing strategie

- **Unit**: domain logic (token refresh, pairing state machine, ping timeout).
- **Integration**: Wearable Data Layer mock (`MessageApiMock`).
- **Instrumentation**: Compose UI testy na Wear emulator (round 384px).
- **Manual**: GW6 Classic real device (single physical zařízení uživatele).

CI: zatím manuální lokální build (jako mobile repo). GitHub Actions workflow může přijít později (`build.yml` s self-hosted runner pokud bundled toolchain).

## 12. Otevřené otázky (k vyřešení v dalších sessions)

- [ ] Sample Samsung Health Sensor SDK approval — vyžaduje partner program (~3 týdny).
- [ ] Watch face Always-On Display optimalizace — limit 1Hz update v AOD.
- [ ] LTE varianta GW6 (ne Classic) — má vlastní eSIM, fallback fungování bez telefonu plně.
- [ ] Tile design system — držet si vlastní `TileColors` palette nebo dědit z `panel_settings.json` přes API?
- [ ] Localizace — cs/en jako mobile, případně Wear OS auto-fallback.

---

**Vlastník:** Zeddi
**Status:** M0 in progress
**Last update:** 2026-05-08
