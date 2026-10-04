#!/usr/bin/env python3
"""
Erzeugt die statische Website (GitHub Pages, Ordner /docs) in Deutsch und Englisch.
Aufruf aus dem Projektordner:  python3 docs/_build/build_site.py

Gemeinsame Angaben stehen in SITE weiter unten. Platzhalter (gelb markiert) müssen vor der
Veröffentlichung ersetzt werden – siehe docs/_build/README.md.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

SITE = {
    # Anbieter/Verantwortlicher – PLATZHALTER ERSETZEN
    "owner_name": '<span class="todo">[Vor- und Nachname bzw. Firma]</span>',
    "owner_street": '<span class="todo">[Straße und Hausnummer]</span>',
    "owner_city": '<span class="todo">[PLZ Ort]</span>',
    "owner_country_de": "Deutschland",
    "owner_country_en": "Germany",
    "email": "contact@flondy.com",
    # Google-Play-Eintrag. Leer lassen, solange die App noch nicht veröffentlicht ist.
    "play_url": "",
    "updated_de": "5. Oktober 2026",
    "updated_en": "5 October 2026",
}

HEART = "M50,88 C50,88 10,62 10,36 C10,22 21,12 33,12 C41,12 47,16 50,22 C53,16 59,12 67,12 C79,12 90,22 90,36 C90,62 50,88 50,88 Z"

ICONS = {
    "repeat": '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 2l4 4-4 4"/><path d="M3 11V9a3 3 0 0 1 3-3h15"/><path d="M7 22l-4-4 4-4"/><path d="M21 13v2a3 3 0 0 1-3 3H3"/></svg>',
    "history": '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 3-6.7L3 8"/><path d="M3 3v5h5"/><path d="M12 7v5l3 2"/></svg>',
    "share": '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3v12"/><path d="M8 7l4-4 4 4"/><path d="M5 12v7a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-7"/></svg>',
    "palette": '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22a10 10 0 1 1 10-10c0 2.8-2.2 3.5-4 3.5h-2a2 2 0 0 0-1.5 3.3c.5.6.5 1.5 0 2-.6.8-1.5 1.2-2.5 1.2z"/><circle cx="7.5" cy="11" r="1.2"/><circle cx="10.5" cy="7" r="1.2"/><circle cx="15.5" cy="7.5" r="1.2"/></svg>',
    "lock": '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="10" width="16" height="11" rx="3"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></svg>',
    "access": '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="4.5" r="1.8"/><path d="M5 8.5l7 1.5 7-1.5"/><path d="M12 10v5l-3 6"/><path d="M12 15l3 6"/></svg>',
    "shield": '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-3.5 8-10V5l-8-3-8 3v7c0 6.5 8 10 8 10z"/><path d="M9 12l2 2 4-4"/></svg>',
    "play": '<svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor" aria-hidden="true"><path d="M4 2.8v18.4c0 .6.7 1 1.2.7l15.6-9.2c.5-.3.5-1.1 0-1.4L5.2 2.1C4.7 1.8 4 2.2 4 2.8z"/></svg>',
}

PALETTES = [
    ("Aubergine", "Aubergine", False, "#2A1430", "#170B1C", ("#FF8A6B", "#FF5C8A", "#A77BFF")),
    ("Mitternacht", "Midnight", False, "#16203F", "#0B1022", ("#FF9A7A", "#FF6A8E", "#8C9BFF")),
    ("Roségold", "Rosé Gold", True, "#33201F", "#1C1214", ("#F6CDB0", "#E59A8C", "#C77D8F")),
    ("Polarlicht", "Aurora", True, "#16233F", "#071A1F", ("#7EF0C8", "#6CC6FF", "#C08CFF")),
    ("Kirschblüte", "Cherry Blossom", True, "#3A1A2B", "#221019", ("#FFC2D6", "#FF7FAF", "#E5579A")),
    ("Samt", "Velvet", True, "#32101C", "#16060B", ("#FF6B6B", "#E5304F", "#9E1440")),
]


def heart_svg(colors, gid, size_attr=""):
    a, b, c = colors
    return (f'<svg viewBox="10 12 80 76" {size_attr} aria-hidden="true"><defs><linearGradient id="{gid}" x1="0" y1="0" x2="1" y2="1">'
            f'<stop offset="0" stop-color="{a}"/><stop offset=".55" stop-color="{b}"/><stop offset="1" stop-color="{c}"/></linearGradient></defs>'
            f'<path d="{HEART}" fill="url(#{gid})"/></svg>')


def head(lang, title, desc, base, canonical_path, alt_path, extra_class=""):
    other = "en" if lang == "de" else "de"
    return f"""<!doctype html>
<html lang="{lang}">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{title}</title>
<meta name="description" content="{desc}">
<meta name="theme-color" content="#170b1c">
<meta property="og:type" content="website">
<meta property="og:title" content="{title}">
<meta property="og:description" content="{desc}">
<meta property="og:image" content="{base}assets/img/og-image.png">
<meta name="twitter:card" content="summary_large_image">
<link rel="alternate" hreflang="{other}" href="{alt_path}">
<link rel="icon" type="image/png" href="{base}assets/img/favicon-64.png">
<link rel="apple-touch-icon" href="{base}assets/img/apple-touch-icon.png">
<link rel="preload" href="{base}assets/fonts/DMSerifDisplay-Regular.woff2" as="font" type="font/woff2" crossorigin>
<link rel="stylesheet" href="{base}assets/style.css">
</head>
<body class="no-js {extra_class}">
<script>document.body.classList.remove('no-js')</script>
"""


def nav(lang, base, alt_path, links):
    skip = "Zum Inhalt springen" if lang == "de" else "Skip to content"
    lang_label = "EN" if lang == "de" else "DE"
    lang_title = "English version" if lang == "de" else "Deutsche Version"
    home = f"{base}" if lang == "de" else f"{base}en/"
    items = "".join(f'<a class="{cls}" href="{href}">{text}</a>' for href, text, cls in links)
    return f"""<a class="skip" href="#main">{skip}</a>
<header class="nav"><div class="wrap">
  <a class="brand" href="{home}"><img src="{base}assets/img/icon-192.png" alt="" width="34" height="34">Check your Love</a>
  <nav class="nav-links" aria-label="{'Hauptnavigation' if lang == 'de' else 'Main navigation'}">{items}
    <a class="lang" href="{alt_path}" hreflang="{'en' if lang == 'de' else 'de'}" title="{lang_title}">{lang_label}</a>
  </nav>
