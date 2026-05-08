# ZeddiHub GearOS

Wear OS aplikace pro Samsung Galaxy Watch 6 Classic (a další Wear OS 3+ hodinky), která rozšiřuje [ZeddiHub Tools](https://zeddihub.eu) na zápěstí.

> **Status:** Alfa — M0 (scaffolding). První funkční pairing flow přijde v M1.

## Co GearOS dělá

- **Server quick controls** — restart/start/stop game serverů z hodinek
- **Push notifikace s actionable replies** — schválit/zamítnout přímo z watch face
- **TOTP 2FA generátor** + **Panic kill switch**
- **Wi-Fi map quick scan**, **Find My Desktop**, **macro triggers**
- **Bezel-driven navigation**, tiles, complications a custom watch face
- **Sleep-aware push routing** + opt-in **Health → telemetry bridge**

Plné feature breakdown je v [plan.md](plan.md).

## Architektura — Hybrid

GearOS funguje jako **companion** se `zeddihub_tools_mobile` (Wearable Data Layer přes BT) a má **standalone fallback** na `zeddihub.eu` API přes LTE/Wi-Fi, když telefon není v dosahu.

Detaily v [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Build

GearOS používá bundled toolchain (JDK 17 + Android SDK 34 + Gradle cache) — stejný pattern jako [zeddihub_tools_mobile](https://github.com/ZeddiS/zeddihub-tools-mobile).

**Bootstrap** (jednorázově): viz [tools/README.md](tools/README.md).

**Build APK** (PowerShell, z rootu repu):

```powershell
$env:JAVA_HOME       = (Resolve-Path .\tools\jdk17).Path
$env:Path            = "$env:JAVA_HOME\bin;$env:Path"
$env:ANDROID_HOME    = (Resolve-Path .\tools\android-sdk).Path
$env:GRADLE_USER_HOME = (Resolve-Path .\tools\gradle-cache).Path
.\gradlew.bat clean :wear:assembleRelease --no-daemon
```

Výstup: `wear/build/outputs/apk/release/ZeddiHub-GearOS-{ver}.apk`

## Instalace na hodinky

**Sideload (developer flow):**

1. V Galaxy Watch zapnout vývojářský režim (Settings → About watch → Software → 7× tap "Software version") a ADB debugging.
2. Spárovat watch s PC přes Wi-Fi ADB (`adb pair <watch-ip>:<port>` → kód z hodinek).
3. `adb -s <serial> install wear/build/outputs/apk/release/ZeddiHub-GearOS-0.1.0.apk`.

**Companion auto-install** (od M1+): instalací `zeddihub_tools_mobile` na párovaný telefon Play Services automaticky doručí watch APK.

## Kontext v rámci ZeddiHub

```
zeddihub-tools-website  ◄── public landing + admin + API (zeddihub.eu)
zeddihub_tools_mobile   ◄── Android phone app (companion partner)
zeddihub_tools_desktop  ◄── Windows desktop tool
zeddihub-tools-gearos   ◄── ▼ tento repo (Wear OS)
```

## Licence

[MIT](LICENSE)

## Autor

[Zeddi](https://zeddihub.eu) · 2026
