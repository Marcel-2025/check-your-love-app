# Check your Love 2.0

Liebestest-App (Unterhaltung) – vollständig neu aufgebaut mit Kotlin, Jetpack Compose und Material 3.
Paketname `dev.flondy.checkyourlove`, versionCode 4, versionName 2.0.

## Aufbau

```
app/src/main/java/dev/flondy/checkyourlove/
├── CheckYourLoveApp.kt        Application + AppContainer (manuelle DI)
├── MainActivity.kt            einzige Activity, Splash, Edge-to-Edge
├── core/                      Ergebnislogik (rein, ohne Android) + Ergebnistexte
├── data/                      Room-DB (Verlauf, Migration), DataStore (Einstellungen)
├── monetization/              Premium-Katalog, Berechtigungen, Billing, Werbung
└── ui/                        Theme, Komponenten, Screens (Test, Verlauf, Einstellungen, Premium, Teilen)
```

- UI-Zustand in ViewModels (`StateFlow`), Daten in Repositories, keine Arbeit auf dem Hauptthread
  (Room/DataStore suspend, Teilen-Bilder auf `Dispatchers.Default`).
- Navigation mit Navigation-Compose statt wiederholtem `setContentView()`.
- Rotation: ViewModels bleiben erhalten; Prozessende: Namen und Ergebnisanzeige über `SavedStateHandle`.

## Ergebnislogik (Version 2)

1. Namen normalisieren: Unicode NFKC, unsichtbare Zeichen entfernen, sprachunabhängig klein,
   Leerraum zusammenfassen. Diakritika bleiben erhalten („Jürgen“ ≠ „Jurgen“).
2. Beide Namen sortieren → Reihenfolge egal.
3. SHA-256 über `CheckYourLove/v2 ␟ name1 ␟ name2` → `Wert mod 101` = 0–100 (gleichverteilt).
4. Ergebnistext: 6 Bereiche (0–19, 20–39, 40–59, 60–79, 80–94, 95–100) mit je 3 Varianten,
   Auswahl ebenfalls deterministisch.

Validierung: nicht leer, mindestens ein Buchstabe (beliebige Schrift), max. 40 Zeichen.
Premium, Werbung oder Einstellungen fließen nie in die Berechnung ein.

**Unterschied zur alten Version:** Früher bekam jeder Name einen Zufallswert 0–49 (Summe 0–98)
und die Datenbank wurde bei jedem Start gelöscht – Ergebnisse waren also nicht reproduzierbar.
Die neue Logik liefert für dasselbe Paar immer dasselbe Ergebnis.

## Datenmigration

Die Datenbankdatei `CheckYourLove.db` wird weiterverwendet. Migration 1 → 2 kopiert alle Zeilen
der alten Tabelle `NameValuePairs` unverändert nach `legacy_name_values` (bleiben erhalten,
werden aber nicht mehr verwendet) und legt `love_results` für Verlauf/Favoriten an.
Getestet in `MigrationTest` mit exakt dem Schema der alten `DbHelper`-Klasse.

## Build & Tests

```bash
./gradlew assembleDebug            # Debug-APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest        # 47 Tests (Logik, Migration, Käufe, UI-Abläufe)
./gradlew testDebugUnitTest -Proborazzi.test.record=true   # zusätzlich Screenshots nach app/build/screenshots
./gradlew lintDebug
```

Release: siehe [docs/MONETARISIERUNG.md](docs/MONETARISIERUNG.md) – ohne AdMob-App-ID bricht
`assembleRelease` bewusst mit einer Anleitung ab.

### Versionen

AGP 8.13.2, Gradle 8.14.3, Kotlin 2.3.21, Compose BOM 2026.06.01 (Compose 1.11, Material 3 1.4),
compileSdk/targetSdk 36, minSdk 24, Room 2.8.5, Play Billing 9.1.0, Google Mobile Ads 25.5.0,
UMP 4.0.0.

Compose 1.12 / core-ktx 1.19 verlangen AGP 9.1 und compileSdk 37. Da das Projekt zuletzt mit
AGP 8.13 in Android Studio geöffnet wurde, ist bewusst die neueste dazu kompatible Linie gewählt.
Ein späteres Upgrade gelingt mit dem AGP-Upgrade-Assistenten.

minSdk stieg von 21 auf 24, weil Google Mobile Ads 25 und Play Billing 8+ das voraussetzen
(Android 5/6 entfallen). targetSdk stieg von 24 auf 36 (Play-Pflicht).

## Entfernt

Alte Java-Klassen, XML-Layouts, Debug-Toasts, auskommentierter Altcode, ungenutzte Bilder
(u. a. ein 14.173 × 22.677 px großes PNG mit 3,8 MB), Social-Media-Buttons und der
PayPal-Spendenlink (externe Zahlungslinks in Play-Apps sind problematisch). Erhalten blieben
Website, Play-Entwicklerseite und Kontakt-E-Mail.

## Screenshots

`docs/screenshots/` – erzeugt mit Robolectric/Roborazzi (kein Emulator verfügbar).