</div></header>
"""


def footer(lang, base):
    if lang == "de":
        links = [(f"{base}datenschutz.html", "Datenschutz"), (f"{base}impressum.html", "Impressum"),
                 (f"mailto:{SITE['email']}", "Kontakt"), (f"{base}en/", "English")]
        note = "Nur zur Unterhaltung – kein wissenschaftlicher Test."
    else:
        links = [(f"{base}en/privacy.html", "Privacy"), (f"{base}en/imprint.html", "Imprint"),
                 (f"mailto:{SITE['email']}", "Contact"), (f"{base}", "Deutsch")]
        note = "Just for fun – not a scientific test."
    items = "".join(f'<a href="{h}">{t}</a>' for h, t in links)
    return f"""<footer><div class="wrap">
  <span>© 2026 Flondy · Check your Love · {note}</span>
  <nav aria-label="Footer">{items}</nav>
</div></footer>
<script>
(function () {{
  var els = document.querySelectorAll('.reveal');
  if (!('IntersectionObserver' in window)) {{ els.forEach(function (e) {{ e.classList.add('in'); }}); return; }}
  var io = new IntersectionObserver(function (entries) {{
    entries.forEach(function (en) {{ if (en.isIntersecting) {{ en.target.classList.add('in'); io.unobserve(en.target); }} }});
  }}, {{ rootMargin: '0px 0px -8% 0px' }});
  els.forEach(function (e) {{ io.observe(e); }});
}})();
</script>
</body>
</html>
"""


def play_button(lang):
    if SITE["play_url"]:
        label = ("Jetzt bei", "Google Play") if lang == "de" else ("Get it on", "Google Play")
        return (f'<a class="btn btn-primary" href="{SITE["play_url"]}" rel="noopener">{ICONS["play"]}'
                f'<span class="stack"><small>{label[0]}</small>{label[1]}</span></a>')
    label = ("Bald bei", "Google Play") if lang == "de" else ("Coming soon to", "Google Play")
    return (f'<span class="btn btn-primary" aria-disabled="true">{ICONS["play"]}'
            f'<span class="stack"><small>{label[0]}</small>{label[1]}</span></span>')


# =====================================================================================
# Landingpage
# =====================================================================================

T = {
    "de": {
        "title": "Check your Love – der liebevoll gestaltete Liebestest",
        "desc": "Zwei Namen, ein Herz, ein Ergebnis: Check your Love ist ein hochwertig gestalteter Liebestest für Android – nur zum Spaß, privat und immer gleich für dasselbe Paar.",
        "nav": [("#so-gehts", "So geht's", "hide-sm"), ("#funktionen", "Funktionen", "hide-sm"), ("#premium", "Premium", "hide-sm"), ("#faq", "FAQ", "")],
        "eyebrow": "Liebestest für Android",
        "h1": "Wie stark ist eure <em>Verbindung?</em>",
        "lead": "Zwei Namen, ein Herz, ein Ergebnis. Check your Love verbindet einen kleinen Moment Spannung mit einem Design, das sich hochwertig anfühlt – verspielt, aber nie kitschig.",
        "more": "Mehr erfahren",
        "hero_note": "Kostenlos · Werbefrei mit einmaligem Premium-Kauf · Nur zum Spaß",
        "img_front": "de-result-dark.webp", "img_back": "de-input-dark.webp",
        "alt_front": "App-Ansicht: Ergebnis 79 % für Anna und Ben", "alt_back": "App-Ansicht: Eingabe von zwei Namen",
        "steps_kicker": "So geht's", "steps_h": "In drei Herzschlägen zum <em>Ergebnis</em>",
        "steps_p": "Kein Konto, keine Anmeldung. Öffnen, Namen eintippen, staunen.",
        "steps": [("Namen eingeben", "Deinen Namen und den deines Schwarms – mit Umlauten, Akzenten oder in jeder anderen Schrift."),
                  ("Aufs Herz tippen", "Das Herz pulsiert kurz, während es genau hinhört. Kein langes Warten, keine Ladebalken."),
                  ("Ergebnis genießen", "Ein animierter Prozentwert, ein kurzer, liebevoller Text – und bei hohen Werten ein kleines Feuerwerk.")],
        "feat_kicker": "Funktionen", "feat_h": "Klein, aber mit <em>viel Liebe</em> gemacht",
        "feat_p": "Alles, was ein Liebestest braucht – und nichts, was nervt.",
        "features": [("repeat", "Immer dasselbe Ergebnis", "Dasselbe Namenspaar ergibt immer denselben Wert – egal in welcher Reihenfolge oder Schreibweise."),
                     ("history", "Verlauf & Favoriten", "Alle Tests auf einen Blick, Lieblingspaare mit einem Herz markieren, einzeln oder komplett löschen."),
                     ("share", "Als Bild teilen", "Das Ergebnis als stilvolle Karte für Story, Chat oder Gruppe – in vier Vorlagen."),
                     ("palette", "Hell, dunkel & 6 Designs", "Aubergine, Mitternacht und vier exklusive Premium-Designs – jeweils für hellen und dunklen Modus."),
                     ("lock", "Privat by Design", "Namen und Verlauf bleiben auf deinem Gerät. Kein Konto, keine eigenen Server, keine Analyse-Tools."),
                     ("access", "Für alle gemacht", "Große Schrift, TalkBack, klare Kontraste und „Bewegung reduzieren“ werden unterstützt.")],
        "gal_kicker": "Einblicke", "gal_h": "So sieht's <em>aus</em>",
        "gallery": [("de-input-dark.webp", "Eingabe"), ("de-result-dark.webp", "Ergebnis"), ("de-history-dark.webp", "Verlauf"),
                    ("de-share-dark.webp", "Teilen"), ("de-settings-dark.webp", "Einstellungen"), ("en-result-light.webp", "Heller Modus")],
        "des_kicker": "Designs", "des_h": "Finde deinen <em>Look</em>",
        "des_p": "Zwei Designs sind immer dabei, vier weitere gibt es mit Premium – oder 24 Stunden zum Ausprobieren per freiwilligem Video.",
        "cards_caption": ["Klassisch", "Polaroid", "Neon", "Minimal"],
        "prem_kicker": "Premium", "prem_h": "Einmal kaufen, <em>für immer</em> behalten",
        "prem_p": "Die Berechnung und jedes Ergebnis sind kostenlos. Mit dem einmaligen Premium-Kauf unterstützt du die Weiterentwicklung und bekommst:",
        "prem_list": ["Keine Werbung – nie wieder", "4 exklusive Designs: Roségold, Polarlicht, Kirschblüte, Samt",
                      "2 zusätzliche Ergebnisanimationen: Herzregen und Sternenfunkeln", "3 Premium-Vorlagen zum Teilen: Polaroid, Neon, Minimal"],
        "prem_fair": "Kein Abo, keine versteckten Kosten. Der Preis wird direkt von Google Play angezeigt. Premium verändert nie ein Ergebnis – die Berechnung ist für alle gleich.",
        "prem_img": "de-premium-dark.webp", "prem_alt": "App-Ansicht: Premium-Bereich",
        "priv_h": "Deine Namen bleiben bei dir",
        "priv_p": "Check your Love speichert Namen und Verlauf ausschließlich lokal auf deinem Smartphone. Die App wird über Werbung von Google AdMob finanziert – deine eingegebenen Namen werden dabei nie weitergegeben.",
        "priv_link": "Zur Datenschutzerklärung",
        "faq_kicker": "FAQ", "faq_h": "Häufige <em>Fragen</em>",
        "faq": [("Ist das Ergebnis wissenschaftlich?", "Nein. Check your Love ist reine Unterhaltung. Der Wert wird aus den beiden Namen berechnet und sagt nichts über echte Gefühle oder die Zukunft einer Beziehung aus."),
                ("Warum bekomme ich immer dasselbe Ergebnis?", "Weil die Berechnung deterministisch ist: Dasselbe Namenspaar ergibt immer denselben Wert – unabhängig von Reihenfolge, Groß-/Kleinschreibung und Leerzeichen. So kann niemand „neu würfeln“."),
                ("Verändert Premium das Ergebnis?", "Nein, niemals. Premium schaltet nur Gestaltung frei (Designs, Animationen, Teilen-Vorlagen) und entfernt die Werbung."),
                ("Wo werden meine Daten gespeichert?", "Nur auf deinem Gerät. Den Verlauf kannst du jederzeit in der App löschen. Mit der Deinstallation werden alle App-Daten entfernt."),
                ("Welche Daten nutzt die Werbung?", "Google AdMob kann z. B. die Werbe-ID, die IP-Adresse und Geräteinformationen verarbeiten. Vorher fragt die App nach deiner Einwilligung – ändern kannst du sie jederzeit unter Einstellungen → Datenschutz."),
                ("Was bringt das freiwillige Video?", "Im Premium-Bereich kannst du dir freiwillig ein kurzes Werbevideo ansehen und damit ein Premium-Design, eine Animation oder eine Teilen-Vorlage 24 Stunden lang nutzen. Die Belohnung gibt es nur, wenn du das Video bis zum Ende ansiehst.")],
    },
    "en": {
        "title": "Check your Love – the beautifully crafted love test",
        "desc": "Two names, one heart, one result: Check your Love is a beautifully designed love test for Android – just for fun, private and always the same for the same pair.",
        "nav": [("#how", "How it works", "hide-sm"), ("#features", "Features", "hide-sm"), ("#premium", "Premium", "hide-sm"), ("#faq", "FAQ", "")],
        "eyebrow": "Love test for Android",
        "h1": "How strong is your <em>bond?</em>",
        "lead": "Two names, one heart, one result. Check your Love turns a moment of suspense into an experience that feels premium – playful, but never cheesy.",
        "more": "Learn more",
        "hero_note": "Free · Ad-free with a one-time Premium purchase · Just for fun",
        "img_front": "en-result-light.webp", "img_back": "en-input-light.webp",
        "alt_front": "App screen: result 46 % for Lena and Tom", "alt_back": "App screen: entering two names",
        "steps_kicker": "How it works", "steps_h": "Three heartbeats to your <em>result</em>",
        "steps_p": "No account, no sign-up. Open it, type two names, smile.",
        "steps": [("Enter two names", "Yours and your crush's – with accents, umlauts or in any script."),
                  ("Tap the heart", "The heart pulses briefly while it listens. No long waits, no loading bars."),
                  ("Enjoy the result", "An animated percentage, a short, kind message – and a little celebration for high scores.")],
        "feat_kicker": "Features", "feat_h": "Small, but made with <em>lots of love</em>",
        "feat_p": "Everything a love test needs – and nothing that gets in the way.",
        "features": [("repeat", "Always the same result", "The same pair of names always gets the same score – whatever the order or spelling."),
                     ("history", "History & favourites", "All your tests at a glance. Mark favourite pairs with a heart, delete single entries or everything."),
                     ("share", "Share as an image", "Turn your result into a stylish card for stories, chats or groups – four templates."),
                     ("palette", "Light, dark & 6 designs", "Aubergine, Midnight and four exclusive Premium designs – each for light and dark mode."),
                     ("lock", "Private by design", "Names and history stay on your device. No account, no own servers, no analytics."),
                     ("access", "Made for everyone", "Large text, TalkBack, clear contrast and “reduce motion” are supported.")],
        "gal_kicker": "A closer look", "gal_h": "See it in <em>action</em>",
        "gallery": [("en-input-light.webp", "Input"), ("en-result-light.webp", "Result"), ("en-history-light.webp", "History"),
                    ("en-settings-light.webp", "Settings"), ("de-result-dark.webp", "Dark mode"), ("de-share-dark.webp", "Share")],
        "des_kicker": "Designs", "des_h": "Find your <em>look</em>",
        "des_p": "Two designs are always included, four more come with Premium – or try one for 24 hours by watching a voluntary video.",
        "cards_caption": ["Classic", "Polaroid", "Neon", "Minimal"],
        "prem_kicker": "Premium", "prem_h": "Buy once, keep <em>forever</em>",
        "prem_p": "The calculation and every result are free. With the one-time Premium purchase you support future development and get:",
        "prem_list": ["No ads – ever", "4 exclusive designs: Rosé Gold, Aurora, Cherry Blossom, Velvet",
                      "2 extra result animations: heart rain and starburst", "3 premium share cards: Polaroid, Neon, Minimal"],
        "prem_fair": "No subscription, no hidden costs. The price is shown directly by Google Play. Premium never changes a result – the calculation is the same for everyone.",
        "prem_img": "de-premium-dark.webp", "prem_alt": "App screen: Premium area",
        "priv_h": "Your names stay with you",
        "priv_p": "Check your Love stores names and history only locally on your phone. The app is funded by ads from Google AdMob – the names you enter are never passed on.",
        "priv_link": "Read the privacy policy",
        "faq_kicker": "FAQ", "faq_h": "Frequently asked <em>questions</em>",
        "faq": [("Is the result scientific?", "No. Check your Love is purely for entertainment. The score is calculated from the two names and says nothing about real feelings or the future of a relationship."),
                ("Why do I always get the same result?", "Because the calculation is deterministic: the same pair of names always gets the same score – regardless of order, capitalisation or spaces. Nobody can simply “re-roll”."),
                ("Does Premium change the result?", "No, never. Premium only unlocks styling (designs, animations, share cards) and removes ads."),
                ("Where is my data stored?", "Only on your device. You can delete your history in the app at any time. Uninstalling removes all app data."),
                ("What data do the ads use?", "Google AdMob may process e.g. the advertising ID, IP address and device information. The app asks for your consent first – you can change it at any time under Settings → Privacy."),
                ("What is the voluntary video for?", "In the Premium area you can choose to watch a short video ad to use a Premium design, animation or share card for 24 hours. You only get the reward if you watch the video to the end.")],
    },
}


def landing(lang):
    t = T[lang]
    base = "" if lang == "de" else "../"
    alt = "en/" if lang == "de" else "../"
    privacy = f"{base}datenschutz.html" if lang == "de" else "privacy.html"
    ids = ("so-gehts", "funktionen") if lang == "de" else ("how", "features")
    steps = "".join(f'<article class="card step reveal"><h3>{h}</h3><p>{p}</p></article>' for h, p in t["steps"])
    feats = "".join(f'<article class="card reveal"><div class="icon">{ICONS[i]}</div><h3>{h}</h3><p>{p}</p></article>' for i, h, p in t["features"])
    gallery = "".join(f'<figure><div class="shot"><img src="{base}assets/img/{img}" alt="{cap}" width="540" height="1171" loading="lazy"></div><figcaption>{cap}</figcaption></figure>' for img, cap in t["gallery"])
    pals = ""
    for i, (de, en, prem, top, bottom, colors) in enumerate(PALETTES):
        name = de if lang == "de" else en
        badge = '<span class="badge">Premium</span>' if prem else ""
        pals += (f'<div class="palette reveal" style="background:linear-gradient(180deg,{top},{bottom})">{badge}'
                 f'{heart_svg(colors, f"p{i}")}<span>{name}</span></div>')
    cards = "".join(f'<figure class="reveal"><img src="{base}assets/img/card-{c}.webp" alt="{cap}" width="432" height="540" loading="lazy"><figcaption>{cap}</figcaption></figure>'
                    for c, cap in zip(["classic", "polaroid", "neon", "minimal"], t["cards_caption"]))
    plist = "".join(f"<li>{x}</li>" for x in t["prem_list"])
    faq = "".join(f"<details class=\"reveal\"><summary>{q}</summary><p>{a}</p></details>" for q, a in t["faq"])
    hero_glow = heart_svg(("#FF8A6B", "#FF5C8A", "#A77BFF"), "hg", 'class="glow-heart"')

    html = head(lang, t["title"], t["desc"], base, "", alt)
    html += nav(lang, base, alt, t["nav"])
    html += f"""<main id="main">
