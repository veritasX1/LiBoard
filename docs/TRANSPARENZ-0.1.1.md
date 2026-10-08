# LiBoard 0.1.1 (Beta) – Transparenzbericht

> Stand 08.10.2026. Erstellt von Michelle 🔵 (Testerin), gegengelesen von Richard 🟡 (Tester). Mit „⏳“ markiert ist nur der Abgleich der Prüfsumme nach dem Hochladen.
> Geprüfte Fassungen: Quelltext 48f44e8f, Debug-APK bb6cf61f vom 08.10. 11:58 (Richard, pruefung-1158.txt, SHA-256 cde82afc…), dieselbe Fassung am Handy (Michelle, 08.10. 16:12). Veröffentlichte Release-APK: versionName 0.1.1, versionCode 3, SHA-256 e7f0c6ebd49183b178b335e96a3036f93e0a00e64dd04a9d2af32b5cb335f5c0, Signatur-Zertifikat SHA-256 fdf9c194…, Quelltext eeb6acb0 (Richard, release-homepages/LiBoard.txt).

Dieser Bericht erscheint zu jeder Veröffentlichung von LiBoard. Er zeigt, was die Tastatur mit dem Netz macht, welche Rechte sie hat, was sie speichert, woraus sie besteht und wie sie den **Wertekompass von LiSoftware** einhält. Jede Aussage hat einen Nachweis. Geprüft hat eine Testerin des Projekts, nicht der Entwickler.

## 1. Kurz gesagt

- **LiBoard hat keinen Internetzugang.** Die App hat keine INTERNET-Berechtigung, Android lässt sie also keine Verbindung aufbauen. Was du tippst, kann dein Gerät über LiBoard nicht verlassen.
- Keine Werbung, keine Käufe, keine Abos, kein Konto.
- Keine Tracker, keine Analyse, keine Absturzberichte, die gesendet werden.
- Keine Cloud-Sicherung, keine Übertragung beim Gerätewechsel.
- Keine KI, kein Modell.

## 2. Netzverbindungen

**Keine.** Nachweise:
- Manifest der geprüften Fassung (aapt2): keine INTERNET-Berechtigung (pruefung-1158.txt, Abschnitt 2).
- Webadressen in der APK: nur Text, den LiBoard nie aufruft (HeliBoard-Projekt und Lizenz auf GitHub, Google-Fehlerberichte zu Compose, JetBrains, XML-Namensräume). Die Download-Funktion für Wörterbücher und die HeliBoard-Links sind entfernt (f8e66751), der veraltete Link-Test wurde in 48f44e8f gelöscht.

## 3. Berechtigungen

| Berechtigung | Wozu | Wann gefragt |
|---|---|---|
| READ_USER_DICTIONARY, WRITE_USER_DICTIONARY | eigene Wörter und Textersetzungen im Android-Benutzerwörterbuch | – (keine Abfrage) |
| READ_CONTACTS | Namen aus deinen Kontakten als Vorschläge | nur wenn du „Namen aus Kontakten“ einschaltest (ab Werk aus) |
| VIBRATE | spürbarer Tastendruck | – |
| RECEIVE_BOOT_COMPLETED | Tastatur nach einem Neustart bereit machen | – |

**Nicht** angefordert: Internet, Standort, Kamera, Mikrofon, Telefon, Konten, Speicher/Fotos.
Android-Backup: aus (allowBackup=false, no_backup schließt Cloud-Sicherung und Gerätewechsel aus, Karte 85b36fc2). Von außen erreichbar: der Tastaturdienst (nur für das System, geschützt durch BIND_INPUT_METHOD), die Einstellungen (zwei Einstiege) und ein Empfänger für Systemereignisse. Der Datei-Anbieter der Zwischenablage ist **nicht** exportiert. Die Compose-Vorschau-Activity (ui-tooling) aus der Testfassung fehlt im Release (0 Treffer im Manifest, 9/9 Komponenten, Richard). Release: nicht debuggable.

## 4. Was LiBoard speichert

Alles bleibt auf dem Gerät und lässt sich in den Einstellungen löschen (vollständige Liste in docs/DATENSCHUTZ.md):
- **Gelernte Wörter** (für Vorschläge, ab Werk an, abschaltbar: Textkorrektur › „Aus meinen Eingaben lernen“ bzw. Erweitert › „Nichts dazulernen“). Getippter Text selbst wird nicht gespeichert.
- **Zwischenablage-Verlauf: ab Werk aus.** Wer ihn einschaltet: Einträge verfallen nach der eingestellten Zeit (ab Werk 10 Minuten) und werden auch beim Start aufgeräumt. Was die kopierende App als vertraulich kennzeichnet, wird nie gespeichert, ebenso nichts, solange ein Passwortfeld aktiv ist. Beim Leeren werden auch kopierte Bilder gelöscht.
- **Wichtig für alle, die schon eine frühere LiBoard-Fassung hatten:** Bis 0.1.0 war der Verlauf ab Werk an und speicherte auch vertrauliche Inhalte. Mit 0.1.1 schaltet LiBoard ihn einmalig aus und löscht alles, was dort lag (Karte f248ea29).
  Nachweis am Gerät (08.10. 16:12, nur Zählungen, keine Inhalte gelesen): Einstellung aus, Rücksetz-Merker gesetzt, 0 Einträge in der Zwischenablage-Tabelle, 0 Bilddateien. Nachweis im Code: ClipboardPrivacyTest (markiert → nie, Passwortfeld → nie, normaler Text → erlaubt). Testlauf 08.10. 12:02: 3 Tests, 0 Fehler (test-results geprüft).
