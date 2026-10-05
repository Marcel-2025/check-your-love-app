# iOS: So geht es auf dem Mac weiter

Das Projekt ist jetzt ein **Kotlin-Multiplatform-Projekt**. Android und iOS teilen sich
Ergebnislogik, Datenbank, Einstellungen, Monetarisierungslogik und die komplette Oberfläche
(Compose Multiplatform). Nur die Anbindung an Apple- und Google-SDKs ist pro Plattform eigen.

```
Check your Love/
├── shared/      gemeinsamer Kotlin-Code (Android + iOS) – hier passiert fast alles
├── app/         Android-App (Play Billing, AdMob Android, Teilen)
├── iosApp/      iOS-App (SwiftUI-Hülle, StoreKit 2, AdMob iOS, UMP, ATT)
│   ├── project.yml            Projektbeschreibung für XcodeGen
│   ├── Configuration/         Debug/Release-Werte, StoreKit-Testkonfiguration
│   └── iosApp/                Swift-Quellen, Info.plist, App-Icon
└── docs/        Website (GitHub Pages)
```

Was bereits geprüft ist: Der gemeinsame Kotlin-Code kompiliert für **iPhone und iPhone-Simulator**
(unter Linux/Windows möglich). Den Swift-Teil und das Zusammenbauen der App kann nur Xcode auf
dem Mac prüfen – rechne beim ersten Build mit ein, zwei kleinen Korrekturen (dafür eignet sich
Claude auf dem Mac sehr gut).

---

## 1. Mac einmalig einrichten (ca. 1 Stunde, vor allem Downloads)

1. **macOS aktualisieren** (Systemeinstellungen → Allgemein → Softwareupdate).
2. **Xcode** aus dem Mac App Store installieren, einmal öffnen und die angebotenen
   Komponenten inklusive **iOS-Plattform/Simulator** installieren.
3. **Android Studio für Mac** installieren und einmal öffnen (Setup-Assistent durchlaufen).
   Grund: Gradle braucht ein JDK und das Android SDK – auch für den iOS-Build, weil beide
   Plattformen in einem Projekt stecken.