<section class="hero"><div class="wrap">
  <div>
    <span class="eyebrow"><span class="dot"></span>{t['eyebrow']}</span>
    <h1>{t['h1']}</h1>
    <p class="lead">{t['lead']}</p>
    <div class="cta">{play_button(lang)}<a class="btn btn-ghost" href="#{ids[0]}">{t['more']}</a></div>
    <p class="note">{t['hero_note']}</p>
  </div>
  <div class="phones" aria-hidden="false">
    {hero_glow}
    <div class="phone back"><img src="{base}assets/img/{t['img_back']}" alt="{t['alt_back']}" width="540" height="1171"></div>
    <div class="phone front"><img src="{base}assets/img/{t['img_front']}" alt="{t['alt_front']}" width="540" height="1171" fetchpriority="high"></div>
  </div>
</div></section>

<section id="{ids[0]}"><div class="wrap">
  <div class="section-head reveal"><span class="kicker">{t['steps_kicker']}</span><h2>{t['steps_h']}</h2><p>{t['steps_p']}</p></div>
  <div class="steps">{steps}</div>
</div></section>

<section id="{ids[1]}" style="padding-top:0"><div class="wrap">
  <div class="section-head reveal"><span class="kicker">{t['feat_kicker']}</span><h2>{t['feat_h']}</h2><p>{t['feat_p']}</p></div>
  <div class="features">{feats}</div>
