# Bundled toolchain

GearOS staví **lokálně** s vlastním JDK, Android SDK a Gradle cache, aby build běžel bez systémových závislostí. Stejný pattern jako [zeddihub_tools_mobile](https://github.com/ZeddiS/zeddihub-tools-mobile).

> **Tato složka NENÍ v gitu.** `.gitignore` vylučuje `tools/jdk17/`, `tools/android-sdk/`, `tools/gradle-cache/`. Bootstrap si dělá každý dev sám.

## Co tu má být

```
tools/
├── jdk17/             # Eclipse Temurin 17 (~300 MB)
├── android-sdk/       # Android SDK 34 + build-tools 34.0.0 + platform-tools (~3 GB)
├── gradle-cache/      # GRADLE_USER_HOME (po prvním buildu ~500 MB)
└── README.md          # tento soubor
```

## Bootstrap — varianta A (kopírovat z mobile repu)

Pokud už máš `zeddihub_tools_mobile` lokálně:

```powershell
# z rootu zeddihub-tools-gearos:
robocopy ..\zeddihub_tools_mobile\tools\jdk17       tools\jdk17       /MIR /NFL /NDL
robocopy ..\zeddihub_tools_mobile\tools\android-sdk tools\android-sdk /MIR /NFL /NDL
mkdir tools\gradle-cache  # prázdná, naplní se při prvním buildu
```

Toto je nejrychlejší způsob. JDK a SDK jsou identické pro mobile + watch builds.

## Bootstrap — varianta B (čerstvý download)

### JDK 17

```powershell
# Eclipse Temurin 17 LTS
$jdkUrl = "https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse"
Invoke-WebRequest -Uri $jdkUrl -OutFile temurin17.zip
Expand-Archive -Path temurin17.zip -DestinationPath tools\
Move-Item tools\jdk-17* tools\jdk17
Remove-Item temurin17.zip
```

### Android SDK 34

```powershell
# command-line tools
$sdkUrl = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
Invoke-WebRequest -Uri $sdkUrl -OutFile cmdline.zip
Expand-Archive -Path cmdline.zip -DestinationPath tools\android-sdk\cmdline-tools-tmp
mkdir tools\android-sdk\cmdline-tools\latest -ea 0
Move-Item tools\android-sdk\cmdline-tools-tmp\cmdline-tools\* tools\android-sdk\cmdline-tools\latest\
Remove-Item -Recurse tools\android-sdk\cmdline-tools-tmp
Remove-Item cmdline.zip

# license accept + install platform 34 + build-tools
$env:JAVA_HOME = (Resolve-Path .\tools\jdk17).Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
echo y | tools\android-sdk\cmdline-tools\latest\bin\sdkmanager.bat --licenses
tools\android-sdk\cmdline-tools\latest\bin\sdkmanager.bat "platforms;android-34" "build-tools;34.0.0" "platform-tools"
```

### Gradle cache

Prázdná složka, Gradle si naplní při prvním `gradlew assembleRelease`.

```powershell
mkdir tools\gradle-cache
```

## Build

Z rootu repu:

```powershell
$env:JAVA_HOME        = (Resolve-Path .\tools\jdk17).Path
$env:Path             = "$env:JAVA_HOME\bin;$env:Path"
$env:ANDROID_HOME     = (Resolve-Path .\tools\android-sdk).Path
$env:GRADLE_USER_HOME = (Resolve-Path .\tools\gradle-cache).Path

# Debug APK (rychlé, bez release keystore)
.\gradlew.bat clean :wear:assembleDebug --no-daemon

# Release APK (vyžaduje keystore.properties — viz keystore.properties.example)
.\gradlew.bat clean :wear:assembleRelease --no-daemon
```

Výstup:
- Debug: `wear/build/outputs/apk/debug/ZeddiHub-GearOS-0.1.0-debug.apk`
- Release: `wear/build/outputs/apk/release/ZeddiHub-GearOS-0.1.0.apk`

## Install na hodinky (ADB Wi-Fi)

1. Watch: Settings → About watch → Software → 7× tap "Software version" → vývojářské volby.
2. Settings → Vývojářské → ADB debugging + Wireless debugging zapnout.
3. Wireless debugging → "Pair new device" → ukáže IP:PORT + 6-digit kód.

```powershell
# z PC s tools\android-sdk v PATH:
adb pair 192.168.1.42:34567   # zadej kód
adb connect 192.168.1.42:5555
adb devices  # ověř že watch je vidět

adb -s <serial> install -r wear\build\outputs\apk\release\ZeddiHub-GearOS-0.1.0.apk
adb -s <serial> shell am start -n com.zeddihub.gearos/.MainActivity
```

## Troubleshooting

| Problém | Řešení |
|---|---|
| `JAVA_HOME is not set` | Spustit `$env:JAVA_HOME = (Resolve-Path .\tools\jdk17).Path` před gradlew. |
| Gradle stahuje `gradle-X.X-bin.zip` opakovaně | `GRADLE_USER_HOME` neukazuje na `tools\gradle-cache`. Nastav před každým buildem. |
| `SDK location not found` | `local.properties` chybí nebo `sdk.dir` nesprávný. Zkopíruj z `local.properties.example` a uprav cestu. |
| Build zamrzne na `Daemon will be stopped at the end` | Vždycky používat `--no-daemon` na Windows. |
| `Failed to find target with hash string 'android-34'` | Chybí platform-34 v SDK. Spustit `sdkmanager "platforms;android-34"`. |
| `No matching client found for package name` | Chybí `google-services.json`. M3+ feature, zatím není potřeba. |