- **Gestendaten-Sammlung aus HeliBoard (NLNet-Projekt):** in LiBoard stillgelegt, nicht einschaltbar, die Taste ist aus der Auswahl entfernt, die Tabelle bleibt leer (am Gerät 0 Einträge).
- Absturzberichte bleiben im App-Ordner, LiBoard fragt beim nächsten Öffnen, ob du sie speichern oder löschen willst. Gesendet wird nichts.

## 5. Bibliotheken

LiBoard baut auf **HeliBoard** auf (GPL-3.0, github.com/HeliBorg/HeliBoard), das auf der AOSP-Tastatur beruht. Dazu: Android Jetpack/Compose (BOM 2025.11.01, Material 3, Navigation; Apache-2.0), AndroidX RecyclerView, ViewPager2, Autofill (Apache-2.0), kotlinx-serialization (Apache-2.0), Reorderable 3.1.0 (Apache-2.0), ColorPicker-Compose 1.1.3 (Apache-2.0). In der App verweist „Über LiBoard › Lizenz“ bisher nur auf die GPL. Eine Liste dieser Bibliotheken mit ihren Lizenztexten fehlt noch, sie folgt in einer der nächsten Fassungen (Karte 3098c7b8).
Keine Werbe-, Analyse-, Absturzmelde- oder Bezahl-Bibliothek.

## 6. Tracker und KI

**Keine Tracker.** Gegenprobe am 08.10.2026 an der Release-APK 0.1.1 (e7f0c6eb…): 51 Präfixe, zwei Suchwege (dexdump und Rohsuche): **0 Treffer**. R8-Zuordnung desselben Builds (mapping.txt 16:24:04) mit Original-Namen: 0 Tracker-Pakete. Positivkontrolle: Compose und helium314 werden gefunden. Ohne INTERNET-Berechtigung wäre ohnehin keine Verbindung möglich.

**Keine KI.** 0 Modelldateien, 0 von 16 KI-Bibliotheken, 0 von 11 Adressen von KI-Diensten, auch in der R8-Zuordnung (Positivkontrolle helium314: 613). Die Wörterbücher (.dict) sind Wortlisten für Vorschläge, keine Modelle. (Richard, liboard-release-011/tracker-suche.txt, tracker-ki.txt)

## 7. Abgleich mit dem Wertekompass

| Wert / Grundsatz | Erfüllt? | Nachweis |
|---|---|---|
| **1. Privatsphäre:** keine Datensammlung, keine Telemetrie, keine Tracker, kein Cloud-Backup | ✔ | kein Internet (Abschnitt 2), Backup aus, Zwischenablage aus und Vertrauliches nie gespeichert, Gestendaten still (Abschnitt 4) |
| **2. Kontrolle:** alles abschaltbar, nichts ohne Zustimmung | ✔ | Lernen, Kontakte, Verlauf, App-Namen schaltbar; Kontakte erst nach Abfrage |
| **3. Einfachheit:** Apples Bedienkonzept (HIG) | ✔ (laufend) | Tastatur und Einstellungen wie iOS (Karten 23328ec0, f51ba6b6, 12b51a3c); Texte DE/EN/FR |
| **4. Kostenlos für immer** | ✔ | keine Käufe, Abos, Werbung |
| **KI** ab Werk aus, kein Modell | ✔ | Vorschläge aus Wörterbüchern (Wortlisten); Suche am Release: 0 Modelle, 0 KI-Bibliotheken, 0 KI-Dienste |
| **Transparenz** | ✔ | dieser Bericht, DATENSCHUTZ.md, offener Quelltext |

## 8. Offene Punkte vor Veröffentlichung

1. ~~Release-Build: Prüfsumme, Tracker- und KI-Suche, ui-tooling fehlt~~ ✔ (e7f0c6eb…). ⏳ Nach dem Hochladen prüfen, dass die Zeile in SHA256SUMS auf dem Server e7f0c6eb… lautet.
2. ~~ClipboardPrivacyTest grün bestätigen~~ ✔ (3/0).
3. Lizenzseite gegen die Bibliotheksliste.
4. 48298cdf (Trackpad-Gestenzone): Olafs Fingertest steht noch aus, Entscheidung Olaf.

---
Erstellt von Michelle 🔵. Rohdaten: Richard nachweise/liboard-apk/pruefung-1158.txt, liboard-datenschutz/; Michelle Karten f248ea29, f51ba6b6, 12b51a3c.