</div></section>

<section style="padding-top:0"><div class="wrap">
  <div class="section-head center reveal"><span class="kicker">{t['gal_kicker']}</span><h2>{t['gal_h']}</h2></div>
  <div class="gallery" tabindex="0" aria-label="{t['gal_kicker']}">{gallery}</div>
</div></section>

<section style="padding-top:0"><div class="wrap">
  <div class="section-head center reveal"><span class="kicker">{t['des_kicker']}</span><h2>{t['des_h']}</h2><p>{t['des_p']}</p></div>
  <div class="palettes">{pals}</div>
  <div class="cards">{cards}</div>
</div></section>

<section id="premium" style="padding-top:0"><div class="wrap">
  <div class="premium reveal">
    <div>
      <span class="kicker">{t['prem_kicker']}</span>
      <h2>{t['prem_h']}</h2>
      <p class="lead" style="font-size:18px">{t['prem_p']}</p>
      <ul class="checklist">{plist}</ul>
      <p class="fair">{t['prem_fair']}</p>
    </div>
    <div class="phone" style="position:relative;margin:0 auto;width:min(280px,100%)"><img src="{base}assets/img/{t['prem_img']}" alt="{t['prem_alt']}" width="540" height="1171" loading="lazy"></div>
  </div>
</div></section>

<section style="padding-top:0"><div class="wrap">
  <div class="card privacy-band reveal">
    <div class="icon" style="width:64px;height:64px;margin:0">{ICONS['shield']}</div>
    <div><h3>{t['priv_h']}</h3><p>{t['priv_p']}</p></div>
    <a class="btn btn-ghost" href="{privacy}">{t['priv_link']}</a>
  </div>
</div></section>

<section id="faq" style="padding-top:0"><div class="wrap">
  <div class="section-head center reveal"><span class="kicker">{t['faq_kicker']}</span><h2>{t['faq_h']}</h2></div>
  <div class="faq">{faq}</div>
</div></section>
</main>
"""
    html += footer(lang, base)
    return html


# =====================================================================================
# Datenschutzerklärung
# =====================================================================================

def controller_block(lang):
    country = SITE["owner_country_de"] if lang == "de" else SITE["owner_country_en"]
    mail = "E-Mail" if lang == "de" else "Email"
    return (f'<div class="box"><p style="margin:0">{SITE["owner_name"]}<br>{SITE["owner_street"]}<br>'
            f'{SITE["owner_city"]}<br>{country}<br>{mail}: <a href="mailto:{SITE["email"]}">{SITE["email"]}</a></p></div>')


def privacy_de():
    return f"""
<h1>Datenschutzerklärung</h1>
<p class="meta">für die App „Check your Love“ (Android, Paket dev.flondy.checkyourlove) und diese Website · Stand: {SITE['updated_de']}</p>