4. **Homebrew** installieren (Anleitung auf https://brew.sh), danach im Terminal:
   ```bash
   brew install xcodegen
   ```
5. Optional, aber empfohlen: **Claude Desktop für Mac** installieren und den Projektordner
   verbinden. Dann kannst du Fehlermeldungen aus Xcode einfach einfügen.

## 2. Projekt auf den Mac holen

Am saubersten über ein **privates** GitHub-Repository (nicht das öffentliche Website-Repo):

1. Auf github.com ein neues **privates** Repository anlegen, z. B. `check-your-love-app`.
2. Unter Windows im Projektordner:
   ```bash
   git remote add app https://github.com/Marcel-2025/check-your-love-app.git
   git push -u app master
   ```
3. Auf dem Mac:
   ```bash
   cd ~/Developer          # oder ein anderer Ordner
   git clone https://github.com/Marcel-2025/check-your-love-app.git "Check your Love"
   ```

Bitte **nicht über iCloud Drive** synchronisieren – iCloud verträgt sich schlecht mit
`.git`- und Build-Ordnern (Konflikte, halb synchronisierte Dateien). USB-Stick/AirDrop
geht im Notfall auch.

Danach das Projekt einmal in **Android Studio auf dem Mac öffnen** – es legt `local.properties`
mit dem Pfad zum Android SDK an (z. B. `sdk.dir=/Users/<name>/Library/Android/sdk`).

## 3. Xcode-Projekt erzeugen und starten

```bash
cd "Check your Love/iosApp"
xcodegen generate
open CheckYourLove.xcodeproj
```

In Xcode:

1. Links das Projekt **CheckYourLove** → Target **CheckYourLove** → Reiter
   **Signing & Capabilities** → bei **Team** deine Apple-ID wählen (Xcode → Settings →
   Accounts → „+“ falls noch nicht angemeldet).
   Ohne kostenpflichtigen Entwickleraccount funktionieren Simulator und das eigene iPhone
   (7 Tage gültig) – für den App Store brauchst du das Developer Program (siehe unten).
2. Oben als Ziel einen Simulator wählen, z. B. **iPhone 16**, dann **▶︎ Run** (⌘R).
3. Der **erste Build dauert lange** (10–20 Minuten): Gradle lädt Kotlin/Native, Xcode lädt
   die Google-SDKs. Danach geht es deutlich schneller.

Wenn `xcodegen generate` später erneut nötig ist (z. B. nach neuen Swift-Dateien), einfach
wieder ausführen – das Xcode-Projekt wird neu erzeugt und ist bewusst nicht im Git.

## 4. Im Simulator testen

| Was | Wie |
|---|---|
| Liebestest, Verlauf, Teilen, Designs | normal bedienen; Teilen öffnet das iOS-Teilen-Menü |
| Werbung | im Debug-Build nur **Google-Testanzeigen** (Banner unten, Interstitial nach mehreren Tests, Video im Premium-Bereich) |
| Einwilligung (DSGVO) | erscheint, wenn AdMob eine Nachricht für die App hat; zum erneuten Testen: Einstellungen → Datenschutz → „Einwilligung zurücksetzen (Debug)“ |
| Tracking-Abfrage (ATT) | erscheint nach der Einwilligung einmalig |
| Premium-Kauf | läuft im Debug über die lokale **StoreKit-Testkonfiguration** (`Configuration/Products.storekit`, Testpreis 2,99 – nur lokal, kein echtes Geld) |
| Kauf ausstehend / erstatten | in Xcode: **Debug → StoreKit → Manage Transactions** (dort ablehnen, erstatten, „Ask to Buy“ aktivieren) |

Auf dem **eigenen iPhone**: per Kabel anschließen, auf dem iPhone unter Einstellungen →
Datenschutz & Sicherheit → **Entwicklermodus** einschalten, dann das iPhone als Ziel wählen.

## 5. Typische Stolpersteine beim ersten Build

- **„java: command not found“ / JAVA_HOME:** Android Studio installieren; das Build-Skript nutzt
  automatisch dessen JDK. Sonst `JAVA_HOME` in Xcode unter Build-Phasen setzen.
- **„SDK location not found“:** Projekt einmal in Android Studio öffnen (legt `local.properties` an).
- **Swift-Fehler in den Google-SDK-Aufrufen:** Google benennt Swift-APIs zwischen Versionen
  gelegentlich um. Fehlermeldung an Claude geben – meist ist es eine Zeile.
- **Signing-Fehler:** Team wählen; die Bundle-ID `dev.flondy.checkyourlove` muss in deinem
  Account noch frei sein (sonst vorübergehend z. B. `dev.flondy.checkyourlove.dev` verwenden).

---

## 6. Weg in den App Store

### 6.1 Apple Developer Program
- Auf https://developer.apple.com/programs/ als **Einzelperson** beitreten (99 USD bzw. Gegenwert
  pro Jahr). Apple-ID mit Zwei-Faktor-Authentifizierung nötig; Freischaltung dauert meist 1–2 Tage.

### 6.2 App in App Store Connect anlegen
1. https://appstoreconnect.apple.com → **Apps → „+“ → Neue App**: Plattform iOS,
   Name (muss im Store eindeutig sein, z. B. „Check your Love – Liebestest“), Primärsprache
   Deutsch, Bundle-ID `dev.flondy.checkyourlove`, SKU z. B. `checkyourlove-ios`.
2. **Geschäft → Verträge, Steuern und Bankdaten**: Vereinbarung für kostenpflichtige Apps
   abschließen, Bank- und Steuerdaten eintragen (Pflicht für In-App-Käufe).
3. **EU-Händlerstatus (DSA)** angeben: Wer Geld verdient, ist „Händler“ – Name, Anschrift,
   E-Mail und Telefon werden dann im EU-App-Store angezeigt.

### 6.3 Premium als In-App-Kauf
1. App → **Monetarisierung → In-App-Käufe → „+“**, Typ **Nicht verbrauchbar**.
2. Produkt-ID am besten gleich wie bei Android wählen (z. B. `premium_unlock`), Preis wählen,
   Anzeigename/Beschreibung DE+EN, Screenshot für die Prüfung anhängen.
