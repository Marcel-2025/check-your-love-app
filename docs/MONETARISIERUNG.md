# Monetarisierung – Stand und offene Schritte

Modell: **dezente Werbung + einmaliger Premium-Kauf** (kein Abo). Die Berechnung und die
Ergebnisanzeige bleiben immer kostenlos. Premium verändert nie ein Ergebnis.

## Übersicht

| Baustein | Umsetzung im Code | Getestet | Noch extern nötig |
|---|---|---|---|
| Einwilligung (UMP 4.0) | fertig – Abfrage vor jeder Anzeigenanfrage, Datenschutz-Optionen in den Einstellungen, Debug-Reset | Ablauf in Robolectric ohne Absturz; echtes Formular nur auf Gerät prüfbar | DSGVO-Nachricht in AdMob → *Datenschutz & Mitteilungen* anlegen |
| Banner (adaptiv, verankert) | fertig – nur auf „Liebestest“ und „Verlauf“, eigener Bereich über der Navigation, ausgeblendet bei Tastatur und Ladefehler | Layout per Screenshot; echte Auslieferung nicht prüfbar | AdMob-App-ID eintragen; vorhandene Banner-ID `…/4122761666` prüfen |
| Interstitial | fertig – nur bei „Neuer Test“, frühestens ab Test 3 der Sitzung, mind. 4 Tests **und** 5 Minuten Abstand | Häufigkeitslogik per Unit-Test | Anzeigenblock in AdMob anlegen → `cyl.admobInterstitialId` |
| Rewarded (24 h Design/Effekt/Vorlage) | fertig – Erklärdialog vorab, Belohnung nur im `OnUserEarnedRewardListener`, automatische Sperre nach 24 h | Ablauf/Abbruch per UI-Test, Ablauflogik per Unit-Test; echte Videos nur auf Gerät | Anzeigenblock anlegen → `cyl.admobRewardedId` |
| Premium-Kauf (Play Billing 9.1) | fertig – Kauf, ausstehend, Abbruch, Fehler, Bestätigung (acknowledge), Wiederherstellung, Erstattung/Widerruf beim nächsten Abgleich | Kauflogik per Unit-Test, kompletter Ablauf mit **Simulation** (nur Debug) per UI-Test | In-App-Produkt anlegen → `cyl.premiumProductId`, Lizenzschlüssel → `cyl.playLicenseKey` |
| Kaufprüfung | Signaturprüfung mit öffentlichem Lizenzschlüssel; keine lokale „Premium = true“-Variable | Unit-Test mit echtem RSA-Schlüssel | Schlüssel eintragen (siehe unten) |

Ohne Konfiguration gilt:
- **Debug-Build**: immer offizielle Google-Testanzeigen; Premium läuft als deutlich markierte
  *Simulation* (kein Geld, keine Verbindung zu Google Play).
- **Release-Build**: bricht ohne `cyl.admobAppId` mit einer klaren Meldung ab (das Werbe-SDK
  würde sonst beim Start abstürzen). Ohne Produkt-ID wird Premium nicht angeboten,
  Interstitial/Rewarded ohne ID sind abgeschaltet. Simulierte Käufe sind im Release unmöglich.

Alle Werte stehen in `monetization.properties` (keine Geheimnisse) und können per
`-Pcyl.…=…` überschrieben werden.

## Schritte in AdMob

1. AdMob → *Apps* → App „Check your Love“ (Paket `dev.flondy.checkyourlove`) auswählen bzw.
   mit dem Play-Eintrag verknüpfen. Die **App-ID** (`ca-app-pub-5414395840028991~…`)
   in `cyl.admobAppId` eintragen.
2. Prüfen, ob der bisherige Banner-Block `ca-app-pub-5414395840028991/4122761666` noch aktiv
   ist (er stammt aus dem alten Code). Sonst neuen Block „Banner“ anlegen.
3. Zwei neue Anzeigenblöcke anlegen: **Interstitial** und **Mit Prämie** (Prämie z. B.
   „1 Freischaltung“). IDs in `cyl.admobInterstitialId` und `cyl.admobRewardedId`.
