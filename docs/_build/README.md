# Website (GitHub Pages)

Statische Landingpage mit Datenschutzerklärung und Impressum, Deutsch und Englisch.
Keine Cookies, kein Tracking, alle Schriften und Bilder lokal.

| Datei | Inhalt |
|---|---|
| `index.html` / `en/index.html` | Landingpage DE / EN |
| `datenschutz.html` / `en/privacy.html` | Datenschutzerklärung DE / EN |
| `impressum.html` / `en/imprint.html` | Impressum DE / EN |
| `app-ads.txt` | AdMob-Verifizierung (siehe unten) |
| `_build/build_site.py` | Generator – Texte hier ändern, dann neu erzeugen |

> Hinweis: Jekyll (Standard bei GitHub Pages) veröffentlicht Ordner mit `_` am Anfang nicht –
> der Generator und diese README bleiben also unsichtbar.

## Vor dem Veröffentlichen

1. **Anbieterangaben** stehen in `SITE` oben in `_build/build_site.py` (Name, Anschrift, E-Mail).
   Nach Änderungen im Projektordner ausführen: `python3 docs/_build/build_site.py`
   Für ein vollständiges Impressum nach § 5 DDG sind der volle Vor- und Nachname und eine
   ladungsfähige Anschrift mit Straße nötig (Feld `owner_street`).
2. Nach Veröffentlichung der App `play_url` in `SITE` setzen
   (`https://play.google.com/store/apps/details?id=dev.flondy.checkyourlove`) – der Button
   „Bald bei Google Play“ wird dann zum Download-Link.
3. Rechtstexte bei Bedarf rechtlich prüfen lassen (keine Rechtsberatung).

## GitHub Pages aktivieren

1. Projekt in ein GitHub-Repository hochladen.
2. Repository → *Settings → Pages* → *Build and deployment*: Source „Deploy from a branch“,
   Branch `master` (bzw. `main`), Ordner **`/docs`** → *Save*.
3. Die Seite erscheint unter `https://<benutzername>.github.io/<repository>/`.
4. Die Datenschutz-URL (`…/datenschutz.html`) eintragen:
   - in der App: `cyl.privacyPolicyUrl` in `monetization.properties`
   - in der Play Console: *App-Inhalte → Datenschutzerklärung*

## app-ads.txt

AdMob sucht die Datei **im Stammverzeichnis der Entwickler-Website**, die in der Play Console
eingetragen ist (z. B. `https://flondy.com/app-ads.txt`). Bei einer Projektseite unter
`…github.io/<repository>/` liegt sie nicht im Stammverzeichnis. Lösungen:
- eigene Domain für diese Pages-Seite einrichten (*Settings → Pages → Custom domain*), oder
- Repository `<benutzername>.github.io` verwenden, oder
- die Datei auf flondy.com hochladen.

## Vorschau-Bild für Social Media

`og:image` ist relativ verlinkt. Manche Netzwerke verlangen eine absolute URL – nach der
Veröffentlichung ggf. in `head()` des Generators die vollständige Adresse einsetzen.