3. Produkt-ID in `iosApp/Configuration/Release.xcconfig` bei `PREMIUM_PRODUCT_ID` eintragen.

### 6.4 Werbung (AdMob)
Du nutzt **dein bestehendes AdMob-Konto** – kein neues Konto, kein separater „Agent“:
1. AdMob → **Apps → App hinzufügen → iOS** („Check your Love“, nach Veröffentlichung mit dem
   App-Store-Eintrag verknüpfen).
2. Anzeigenblöcke anlegen: **Banner**, **Interstitial**, **Interstitial mit Prämie**.
3. App-ID und Block-IDs in `iosApp/Configuration/Release.xcconfig` eintragen
   (`ADMOB_APP_ID`, `AD_BANNER_ID`, `AD_INTERSTITIAL_ID`, `AD_REWARDED_ID`).
4. **Datenschutz & Mitteilungen**: die bestehende DSGVO-Nachricht zusätzlich auf die iOS-App
   anwenden; optional eine **IDFA-Erläuterung** (erscheint vor der ATT-Abfrage).
5. Die vorhandene `app-ads.txt` gilt automatisch auch für die iOS-App.

Ohne `ADMOB_APP_ID` startet das Werbe-SDK im Release gar nicht – die App läuft dann werbefrei.

### 6.5 App-Datenschutz („Datenschutz-Etiketten“)
In App Store Connect → **App-Datenschutz**. Eigene Daten erhebt die App keine (Namen und
Verlauf bleiben auf dem Gerät). Für Google AdMob sind laut Google u. a. anzugeben:
Kennungen (Geräte-ID), Nutzungsdaten (Produktinteraktion, Werbedaten), Diagnose, ungefährer
Standort – Zweck „Werbung von Drittanbietern“/„Analyse“; „zum Tracking verwendet“, wenn
personalisierte Werbung erlaubt wird. Aktuelle Liste: Google-Hilfe „Datenoffenlegung im App Store“.

### 6.6 Store-Eintrag
- Kategorie **Unterhaltung**, Altersfreigabe-Fragebogen ausfüllen.
- Datenschutz-URL: `https://marcel-2025.github.io/Check-your-Love/datenschutz.html`
  (die Datenschutzerklärung erwähnt bereits beide Plattformen sinngemäß; bei Bedarf um „App Store“ ergänzen).
- Support-URL: die Website.
- Screenshots für iPhone 6,9" (1320 × 2868 px) – einfach im Simulator „iPhone 16 Pro Max“
  mit ⌘S aufnehmen.

### 6.7 Hochladen und prüfen lassen
1. In Xcode als Ziel **Any iOS Device (arm64)** wählen → **Product → Archive**.
2. Im Organizer **Distribute App → App Store Connect → Upload**.
3. In App Store Connect unter **TestFlight** selbst testen (auch der Kauf ist dort kostenlos testbar).
4. Version ausfüllen, Build auswählen, **Zur Prüfung einreichen**. Hinweis für das Review-Team:
   „Unterhaltungs-App; Premium-Kauf ist ein Einmalkauf ohne Einfluss auf Ergebnisse.“
   Die Prüfung dauert meist 1–2 Tage. Apple ist bei sehr einfachen oder sehr ähnlichen Apps
   streng – die Zusatzfunktionen (Verlauf, Teilen-Karten, Designs) helfen hier.

---

## 7. Weiterentwickeln

- **Gemeinsame Änderungen** (Texte, Oberfläche, Logik) nur in `shared/` – wirken auf beiden
  Plattformen. Texte: `shared/src/commonMain/composeResources/values(-de)/strings.xml`.
- **Android** baust du wie bisher in Android Studio (Windows oder Mac).
- **iOS**: in Xcode ▶︎ Run; Xcode ruft Gradle automatisch auf.
- Prüfen ohne Mac (Windows): `gradlew :shared:compileKotlinIosArm64` kompiliert den
  gemeinsamen Code für iOS.
