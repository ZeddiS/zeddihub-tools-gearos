# Features — kompletní katalog

> Roadmap je v [plan.md §3](../plan.md). Tento dokument popisuje **každou** feature detailně — UX, závislosti, edge cases.

## Index

| Milestone | Feature | Status |
|---|---|---|
| M0 | Scaffolding | ✅ |
| M1 | 6-digit pairing | ⏳ |
| M1 | Wearable Data Layer companion | ⏳ |
| M2 | Server quick controls | ⏳ |
| M2 | ServerStatus tile | ⏳ |
| M2 | Ping complication | ⏳ |
| M2 | Alert count complication | ⏳ |
| M2 | Bezel-driven navigation | ⏳ |
| M3 | FCM push receive | ⏳ |
| M3 | Actionable notifications | ⏳ |
| M3 | Ongoing activity (CSAH) | ⏳ |
| M3 | Sleep-aware push routing | ⏳ |
| M4 | TOTP 2FA generator | ⏳ |
| M4 | Panic kill switch | ⏳ |
| M4 | Biometric unlock | ⏳ |
| M4 | Geofence auto-mode | ⏳ |
| M5 | Wi-Fi map quick scan | ⏳ |
| M5 | Find My Desktop | ⏳ |
| M5 | Voice commands | ⏳ |
| M5 | Macro/desktop trigger | ⏳ |
| M5 | Health → telemetry bridge | ⏳ |
| M5 | Custom watch face | ⏳ |

---

## M1 — Pairing + Companion sync

### 6-digit pairing

**UX flow:**
1. Uživatel v `zeddihub-tools-website/admin/clients.php?tab=watches` klikne "Generovat kód".
2. Web vrátí 6 číslic, expirují za 5 min.
3. Na watch otevře GearOS → PairingScreen.
4. Bezel rotace prohazuje aktuální číslici, tap potvrdí, posune na další pozici.
5. Po 6 číslicích → `POST /api/watch/pair { code, deviceLabel }`.
6. Server vrátí JWT s 24h TTL → uložen do `SecureTokenStorage`.

**Edge cases:**
- Wrong code → 1s haptic + zobrazí "Neplatný kód", zachová zadané číslice (ne reset).
- Expired code → "Kód vypršel, vygeneruj nový".
- 5 nesprávných pokusů / 5 min → IP-level lockout 1h (server-side).
- No internet → ukáže "Bez sítě, zkus později" + retry button.

### Wearable Data Layer companion

Po pairingu watch periodicky:
- Po `/zeddihub/auth/refresh` requestu vyšle handshake → mobile vrátí čerstvý token.
- Subscribe na `/zeddihub/server/status` data items → UI updatuje server cards.
- Listenuje pro `/zeddihub/push/relay` messages → vytvoří wear notifikaci.

**Battery saving:** Connection check per 30s, full sync per 5min při foreground, per 30min při background.

---

## M2 — Server quick controls

### HomeScreen → ScalingLazyColumn

ScalingLazyColumn (Wear Compose) zobrazí seznam serverů. Bezel rotace scrolluje, tap otevře `ServerDetailScreen`.

**Karta serveru:**
- Status dot (zelená/oranžová/červená)
- Jméno + ping (ms)
- Last action timestamp

### ServerDetailScreen

Confirm-gated akce:
- **Restart** (long-press 1s na orange button)
- **Stop** (long-press 1s na red button)
- **Start** (single tap, low risk)
- **Ping now** (no confirm)

Po akci: ongoing activity banner s progress (M3).

### Tile + Complications

| Surface | Co zobrazí | Update perioda |
|---|---|---|
| `ServerStatusTileService` | Top 3 servery, status + ping | Każdých 10 min nebo on-demand |
| `PingComplicationService` | Ping latence k primárnímu serveru | 10 min |
| `AlertCountComplicationService` | Count nepřečtených pushů | On change (broadcast) |

### Bezel-driven navigation

`Horologist` `RotaryHandler` namapovaný na:
- Server list scroll (HomeScreen)
- Cycle mezi servery v ServerDetail (vlevo/vpravo bezel)
- Audit log scroll
- DND intensity slider

Bezel **nikdy** nepouštět destruktivní akce (anti-misclick).

---

## M3 — Push notifications

### FCM receive

Watch má vlastní FCM token, registruje do `zh_push_tokens` s `platform=watch`. Push payload formát identický s mobile (`type`, `title`, `body`, `severity`, `actions[]`).