<nav class="toc" aria-label="Inhalt"><strong>Inhalt</strong><ol>
<li><a href="#verantwortlicher">Verantwortlicher</a></li>
<li><a href="#ueberblick">Überblick</a></li>
<li><a href="#lokal">Daten auf deinem Gerät</a></li>
<li><a href="#werbung">Werbung (Google AdMob)</a></li>
<li><a href="#einwilligung">Einwilligung &amp; Widerruf</a></li>
<li><a href="#kaeufe">Käufe (Google Play)</a></li>
<li><a href="#teilen">Teilen von Ergebnissen</a></li>
<li><a href="#berechtigungen">Berechtigungen</a></li>
<li><a href="#kontakt">Kontakt per E-Mail</a></li>
<li><a href="#website">Diese Website</a></li>
<li><a href="#drittland">Übermittlung in Drittländer</a></li>
<li><a href="#kinder">Kinder und Jugendliche</a></li>
<li><a href="#rechte">Deine Rechte</a></li>
<li><a href="#aenderungen">Änderungen</a></li>
</ol></nav>

<h2 id="verantwortlicher">1. Verantwortlicher</h2>
<p>Verantwortlich für die Datenverarbeitung im Sinne der Datenschutz-Grundverordnung (DSGVO) ist:</p>
{controller_block('de')}

<h2 id="ueberblick">2. Überblick</h2>
<ul>
<li><strong>Kein Konto, keine eigenen Server:</strong> Die App kommt ohne Registrierung aus. Wir betreiben keine Server, auf denen Daten aus der App gespeichert werden.</li>
<li><strong>Namen bleiben lokal:</strong> Die eingegebenen Namen, Ergebnisse, Favoriten und Einstellungen werden nur auf deinem Gerät gespeichert. Wir erhalten sie nicht, und sie werden weder an Werbe- noch an Analysedienste übermittelt.</li>
<li><strong>Keine Analyse- oder Tracking-Tools:</strong> Die App enthält keine Analyse-, Crash-Reporting- oder Marketing-Tracking-Dienste.</li>
<li><strong>Werbung:</strong> Die kostenlose Version wird über Google AdMob finanziert. Dabei verarbeitet Google personenbezogene Daten (Abschnitt 4) – nach deiner Auswahl im Einwilligungsdialog.</li>
<li><strong>Käufe:</strong> Der optionale Premium-Kauf wird vollständig über Google Play abgewickelt (Abschnitt 6).</li>
</ul>

<h2 id="lokal">3. Daten auf deinem Gerät</h2>
<p>Damit die App funktioniert, speichert sie folgende Daten ausschließlich lokal im privaten App-Speicher deines Geräts:</p>
<table>
<tr><th>Daten</th><th>Zweck</th></tr>
<tr><td>Eingegebene Namen, berechnetes Ergebnis, Zeitpunkt, Favoriten-Markierung</td><td>Verlauf und Favoriten</td></tr>
<tr><td>Einstellungen (Modus, Design, Animation, Teilen-Vorlage, Haptik, Bewegung)</td><td>Darstellung nach deinen Wünschen</td></tr>
<tr><td>Zeitpunkt der letzten Vollbild-Werbung und Anzahl der Tests seither</td><td>Begrenzung der Werbehäufigkeit</td></tr>
<tr><td>Ablaufzeitpunkt von 24-Stunden-Freischaltungen</td><td>Freiwillige Belohnungen durch Werbevideos</td></tr>
<tr><td>Deine Einwilligungsentscheidung (durch das SDK von Google)</td><td>Nachweis und Beachtung deiner Auswahl</td></tr>
</table>
<p>Falls aus einer früheren App-Version noch Daten vorhanden waren, werden diese bei der Aktualisierung unverändert lokal übernommen.</p>
<p><strong>Rechtsgrundlage:</strong> Art. 6 Abs. 1 lit. b DSGVO (Bereitstellung der von dir genutzten Funktionen) bzw. Art. 6 Abs. 1 lit. f DSGVO; der Zugriff auf den Gerätespeicher ist für die von dir gewünschte Funktion unbedingt erforderlich (§ 25 Abs. 2 Nr. 2 TDDDG).</p>
<p><strong>Speicherdauer &amp; Löschung:</strong> Die Daten bleiben gespeichert, bis du sie löschst – einzelne Einträge oder den gesamten Verlauf direkt in der App, alle Daten über „App-Daten löschen“ in den Android-Einstellungen oder durch Deinstallation.</p>
<p><strong>Sicherungen:</strong> Namen, Verlauf und Einstellungen sind von der automatischen Android-Cloud-Sicherung ausgenommen. Bei einer direkten Übertragung von einem alten auf ein neues Gerät können sie mit übertragen werden.</p>

<h2 id="werbung">4. Werbung (Google AdMob)</h2>
<p>In der kostenlosen Version zeigen wir Werbung des Dienstes <strong>Google AdMob</strong> an: ein Banner im Test- und Verlaufsbildschirm, gelegentlich eine Vollbild-Werbung in einer natürlichen Pause (frühestens nach mehreren Tests und mit mehreren Minuten Abstand) sowie – nur wenn du es ausdrücklich auswählst – ein Video mit Belohnung. Mit Premium wird keine Werbung mehr geladen.</p>
<p><strong>Anbieter:</strong> Google Ireland Limited, Gordon House, Barrow Street, Dublin 4, Irland (für Nutzer im EWR und in der Schweiz).</p>
<p><strong>Verarbeitete Daten (durch Google):</strong> insbesondere die Werbe-ID deines Geräts, IP-Adresse und daraus abgeleiteter ungefährer Standort, Geräte- und Systeminformationen (z. B. Modell, Android-Version, Sprache), Informationen zur App sowie Interaktionen mit Anzeigen (z. B. Einblendungen, Klicks) und Diagnosedaten.</p>
<p><strong>Zwecke:</strong> Auslieferung und Darstellung von Anzeigen, Begrenzung der Anzeigenhäufigkeit, Messung und Abrechnung, Betrugs- und Missbrauchserkennung sowie – nur mit deiner Einwilligung – personalisierte Werbung.</p>
<p><strong>Was nicht übermittelt wird:</strong> Die in der App eingegebenen Namen und Ergebnisse werden niemals an Google AdMob übergeben. Die App fordert nur Anzeigen bis zur Altersfreigabe „Teen“ an.</p>
<p><strong>Rechtsgrundlage:</strong> deine Einwilligung (Art. 6 Abs. 1 lit. a DSGVO, § 25 Abs. 1 TDDDG), soweit Informationen auf deinem Gerät gespeichert oder ausgelesen werden oder Werbung personalisiert wird. Welche Anzeigen geladen werden, richtet sich nach deiner Auswahl im Einwilligungsdialog.</p>
<p>Weitere Informationen: <a href="https://policies.google.com/privacy" rel="noopener">Datenschutzerklärung von Google</a> · <a href="https://policies.google.com/technologies/partner-sites" rel="noopener">Wie Google Daten aus Apps von Partnern verwendet</a> · <a href="https://adssettings.google.com" rel="noopener">Einstellungen für Werbung bei Google</a>. Die Werbe-ID kannst du in den Android-Einstellungen unter „Datenschutz → Werbung“ zurücksetzen oder löschen.</p>