4. *Datenschutz & Mitteilungen* → **DSGVO**-Nachricht erstellen und veröffentlichen
   (ggf. auch US-Bundesstaaten). Ohne veröffentlichte Nachricht zeigt UMP kein Formular.
5. Unter *Blockierungseinstellungen* ggf. sensible Kategorien sperren; die App fordert
   bereits nur Anzeigen bis Freigabe „T“ (Teen) an.
6. `app-ads.txt` auf der in Play hinterlegten Entwickler-Website (z. B. flondy.com)
   veröffentlichen.
7. Zum Testen eigener Geräte: Testgeräte in AdMob hinterlegen – im Debug-Build werden
   ohnehin nur Testanzeigen geladen.

## Schritte in der Play Console

1. **Monetarisieren → Produkte → In-App-Produkte**: Einmalprodukt anlegen, z. B.
   `premium_unlock` (ID frei wählbar, danach nicht änderbar), Preis festlegen, aktivieren.
   ID in `cyl.premiumProductId` eintragen. Die App zeigt ausschließlich den von Play
   gelieferten Preis.
2. **Monetarisierung einrichten → Lizenzierung**: Base64-„öffentlichen Schlüssel“ kopieren →
   `cyl.playLicenseKey`.
3. **Einstellungen → Lizenztests**: eigene Google-Konten als Lizenztester eintragen
   (Testkäufe ohne Belastung, inkl. „langsame Testkarte“ für ausstehende Käufe).
4. App-Bundle (versionCode 4) in einen **internen Test-Track** hochladen – erst dann
   liefert Play die Produktdaten an Testgeräte.
5. **App-Inhalte**:
   - *Werbung*: „Enthält Werbung“ = Ja.
   - *Werbe-ID*: Ja (das Werbe-SDK fordert `AD_ID` an) – Zweck „Werbung oder Marketing“.
   - *Zielgruppe*: Empfehlung 13+ bzw. 16+ (Liebestest). Bei Einbeziehung von Kindern unter 13
     gilt die Familienrichtlinie – dann müssten Werbeeinstellungen angepasst werden.
   - *Datensicherheit*: Namen/Verlauf bleiben lokal (nicht erhoben). Das Werbe-SDK erhebt
     Geräte-/Werbe-IDs, ungefähren Standort (IP), App-Interaktionen, Diagnosedaten –
     Angaben laut aktueller Google-Mobile-Ads-Dokumentation übernehmen.
   - *Datenschutzerklärung*: **Pflicht** bei Werbung. Eine vorhandene, echte URL in
     `cyl.privacyPolicyUrl` eintragen (dann erscheint der Menüpunkt in der App) und in der
     Play Console hinterlegen. Die App erfindet keinen Link.
   - *Altersfreigabe (IARC)*-Fragebogen ausfüllen.
6. Release-Signierung wie bisher (Play App Signing); im Projekt ist keine Signierung hinterlegt.

## Testen auf einem echten Gerät (empfohlen vor dem Release)

- Debug-Build: Testanzeigen (Banner, Interstitial nach mehreren Tests, Rewarded im
  Premium-Bereich), Einwilligung per „Einwilligung zurücksetzen (Debug)“ erneut auslösen.
- Release-Kandidat mit echten IDs aus dem internen Test-Track installieren, mit
  Lizenztester-Konto: Kauf erfolgreich, Abbruch, „langsame Testkarte“ (ausstehend →
  später bestätigt), App neu installieren → „Käufe wiederherstellen“, Testkauf in der
  Play Console erstatten/widerrufen → nach erneutem App-Start ist Premium entfernt.
- Flugmodus: Test, Verlauf, Teilen funktionieren; Werbung fehlt ohne Fehlermeldung.

## Warum keine Server-Validierung?

Ein Server würde laufende Kosten verursachen. Für einen einmaligen, rein kosmetischen Kauf
ist die Kombination aus Play-Abfrage bei jedem App-Start (`queryPurchasesAsync`, erkennt
Erstattungen) und lokaler Signaturprüfung angemessen. Wer später ein Backend betreibt, kann
die Prüfung über die Google Play Developer API ergänzen.