### Actionable notifications

Wear `NotificationCompat.Action` s `RemoteInput` (voice reply) nebo přímé akční tlačítka:
- **Schválit / Zamítnout** (login z neznámé IP)
- **Restart now / Later** (server alert)
- **Ack** (info push)

Akce → `MessageClient` → mobile → API call.

### Ongoing activity (CSAH)

`androidx.core.app.NotificationCompat.Builder` s `setOngoing(true)` + `setProgress()`. Galaxy Watch UI ukazuje banner "Restart Minecraft serveru… 67%". Cancel button = abort.

### Sleep-aware push routing

`HealthServicesClient.measureClient.registerMeasureCallback(SLEEP_STATE)`:
- `AWAKE` → standard push.
- `LIGHT_SLEEP / DEEP_SLEEP / REM` → push queue na server-side (heartbeat reportuje `device_state`).
- Critical pushe (severity=critical) override sleep gate.

---

## M4 — Bezpečnost

### TOTP 2FA generator

Seedy synced z mobile přes `DataClient` (encrypted blob, AES dekryptováno klíčem odvozeným z auth tokenu).

UI:
- ScalingLazyColumn s TOTP účty (label + 6-digit kód + countdown ring).
- Tap kód → copy to clipboard + 5s haptic confirm.

**Rotace:** 30s standard. Countdown ring se přeleje accent color při < 5s remaining.

### Panic kill switch

**Tile** s "PANIC" labelem. Long-press 3s + biometric prompt → `POST /api/auth/panic`:
- Invalidate **všechny** session tokeny napříč zařízeními.
- Kick uživatele z game serverů.
- Flag account `panic_triggered_at`.
- Send notification ("PANIC triggered from GearOS, all sessions revoked").

**Undo grace 30s** — server drží action v queue, watch ukáže "Cancel panic" countdown. Po 30s = irreversible.

### Biometric unlock

`androidx.biometric.BiometricPrompt` na Wear (GW6 Classic má fingerprint? **Ne** — pouze PIN/pattern). Fallback PIN screen.

Triggered při:
- Otevření TOTP screen
- Panic confirm
- Server destructive action (stop, deletování konfigurace)

### Geofence auto-mode

Opt-in v Settings. `FusedLocationProviderClient` passive mode:
- Doma (50m fence) → "Relax mode" (méně agresivní notifikace, žádný panic auto-arm).
- Mimo → "Vigilant mode" (push pro každý login, panic 1-tap accessible).

Battery: Passive only, ne active polling. ~3% denně dle Google Play Services dat.

---

## M5 — Power user

### Wi-Fi map quick scan

Tap watch tile → 1× scan okolních APek → upload do `/api/wifi-map/`. UI ukáže count okolních sítí + last upload timestamp.

### Find My Desktop

`POST /api/desktop/locate { user_id }` → desktop ZeddiHubTools.exe poll endpoint detekuje request → spustí loud sound + flash screen na 30s. Watch ukáže "Hledá se PC… (30s)".

### Voice commands

Wear OS Assistant intent filter v manifestu. Frázi "OK Google, restart Minecraft serveru" → spustí GearOS s deep link `gearos://server/<id>/restart` → confirm gate (long-press) → API call.

Whitelisted phrasings v `voice_commands.xml`:
- "restart [server name]"
- "ping [server name]"
- "panic"
- "ack last alert"

### Macro/desktop trigger

List desktopových maker (z `zeddihub_tools_desktop` definitions). Tap = `POST /api/desktop/macro/trigger { macro_id }`. Vyžaduje desktop online (heartbeat < 5min).

### Health → telemetry bridge

Opt-in toggle v Settings. Subscribe na `HealthServicesClient` PassiveListenerService:
- Heart rate (avg per 5min)
- Steps (cumulative reset 00:00)
- Sleep state changes

Upload do `telemetry_log.json` jako `kind=health`, agg only (nikdy raw bio data point), 30d retention.

### Custom watch face

`WatchFaceService` (canvas-based pro flexibility). Render budget:
- Interactive mode: 60Hz, full color, all elements.
- AOD: 1Hz, dimmed palette, only time + 1 server status dot.

Configurable přes `WatchFaceEditor`:
- Accent color (čte z `panel_settings.json` přes API)
- Server dots count (1-3)
- Show/hide push count badge
