# Pairing flow

> Jak GearOS získá přístup k uživatelově ZeddiHub účtu.

## Předpoklady

- Galaxy Watch 6 Classic (nebo jakýkoliv Wear OS 3+) s nainstalovanou GearOS.
- ZeddiHub účet (existující, založený přes web/mobile).
- Aspoň jeden způsob, jak vygenerovat pairing kód: web admin, mobile app.

## Krok 1 — Vygenerovat kód

### Z webu (existing user)

1. Login na `https://zeddihub.eu/admin/`.
2. Sidebar → **Klienti** → tab **Watches** (M1+ rozšíření `clients.php`).
3. Klikni "Generovat kód pro nové hodinky".
4. Web zobrazí 6 číslic (např. `427-915`) s 5min countdownem.

### Z mobile (paired phone)

1. Otevři ZeddiHub Tools mobile.
2. Settings → "Connect Watch" sekce (M1+ rozšíření).
3. Tap "Generate pairing code".
4. Stejný 6-digit kód, stejná 5min expirace.

Server-side: `POST /api/watch/pair-code/start { user_id }` → `{ code, expires_at }`. Kód uložen v `zh_watch_pair_codes (code_hash, user_id, expires_at)` tabulce.

## Krok 2 — Zadat na hodinkach

1. Otevři GearOS na hodinkách → uvítací obrazovka **PairingScreen**.
2. Bezel rotace prochází číslicemi (0-9), tap potvrdí.
3. Po každé číslici se kursor posune doprava. Zpět = swipe-dismiss back jednu pozici.
4. Po 6. číslici GearOS automaticky volá `POST /api/watch/pair { code, deviceLabel: "Galaxy Watch 6 Classic" }`.

Server validace:
- Hash 6-digit kódu, lookup v `zh_watch_pair_codes`.
- Pokud kód existuje a `expires_at > now()`:
  - Vygeneruj JWT s 24h TTL, claims `{ user_id, device_label, kind: "watch" }`.
  - Vytvoř row v `zh_watch_tokens (id, user_id, device_label, jwt_hash, paired_at)`.
  - Smaž row z `zh_watch_pair_codes` (one-shot).
  - Vrať `{ token, username, expires_at }`.
- Pokud expirovaný / neplatný:
  - Increment `failed_attempts`.
  - Po 5 selháních z jedné IP → 1h IP-level lockout.

## Krok 3 — Po pairingu

GearOS:
1. Token uložen do **EncryptedSharedPreferences** (`SecureTokenStorage.setAuthToken`).
2. Naviguje na **HomeScreen**.
3. Subscribe na Wearable Data Layer pro server status updates.

Server:
1. `zh_watch_tokens` row vytvořen, viditelný v admin tabu "Watches".
2. Heartbeat čeká (M1 GearOS po pairingu pošle `POST /api/watch/heartbeat { battery: 78, phoneConnected: true }`).

## Token refresh

GearOS držuje token 24h. Refresh paths:

### Companion path (preferred)
- `MessageClient` request `/zeddihub/auth/refresh` → mobile (který má dlouhodobý refresh token) volá `/api/auth/refresh` → vrátí čerstvý JWT → mobile pošle zpět přes Data Layer.
- Latence: 50-200ms.

### Standalone path (fallback)
- `POST /api/auth/refresh` přímo z hodinek (LTE/Wi-Fi).
- Vyžaduje aktuální (ještě validní) token v Authorization header.
- Rate limit: 1× / hodina (pro mitigaci replay útoků).
- Pokud token už expired → forced re-pairing.

## Revoke / unpair

### Z hodinek
- Settings → "Unpair watch" → klient lokálně smaže token + reset DataStore + push do `/api/watch/revoke`.

### Z webu / mobile
- Admin tab "Watches" → tlačítko "Revoke" u dané řádky.
- `UPDATE zh_watch_tokens SET revoked_at = NOW() WHERE id = ?`.
- Server-side každý další API call s revokovaným tokenem vrátí 401 → GearOS detekuje, zobrazí "Hodinky byly odpárovány" + naviguje na PairingScreen.

### Panic auto-revoke
- Panic button (M4) cascading invalidates **všechny** active tokeny napříč zařízeními (mobile, watch, web sessions).

## Bezpečnostní úvahy

### Brute-force kódu
- 6 číslic = 1M kombinací.
- 5 min TTL × 5 pokusů max → efektivní search space je 1 pokus / minutu.
- Po 5 selháních: IP lockout 1h.
- Server-side `zh_watch_pair_codes_audit` log s každým pokusem.

### Krádež hodinek
- Krátký 24h TTL → max okno zneužití.
- Standalone refresh požaduje validní starý token (zlodej musí ukrást včas).
- Panic z jiného zařízení invalidates okamžitě.

### MITM
- HTTPS enforced (`networkSecurityConfig` v manifestu).
- Certificate pinning na produkčním buildu (M2+).

### Replay
- Token JWT obsahuje `iat` + `jti`, server-side blacklist invalidovaných.
- Heartbeat má nonce v Authorization header (M3+).