<h2 id="einwilligung">5. Einwilligung &amp; Widerruf</h2>
<p>Bevor Werbung geladen wird, fragt die App über die <strong>User Messaging Platform von Google</strong> nach deiner Einwilligung, sofern dies für deinen Standort erforderlich ist. Deine Entscheidung wird lokal auf dem Gerät gespeichert.</p>
<p>Du kannst deine Einwilligung jederzeit mit Wirkung für die Zukunft ändern oder widerrufen: <strong>App → Einstellungen → Datenschutz → „Datenschutzeinstellungen für Werbung“</strong>. Die Rechtmäßigkeit der bis zum Widerruf erfolgten Verarbeitung bleibt unberührt. Die Grundfunktion der App kannst du unabhängig von deiner Entscheidung nutzen.</p>

<h2 id="kaeufe">6. Käufe (Google Play)</h2>
<p>Der optionale Premium-Kauf wird über die Abrechnung von <strong>Google Play</strong> abgewickelt. Zahlungsdaten gibst du ausschließlich gegenüber Google an; wir erhalten sie nicht.</p>
<p>Die App erhält von Google Play lediglich Informationen zum Kauf (z. B. Produkt-ID, Kaufstatus, Kauf-Token, Bestellnummer und Zeitpunkt), um Premium freizuschalten, die Echtheit des Kaufs auf dem Gerät zu prüfen, den Kauf zu bestätigen und ihn nach einer Neuinstallation wiederherzustellen. Diese Informationen werden nicht auf eigenen Servern gespeichert. Erstattete oder widerrufene Käufe werden beim nächsten Abgleich mit Google Play berücksichtigt.</p>
<p><strong>Rechtsgrundlage:</strong> Art. 6 Abs. 1 lit. b DSGVO (Vertragserfüllung). Für die Verarbeitung durch Google gilt die <a href="https://policies.google.com/privacy" rel="noopener">Datenschutzerklärung von Google</a> sowie die <a href="https://payments.google.com/payments/apis-secure/u/0/get_legal_document?ldo=0&amp;ldt=privacynotice" rel="noopener">Datenschutzhinweise von Google Payments</a>.</p>

<h2 id="teilen">7. Teilen von Ergebnissen</h2>
<p>Wenn du ein Ergebnis teilst, erstellt die App ein Bild mit beiden Namen und dem Ergebnis. Es wird kurzzeitig im temporären Speicher der App abgelegt und über den Android-Teilen-Dialog an die von dir gewählte App übergeben. Für die weitere Verarbeitung ist die jeweilige Ziel-App bzw. deren Anbieter verantwortlich. Bitte teile Namen anderer Personen nur, wenn diese einverstanden sind.</p>

<h2 id="berechtigungen">8. Berechtigungen der App</h2>
<table>
<tr><th>Berechtigung</th><th>Zweck</th></tr>
<tr><td>Internetzugriff, Netzwerkstatus</td><td>Laden von Werbung, Abfrage von Käufen bei Google Play</td></tr>
<tr><td>Werbe-ID (<code>AD_ID</code>, durch das Google-Werbe-SDK)</td><td>Werbung, siehe Abschnitt 4</td></tr>
<tr><td>Google-Play-Abrechnung</td><td>Premium-Kauf</td></tr>
<tr><td>Vibration</td><td>optionales haptisches Feedback (abschaltbar)</td></tr>
</table>
<p>Die App greift nicht auf Kontakte, Fotos, Kamera, Mikrofon oder den genauen Standort zu.</p>

<h2 id="kontakt">9. Kontakt per E-Mail</h2>
<p>Wenn du uns per E-Mail kontaktierst, verarbeiten wir deine E-Mail-Adresse und den Inhalt deiner Nachricht, um dein Anliegen zu bearbeiten (Art. 6 Abs. 1 lit. b bzw. f DSGVO). Die Daten werden gelöscht, sobald sie dafür nicht mehr benötigt werden und keine gesetzlichen Aufbewahrungspflichten bestehen.</p>

<h2 id="website">10. Diese Website</h2>
<p>Diese Website wird über <strong>GitHub Pages</strong> bereitgestellt (GitHub, Inc., 88 Colin P. Kelly Jr. Street, San Francisco, CA 94107, USA). Beim Aufruf verarbeitet GitHub technisch notwendige Daten wie deine IP-Adresse, Datum und Uhrzeit, aufgerufene Seite und Browser-Informationen, um die Seite auszuliefern und die Sicherheit zu gewährleisten (Art. 6 Abs. 1 lit. f DSGVO). Näheres: <a href="https://docs.github.com/site-policy/privacy-policies/github-general-privacy-statement" rel="noopener">Datenschutzerklärung von GitHub</a>.</p>
<p>Die Website verwendet <strong>keine Cookies, kein Tracking und keine Analyse-Tools</strong>. Schriftarten und Bilder werden von dieser Website selbst geladen, nicht von Drittanbietern.</p>

<h2 id="drittland">11. Übermittlung in Drittländer</h2>
<p>Bei der Nutzung von Diensten von Google und GitHub können Daten in die USA übermittelt werden. Google LLC und GitHub, Inc. sind nach dem EU-US Data Privacy Framework zertifiziert; die Übermittlung stützt sich auf den entsprechenden Angemessenheitsbeschluss der EU-Kommission (Art. 45 DSGVO) bzw. ergänzend auf Standardvertragsklauseln (Art. 46 DSGVO).</p>

<h2 id="kinder">12. Kinder und Jugendliche</h2>
<p>Die App richtet sich nicht an Kinder unter 13 Jahren. Die Werbung ist auf Inhalte bis zur Altersfreigabe „Teen“ beschränkt.</p>

<h2 id="rechte">13. Deine Rechte</h2>
<p>Du hast nach der DSGVO das Recht auf Auskunft (Art. 15), Berichtigung (Art. 16), Löschung (Art. 17), Einschränkung der Verarbeitung (Art. 18), Datenübertragbarkeit (Art. 20) sowie das Recht, eine Einwilligung jederzeit zu widerrufen (Art. 7 Abs. 3).</p>
<p><strong>Widerspruchsrecht:</strong> Soweit wir Daten auf Grundlage berechtigter Interessen (Art. 6 Abs. 1 lit. f DSGVO) verarbeiten, kannst du aus Gründen, die sich aus deiner besonderen Situation ergeben, jederzeit widersprechen (Art. 21 DSGVO).</p>
<p>Da Namen und Verlauf nur auf deinem Gerät liegen, kannst du sie dort selbst einsehen und löschen; wir haben darauf keinen Zugriff. Für Daten, die Google verarbeitet, kannst du dich auch direkt an Google wenden. Zur Ausübung deiner Rechte genügt eine E-Mail an <a href="mailto:{SITE['email']}">{SITE['email']}</a>.</p>
<p>Du hast außerdem das Recht, dich bei einer Datenschutz-Aufsichtsbehörde zu beschweren, insbesondere in dem Mitgliedstaat deines Aufenthaltsorts, deines Arbeitsplatzes oder des Orts des mutmaßlichen Verstoßes (Art. 77 DSGVO).</p>

