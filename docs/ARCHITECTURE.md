# Architektura

> Detailnější popis Hybrid architektury popsané v [plan.md §2](../plan.md).

## Vrstvy

```
┌─────────────────────────────────────────────────────────────┐
│  ui/                          Compose for Wear screens      │
│  ├── pairing/                  PairingScreen + ViewModel    │
│  ├── home/                     HomeScreen (server cards)    │
│  ├── servers/                  ServerDetail (M2)            │
│  ├── alerts/                   AlertList (M3)               │
│  ├── totp/                     TotpScreen (M4)              │
│  └── settings/                 Settings (M5)                │
├─────────────────────────────────────────────────────────────┤
│  domain/                      Use-case + repository ifaces  │
├─────────────────────────────────────────────────────────────┤
│  data/                                                      │
│  ├── auth/         SecureTokenStorage (Keystore-backed)     │
│  ├── pairing/      PairingRepository (6-digit flow)         │
│  ├── sync/         WearableConnection + DataLayer client    │
│  ├── api/          Retrofit GearOsApi (standalone fallback) │
│  ├── local/        AppPreferences (DataStore)               │
│  └── push/         FCM service (M3)                         │
├─────────────────────────────────────────────────────────────┤
│  tiles/, complications/, watchface/   Background services   │
│  voice/                       Wear OS Assistant intents     │
│  health/                      Health Services bridge        │
├─────────────────────────────────────────────────────────────┤
│  di/                          Hilt SingletonComponent       │
│  util/                        Helpery, formatters           │
└─────────────────────────────────────────────────────────────┘
```

## Hybrid sync — rozhodovací strom

```
[uživatel zmáčkne "Restart server X"]
            │
            ▼
   WearableConnection.current()
            │
   ┌────────┴────────┐
   ▼                 ▼
CONNECTED       DISCONNECTED
   │                 │
   ▼                 ▼
MessageClient.   GearOsApi.serverAction()
sendMessage()    (přes LTE/Wi-Fi)
   │                 │
   ▼                 ▼
mobile přijme    server přímý ACK
listener,
provede HTTP
call do api/,
ACK zpět
   │                 │
   └─────────┬───────┘
             ▼
   UI zobrazí výsledek
```

## Auth state machine

```
                ┌──── invalidate (panic, revoke, expire) ────┐
                ▼                                            │
     ┌──────────────────┐                                    │
     │     Unpaired     │  ← token = null                    │
     └────────┬─────────┘                                    │
              │ user zadá 6-digit kód                        │
              ▼                                              │
     ┌──────────────────┐                                    │
     │     Pairing      │  ← API call /api/watch/pair        │
     └────────┬─────────┘                                    │
              │ success → token, expiresAt                   │
              ▼                                              │
     ┌──────────────────┐                                    │
     │   Authenticated  │ ─── refresh přes Data Layer ───┐  │
     └────────┬─────────┘                                │  │
              │                                           │  │
              └─────── token expired ─────────────────────┘  │
                                                              │
                       ┌── všude může nastat ─────────────────┘
```

## Data Layer protokol

Wearable Data Layer má dva primární mechanismy:

| Mechanismus | Použití | Latence | Velikost |
|---|---|---|---|
| `MessageClient` | Stateless RPC (akce, přečtení) | Nízká | < 100 KB |
| `DataClient` | Sync state (last seen alert, push payload) | Střední | < 1 MB |

GearOS používá:
- **MessageClient** pro: pairing handshake, server akce, panic call.
- **DataClient** pro: server status snapshot (mobile periodicky updatuje, watch čte).

**Path konvence** (`/zeddihub/<area>/<action>`):

```
/zeddihub/pair/handshake          MessageClient (req/res)
/zeddihub/auth/refresh             MessageClient
/zeddihub/server/status            DataClient (mobile push, watch read)
/zeddihub/server/action            MessageClient
/zeddihub/push/relay               MessageClient (mobile → watch)
/zeddihub/push/ack                 MessageClient (watch → mobile)
/zeddihub/panic/trigger            MessageClient
/zeddihub/totp/seeds               DataClient (encrypted blob, sync přes mobile master)
```

## Bezpečnost

### Token lifecycle
- TTL 24h (krátký, watch má vyšší ztrátové riziko než phone).
- Refresh přes mobile (Data Layer) — mobile má dlouhodobý refresh token.
- Standalone refresh path (přes `/api/auth/refresh`) má rate limit 1× / hodina.

### Encrypted storage
- Auth token + TOTP seedy v **EncryptedSharedPreferences** (AES256-GCM, klíč v Android Keystore).
- DataStore pro non-sensitive data (lastUsername, UI prefs).

### Panic gate
- Long-press 3s + biometric prompt pro spuštění.
- 30s undo grace na server-side (kódy se invalidují až po grace okně).

### Sleep-aware push (M3)
- Watch reportuje `device_state: sleeping` přes heartbeat.
- Server `push.php` worker drží non-critical pushe v queue, doručí ráno.
- `severity: critical` flag overridne sleep gate.

## Tile + Complication isolation

Tile a Complication services běží v **separátním procesu** (Wear OS sandbox). Důsledky:
- Žádný přímý přístup k Hilt graph z `MainActivity` procesu.
- Sdílení dat přes:
  - `DataStore` (suspending read v Tile builder) — jednoduché read-only.
  - `WorkManager` worker, který připraví snapshot do DataStore — komplexnější update.

Pro GearOS M2: každý Tile má vlastní lightweight Hilt subcomponent (`@AndroidEntryPoint`-style approach + manuální injection).

## Build varianty

```
buildTypes:
  debug      → applicationId com.zeddihub.gearos.debug, debug keystore, vCode bumped on each build
  release    → applicationId com.zeddihub.gearos, release keystore, R8 minify+shrink

flavors (M3+):
  fcmProd    → BuildConfig.FCM_TOKEN_ENDPOINT = "https://zeddihub.eu/api/push/register"
  fcmStaging → "https://staging.zeddihub.eu/..."
```

## Versioning

Viz [plan.md §9](../plan.md). Watch má vlastní version line (0.x.x), independent na mobile/web 0.9.3.