<h2 id="aenderungen">14. Änderungen</h2>
<p>Wir passen diese Datenschutzerklärung an, wenn sich die App, die eingesetzten Dienste oder die Rechtslage ändern. Es gilt die jeweils auf dieser Seite veröffentlichte Fassung.</p>
"""


def privacy_en():
    return f"""
<h1>Privacy Policy</h1>
<p class="meta">for the app “Check your Love” (Android, package dev.flondy.checkyourlove) and this website · Last updated: {SITE['updated_en']}</p>
<p class="box">This is a translation for convenience. In case of doubt, the <a href="../datenschutz.html" hreflang="de">German version</a> prevails.</p>

<nav class="toc" aria-label="Contents"><strong>Contents</strong><ol>
<li><a href="#controller">Controller</a></li>
<li><a href="#overview">Overview</a></li>
<li><a href="#local">Data on your device</a></li>
<li><a href="#ads">Advertising (Google AdMob)</a></li>
<li><a href="#consent">Consent &amp; withdrawal</a></li>
<li><a href="#purchases">Purchases (Google Play)</a></li>
<li><a href="#sharing">Sharing results</a></li>
<li><a href="#permissions">Permissions</a></li>
<li><a href="#contact">Contact by email</a></li>
<li><a href="#website">This website</a></li>
<li><a href="#transfers">International transfers</a></li>
<li><a href="#children">Children</a></li>
<li><a href="#rights">Your rights</a></li>
<li><a href="#changes">Changes</a></li>
</ol></nav>

<h2 id="controller">1. Controller</h2>
<p>The controller within the meaning of the General Data Protection Regulation (GDPR) is:</p>
{controller_block('en')}

<h2 id="overview">2. Overview</h2>
<ul>
<li><strong>No account, no own servers:</strong> The app works without registration. We do not operate any servers that store data from the app.</li>
<li><strong>Names stay local:</strong> The names you enter, results, favourites and settings are stored only on your device. We do not receive them, and they are never sent to advertising or analytics services.</li>
<li><strong>No analytics or tracking tools:</strong> The app contains no analytics, crash reporting or marketing tracking services.</li>
<li><strong>Advertising:</strong> The free version is funded by Google AdMob. Google processes personal data for this (section 4) – according to your choice in the consent dialog.</li>
<li><strong>Purchases:</strong> The optional Premium purchase is handled entirely by Google Play (section 6).</li>
</ul>

<h2 id="local">3. Data on your device</h2>
<p>To work, the app stores the following data only locally in its private storage on your device:</p>
<table>
<tr><th>Data</th><th>Purpose</th></tr>
<tr><td>Names entered, calculated result, time, favourite flag</td><td>History and favourites</td></tr>
<tr><td>Settings (mode, design, animation, share card, haptics, motion)</td><td>Personalised appearance</td></tr>
<tr><td>Time of the last full-screen ad and number of tests since</td><td>Limiting ad frequency</td></tr>
<tr><td>Expiry time of 24-hour unlocks</td><td>Voluntary rewards for watching video ads</td></tr>
<tr><td>Your consent choice (stored by Google's SDK)</td><td>Recording and respecting your choice</td></tr>
</table>
<p>Any data left from an earlier version of the app is kept locally and unchanged when you update.</p>
<p><strong>Legal basis:</strong> Art. 6(1)(b) GDPR (providing the features you use) or Art. 6(1)(f) GDPR; access to device storage is strictly necessary for the feature you request (Section 25(2) no. 2 German TDDDG).</p>
<p><strong>Retention &amp; deletion:</strong> The data remains until you delete it – single entries or the entire history in the app, all data via “Clear app data” in Android settings, or by uninstalling.</p>
<p><strong>Backups:</strong> Names, history and settings are excluded from automatic Android cloud backup. They may be included when you transfer data directly from an old to a new device.</p>

<h2 id="ads">4. Advertising (Google AdMob)</h2>
<p>In the free version we show ads from <strong>Google AdMob</strong>: a banner on the test and history screens, occasionally a full-screen ad at a natural break (only after several tests and several minutes apart) and – only if you explicitly choose it – a rewarded video. With Premium, no ads are loaded.</p>
<p><strong>Provider:</strong> Google Ireland Limited, Gordon House, Barrow Street, Dublin 4, Ireland (for users in the EEA and Switzerland).</p>
<p><strong>Data processed (by Google):</strong> in particular your device's advertising ID, IP address and approximate location derived from it, device and system information (e.g. model, Android version, language), information about the app, interactions with ads (e.g. impressions, clicks) and diagnostic data.</p>
<p><strong>Purposes:</strong> serving and displaying ads, frequency capping, measurement and billing, fraud and abuse prevention and – only with your consent – personalised advertising.</p>
<p><strong>What is not shared:</strong> Names and results entered in the app are never passed to Google AdMob. The app only requests ads rated up to “Teen”.</p>
<p><strong>Legal basis:</strong> your consent (Art. 6(1)(a) GDPR, Section 25(1) TDDDG) where information is stored on or read from your device or ads are personalised. Which ads are loaded depends on your choice in the consent dialog.</p>
<p>More information: <a href="https://policies.google.com/privacy" rel="noopener">Google Privacy Policy</a> · <a href="https://policies.google.com/technologies/partner-sites" rel="noopener">How Google uses information from partner apps</a> · <a href="https://adssettings.google.com" rel="noopener">Google ad settings</a>. You can reset or delete your advertising ID in Android settings under “Privacy → Ads”.</p>

<h2 id="consent">5. Consent &amp; withdrawal</h2>
<p>Before ads are loaded, the app uses Google's <strong>User Messaging Platform</strong> to ask for your consent where required for your location. Your choice is stored locally on the device.</p>
<p>You can change or withdraw your consent at any time with effect for the future: <strong>App → Settings → Privacy → “Ad privacy settings”</strong>. Withdrawal does not affect the lawfulness of processing before the withdrawal. You can use the core features regardless of your choice.</p>

<h2 id="purchases">6. Purchases (Google Play)</h2>
<p>The optional Premium purchase is processed via <strong>Google Play</strong> billing. You provide payment details only to Google; we never receive them.</p>
<p>The app only receives purchase information from Google Play (e.g. product ID, purchase state, purchase token, order number and time) in order to unlock Premium, verify the purchase on the device, acknowledge it and restore it after reinstalling. This information is not stored on our own servers. Refunded or revoked purchases are taken into account at the next sync with Google Play.</p>
<p><strong>Legal basis:</strong> Art. 6(1)(b) GDPR (performance of a contract). Processing by Google is governed by the <a href="https://policies.google.com/privacy" rel="noopener">Google Privacy Policy</a> and the <a href="https://payments.google.com/payments/apis-secure/u/0/get_legal_document?ldo=0&amp;ldt=privacynotice" rel="noopener">Google Payments Privacy Notice</a>.</p>

<h2 id="sharing">7. Sharing results</h2>
<p>When you share a result, the app creates an image with both names and the result. It is stored briefly in the app's temporary storage and handed to the app you choose via the Android share sheet. The receiving app and its provider are responsible for any further processing. Please only share other people's names if they agree.</p>

<h2 id="permissions">8. App permissions</h2>
<table>
<tr><th>Permission</th><th>Purpose</th></tr>
<tr><td>Internet access, network state</td><td>Loading ads, querying purchases from Google Play</td></tr>
<tr><td>Advertising ID (<code>AD_ID</code>, added by Google's ads SDK)</td><td>Advertising, see section 4</td></tr>
<tr><td>Google Play billing</td><td>Premium purchase</td></tr>
<tr><td>Vibration</td><td>optional haptic feedback (can be turned off)</td></tr>
</table>
<p>The app does not access contacts, photos, camera, microphone or precise location.</p>

<h2 id="contact">9. Contact by email</h2>
<p>If you contact us by email, we process your email address and the content of your message to handle your request (Art. 6(1)(b) or (f) GDPR). The data is deleted once it is no longer needed and no statutory retention obligations apply.</p>

<h2 id="website">10. This website</h2>
<p>This website is hosted on <strong>GitHub Pages</strong> (GitHub, Inc., 88 Colin P. Kelly Jr. Street, San Francisco, CA 94107, USA). When you visit it, GitHub processes technically necessary data such as your IP address, date and time, the page requested and browser information to deliver the site and ensure security (Art. 6(1)(f) GDPR). Details: <a href="https://docs.github.com/site-policy/privacy-policies/github-general-privacy-statement" rel="noopener">GitHub Privacy Statement</a>.</p>
<p>This website uses <strong>no cookies, no tracking and no analytics</strong>. Fonts and images are served from this website itself, not from third parties.</p>

<h2 id="transfers">11. International transfers</h2>
<p>When services of Google and GitHub are used, data may be transferred to the USA. Google LLC and GitHub, Inc. are certified under the EU-US Data Privacy Framework; transfers are based on the European Commission's adequacy decision (Art. 45 GDPR) and, additionally, on standard contractual clauses (Art. 46 GDPR).</p>

<h2 id="children">12. Children</h2>
<p>The app is not directed at children under 13. Ads are limited to content rated up to “Teen”.</p>

<h2 id="rights">13. Your rights</h2>
<p>Under the GDPR you have the right of access (Art. 15), rectification (Art. 16), erasure (Art. 17), restriction of processing (Art. 18), data portability (Art. 20) and the right to withdraw consent at any time (Art. 7(3)).</p>
<p><strong>Right to object:</strong> Where we process data on the basis of legitimate interests (Art. 6(1)(f) GDPR), you may object at any time on grounds relating to your particular situation (Art. 21 GDPR).</p>
<p>As names and history are stored only on your device, you can view and delete them there yourself; we have no access to them. For data processed by Google, you can also contact Google directly. To exercise your rights, simply email <a href="mailto:{SITE['email']}">{SITE['email']}</a>.</p>
<p>You also have the right to lodge a complaint with a data protection supervisory authority, in particular in the member state of your residence, place of work or place of the alleged infringement (Art. 77 GDPR).</p>

<h2 id="changes">14. Changes</h2>
<p>We update this privacy policy when the app, the services used or the legal situation change. The version published on this page applies.</p>
"""


def imprint(lang):
    if lang == "de":
        return f"""
<h1>Impressum</h1>
<p class="meta">Angaben gemäß § 5 Digitale-Dienste-Gesetz (DDG)</p>
{controller_block('de')}
<h2>Verantwortlich für den Inhalt</h2>
<p>{SITE['owner_name']}, Anschrift wie oben (§ 18 Abs. 2 MStV).</p>
<h2>Haftung für Links</h2>
<p>Diese Website enthält Links zu externen Websites Dritter, auf deren Inhalte wir keinen Einfluss haben. Für die Inhalte der verlinkten Seiten ist stets der jeweilige Anbieter verantwortlich.</p>
<h2>Verbraucherstreitbeilegung</h2>
<p>Wir sind nicht bereit und nicht verpflichtet, an Streitbeilegungsverfahren vor einer Verbraucherschlichtungsstelle teilzunehmen.</p>
"""
    return f"""
<h1>Imprint</h1>
<p class="meta">Information pursuant to Section 5 German Digital Services Act (DDG)</p>
<p class="box">Translation for convenience; the <a href="../impressum.html" hreflang="de">German version</a> prevails.</p>
{controller_block('en')}
<h2>Responsible for content</h2>
<p>{SITE['owner_name']}, address as above.</p>
<h2>Liability for links</h2>
<p>This website contains links to external third-party websites over whose content we have no control. The respective provider is always responsible for the content of linked pages.</p>
<h2>Consumer dispute resolution</h2>
<p>We are neither willing nor obliged to participate in dispute resolution proceedings before a consumer arbitration board.</p>
"""


def legal_page(lang, title, body, base, alt_path):
    links = ([(f"{base}", "Start" if lang == "de" else "Home", "")])
    html = head(lang, f"{title} – Check your Love", title, base, "", alt_path, "legal")
    html += nav(lang, base, alt_path, links)
    html += f'<main id="main">{body}</main>\n'
    html += footer(lang, base)
    return html


def main():
    (ROOT / "en").mkdir(exist_ok=True)
    pages = {
        "index.html": landing("de"),
        "en/index.html": landing("en"),
        "datenschutz.html": legal_page("de", "Datenschutzerklärung", privacy_de(), "", "en/privacy.html"),
        "en/privacy.html": legal_page("en", "Privacy Policy", privacy_en(), "../", "../datenschutz.html"),
        "impressum.html": legal_page("de", "Impressum", imprint("de"), "", "en/imprint.html"),
        "en/imprint.html": legal_page("en", "Imprint", imprint("en"), "../", "../impressum.html"),
    }
    # Englische Startseite: Footer/Links zeigen auf ../
    for path, html in pages.items():
        (ROOT / path).write_text(html, encoding="utf-8")
        print("geschrieben:", path)


if __name__ == "__main__":
    main()
